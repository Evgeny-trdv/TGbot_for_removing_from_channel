package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.BanChatMember;
import com.pengrad.telegrambot.request.UnbanChatMember;
import org.springframework.stereotype.Service;

import static ru.telegrambot.telegram_bot_support.constant.InformationConstant.TARGET_CHANNELS;

/**
 * Сервис для удаления и разбанивания пользователя из всех групп/каналов в списке
 */
@Service
public class RemoverUserFromChannelsService {

    private final TelegramBot telegramBot;

    public RemoverUserFromChannelsService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    /**
     * метод удаляющий и разбанивающий пользователя из всех групп/каналов в списке через цикл
     * @param chatId id чата последователя
     */
    public void removeUserFromChannels(Long chatId) {
        for (String channelId : TARGET_CHANNELS) {
            try {
                // Баним пользователя для удаления из группы/канала
                BanChatMember ban = new BanChatMember(channelId, chatId);
                telegramBot.execute(ban);

                // Разбаниваем пользователя для возможности вернуться в группу/канал
                UnbanChatMember unban = new UnbanChatMember(channelId, chatId);
                telegramBot.execute(unban);

            } catch (IllegalAccessError e) {
                // Логируем ошибку для конкретной группы/канала
                System.err.println("Error removing user from channel " + chatId + ": " + e.getMessage());
            }
        }
    }
}
