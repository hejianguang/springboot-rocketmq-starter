package com.beescm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by Administrator on 2017/5/19.
 */
@EnableAsync               //（异步回调）让@Async注解能够生效,不能加在静态方法上
@EnableScheduling
@ServletComponentScan
@SpringBootApplication
public class BootApplication {
    public static void main(String[] args){
        SpringApplication.run(BootApplication.class, args);
    }
}
