package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.request.ForwardMessage;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static ru.telegrambot.telegram_bot_support.constant.TelegramConstant.ADMIN_CHAT_ID;

@Service
public class ForwarderPhotoToVerifyService {

    public final List<Long> listWaiting = new CopyOnWriteArrayList<>();
    public final Map<Long ,ForwardMessage> forwardMap = new HashMap<>();

    public ForwardMessage forwardMessageToAdmin(long chatId, Integer messageId) {

        ForwardMessage forward = new ForwardMessage(ADMIN_CHAT_ID,
                chatId,
                messageId);
        listWaiting.add(chatId);
        forwardMap.put(chatId, forward);
        return forward;
    }

}
