package ru.telegrambot.telegram_bot_support.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.telegrambot.telegram_bot_support.listener.service.CheckingService;
import ru.telegrambot.telegram_bot_support.listener.service.ForwardPhotoToCheck;
import ru.telegrambot.telegram_bot_support.listener.service.SendMessageService;
import ru.telegrambot.telegram_bot_support.listener.service.UserStateRegistry;
import ru.telegrambot.telegram_bot_support.model.UserFollowing;
import ru.telegrambot.telegram_bot_support.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


import static ru.telegrambot.telegram_bot_support.constant.TelegramConstant.*;


@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    public static long chatIdNewUser = 0L;

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final UserRepository userRepository;
    private final SendMessageService sendMessageService;
    private final ForwardPhotoToCheck forwardPhotoToCheck;
    private final UserStateRegistry userStateRegistry;
    private final CheckingService checkingService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, UserRepository userRepository, SendMessageService sendMessageService, ForwardPhotoToCheck forwardPhotoToCheck, UserStateRegistry userStateRegistry, CheckingService checkingService) {
        this.telegramBot = telegramBot;
        this.userRepository = userRepository;
        this.sendMessageService = sendMessageService;
        this.forwardPhotoToCheck = forwardPhotoToCheck;
        this.userStateRegistry = userStateRegistry;
        this.checkingService = checkingService;
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
                long chatId = update.message().chat().id();
                ForwardMessage forwardMessage = forwardPhotoToCheck.forwardMessageToDaniel(
                            update.message().chat().id(),
                            update.message().messageId());

                telegramBot.execute(forwardMessage);

                telegramBot.execute(new SendMessage(
                            update.message().chat().id(),
                            "Фото получено на проверку! Пожалуйста, ожидайте"));

                telegramBot.execute(new SendMessage(
                            YOUR_CHAT_ID,
                            "Имя клиента: "
                                    + update.message().chat().firstName()
                                    + "\nUsername: "
                                    + update.message().from().username()
                                    + "\nid: "
                                    + update.message().chat().id()
                                    + "\nвведите \"ДА\", если оплата прошла и \"НЕТ\", если оплата не прошла"));

                userStateRegistry.setUserState(YOUR_CHAT_ID, "AWAITING_NAME");
                chatIdNewUser = chatId;
            }

            if (update.message().text() != null) {

                // Проверяем, есть ли у пользователя состояние
                String userState = userStateRegistry.getUserState(YOUR_CHAT_ID);

                if (userState != null) {
                    // Если бот ожидает ввод от пользователя
                    if (update.message().text().equalsIgnoreCase("да")) {
                        /**
                         * метод для работы логики Данила в момент получения фотографии
                         * необходим ответ от Данила для дальнейшей записи в БД юзера
                         */
                        checkingService.handleUserInput(YOUR_CHAT_ID, userState, chatIdNewUser);

                        //handleUserInput(YOUR_CHAT_ID, userState, chatIdNewUser);

                        telegramBot.execute(sendMessageService.getSendTextMessageAboutSuccessfulChecking(chatIdNewUser));

/*                        telegramBot.execute(new SendMessage(
                                chatIdNewUser,
                                "Фото прошло проверку! В ближайшее время вам придёт папка с чатами"
                                + "\nCрок подписки - 30 дней не зависимо от месяца"));*/
                        return;
                    }
                }

                if (update.message().text().equals("/start")) {
                    SendMessage sendMessage = sendMessageService.getSendStartMessage(
                            update.message().chat().id(),
                            update.message().from().firstName());
                    telegramBot.execute(sendMessage);
                }

                if (update.message().text().equals("/leave")) {
                    removeUser(update.message().chat().id());
                }

            }
            if (update.message().forwardFrom() != null) {

                if (update.message().chat().id().equals(YOUR_CHAT_ID)
                        && update.message().forwardFrom().isBot()
                        && update.message().text().equalsIgnoreCase("ДА")) {
                    //telegramBot.execute(new SendMessage(YOUR_CHAT_ID, "привет"));
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

    private void handleUserInput(long userId, String userState, long chatIdNewUser) {
        /**
         * userId это Данил
         * здесь логика подтверждения оплаты подписки
         * затем происходит запись юзера в БД и начало отсчёта периода подписки
         */
        switch (userState) {
            case "AWAITING_NAME":
                sendTextMessage(userId, "Пользователь был успешно внесен в БД!");
                saveUserInBD(chatIdNewUser);
                userStateRegistry.clearUserState(userId);
                break;
            // Можно добавить другие состояния
        }
    }

    private void saveUserInBD(long chatIdNewUser) {
        LocalDateTime now = LocalDateTime.now(); //день оплаты
        LocalDateTime next = LocalDateTime.now().plusDays(28); //день уведомления об окончании подписки
        UserFollowing user = new UserFollowing(
                chatIdNewUser,
                now,
                next
        );
        user.setPayment(true);
        userRepository.save(user);
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        try {
            telegramBot.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
