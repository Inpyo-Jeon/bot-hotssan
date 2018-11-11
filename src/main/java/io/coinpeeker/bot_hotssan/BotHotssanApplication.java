package io.coinpeeker.bot_hotssan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = {"io.coinpeeker.bot_hotssan.repository"})
public class BotHotssanApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotHotssanApplication.class, args);
    }
}
