package ru.telegrambot.telegram_bot_support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelegramBotSupportApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramBotSupportApplication.class, args);
	}

}
