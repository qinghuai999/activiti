package com.activiti.demo.springbootactivitidemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude={org.activiti.spring.boot.SecurityAutoConfiguration.class})
//@EnableCaching
public class SpringbootActivitiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootActivitiDemoApplication.class, args);
    }

}
