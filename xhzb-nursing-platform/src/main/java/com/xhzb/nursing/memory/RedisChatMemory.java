package com.xhzb.nursing.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.xhzb.nursing.domain.vo.Msg;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Comparator;
import java.util.List;

@Component
public class RedisChatMemory implements ChatMemory {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_TEMPLATE = "chat:memory:%s";

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (ObjectUtils.isEmpty(messages)) {
            return;
        }
        /**
         * 处理SpringAI的每条消息，先转为MsgVo再转为JSON字符串
         */
//        List<String> msgList = new ArrayList<>();
//        for (Message message : messages) {
//            MsgVo msg = new MsgVo(message);
//            String str = JSONUtil.toJsonStr(msg);
//            msgList.add(str);
//        }
        List<String> msgList = messages.stream()
                .map(Msg::new)
                .map(str -> JSONUtil.toJsonStr(str))
                .toList();

        //存储消息到Redis的List类型中
        String ket = String.format(KEY_TEMPLATE,conversationId);
        redisTemplate.opsForList().leftPushAll(ket,msgList);
    }

    @Override
    public List<Message> get(String conversationId) {
        String key = String.format(KEY_TEMPLATE,conversationId);
        List<String> msgList = redisTemplate.opsForList().range(key, 0, -1);
        if (CollUtil.isEmpty(msgList)) {
            return List.of();
        }

//        List<MsgVo> msgVos = new ArrayList<>();
//        for (String msg : msgList) {
//            MsgVo msgVo = JSONUtil.toBean(msg,MsgVo.class);
//            msgVos.add(msgVo);
//        }
//        msgVos.sort(Comparator.comparing(MsgVo::getCreateTime));
//        List<Message> messages = new ArrayList<>();
//        for (MsgVo msgVo : msgVos) {
//            Message message = msgVo.toMessage();
//            messages.add(message);
//        }
        List<Message> messageList = msgList.stream()
                .map(msg -> JSONUtil.toBean(msg, Msg.class))
                .sorted(Comparator.comparing(Msg::getCreateTime))
                .map(Msg::toMessage)
                .toList();

        return messageList;
    }

    @Override
    public void clear(String conversationId) {
        String key = String.format(KEY_TEMPLATE,conversationId);
        redisTemplate.delete(key);
    }
}
