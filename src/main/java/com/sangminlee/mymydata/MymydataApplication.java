package com.sangminlee.mymydata;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@Push
@SpringBootApplication(exclude = {ChromaVectorStoreAutoConfiguration.class})
public class MymydataApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(MymydataApplication.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

}
