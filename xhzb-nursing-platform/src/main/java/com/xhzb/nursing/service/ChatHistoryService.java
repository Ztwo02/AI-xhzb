package com.xhzb.nursing.service;

import com.xhzb.nursing.domain.vo.MessageVo;

import java.util.List;

public interface ChatHistoryService {

    void saveHistory(String userId,String chatId);

    List<String> findChatIdList(String userId);

    List<MessageVo> findChatHistoryMessage(String chatId);

    void deleteHistory(String userId,String chatId);
}
