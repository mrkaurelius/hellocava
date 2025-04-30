package dev.kumru.javaweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan("dev.kumru")
@EntityScan(basePackages = "dev.kumru")
@ConfigurationPropertiesScan("dev.kumru")
@ComponentScan
public class JavaWebApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(JavaWebApplication.class, args);

        LoggingSystem loggingSystem = context.getBean(LoggingSystem.class);
        System.out.println("Active Logging System: " + loggingSystem.getClass().getName());
    }

}
