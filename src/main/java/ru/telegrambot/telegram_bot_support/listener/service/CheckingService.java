package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.stereotype.Service;
import ru.telegrambot.telegram_bot_support.model.UserFollowing;
import ru.telegrambot.telegram_bot_support.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class CheckingService {

    public final UserStateRegistry userStateRegistry;
    public final SendMessageService sendMessageService;
    public final TelegramBot telegramBot;
    public final UserRepository userRepository;

    public CheckingService(UserStateRegistry userStateRegistry, SendMessageService sendMessageService, TelegramBot telegramBot, UserRepository userRepository) {
        this.userStateRegistry = userStateRegistry;
        this.sendMessageService = sendMessageService;
        this.telegramBot = telegramBot;
        this.userRepository = userRepository;
    }

    public void handleUserInput(long chatId, String userState, long chatIdNewUser) {
        switch (userState) {
            case "AWAITING_NAME":
                telegramBot.execute(sendMessageService.getSendTextMessageAboutSaveUserToBD(chatId));
                saveUserInBD(chatIdNewUser);
                userStateRegistry.clearUserState(chatId);
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




}
