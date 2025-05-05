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
    private final SendMessageService sendMessageService;
    private final TelegramBot telegramBot;

    public NotificationService(UserRepository userRepository, SendMessageService sendMessageService, TelegramBot telegramBot) {
        this.userRepository = userRepository;
        this.sendMessageService = sendMessageService;
        this.telegramBot = telegramBot;
    }


    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotificationToUsersAboutApproachingEndSubscription() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        /**
         * создаётся список уведомлений об окончании подписки с теми данными из БД, которые ещё актуальны
         */
        List<UserFollowing> listUserToSendNotification = userRepository.findByDateNotificationBeforeAndSentFalse(now);

        /**
         * C помощью цикла проходим по всем notifications
         * вызываем метод для отправки сообщения
         */
        for (UserFollowing userFollowing : listUserToSendNotification) {
            telegramBot.execute(sendMessageService.sendMessageNotificationAboutApproachingEndSubscription(userFollowing));
            userFollowing.setSent(true);
            userRepository.save(userFollowing);

        }
    }
}
