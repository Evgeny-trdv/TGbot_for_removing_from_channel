package ru.telegrambot.telegram_bot_support.listener.service.message;

import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import org.springframework.stereotype.Service;
import ru.telegrambot.telegram_bot_support.model.UserFollowing;

import static ru.telegrambot.telegram_bot_support.constant.TelegramConstant.*;

/**
 * сервис для подготовки фотографий и сообщений
 * для отправления пользователям или администратору
 */
@Service
public class PreparerMessageService {

    public SendPhoto getStartPhotoMessage(long chatId, String firstNameUser) {
        java.io.File file = new java.io.File("src/main/resources/static/IMG_9145.PNG");
        SendPhoto photo = new SendPhoto(chatId, file);
        photo.caption(TEXT_INITIAL);

        return photo;
    }

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

    public SendMessage getSendTextMessageToUserAboutGettingPhoto(long chatId) {
        return new SendMessage(
                chatId,
                "Фото получено на проверку! Пожалуйста, ожидайте");
    }

    public SendMessage getSendTextMessageToAdminAboutSaveUserToDB(long chatId) {
        return new SendMessage(chatId, "Пользователь был успешно внесен в БД!");
    }

    public SendMessage getSendTextMessageToUserAboutSuccessfulChecking(long chatId) {
        return new SendMessage(
                chatId,
                "Фото прошло проверку! В ближайшее время вам придёт папка с чатами"
                + "\nCрок подписки - 30 дней не зависимо от месяца");
    }

    public SendMessage sendMessageNotificationAboutApproachingEndSubscription(UserFollowing userFollowing) {
        return new SendMessage(
                userFollowing.getChatId().toString(),
                TEXT_NOTIFICATION);
    }

    public SendMessage sendMessageNotificationAboutEndedSubscription(UserFollowing userFollowing) {
        return new SendMessage(
                userFollowing.getChatId().toString(),
                TEXT_ABOUT_ENDED_SUBSCRIPTION);
    }
}
