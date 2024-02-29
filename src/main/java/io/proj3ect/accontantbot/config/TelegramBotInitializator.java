package io.proj3ect.accontantbot.config;

import io.proj3ect.accontantbot.service.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

//класс для инициялизации бота
@Component
public class TelegramBotInitializator {
    //авторизует все функции из класса TelegramBot
    @Autowired
    TelegramBot telegramBot;
    //анатоция которые отвечает если что то происходит
    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi=new TelegramBotsApi(DefaultBotSession.class);
        try{
            telegramBotsApi.registerBot(telegramBot);
        }catch (TelegramApiException e){
            System.out.println("Problem "+e.getMessage());
        }
    }
}
