package com.aurora.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponseDTO {

    /**
     * AI回复内容
     */
    private String content;

    /**
     * 生成的图片URL (仅图片生成时有值)
     */
    private String imageUrl;

    /**
     * 响应类型: text, image, article
     */
    private String type;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 消耗的token数
     */
    private Integer tokens;
}
