package com.aurora.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequestDTO {

    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000字符")
    private String message;

    /**
     * 聊天类型: article_generate(文章生成), image_generate(图片生成), format_optimize(格式优化)
     */
    @NotBlank(message = "聊天类型不能为空")
    private String chatType;

    /**
     * 文章主题/关键词
     */
    private String keywords;

    /**
     * 文章长度要求
     */
    private String length;

    /**
     * 文章风格
     */
    private String style;

    /**
     * 目标读者
     */
    private String audience;

    /**
     * 需要优化的文章内容
     */
    private String articleContent;

    /**
     * 是否启用深度搜索
     */
    @Builder.Default
    private Boolean enableDeepSearch = false;
}
