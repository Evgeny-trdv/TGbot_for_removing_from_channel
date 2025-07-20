package ru.telegrambot.telegram_bot_support.listener.service;

import org.springframework.stereotype.Service;
import ru.telegrambot.telegram_bot_support.listener.service.message.PreparerMessageService;
import ru.telegrambot.telegram_bot_support.listener.service.message.SenderMessageService;
import ru.telegrambot.telegram_bot_support.model.UserFollowing;
import ru.telegrambot.telegram_bot_support.repository.UserRepository;

import java.time.LocalDateTime;

import static ru.telegrambot.telegram_bot_support.constant.InformationConstant.ADMIN_CHAT_ID;

/**
 * Сервис для добавления пользователя в базу данных
 * и уведомления администратора
 */
@Service
public class AddingUserToDataBaseService {

    private final PreparerMessageService preparerMessageService;
    private final UserRepository userRepository;
    private final SenderMessageService senderMessageService;

    public AddingUserToDataBaseService(PreparerMessageService preparerMessageService, UserRepository userRepository, SenderMessageService senderMessageService) {
        this.preparerMessageService = preparerMessageService;
        this.userRepository = userRepository;
        this.senderMessageService = senderMessageService;
    }

    /**
     * Метод добавления пользователя в базу данных,
     * пока состояние администратора активно
     * отправляет сообщение о том, что пользователь добавлен в БД
     * @param adminState состояние администратора
     * @param chatIdNewUser id чата пользователя
     */
    public void handleUserInput(String adminState, long chatIdNewUser) {
        if (!adminState.isEmpty()) {
            senderMessageService.sendMessage(
                    preparerMessageService.getSendTextMessageToAdminAboutSaveUserToDB(ADMIN_CHAT_ID));
            //telegramBot.execute(preparerMessageService.getSendTextMessageToAdminAboutSaveUserToDB(chatId));
            saveUserToDataBase(chatIdNewUser);
            // Можно добавить другие состояния
        }
    }

    private void saveUserToDataBase(long chatIdNewUser) {
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
