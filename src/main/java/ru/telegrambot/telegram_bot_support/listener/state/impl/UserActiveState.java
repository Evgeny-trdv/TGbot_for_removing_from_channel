package ru.telegrambot.telegram_bot_support.listener.state.impl;

import org.springframework.stereotype.Component;
import ru.telegrambot.telegram_bot_support.listener.state.ActiveState;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserActiveState implements ActiveState {
    private final ConcurrentHashMap<Long, String> userStates = new ConcurrentHashMap<>();

    // Устанавливаем состояние пользователя (например, "AWAITING_NAME")
    @Override
    public void setUserState(Long chatId, String state) {
        userStates.put(chatId, state);
    }

    // Получаем текущее состояние
    @Override
    public String getUserState(Long chatId) {
        return userStates.get(chatId);
    }

    // Сбрасываем состояние
    @Override
    public void clearUserState(Long chatId) {
        userStates.remove(chatId);
    }
}
