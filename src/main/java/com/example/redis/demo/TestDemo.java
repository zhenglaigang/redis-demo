package com.example.redis.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.hash.ObjectHashMapper;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TestDemo {

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @Autowired
  private ObjectMapper objectMapper;


  //low/high api
  public void test() {

    //高阶API - RedisTemplate
    redisTemplate.opsForValue().set("k1", "k1_value");
    System.out.println(redisTemplate.opsForValue().get("k1"));

    //高阶API - StringRedisTemplate
    stringRedisTemplate.opsForValue().set("k2", "k2_value");
    System.out.println(stringRedisTemplate.opsForValue().get("k2"));

    //低阶API - 自己获取连接，进行操作
    RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
    conn.set("k2".getBytes(), "123".getBytes());
    System.out.println(new String(conn.get("k2".getBytes())));
  }

  //redis-hash
  public void test2() {
    //1. 直接存入hash
    redisTemplate.opsForHash().put("dhd", "name", "zhangsan");
//    redisTemplate.opsForHash().put("dhd", "age", 18);
//    System.out.println(redisTemplate.opsForHash().get("dhd", "name"));

    //使用stringRedisTemplate会报错：转换错误（因为value中有 int类型属性），需要指定序列化
//    stringRedisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
//    stringRedisTemplate.opsForHash().put("dhd", "name", "zhangsan");
//    stringRedisTemplate.opsForHash().put("dhd", "age", 18);
//    System.out.println(stringRedisTemplate.opsForHash().get("dhd", "name"));

    //2. 实际场景多为实体对象数据，需要将实体对象转换为hash：ObjectMapper(Object-->JSON)；Jackson2HashMapper(JSON--> hash)
    Person person = new Person();
    person.setName("zhangsan");
    person.setAge(18);

    Jackson2HashMapper mapper = new Jackson2HashMapper(objectMapper, false);
    redisTemplate.opsForHash().putAll("dhd", mapper.toHash(person));
    Person per = objectMapper.convertValue(mapper.fromHash(redisTemplate.opsForHash().entries("dhd")), Person.class);
//    stringRedisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
//    stringRedisTemplate.opsForHash().putAll("dhd", mapper.toHash(person));
//    Person per = objectMapper.convertValue(mapper.fromHash((HashMap)stringRedisTemplate.opsForHash().entries("dhd")), Person.class);

    System.out.println(per.getName());
  }

  //自定义Template
  @Autowired
  @Qualifier("ooxx")
  private StringRedisTemplate stringTemplate;
  //自定义Template
  public void test3() {
    stringRedisTemplate.opsForHash().put("dhd", "name", "zhangsan");
    stringRedisTemplate.opsForHash().put("dhd", "age", 18);
    System.out.println(stringRedisTemplate.opsForHash().get("dhd", "name"));
  }

  //发布、订阅
  public void test4() {

    //发布
    stringRedisTemplate.convertAndSend("ooxx", "hello");

    //低阶API - 订阅
    RedisConnection connection = stringRedisTemplate.getConnectionFactory().getConnection();
    connection.subscribe(new MessageListener() {
      @Override
      public void onMessage(Message message, byte[] pattern) {
        System.out.println(new String(message.getBody()));
      }
    }, "ooxx".getBytes());

    while (true) {
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      stringRedisTemplate.convertAndSend("ooxx", "hello from wo zi ji");
    }
  }

}
