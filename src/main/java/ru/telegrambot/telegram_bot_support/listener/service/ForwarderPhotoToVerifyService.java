package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.request.ForwardMessage;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static ru.telegrambot.telegram_bot_support.constant.InformationConstant.ADMIN_CHAT_ID;

/**
 * сервис для переотправления сообщения-фотографии
 * администратору на проверку
 */
@Service
public class ForwarderPhotoToVerifyService {

    public final List<Long> listWaiting = new CopyOnWriteArrayList<>();
    public final Map<Long ,ForwardMessage> forwardMap = new HashMap<>();

    /**
     * метод для формирования пересыльного
     * сообщения администатору
     * @param chatId id чата пользователя
     * @param messageId сообщение пользователя
     * @return ForwardMessage пересыльное сообщение
     */
    public ForwardMessage forwardMessageToAdmin(long chatId, Integer messageId) {

        ForwardMessage forward = new ForwardMessage(ADMIN_CHAT_ID,
                chatId,
                messageId);
        addWaiting(chatId, forward);
        return forward;
    }

    /**
     * метод - активация ожидания ответа
     * для предотвращения потери запроса
     * @param chatId id чата пользователя
     * @param forward пересыльное сообщение
     */
    private void addWaiting(Long chatId, ForwardMessage forward) {
        listWaiting.add(chatId);
        forwardMap.put(chatId, forward);
    }
}
