package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.stereotype.Service;
import ru.telegrambot.telegram_bot_support.model.UserFollowing;
import ru.telegrambot.telegram_bot_support.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class CheckingService {

    public final UserStateRegistry userStateRegistry;
    public final MessageService messageService;
    public final TelegramBot telegramBot;
    public final UserRepository userRepository;

    public CheckingService(UserStateRegistry userStateRegistry, MessageService messageService, TelegramBot telegramBot, UserRepository userRepository) {
        this.userStateRegistry = userStateRegistry;
        this.messageService = messageService;
        this.telegramBot = telegramBot;
        this.userRepository = userRepository;
    }

    public void handleUserInput(long chatId, String userState, long chatIdNewUser) {
        if (userState.equals("AWAITING_NAME")) {
            telegramBot.execute(messageService.getSendTextMessageToAdminAboutSaveUserToBD(chatId));
            saveUserInBD(chatIdNewUser);
            //userStateRegistry.clearUserState(chatId);
            // Можно добавить другие состояния
        }
    }

    private void saveUserInBD(long chatIdNewUser) {
        LocalDateTime now = LocalDateTime.now(); //день оплаты
        LocalDateTime notification = LocalDateTime.now().plusDays(28); //день уведомления об окончании подписки
        LocalDateTime end = LocalDateTime.now().plusDays(30); //день уведомления об окончании подписки
        UserFollowing user = new UserFollowing(
                chatIdNewUser,
                now,
                notification,
                end
        );
        user.setPayment(true);
        userRepository.save(user);
    }




}
