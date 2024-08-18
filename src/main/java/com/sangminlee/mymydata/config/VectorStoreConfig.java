package com.sangminlee.mymydata.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chroma.ChromaApi;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.ai.transformer.SummaryMetadataEnricher;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.ChromaVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

/**
 * 벡터 스토어 설정을 위한 구성 클래스입니다.
 * 이 클래스는 SimpleVectorStore와 ChromaVectorStore를 조건부로 설정합니다.
 * 두 벡터 스토어 구현(Simple, Chroma) 중 하나를 선택적으로 사용할 수 있도록 설계되어 있어,
 * 애플리케이션의 요구사항에 따라 유연하게 벡터 스토어를 선택할 수 있습니다.
 */
@Getter
@Setter
@Configuration
@RequiredArgsConstructor
public class VectorStoreConfig {

    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;

    @Value("${app.vectorstore.path:./simple-vectorstore.json}")
    private String vectorStorePath;
    @Value("${app.resources}")
    private List<String> pdfUrl;
    @Value("${spring.ai.vectorstore.chroma.collection-name}")
    private String chromaCollectionName;
    @Value("classpath:/prompt/default-summary-prompt.st")
    private Resource defaultSummaryPromptResource;

    /**
     * SimpleVectorStore 빈을 생성하고 구성합니다.
     * 'app.vectorstore.target' 속성이 'simple'일 때 활성화됩니다.
     * 생성한 vectorstore 파일은 프로젝트 최상단에 저장됩니다.
     *
     * @return 구성된 SimpleVectorStore 인스턴스
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @Bean
    @ConditionalOnProperty(name = "app.vectorstore.target", havingValue = "simple", matchIfMissing = true)
    SimpleVectorStore simpleVectorStore() throws IOException {
        var vectorStore = new SimpleVectorStore(embeddingModel);
        File vectorStoreFile = new File(vectorStorePath);
        if (vectorStoreFile.exists()) {
            vectorStore.load(vectorStoreFile);
        } else {
            vectorStore.add(processPdfDocuments());
            vectorStore.save(vectorStoreFile);
        }
        return vectorStore;
    }

    /**
     * ChromaVectorStore 빈을 생성하고 구성합니다.
     * 'app.vectorstore.target' 속성이 'chroma'일 때 활성화됩니다.
     *
     * @param chromaApi ChromaApi 인스턴스
     * @return 구성된 ChromaVectorStore 인스턴스
     * @throws Exception 벡터 스토어 처리 중 오류 발생 시
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.vectorstore.target", havingValue = "chroma")
    ChromaVectorStore chromaVectorStore(ChromaApi chromaApi) throws Exception {
        ChromaVectorStore vectorStore;
        if (chromaApi.getCollection(chromaCollectionName) == null) {
            vectorStore = new ChromaVectorStore(embeddingModel, chromaApi, true);
            List<Document> documents = processPdfDocuments();
            vectorStore.afterPropertiesSet();
            vectorStore.add(documents);
        } else {
            vectorStore = new ChromaVectorStore(embeddingModel, chromaApi, true);
        }
        return vectorStore;
    }

    /**
     * PDF 문서를 처리하여 Document 리스트를 생성합니다.
     *
     * @return 처리된 Document 리스트
     * @throws IOException 문서 처리 중 오류 발생 시
     */
    private List<Document> processPdfDocuments() throws IOException {
        String defaultSummaryPrompt = defaultSummaryPromptResource.getContentAsString(Charset.defaultCharset());

        PdfDocumentReaderConfig pdfReaderConfig = PdfDocumentReaderConfig.builder()
                .withPageTopMargin(2)
                .withPageExtractedTextFormatter(ExtractedTextFormatter.builder().withLeftAlignment(true).build())
                .withPagesPerDocument(1)
                .build();
        TextSplitter textSplitter = new TokenTextSplitter(300, 250, 5, 10000, true);
        var summaryMetadataEnricher = new SummaryMetadataEnricher(chatModel,
                List.of(SummaryMetadataEnricher.SummaryType.CURRENT), defaultSummaryPrompt, MetadataMode.ALL);
        var keywordMetadataEnricher = new KeywordMetadataEnricher(chatModel, 10);

        return pdfUrl.stream()
                .map(v -> new PagePdfDocumentReader(v, pdfReaderConfig))
                .map(PagePdfDocumentReader::get)
                .map(textSplitter)
                .map(summaryMetadataEnricher)
                .map(keywordMetadataEnricher)
                .flatMap(Collection::stream)
                .toList();
    }

}
