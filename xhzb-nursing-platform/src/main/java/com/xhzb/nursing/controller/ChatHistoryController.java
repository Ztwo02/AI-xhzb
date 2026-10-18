package com.xhzb.nursing.controller;

import com.xhzb.common.core.domain.AjaxResult;
import com.xhzb.common.utils.SecurityUtils;
import com.xhzb.nursing.domain.vo.MessageVo;
import com.xhzb.nursing.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.xhzb.common.core.domain.AjaxResult.success;

@RestController
@RequestMapping("/ai/history")
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @GetMapping
    public AjaxResult findChatIdList(){
        List<String> chatIdList = chatHistoryService.findChatIdList(SecurityUtils.getUserId().toString());
        return success(chatIdList);
    }

    @GetMapping("/{chatId}")
    public AjaxResult getChatIdList(@PathVariable String chatId){
        List<MessageVo> messageVos = chatHistoryService.findChatHistoryMessage(chatId);
        return success(messageVos);
    }

    @DeleteMapping("/{chatId}")
    public AjaxResult deleteChatHistory(@PathVariable String chatId){
        chatHistoryService.deleteHistory(chatId,SecurityUtils.getUserId().toString());
        return success();
    }
}
