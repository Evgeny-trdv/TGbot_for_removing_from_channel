package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.BanChatMember;
import com.pengrad.telegrambot.request.UnbanChatMember;
import org.springframework.stereotype.Service;

import static ru.telegrambot.telegram_bot_support.constant.TelegramConstant.TARGET_CHANNELS;

@Service
public class RemoverUserFromChannelsService {

    private final TelegramBot telegramBot;

    public RemoverUserFromChannelsService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void removeUserFromChannels(Long chatId) {
        for (String channelId : TARGET_CHANNELS) {
            try {
                // Сначала баним (это удалит из канала)
                BanChatMember ban = new BanChatMember(channelId, chatId);
                telegramBot.execute(ban);

                // Затем разбаниваем (если нужно, чтобы мог присоединиться снова)
                UnbanChatMember unban = new UnbanChatMember(channelId, chatId);
                telegramBot.execute(unban);

            } catch (IllegalAccessError e) {
                //allSuccess = false;
                // Логируем ошибку для конкретного канала
                System.err.println("Error removing user from channel " + chatId + ": " + e.getMessage());
            }
        }
    }
}
