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
import ru.telegrambot.telegram_bot_support.listener.service.message.PreparerMessageService;
import ru.telegrambot.telegram_bot_support.listener.service.message.SenderMessageService;
import ru.telegrambot.telegram_bot_support.listener.state.impl.AdminActiveState;
import ru.telegrambot.telegram_bot_support.listener.state.impl.UserActiveState;

import java.util.List;


import static ru.telegrambot.telegram_bot_support.constant.InformationConstant.*;


@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    public static long chatIdNewUser = 0L;

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final PreparerMessageService preparerMessageService;
    private final SenderMessageService senderMessageService;
    private final ForwarderPhotoToVerifyService forwarderPhotoToVerifyService;
    private final UserActiveState userActiveState;
    private final AdminActiveState adminActiveState;
    private final AddingUserToDataBaseService addingUserToDataBaseService;
    private final InlineButtonService inlineButtonService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, PreparerMessageService preparerMessageService, SenderMessageService senderMessageService, ForwarderPhotoToVerifyService forwarderPhotoToVerifyService, UserActiveState userActiveState, AdminActiveState adminActiveState, AddingUserToDataBaseService addingUserToDataBaseService, InlineButtonService inlineButtonService) {
        this.telegramBot = telegramBot;
        this.preparerMessageService = preparerMessageService;
        this.senderMessageService = senderMessageService;
        this.forwarderPhotoToVerifyService = forwarderPhotoToVerifyService;
        this.userActiveState = userActiveState;
        this.adminActiveState = adminActiveState;
        this.addingUserToDataBaseService = addingUserToDataBaseService;
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

        /**
         * часть кода, отвечающего за пересылку сообщения с фотографией(чек оплаты)
         * от пользователя к администратору для дальнейшей проверки оригинальности фотографии
         */
        if (messageChat.photo() != null && userActiveState.getUserState(update.message().chat().id()) != null) {
            ForwardMessage forwardMessage = forwarderPhotoToVerifyService.forwardMessageToAdmin(
                    update.message().chat().id(),
                    update.message().messageId());
            senderMessageService.sendForwardMessage(forwardMessage);

            SendMessage textMessageToAdminForCheckingPayment = preparerMessageService
                    .getTextMessageToAdminForCheckingPayment(
                            update.message().chat().id(),
                            update.message().chat().firstName(),
                            update.message().from().username());
            senderMessageService.sendMessage(textMessageToAdminForCheckingPayment);

            SendMessage sendTextMessageToUserAboutGettingPhoto = preparerMessageService.
                    getSendTextMessageToUserAboutGettingPhoto(
                            update.message().chat().id());
            senderMessageService.sendMessage(sendTextMessageToUserAboutGettingPhoto);

            adminActiveState.setUserState(ADMIN_CHAT_ID, "AWAITING_NAME");

            chatIdNewUser = update.message().chat().id();
            return;
        }

        if (messageChat.text() != null) {
            String adminState = adminActiveState.getUserState(ADMIN_CHAT_ID);

            if (adminState != null
                    && messageChat.text().equalsIgnoreCase("да")
                    && messageChat.replyToMessage() != null
                    && !(forwarderPhotoToVerifyService.forwardMap.isEmpty())) {
                // Если бот ожидает ввод от пользователя
                    /**
                     * метод для работы логики Данила в момент получения фотографии
                     * необходим ответ от Данила для дальнейшей записи в БД юзера
                     */
                for (Long l : forwarderPhotoToVerifyService.listWaiting) {

                    addingUserToDataBaseService.handleUserInput(adminState, l); //добавление пользователя в БД после подтверждения оплаты + сообщение об этом
                    telegramBot.execute(preparerMessageService.getSendTextMessageToUserAboutSuccessfulChecking(l)); //сообщение пользователю о успешной проверки скриншота об оплате
                    forwarderPhotoToVerifyService.forwardMap.remove(l);
                    forwarderPhotoToVerifyService.listWaiting.remove(l);
                }

                if (forwarderPhotoToVerifyService.forwardMap.isEmpty()) {
                    adminActiveState.clearUserState(ADMIN_CHAT_ID);
                }
                    return;
            }

            /**
             * часть кода, отвечющее за активацию бота
             * по команде /start с текстом, кнопками и фотографией
             */
            if (update.message().text().equals("/start")) {

                InlineKeyboardMarkup keyboardMarkup = inlineButtonService.getButtonsForStart();

                SendPhoto startPhotoMessage = preparerMessageService.getStartPhotoMessage(
                        update.message().chat().id()
                ).replyMarkup(keyboardMarkup);
                senderMessageService.sendPhoto(startPhotoMessage);

                /*telegramBot.execute(preparerMessageService.getStartPhotoMessage(
                        update.message().chat().id(),
                        update.message().from().firstName()
                ).replyMarkup(keyboardMarkup));*/
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
                    userActiveState.setUserState(chatId
                             , "waiting_photo");
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
