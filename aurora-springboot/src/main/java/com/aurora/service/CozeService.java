package com.aurora.service;

import com.aurora.model.dto.CozeConfigDTO;
import com.aurora.model.dto.CozeChatRequestDTO;
import com.aurora.model.dto.CozeChatResponseDTO;

import java.util.List;

/**
 * 扣子智能体服务接口
 *
 * @author aurora
 * @since 2024-01-01
 */
public interface CozeService {

    /**
     * 获取扣子智能体配置
     *
     * @return 配置信息
     */
    CozeConfigDTO getCozeConfig();

    /**
     * 发送消息到扣子智能体
     *
     * @param request 聊天请求
     * @return 智能体回复
     */
    CozeChatResponseDTO sendMessage(CozeChatRequestDTO request);

    /**
     * 获取对话历史
     *
     * @param conversationId 对话ID
     * @return 对话历史
     */
    List<Object> getConversation(String conversationId);

    /**
     * 清除对话历史
     *
     * @param conversationId 对话ID
     */
    void clearConversation(String conversationId);
}
