package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.request.SendMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class SendStartMessageService {

    public SendMessage getSendMessage(long chatId, String firstNameUser) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                "Добро пожаловать" + firstNameUser +", для получения доступа к чатам необходимо провести оплату. \n"
                        + "\n Доступ к чатам выполняется по подписке. " +
                        "\n Срок подписки - 1 месяц." +
                        "\n Цена подписки - 50$" +
                        "\n переводите на USDT:номер кошелька \n" +
                        "\n После завершения оплаты отправьте боту скриншот транзакции");

        return sendMessage;
    }
}
