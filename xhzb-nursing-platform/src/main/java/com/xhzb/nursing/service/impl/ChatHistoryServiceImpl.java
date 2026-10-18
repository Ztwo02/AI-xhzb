package com.xhzb.nursing.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.xhzb.nursing.domain.vo.MessageVo;
import com.xhzb.nursing.service.ChatHistoryService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_TEMPLATE = "chat:history:%s";
    @Autowired
    private ChatMemory chatMemory;

    @Override
    public void saveHistory(String userId, String chatId) {
        String key = String.format(KEY_TEMPLATE, userId);
        redisTemplate.opsForSet().add(key, chatId);
    }

    @Override
    public List<String> findChatIdList(String userId) {
        String key = String.format(KEY_TEMPLATE, userId);
        Set<String> chatIds = redisTemplate.opsForSet().members(key);
        if (CollUtil.isEmpty(chatIds)) {
            return List.of();
        }
        List<String> list = chatIds.stream()
                .sorted(Comparator.comparing(String::toString).reversed())
                .toList();

        return list;
    }

    @Override
    public List<MessageVo> findChatHistoryMessage(String chatId) {
        List<Message> messages = chatMemory.get(chatId);
        if (CollUtil.isEmpty(messages)) {
            return List.of();
        }
        List<MessageVo> messageVoList = messages.stream()
                .map(MessageVo::new)
                .toList();
        return messageVoList;
    }

    @Override
    public void deleteHistory(String userId, String chatId) {
        String key = String.format(KEY_TEMPLATE, userId);
        redisTemplate.opsForSet().remove(key, chatId);

        chatMemory.clear(chatId);
    }
}
