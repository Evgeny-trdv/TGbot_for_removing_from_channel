package ru.telegrambot.telegram_bot_support.listener.service.message;

import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import org.springframework.stereotype.Service;
import ru.telegrambot.telegram_bot_support.model.UserFollowing;

import static ru.telegrambot.telegram_bot_support.constant.InformationConstant.*;
import static ru.telegrambot.telegram_bot_support.constant.TextMessageConstant.*;

/**
 * сервис для подготовки фотографий и сообщений
 * для отправления пользователям или администратору
 */
@Service
public class PreparerMessageService {

    /**
     * метод подготовки приветствующей фотографии
     * @param chatId id чата пользователя
     * @return SendPhoto приветствующую фотографию
     */
    public SendPhoto getStartPhotoMessage(long chatId) {
        java.io.File file = new java.io.File("src/main/resources/static/IMG_9145.PNG");
        SendPhoto photo = new SendPhoto(chatId, file);
        photo.caption(TEXT_INITIAL);

        return photo;
    }

    /**
     * метод подготовки текстового сообщения для администратора
     * касательно информации и проверки оплаты пользователя
     * @param chatId id чата пользователя
     * @param firstNameUser имя пользователя
     * @param username юзернэйм пользователя
     * @return SendMessage текстовое сообщение
     */
    public SendMessage getTextMessageToAdminForCheckingPayment(long chatId, String firstNameUser, String username) {
        return new SendMessage(
                ADMIN_CHAT_ID,
                "Имя клиента: "
                        + firstNameUser
                        + "\nUsername: "
                        + username
                        + "\nid: "
                        + chatId
                        + "\nвведите \"ДА\", если оплата прошла и \"НЕТ\", если оплата не прошла");
    }

    /**
     * метод подготовки текстового сообщения для пользователя об ожидании для проверки подлинности фото
     * @param chatId id чата пользователя
     * @return SendMessage текстовое сообщение
     */
    public SendMessage getSendTextMessageToUserAboutGettingPhoto(long chatId) {
        return new SendMessage(
                chatId,
                TEXT_ABOUT_GETTING_PHOTO);
    }

    /**
     * метод подготовки текстового сообщения для администратора о добавлении пользователя в БД
     * @param chatId id чата пользователя
     * @return SendMessage текстовое сообщение
     */
    public SendMessage getSendTextMessageToAdminAboutSaveUserToDB(long chatId) {
        return new SendMessage(
                chatId,
                TEXT_ABOUT_SAVE_USER_TO_DATABASE);
    }

    /**
     * метод подготовки текстового сообщения для пользователя об активации подписки
     * @param chatId id чата пользователя
     * @return SendMessage текстовое сообщение
     */
    public SendMessage getSendTextMessageToUserAboutSuccessfulVerifying(long chatId) {
        return new SendMessage(
                chatId,
                TEXT_ABOUT_SUCCESSFUL_VERIFYING);
    }

    /**
     * метод подготовки текстового сообщения для пользователя подписки о скором окончании подписки
     * @param userFollowing пользователь с подпиской
     * @return SendMessage текстовое сообщение
     */
    public SendMessage sendMessageNotificationAboutApproachingEndSubscription(UserFollowing userFollowing) {
        return new SendMessage(
                userFollowing.getChatId().toString(),
                TEXT_NOTIFICATION);
    }

    /**
     * метод подготовки текстового сообщения для пользователя об окончании подписки
     * @param userFollowing пользователь с подпиской
     * @return SendMessage текстовое сообщение
     */
    public SendMessage sendMessageNotificationAboutEndedSubscription(UserFollowing userFollowing) {
        return new SendMessage(
                userFollowing.getChatId().toString(),
                TEXT_ABOUT_ENDED_SUBSCRIPTION);
    }
}
