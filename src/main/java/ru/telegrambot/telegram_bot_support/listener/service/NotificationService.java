package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.telegrambot.telegram_bot_support.model.UserFollowing;
import ru.telegrambot.telegram_bot_support.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationService {

    private final UserRepository userRepository;
    private final MessageService messageService;
    private final TelegramBot telegramBot;
    private final RemoverUserFromChannelsService removerUserFromChannelsService;

    public NotificationService(UserRepository userRepository, MessageService messageService, TelegramBot telegramBot, RemoverUserFromChannelsService removerUserFromChannelsService) {
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.telegramBot = telegramBot;
        this.removerUserFromChannelsService = removerUserFromChannelsService;
    }


    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotificationToUsersAboutApproachingEndSubscription() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        /**
         * создаётся список уведомлений об окончании подписки с теми данными из БД, которые ещё актуальны
         */
        List<UserFollowing> listUserToSendNotification = userRepository.findByDateNotificationBeforeAndSentNotificationFalse(now);

        /**
         * C помощью цикла проходим по всем notifications
         * вызываем метод для отправки сообщения
         */
        for (UserFollowing userFollowing : listUserToSendNotification) {
            telegramBot.execute(messageService.sendMessageNotificationAboutApproachingEndSubscription(userFollowing));
            userFollowing.setSentNotification(true);
            userRepository.save(userFollowing);
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotificationToUsersAboutEndedSubscription() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        /**
         * создаётся список уведомлений об окончании подписки с теми данными из БД, которые ещё актуальны
         */
        List<UserFollowing> listUserToSendNotification = userRepository.findByDateEndedBeforeAndSentEndedFalse(now);

        /**
         * C помощью цикла проходим по всем notifications
         * вызываем метод для отправки сообщения
         */
        for (UserFollowing userFollowing : listUserToSendNotification) {
            Long chatId = userFollowing.getChatId();
            telegramBot.execute(messageService.sendMessageNotificationAboutEndedSubscription(userFollowing));

            userFollowing.setSentEnded(true);
            userRepository.save(userFollowing);

//            userRepository.findByChatId(chatId).setDateStarted(null);
//            userRepository.findByChatId(chatId).setDateNotification(null);
//            userRepository.findByChatId(chatId).setDateEnded(null);
//            userRepository.findByChatId(chatId).setPayment(false);

            removerUserFromChannelsService.removeUserFromChannels(chatId);
            userRepository.delete(userFollowing);
//            userRepository.save(userFollowing);
        }
    }
}
