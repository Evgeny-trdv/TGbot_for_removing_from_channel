package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;

@Service
public class SendMessageService {

    public SendMessage getSendStartMessage(long chatId, String firstNameUser) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                "Добро пожаловать, " + firstNameUser + ", для получения доступа к чатам необходимо провести оплату. \n"
                        + "\n Доступ к чатам выполняется по подписке. " +
                        "\n Срок подписки - 1 месяц." +
                        "\n Цена подписки - 50$" +
                        "\n переводите на USDT:номер кошелька \n" +
                        "\n После завершения оплаты отправьте боту скриншот транзакции");
        return sendMessage;
    }

    public SendMessage getSendTextMessageAboutSaveUserToBD(long chatId) {
        SendMessage sendMessageAboutSaveUserToBD = new SendMessage(chatId, "Пользователь был успешно внесен в БД!");
        return sendMessageAboutSaveUserToBD;
    }

    public SendMessage getSendTextMessageAboutSuccessfulChecking(long chatId) {
        SendMessage sendMessageAboutSuccessfulChecking = new SendMessage(
                chatId,
                "Фото прошло проверку! В ближайшее время вам придёт папка с чатами"
                + "\nCрок подписки - 30 дней не зависимо от месяца");
        return sendMessageAboutSuccessfulChecking;
    }
}
