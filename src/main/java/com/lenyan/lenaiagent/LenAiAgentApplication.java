package com.lenyan.lenaiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableAsync //是 Spring 框架中的一个注解，用于开启异步方法执行的支持。
@SpringBootApplication
@MapperScan("com.lenyan.lenaiagent.mapper")
public class LenAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(LenAiAgentApplication.class, args);
    }

}
