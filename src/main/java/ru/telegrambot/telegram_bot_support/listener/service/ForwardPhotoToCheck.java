package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.request.ForwardMessage;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;

import static ru.telegrambot.telegram_bot_support.constant.TelegramConstant.YOUR_CHAT_ID;

@Service
public class ForwardPhotoToCheck {

    public ForwardMessage forwardMessageToDaniel(long chatId, Integer messageId) {

        ForwardMessage forward = new ForwardMessage(YOUR_CHAT_ID,
                chatId,
                messageId);
        return forward;
    }

}
