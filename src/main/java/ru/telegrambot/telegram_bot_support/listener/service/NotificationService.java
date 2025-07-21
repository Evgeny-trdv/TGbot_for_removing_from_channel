package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.telegrambot.telegram_bot_support.listener.service.message.PreparerMessageService;
import ru.telegrambot.telegram_bot_support.model.UserFollowing;
import ru.telegrambot.telegram_bot_support.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Сервис для уведомления пользователя
 */
@Service
public class NotificationService {

    private final UserRepository userRepository;
    private final PreparerMessageService preparerMessageService;
    private final TelegramBot telegramBot;
    private final RemoverUserFromChannelsService removerUserFromChannelsService;

    public NotificationService(UserRepository userRepository, PreparerMessageService preparerMessageService, TelegramBot telegramBot, RemoverUserFromChannelsService removerUserFromChannelsService) {
        this.userRepository = userRepository;
        this.preparerMessageService = preparerMessageService;
        this.telegramBot = telegramBot;
        this.removerUserFromChannelsService = removerUserFromChannelsService;
    }

    /**
     * метод для отправления уведомления пользователям о скором окончании подписки
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotificationToUsersAboutApproachingEndSubscription() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        // создаётся список уведомлений о скором окончании подписки
        // с теми пользователями из базы данных, которые ещё не получали уведомления
        List<UserFollowing> listUserToSendNotification = userRepository.findByDateNotificationBeforeAndSentNotificationFalse(now);

        // с помощью цикла проходим по всему списку пользователей
        // отправляем им уведомление и обновляем данные о пользователе в базе данных
        for (UserFollowing userFollowing : listUserToSendNotification) {
            telegramBot.execute(preparerMessageService.sendMessageNotificationAboutApproachingEndSubscription(userFollowing));
            userFollowing.setSentNotification(true);
            userRepository.save(userFollowing);
        }
    }

    /**
     * метод для отправления уведомления пользователям об окончании их подписки
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotificationToUsersAboutEndedSubscription() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<UserFollowing> listUserToSendNotification = userRepository.findByDateEndedBeforeAndSentEndedFalse(now);

        for (UserFollowing userFollowing : listUserToSendNotification) {
            Long chatId = userFollowing.getChatId();
            telegramBot.execute(preparerMessageService.sendMessageNotificationAboutEndedSubscription(userFollowing));
            removerUserFromChannelsService.removeUserFromChannels(chatId);
            userRepository.delete(userFollowing);
        }
    }
}
