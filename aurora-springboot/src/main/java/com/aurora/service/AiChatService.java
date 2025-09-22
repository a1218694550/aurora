package com.aurora.service;

import com.aurora.model.dto.AiChatRequestDTO;
import com.aurora.model.dto.AiChatResponseDTO;

public interface AiChatService {

    /**
     * AI聊天对话
     * @param request 聊天请求
     * @return 聊天响应
     */
    AiChatResponseDTO chat(AiChatRequestDTO request);

    /**
     * 生成文章
     * @param keywords 关键词
     * @param length 长度要求
     * @param style 写作风格
     * @param audience 目标读者
     * @return 生成的文章内容
     */
    AiChatResponseDTO generateArticle(String keywords, String length, String style, String audience);

    /**
     * 生成图片
     * @param prompt 图片描述
     * @return 图片URL
     */
    AiChatResponseDTO generateImage(String prompt);

    /**
     * 优化文章格式
     * @param content 原文内容
     * @return 优化后的内容
     */
    AiChatResponseDTO optimizeFormat(String content);

    /**
     * 检查AI服务是否可用
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 获取可用的AI模型列表（包含所有提供商）
     * @return 模型列表，包含提供商信息
     */
    java.util.Map<String, java.util.List<String>> getAllAvailableModels();

    /**
     * 获取当前提供商的模型列表
     * @return 模型列表
     */
    java.util.List<String> getAvailableModels();

    /**
     * 切换AI模型（支持跨提供商切换）
     * @param modelName 模型名称
     * @return 切换结果
     */
    boolean switchModel(String modelName);

    /**
     * 获取当前使用的模型信息
     * @return 当前模型信息
     */
    java.util.Map<String, Object> getCurrentModelInfo();
}
