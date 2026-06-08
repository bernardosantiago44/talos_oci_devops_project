package com.springboot.MyTodoList.util;

import com.springboot.MyTodoList.config.BotProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@ConditionalOnExpression("!'${telegram.bot.token:}'.trim().isEmpty()")
public class MyTodoListBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTodoListBot.class);

    private final TelegramClient telegramClient;
    private final BotProps botProps;

    public MyTodoListBot(TelegramClient telegramClient, BotProps botProps) {
        this.telegramClient = telegramClient;
        this.botProps = botProps;
    }

    @Override
    public String getBotToken() {
        return botProps.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text(update.getMessage().getText())
                    .build();
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException exception) {
                LOGGER.error("Failed to send Telegram message", exception);
            }
        }
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        System.out.println("Registered bot running state is: " + botSession.isRunning());
    }
}
