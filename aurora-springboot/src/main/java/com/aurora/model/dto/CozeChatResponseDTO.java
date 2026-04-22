package com.aurora.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扣子智能体聊天响应DTO
 *
 * @author aurora
 * @since 2024-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CozeChatResponseDTO {

    /**
     * 智能体回复内容
     */
    private String content;

    /**
     * 对话ID
     */
    private String conversationId;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 回复时间戳
     */
    private Long timestamp;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
}
