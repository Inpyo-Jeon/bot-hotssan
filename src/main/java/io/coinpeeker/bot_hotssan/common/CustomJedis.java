package io.coinpeeker.bot_hotssan.common;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component
public class CustomJedis {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

//
//    private JedisPool jedisPool;
//
//    public CustomJedis(){
//        jedisPool = new JedisPool(redisHost, redisPort);
//    }
//
//    public Jedis getResource(){
//        return this.jedisPool.getResource();
//    }

}
