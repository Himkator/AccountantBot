package io.proj3ect.accontantbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data //удобная анотация для геттеров и сеттеров
@Configuration //Конфигурация для всего проекта
@PropertySource("application.properties") //откуда берет данные
public class TelegramBotConfig {
    @Value("${bot.name}")
    String botName;
    @Value("${bot.token}")
    String botToken;
    @Value("${bot.HostId}")
    Long botHostId;
}
