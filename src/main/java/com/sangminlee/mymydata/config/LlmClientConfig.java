package com.sangminlee.mymydata.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * LLM(Large Language Model) 클라이언트 설정을 위한 구성 클래스입니다. <br>
 * 이 클래스는 ChatClient 빈을 생성하고 관련된 설정을 수행합니다.
 */
@Configuration
public class LlmClientConfig {

    /**
     * 기본 시스템 프롬프트 리소스를 주입받습니다.
     */
    @Value("classpath:/prompt/default-system-prompt.st")
    private Resource defaultSystemPrompt;

    /**
     * QuestionAnswerAdvisor에 활용되는 시스템 프롬프트 리소스를 주입받습니다.
     */
    @Value("classpath:/prompt/user-text-advise.st")
    private Resource userTextAdviseResource;

    /**
     * 설정에 따른 ChatClient 빈을 생성하고 구성합니다.
     *
     * @param chatModel   ChatModel 인스턴스
     * @param vectorStore VectorStore 인스턴스
     * @param chatMemory  ChatMemory 인스턴스
     * @return 구성된 ChatClient 인스턴스
     * @throws IOException 리소스 읽기 실패 시 발생
     */
    @Bean
    ChatClient chatClient(ChatModel chatModel, VectorStore vectorStore, ChatMemory chatMemory) throws IOException {
        String userTextAdvise = userTextAdviseResource.getContentAsString(Charset.defaultCharset());
        SearchRequest searchRequest = SearchRequest.defaults().withTopK(5).withSimilarityThreshold(0.01);

        return ChatClient.builder(chatModel)
                .defaultSystem(defaultSystemPrompt)
                .defaultAdvisors(
                        // 채팅 기억 advisor를 추가합니다.
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 벡터스토어를 사용하여 관련 컨텍스트를 질문에 활용하도록 합니다.
                        new QuestionAnswerAdvisor(vectorStore, searchRequest, userTextAdvise),
                        // 가장 간단한 로깅 advisor 추가합니다.
                        new SimpleLoggerAdvisor()
                )
                .build();
    }
}
