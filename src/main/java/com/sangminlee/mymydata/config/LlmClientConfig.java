package com.sangminlee.mymydata.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.Charset;

@Configuration
public class LlmClientConfig {

    @Value("classpath:/prompt/default-system-prompt.st")
    private Resource defaultSystemPrompt;

    @Value("classpath:/prompt/user-text-advise.st")
    private Resource userTextAdviseResource;

    @Bean
    ChatClient chatClient(ChatModel chatModel, VectorStore vectorStore) throws IOException {
        String userTextAdvise = userTextAdviseResource.getContentAsString(Charset.defaultCharset());
        SearchRequest searchRequest = SearchRequest.defaults().withTopK(5).withSimilarityThreshold(0.01);

        return ChatClient.builder(chatModel)
                .defaultSystem(defaultSystemPrompt)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
                        new QuestionAnswerAdvisor(vectorStore, searchRequest, userTextAdvise),
                        new SimpleLoggerAdvisor()
                )
                .build();
    }
}
