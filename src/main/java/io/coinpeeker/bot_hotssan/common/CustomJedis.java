package io.coinpeeker.bot_hotssan.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class CustomJedis {

    private JedisPool jedisPool;

    public CustomJedis(){
        jedisPool = new JedisPool();
    }

    public Jedis getResource(){
        return this.jedisPool.getResource();
    }

}
