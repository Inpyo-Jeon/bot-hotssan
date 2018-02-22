package io.coinpeeker.bot_hotssan.module;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ModuleTest {

    @Autowired
    private Jedis jedis;
    @Test
    public void 레디스테스트() {

//        redisTemplate.opsForSet().add("jayden", "12345");

        jedis.set("jayden", "12345");

    }
}
