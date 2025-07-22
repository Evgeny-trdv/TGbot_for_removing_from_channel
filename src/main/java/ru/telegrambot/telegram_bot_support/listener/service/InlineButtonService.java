package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import org.springframework.stereotype.Service;

/**
 * сервис, отвечающий за активацию кнопок
 * и переход между ними
 */
@Service
public class InlineButtonService {

    /**
     * метод для активации стартовых кнопок
     * @return кнопки под сообщением
     */
    public InlineKeyboardMarkup getButtonsForStart() {
        InlineKeyboardButton BigButton = new InlineKeyboardButton(
                "Список приваток").callbackData("list");
        InlineKeyboardButton SmallButton1 = new InlineKeyboardButton(
                "Подписка").callbackData("pay");
        InlineKeyboardButton SmallButton2 = new InlineKeyboardButton(
                "Поддержка").callbackData("support");

        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {BigButton},
                new InlineKeyboardButton[] {SmallButton1, SmallButton2}
        );
    }

    /**
     * метод для предоставления кнопок после нажатия "Список приваток"
     * @return кнопки под сообщением
     */
    public InlineKeyboardMarkup getButtonsForList() {
        InlineKeyboardButton BigButton = new InlineKeyboardButton(
                "← Вернуться назад").callbackData("back");
        InlineKeyboardButton SmallButton1 = new InlineKeyboardButton(
                "Подписка").callbackData("pay");
        InlineKeyboardButton SmallButton2 = new InlineKeyboardButton(
                "Поддержка").callbackData("support");

        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {SmallButton1, SmallButton2},
                new InlineKeyboardButton[] {BigButton}
        );
    }

    /**
     * метод для предоставления кнопок после нажатия "Подписка"
     * @return кнопки под сообщением
     */
    public InlineKeyboardMarkup getButtonsForPayment() {
        InlineKeyboardButton button1 = new InlineKeyboardButton(
                "← Вернуться назад").callbackData("back");
        InlineKeyboardButton button2 = new InlineKeyboardButton(
                "Список приваток").callbackData("list");
        InlineKeyboardButton button3 = new InlineKeyboardButton(
                "Поддержка").callbackData("support");

        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {button2},
                new InlineKeyboardButton[] {button3},
                new InlineKeyboardButton[] {button1}
        );
    }

    /**
     * метод для предоставления кнопок после нажатия "Поддержка"
     * @return кнопки под сообщением
     */
    public InlineKeyboardMarkup getButtonsForSupport() {
        InlineKeyboardButton button1 = new InlineKeyboardButton(
                "← Вернуться назад").callbackData("back");
        InlineKeyboardButton button2 = new InlineKeyboardButton(
                "Список приваток").callbackData("list");
        InlineKeyboardButton button3 = new InlineKeyboardButton(
                "Подписка").callbackData("pay");

        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {button2},
                new InlineKeyboardButton[] {button3},
                new InlineKeyboardButton[] {button1}
        );
    }

}
