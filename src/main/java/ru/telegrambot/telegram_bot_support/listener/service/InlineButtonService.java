package ru.telegrambot.telegram_bot_support.listener.service;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import org.springframework.stereotype.Service;

@Service
public class InlineButtonService {

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
