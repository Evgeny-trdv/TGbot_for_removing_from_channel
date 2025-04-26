package ru.telegrambot.telegram_bot_support.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.BanChatMember;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.UnbanChatMember;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.telegrambot.telegram_bot_support.model.UserFollowing;
import ru.telegrambot.telegram_bot_support.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final String TEXT_NOTIFICATION = "Ваша подписка подходит к концу через 2 дня";
    private final String TEXT_ABOUT_ENDED_SUBSCRIPTION = "Ваша подписка закончилась";

    private static final List<String> TARGET_CHANNELS = Arrays.asList(
            "-1002516647653",
            "-1002606419459"// Пример числового ID канала
    );

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {

        updates.forEach(update -> {

            if (update.message() == null || update.message().text() == null) {
                return;
            }

            logger.info("Processing update {}", update);

            String messageUser = update.message().text();
            long userId = update.message().from().id();
            long chatId = update.message().chat().id();

            if (messageUser.equals("/start")) {

                SendMessage sendMessage = new SendMessage(
                        chatId,
                        "Добро пожаловать, для получения доступа к чатам необходимо провести оплату");

                KeyboardButton paymentButton = new KeyboardButton("/payment");

                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(paymentButton);
                keyboardMarkup.resizeKeyboard(true);

                keyboardMarkup.oneTimeKeyboard(true);

                sendMessage.replyMarkup(keyboardMarkup);
                telegramBot.execute(sendMessage);

            }

            if (messageUser.equals("/leave")) {
                removeUser(userId);
            }

            if (messageUser.equals("/payment")) {
                LocalDateTime now = LocalDateTime.now(); //день оплаты
                LocalDateTime next = LocalDateTime.now().plusDays(28); //день уведомления об окончании подписки
                UserFollowing user = new UserFollowing(
                     userId,
                     chatId,
                        now,
                        next
                );
                user.setPayment(true);
                userRepository.save(user);
                telegramBot.execute(new SendMessage(
                        chatId,
                        "Successful"
                ));
            }

        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendUsersNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        /**
     * создаётся список уведомлений об окончании подписки с теми данными из БД, которые ещё актуальны
     */
        List<UserFollowing> notifications = userRepository.findByDateEndedBeforeAndSentFalse(now);

        /**
     * C помощью цикла проходим по всем notifications
     * вызываем метод для отправки сообщения
     */
        for (UserFollowing notification : notifications) {
            sendMessageNotifications(notification);
            notification.setSent(true);
            userRepository.save(notification);
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotificationAboutEndedSubscription() {
        LocalDateTime now = LocalDateTime.now();

        //от уведомления об окончании плюс 2 дня
        List<UserFollowing> notifications = userRepository.findByDateEndedBeforeAndSentFalse(now);

        //по циклу для очистки данных в БД тех пользователей, у которых закончилась подписка
        for (UserFollowing notification : notifications) {
            sendMessageNotificationsAboutEndedSubscription(notification);
            userRepository.findByUserId(notification.getUserId()).setDateStarted(null);
            userRepository.findByUserId(notification.getUserId()).setDateEnded(null);
            userRepository.findByUserId(notification.getUserId()).setPayment(false);

            //удаление пользователя из списка каналов
            for (String chatId : TARGET_CHANNELS) {
                try {
                    // Сначала баним (это удалит из канала)
                    BanChatMember ban = new BanChatMember(chatId, notification.getUserId());
                    telegramBot.execute(ban);

                    // Затем разбаниваем (если нужно, чтобы мог присоединиться снова)
                    UnbanChatMember unban = new UnbanChatMember(chatId, notification.getUserId());
                    telegramBot.execute(unban);

                } catch (IllegalAccessError e) {
                    //allSuccess = false;
                    // Логируем ошибку для конкретного канала
                    System.err.println("Error removing user from channel " + chatId + ": " + e.getMessage());
                }
            }
        }

    }

    public void removeUser(Long userId) {
        for (String chatId : TARGET_CHANNELS) {
            try {
                // Сначала баним (это удалит из канала)
                BanChatMember ban = new BanChatMember(chatId, userId);
                telegramBot.execute(ban);

                // Затем разбаниваем (если нужно, чтобы мог присоединиться снова)
                UnbanChatMember unban = new UnbanChatMember(chatId, userId);
                telegramBot.execute(unban);

            } catch (IllegalAccessError e) {
                //allSuccess = false;
                // Логируем ошибку для конкретного канала
                System.err.println("Error removing user from channel " + chatId + ": " + e.getMessage());
            }
        }
    }

    private void sendMessageNotifications(UserFollowing notification) {
        SendMessage sendMessage = new SendMessage(
                notification.getChatId().toString(),
                TEXT_NOTIFICATION);

        try {
            telegramBot.execute(sendMessage);
        } catch (Exception e) {
            logger.error("Ошибка отправки сообщения в чат{}", notification.getChatId(), e);
        }
    }

    private void sendMessageNotificationsAboutEndedSubscription(UserFollowing notification) {
        SendMessage sendMessage = new SendMessage(
                notification.getChatId().toString(),
                TEXT_ABOUT_ENDED_SUBSCRIPTION);

        try {
            telegramBot.execute(sendMessage);
        } catch (Exception e) {
            logger.error("Ошибка отправки сообщения в чат{}", notification.getChatId(), e);
        }
    }
}
