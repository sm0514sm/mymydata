package com.sangminlee.mymydata.config;

import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "app.vectorstore.target", havingValue = "chroma")
@Import(ChromaVectorStoreAutoConfiguration.class)
public class ConditionalChromaConfiguration {
}