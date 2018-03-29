package io.coinpeeker.bot_hotssan.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class CustomJedis {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    private JedisPool jedisPool;

    public CustomJedis(){
        jedisPool = new JedisPool(redisHost, redisPort);
    }

    public Jedis getResource(){
        return this.jedisPool.getResource();
    }

}
