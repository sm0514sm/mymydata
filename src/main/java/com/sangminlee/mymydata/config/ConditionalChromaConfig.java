package com.sangminlee.mymydata.config;

import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Chroma 벡터 스토어를 조건부로 활성화하기 위한 설정 클래스입니다. <br>
 * 이 클래스는 'app.vectorstore.target' 속성이 'chroma'로 설정된 경우에만 활성화됩니다.
 */
@Configuration
@ConditionalOnProperty(name = "app.vectorstore.target", havingValue = "chroma")
@Import(ChromaVectorStoreAutoConfiguration.class)
public class ConditionalChromaConfig {
}