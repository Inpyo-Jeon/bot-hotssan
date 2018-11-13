package io.coinpeeker.bot_hotssan;

import io.coinpeeker.bot_hotssan.scheduler.listed.UpbitListedScheduler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@ActiveProfiles("local")
public class BotHotssanApplicationTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpbitListedScheduler.class);

	@Value("${spring.redis.host}")
	private String redisHost;

	@Test
	public void contextLoads() {
		LOGGER.warn("{}", redisHost);
	}

}
