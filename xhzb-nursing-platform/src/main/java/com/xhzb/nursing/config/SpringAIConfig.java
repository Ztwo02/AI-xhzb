package com.xhzb.nursing.config;

import com.xhzb.nursing.constants.SystemConstants;
import com.xhzb.nursing.tool.NursingProjectTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class SpringAIConfig {
    /**
     *SpringAI基于AOP机制实现与大模型对话过程的增强、拦截、修改等功能。
     *所有增强通知都需要实现Advisor接口
     *
     * 一些基本实现的增强功能：
     *      SimpleLoggerAdvisor --> 实现日志记录的Advisor
     *      MessageChatMemoryAdvisor --> 实现会话记忆的Advisor
     *      QuestionAnswerAdvisor --> 实现RAG的Advisor
     */

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel, NursingProjectTool nursingProjectTool, ChatMemory chatMemory, VectorStore vectorStore) {
        return ChatClient
                .builder(openAiChatModel) //创建LLM-model
                .defaultTools(nursingProjectTool) //定义Tool Calling
                .defaultSystem(SystemConstants.prompt) //定义System prompt
                .defaultAdvisors(new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.7)
                                        .topK(3)
                                        .build())
                                .build()
                ) //定义Advisor Log、MessageChatMemory
                .build();
    }

    @Bean
    public VectorStore redisVectorStore(OpenAiEmbeddingModel openAiEmbeddingModel) {
        JedisPooled jedisPooled = new JedisPooled("192.168.111.128",6378);
        return RedisVectorStore.builder(jedisPooled, openAiEmbeddingModel)
                .indexName("spring-ai-index") //向量索引名
                .prefix("doc:") //向量索引名称
                .initializeSchema(true) //应用启动自动创建索引，首次启动true，生产关闭自动创建索引false
                .build();
    }
}
