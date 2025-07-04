package ru.telegrambot.telegram_bot_support.listener.service.message;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.ForwardMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import org.springframework.stereotype.Service;

/**
 * сервис для предоставления отправления
 * ответа от телеграмм бота
 */
@Service
public class SenderMessageService {

    private final TelegramBot telegramBot;

    public SenderMessageService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    /**
     * метод отправления сообщения от телеграмм бота
     * @param message текст сообщения
     */
    public void sendMessage(SendMessage message) {
        telegramBot.execute(message);
    }

    /**
     * метод отправления фотографии от телеграмм бота
     * @param photo фотография
     */
    public void sendPhoto(SendPhoto photo) {
        telegramBot.execute(photo);
    }

    /**
     * метод отправления пересыльного сообщения от телеграмм бота
     * @param forwardMessage пересыльное сообщение
     */
    public void sendForwardMessage(ForwardMessage forwardMessage) {
        telegramBot.execute(forwardMessage);
    }
}
