package com.aurora.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfigProperties {

    /**
     * AI服务提供商 (deepseek, gpt, claude等)
     */
    private String provider = "deepseek";

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API基础URL
     */
    private String baseUrl;

    /**
     * 默认模型
     */
    private String defaultModel;

    /**
     * 图片生成模型
     */
    private String imageModel;

    /**
     * 请求超时时间(秒)
     */
    private Integer timeout = 30;

    /**
     * 最大令牌数 (DeepSeek API限制: 1-8192)
     */
    private Integer maxTokens = 4096;

    /**
     * 温度参数
     */
    private Double temperature = 0.7;

    /**
     * 是否启用AI功能
     */
    private Boolean enabled = true;

    /**
     * DeepSeek配置
     */
    private DeepSeekConfig deepseek = new DeepSeekConfig();

    /**
     * GPT配置
     */
    private GptConfig gpt = new GptConfig();

    @Data
    public static class DeepSeekConfig {
        private String apiKey;
        private String baseUrl = "https://api.deepseek.com";
        
        // 基础模型
        private String chatModel = "deepseek-chat";
        private String codeModel = "deepseek-coder";
        
        // 推理模型 (R1系列)
        private String reasoningModel = "deepseek-reasoner";
        
        // V3系列模型
        private String v3Model = "deepseek-ai/DeepSeek-V3";
        
        // R1系列模型
        private String r1Model = "deepseek-ai/DeepSeek-R1";
        private String r1DistillModel = "deepseek-ai/DeepSeek-R1-Distill-Qwen-32B";
        private String r1DistillLlamaModel = "deepseek-ai/DeepSeek-R1-Distill-Llama-8B";
        
        // 当前使用的模型 (可动态切换)
        private String currentModel = "deepseek-chat";
        
        // 模型配置映射 (max_tokens限制在1-8192范围内)
        private ModelConfig chat = new ModelConfig("deepseek-chat", 4096, 0.7, false);
        private ModelConfig coder = new ModelConfig("deepseek-coder", 4096, 0.1, false);
        private ModelConfig v3 = new ModelConfig("deepseek-ai/DeepSeek-V3", 8192, 0.7, true);
        private ModelConfig r1 = new ModelConfig("deepseek-ai/DeepSeek-R1", 8192, 0.7, true);
        private ModelConfig r1Distill = new ModelConfig("deepseek-ai/DeepSeek-R1-Distill-Qwen-32B", 4096, 0.7, true);
        private ModelConfig r1DistillLlama = new ModelConfig("deepseek-ai/DeepSeek-R1-Distill-Llama-8B", 4096, 0.7, true);
    }
    
    @Data
    public static class ModelConfig {
        private String modelName;
        private Integer maxTokens;
        private Double temperature;
        private Boolean supportsDeepSearch = false; // 是否支持深度搜索
        
        public ModelConfig() {}
        
        public ModelConfig(String modelName, Integer maxTokens, Double temperature) {
            this.modelName = modelName;
            this.maxTokens = maxTokens;
            this.temperature = temperature;
            this.supportsDeepSearch = false;
        }
        
        public ModelConfig(String modelName, Integer maxTokens, Double temperature, Boolean supportsDeepSearch) {
            this.modelName = modelName;
            this.maxTokens = maxTokens;
            this.temperature = temperature;
            this.supportsDeepSearch = supportsDeepSearch;
        }
    }

    @Data
    public static class GptConfig {
        private String apiKey;
        private String baseUrl = "https://api.openai.com";
        private String chatModel = "gpt-3.5-turbo";
        private String imageModel = "dall-e-3";
    }
}
