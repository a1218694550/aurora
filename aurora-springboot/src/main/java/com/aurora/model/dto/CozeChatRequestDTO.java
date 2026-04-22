package com.aurora.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扣子智能体聊天请求DTO
 *
 * @author aurora
 * @since 2024-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CozeChatRequestDTO {

    /**
     * 用户消息
     */
    private String message;

    /**
     * 对话ID
     */
    private String conversationId;

    /**
     * 用户ID（可选）
     */
    private Integer userId;
}
