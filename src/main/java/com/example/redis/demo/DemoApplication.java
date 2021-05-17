package com.example.redis.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DemoApplication {

  public static void main(String[] args) {

    ConfigurableApplicationContext confige = SpringApplication.run(DemoApplication.class, args);

    TestDemo testDemo = confige.getBean(TestDemo.class);
    testDemo.test4();
  }

}
