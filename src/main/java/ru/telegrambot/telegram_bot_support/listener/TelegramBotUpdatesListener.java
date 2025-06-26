package ru.telegrambot.telegram_bot_support.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.telegrambot.telegram_bot_support.listener.service.*;
import ru.telegrambot.telegram_bot_support.repository.UserRepository;

import java.util.List;


import static ru.telegrambot.telegram_bot_support.constant.TelegramConstant.*;


@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    public static long chatIdNewUser = 0L;

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final MessageService messageService;
    private final ForwarderPhotoToVerifyService forwarderPhotoToVerifyService;
    private final UserStateRegistry userStateRegistry;
    private final CheckingService checkingService;
    private final InlineButtonService inlineButtonService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, MessageService messageService, ForwarderPhotoToVerifyService forwarderPhotoToVerifyService, UserStateRegistry userStateRegistry, CheckingService checkingService, InlineButtonService inlineButtonService) {
        this.telegramBot = telegramBot;
        this.messageService = messageService;
        this.forwarderPhotoToVerifyService = forwarderPhotoToVerifyService;
        this.userStateRegistry = userStateRegistry;
        this.checkingService = checkingService;
        this.inlineButtonService = inlineButtonService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
        try {
            setCommands();
        } catch (TelegramException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update {}", update);

            if (update.callbackQuery() != null) {
                handleCallbackQuery(update);
                return; // Выходим, так как это отдельный тип обновления
            }

            if (update.message() != null) {
                handleMessage(update);
            }

        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleMessage(Update update) {
        Message messageChat = update.message();

        // Обработка фото
        if (messageChat.photo() != null && userStateRegistry.getUserState(update.message().chat().id()) != null) {
            ForwardMessage forwardMessage = forwarderPhotoToVerifyService.forwardMessageToAdmin(
                    update.message().chat().id(),
                    update.message().messageId());

            telegramBot.execute(forwardMessage);
            telegramBot.execute(
                    messageService
                            .getSendTextMessageToAdminForCheckingPayment(
                                    update.message().chat().id(),
                                    update.message().chat().firstName(),
                                    update.message().from().username()
                            ));

            telegramBot.execute(
                    messageService.
                            getSendTextMessageToUserAboutGettingPhoto(
                                    update.message().chat().id()));

            userStateRegistry.setUserState(ADMIN_CHAT_ID, "AWAITING_NAME");

            chatIdNewUser = update.message().chat().id();
            return;
        }

        if (messageChat.text() != null) {
            String adminState = userStateRegistry.getUserState(ADMIN_CHAT_ID);

            if (adminState != null
                    && messageChat.text().equalsIgnoreCase("да")
                    && messageChat.replyToMessage() != null
                    /*&& !(forwardPhotoToCheck.listWaiting.isEmpty())*/
                    && !(forwarderPhotoToVerifyService.forwardMap.isEmpty())) {
                // Если бот ожидает ввод от пользователя
                    /**
                     * метод для работы логики Данила в момент получения фотографии
                     * необходим ответ от Данила для дальнейшей записи в БД юзера
                     */
                for (Long l : forwarderPhotoToVerifyService.listWaiting) {

                    checkingService.handleUserInput(ADMIN_CHAT_ID, adminState, l); //добавление пользователя в БД после подтверждения оплаты + сообщение об этом
                    telegramBot.execute(messageService.getSendTextMessageToUserAboutSuccessfulChecking(l)); //сообщение пользователю о успешной проверки скриншота об оплате
                    //forwardPhotoToCheck.listWaiting.remove(forwardPhotoToCheck.listWaiting.size() - 1);
                    forwarderPhotoToVerifyService.forwardMap.remove(l);
                    forwarderPhotoToVerifyService.listWaiting.remove(l);
                }

                if (/*forwardPhotoToCheck.listWaiting.isEmpty()
                        && */forwarderPhotoToVerifyService.forwardMap.isEmpty()) {
                    userStateRegistry.clearUserState(ADMIN_CHAT_ID);
                }
                    return;
            }

            if (update.message().text().equals("/start")) {

                InlineKeyboardMarkup keyboardMarkup = inlineButtonService.getButtonsForStart();

                telegramBot.execute(messageService.getSendStartMessage(
                        update.message().chat().id(),
                        update.message().from().firstName()
                ).replyMarkup(keyboardMarkup));
            }

        }
    }

    private void handleCallbackQuery(Update update) {
        try {
            // 1. Получаем callback query
            CallbackQuery callbackQuery = update.callbackQuery();
            if (callbackQuery == null || callbackQuery.message() == null) {
                //logger.warn("Invalid callback query");
                return;
            }

            // 2. Извлекаем данные
            String callbackData = callbackQuery.data();
            Message message = callbackQuery.message();
            long chatId = message.chat().id();
            int messageId = message.messageId();
            String callbackId = callbackQuery.id();

            //logger.info("Processing callback: {}", callbackData);
            //logger.info("Handling callback: {} in chat {}", callbackData, chatId);

            // 3. Отвечаем на callback (убираем "часики")
            //telegramBot.execute(new AnswerCallbackQuery(callbackId));

            // 4. Создаем клавиатуру
            InlineKeyboardMarkup keyboardMarkup;
            String responseText;

            // 5. Обрабатываем разные варианты callbackData
            switch (callbackData) {
                case "list":
                    responseText = LIST_CHANNELS;
                    keyboardMarkup = inlineButtonService.getButtonsForList();
                    break;

                case "pay":
                    responseText = PAYMENT;
                    keyboardMarkup = inlineButtonService.getButtonsForPayment();
                    userStateRegistry.setUserState(chatId
                             , "photo");
                    break;

                case "support":
                    responseText = SUPPORT;
                    keyboardMarkup = inlineButtonService.getButtonsForSupport();
                    break;

                case "back":
                    responseText = TEXT_INITIAL;

                    keyboardMarkup = inlineButtonService.getButtonsForStart();
                    break;

                default:
                    responseText = "Неизвестная команда";
                    keyboardMarkup = null;
            }

            // 6. Создаем и отправляем сообщение
            EditMessageCaption editMessageCaption = new EditMessageCaption(chatId, messageId).caption(responseText).parseMode(ParseMode.HTML);
            //EditMessageText editMessage = new EditMessageText(chatId, messageId, responseText);
            if (keyboardMarkup != null) {
                editMessageCaption.replyMarkup(keyboardMarkup);
            }

            logger.info("Sending edit for message {} in chat {}", messageId, chatId);
            BaseResponse response = telegramBot.execute(editMessageCaption);

            if (!response.isOk()) {
                logger.error("Failed to edit message: {}", response.description());
            }

        } catch (Exception e) {
            logger.error("Callback processing error", e);
        }
    }

    private void setCommands() throws TelegramException {
        BotCommand commandFirst = new BotCommand("/start", "Запуск бота");
        BotCommand commandSecond = new BotCommand("/help", "nothing");

        telegramBot.execute(new SetMyCommands(commandFirst, commandSecond));
    }
}
