package ru.telegrambot.telegram_bot_support.listener.service;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStateRegistry {
    private final ConcurrentHashMap<Long, String> userStates = new ConcurrentHashMap<>();

    // Устанавливаем состояние пользователя (например, "AWAITING_NAME")
    public void setUserState(Long userId, String state) {
        userStates.put(userId, state);
    }

    // Получаем текущее состояние
    public String getUserState(Long userId) {
        return userStates.get(userId);
    }

    // Сбрасываем состояние
    public void clearUserState(Long userId) {
        userStates.remove(userId);
    }
}
