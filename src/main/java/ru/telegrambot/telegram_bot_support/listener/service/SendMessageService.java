package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import org.springframework.stereotype.Service;
import ru.telegrambot.telegram_bot_support.model.UserFollowing;

import static ru.telegrambot.telegram_bot_support.constant.TelegramConstant.*;

@Service
public class SendMessageService {

    public SendPhoto getSendStartMessage(long chatId, String firstNameUser) {
        java.io.File file = new java.io.File("src/main/resources/static/2024-05-12_13-00-06.png");
        SendPhoto photo = new SendPhoto(chatId, file);
        photo.caption("Добро пожаловать, " + firstNameUser + ", для получения доступа к чатам необходимо провести оплату. \n"
                + "\n Доступ к чатам выполняется по подписке. " +
                "\n Срок подписки - 1 месяц." +
                "\n Цена подписки - 50$" +
                "\n переводите на USDT:номер кошелька \n" +
                "\n После завершения оплаты отправьте боту скриншот транзакции");
        return photo;
    }

    public SendMessage getSendTextMessageToAdminForCheckingPayment(long chatId, String firstNameUser, String username) {
        return new SendMessage(
                YOUR_CHAT_ID,
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

    public SendMessage getSendTextMessageToAdminAboutSaveUserToBD(long chatId) {
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
