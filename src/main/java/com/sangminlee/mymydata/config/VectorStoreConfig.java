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
