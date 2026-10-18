package com.xhzb.nursing.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * 构造方法：实现将SpringAI的Message转为我们的Msg功能
 * toMessage方法：实现将我们的Msg转为SpringAI的Message
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Msg {
    MessageType messageType;
    String text;
    Map<String,Object> metadata;
    private LocalDateTime createTime;


    public Msg(Message message){
        this.messageType = message.getMessageType();
        this.text = message.getText();
        this.metadata = message.getMetadata();
        createTime = LocalDateTime.now();
    }

    public Message toMessage(){
        return switch (messageType){
            case SYSTEM -> new SystemMessage(text);
            case USER -> UserMessage.builder().text(text).media(List.of()).metadata(metadata).build();
            case ASSISTANT -> AssistantMessage.builder().content(text).properties(metadata).media(List.of()).build();
            default -> throw new IllegalArgumentException("Unsupported message type" + messageType);
        };
    }
}
