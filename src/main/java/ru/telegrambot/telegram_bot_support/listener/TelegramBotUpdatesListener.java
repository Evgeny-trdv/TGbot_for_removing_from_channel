package ru.telegrambot.telegram_bot_support.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.telegrambot.telegram_bot_support.listener.service.ForwardPhotoToCheck;
import ru.telegrambot.telegram_bot_support.listener.service.SendStartMessageService;
import ru.telegrambot.telegram_bot_support.model.UserFollowing;
import ru.telegrambot.telegram_bot_support.repository.UserRepository;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static ru.telegrambot.telegram_bot_support.constant.TelegramConstant.*;


@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final UserRepository userRepository;
    private final SendStartMessageService sendStartMessageService;
    private final ForwardPhotoToCheck forwardPhotoToCheck;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, UserRepository userRepository, SendStartMessageService sendStartMessageService, ForwardPhotoToCheck forwardPhotoToCheck) {
        this.telegramBot = telegramBot;
        this.userRepository = userRepository;
        this.sendStartMessageService = sendStartMessageService;
        this.forwardPhotoToCheck = forwardPhotoToCheck;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {

        updates.forEach(update -> {
            logger.info("Processing update {}", update);


            if (update.message() == null) {
                /**
                 * игнорирование ошибки пустой строчки
                 */
                return;
            }
            if (update.message().photo() != null) {

                ForwardMessage forwardMessage = forwardPhotoToCheck.forwardMessageToDaniel(
                            update.message().chat().id(),
                            update.message().messageId());
                telegramBot.execute(forwardMessage);
                telegramBot.execute(new SendMessage(
                            update.message().chat().id(),
                            "Фото получено на проверку!"));
                telegramBot.execute(new SendMessage(
                            YOUR_CHAT_ID,
                            "Имя клиента: "
                                    + update.message().chat().firstName()
                                    + "\nUsername: "
                                    + update.message().from().username()
                                    + "\nid: "
                                    + update.message().chat().id()));
            }

            if (update.message().text() != null) {

                if (update.message().text().equals("/start")) {
                    SendMessage sendMessage = sendStartMessageService.getSendMessage(
                            update.message().chat().id(),
                            update.message().from().firstName());
                    telegramBot.execute(sendMessage);
                }

                if (update.message().text().equals("/leave")) {
                    removeUser(update.message().chat().id());
                }
            }

            /*if (messageTextUser.equals("/payment")) {
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
            }*/

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
            userRepository.findByChatId(notification.getChatId()).setDateStarted(null);
            userRepository.findByChatId(notification.getChatId()).setDateEnded(null);
            userRepository.findByChatId(notification.getChatId()).setPayment(false);

            //удаление пользователя из списка каналов
            for (String chatId : TARGET_CHANNELS) {
                try {
                    // Сначала баним (это удалит из канала)
                    BanChatMember ban = new BanChatMember(chatId, notification.getChatId());
                    telegramBot.execute(ban);

                    // Затем разбаниваем (если нужно, чтобы мог присоединиться снова)
                    UnbanChatMember unban = new UnbanChatMember(chatId, notification.getChatId());
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
