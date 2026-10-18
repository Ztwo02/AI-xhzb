package com.xhzb.nursing.controller;

import cn.hutool.core.util.StrUtil;
import com.xhzb.common.exception.base.BaseException;
import com.xhzb.common.utils.SecurityUtils;
import com.xhzb.nursing.service.ChatHistoryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @RequestMapping(value = "/chat",produces = "text/html;charset=UTF-8")
    public Flux<String> chat(String prompt,String chatId){

        if (StrUtil.isBlank(chatId) || "null".equals(chatId.trim())){
            throw new BaseException("请先新建对话");
        }

        chatHistoryService.saveHistory(SecurityUtils.getUserId().toString(), chatId);

        return chatClient.prompt()
                .user(prompt)
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatId))
                .stream()
                .content();
    }
}
