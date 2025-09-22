package com.aurora.service.impl;

import com.aurora.config.properties.AiConfigProperties;
import com.aurora.model.dto.AiChatRequestDTO;
import com.aurora.model.dto.AiChatResponseDTO;
import com.aurora.service.AiChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    @Autowired
    private AiConfigProperties aiConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public AiChatResponseDTO chat(AiChatRequestDTO request) {
        if (!aiConfig.getEnabled()) {
            return AiChatResponseDTO.builder()
                    .success(false)
                    .error("AI功能未启用")
                    .build();
        }

        try {
            switch (request.getChatType()) {
                case "article_generate":
                    return generateArticle(request.getKeywords(), request.getLength(), 
                                         request.getStyle(), request.getAudience());
                case "image_generate":
                    return generateImage(request.getMessage());
                case "format_optimize":
                    return optimizeFormat(request.getArticleContent());
                default:
                    return chatWithAi(request.getMessage(), request.getEnableDeepSearch());
            }
        } catch (Exception e) {
            log.error("AI聊天服务异常", e);
            return AiChatResponseDTO.builder()
                    .success(false)
                    .error("AI服务暂时不可用: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public AiChatResponseDTO generateArticle(String keywords, String length, String style, String audience) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下要求生成一篇文章：\n");
        prompt.append("主题关键词：").append(keywords).append("\n");
        
        if (StringUtils.hasText(length)) {
            prompt.append("文章长度：").append(length).append("\n");
        }
        if (StringUtils.hasText(style)) {
            prompt.append("写作风格：").append(style).append("\n");
        }
        if (StringUtils.hasText(audience)) {
            prompt.append("目标读者：").append(audience).append("\n");
        }
        
        prompt.append("\n要求：\n");
        prompt.append("1. 文章结构清晰，包含标题、引言、正文、结论\n");
        prompt.append("2. 内容原创且有价值\n");
        prompt.append("3. 使用Markdown格式\n");
        prompt.append("4. 适当添加小标题和段落分隔\n");
        prompt.append("5. 语言流畅，逻辑清晰\n");

        return chatWithAi(prompt.toString());
    }

    @Override
    public AiChatResponseDTO generateImage(String prompt) {
        if ("gpt".equals(aiConfig.getProvider()) && StringUtils.hasText(aiConfig.getGpt().getImageModel())) {
            return generateImageWithGpt(prompt);
        } else {
            return AiChatResponseDTO.builder()
                    .success(false)
                    .error("当前AI提供商不支持图片生成功能")
                    .type("image")
                    .build();
        }
    }

    @Override
    public AiChatResponseDTO optimizeFormat(String content) {
        String prompt = "请优化以下文章的格式和排版，要求：\n" +
                "1. 统一使用Markdown格式\n" +
                "2. 合理划分段落和章节\n" +
                "3. 添加适当的标题层级\n" +
                "4. 优化文字排版，提高可读性\n" +
                "5. 保持原文内容不变，只优化格式\n\n" +
                "原文内容：\n" + content;

        return chatWithAi(prompt);
    }

    @Override
    public boolean isAvailable() {
        return aiConfig.getEnabled() && StringUtils.hasText(getApiKey());
    }
    
    /**
     * 验证API密钥格式
     */
    private boolean validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        
        // DeepSeek API密钥格式验证
        if (apiKey.startsWith("sk-") && apiKey.length() > 20) {
            return true;
        }
        
        log.warn("API密钥格式可能不正确: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "***");
        return false;
    }
    
    /**
     * 验证并限制max_tokens参数
     * DeepSeek API的max_tokens有效范围是[1, 8192]
     */
    private int validateMaxTokens(Integer maxTokens) {
        if (maxTokens == null) {
            log.warn("max_tokens为null，使用默认值2048");
            return 2048;
        }
        
        if (maxTokens < 1) {
            log.warn("max_tokens值{}小于最小值1，调整为1", maxTokens);
            return 1;
        }
        
        if (maxTokens > 8192) {
            log.warn("max_tokens值{}超过最大值8192，调整为8192", maxTokens);
            return 8192;
        }
        
        return maxTokens;
    }

    private AiChatResponseDTO chatWithAi(String message) {
        return chatWithAi(message, false);
    }
    
    private AiChatResponseDTO chatWithAi(String message, Boolean enableDeepSearch) {
        String provider = aiConfig.getProvider().toLowerCase();
        
        switch (provider) {
            case "deepseek":
                return chatWithDeepSeek(message, null, enableDeepSearch);
            case "gpt":
                return chatWithGpt(message);
            default:
                return AiChatResponseDTO.builder()
                        .success(false)
                        .error("不支持的AI提供商: " + provider)
                        .build();
        }
    }

    
    private AiChatResponseDTO chatWithDeepSeek(String message, String specificModel) {
        return chatWithDeepSeek(message, specificModel, false);
    }
    
    private AiChatResponseDTO chatWithDeepSeek(String message, String specificModel, Boolean enableDeepSearch) {
        try {
            String url = aiConfig.getDeepseek().getBaseUrl() + "/v1/chat/completions";
            
            // 获取模型配置
            String modelName = specificModel != null ? specificModel : aiConfig.getDeepseek().getCurrentModel();
            AiConfigProperties.ModelConfig modelConfig = getDeepSeekModelConfig(modelName);
            
            // 检查API密钥
            String apiKey = aiConfig.getDeepseek().getApiKey();
            if (!validateApiKey(apiKey)) {
                log.error("DeepSeek API密钥未配置或格式不正确");
                return AiChatResponseDTO.builder()
                        .success(false)
                        .error("DeepSeek API密钥未配置或格式不正确，请检查配置文件中的API密钥")
                        .build();
            }
            
            // 验证和限制max_tokens参数
            int maxTokens = validateMaxTokens(modelConfig.getMaxTokens());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelConfig.getModelName());
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", modelConfig.getTemperature());
            
            // 如果支持深度搜索且用户启用了深度搜索
            if (enableDeepSearch != null && enableDeepSearch && modelConfig.getSupportsDeepSearch()) {
                requestBody.put("search", true);
                log.info("已启用深度搜索功能");
            }
            
            log.info("DeepSeek API请求 - URL: {}, Model: {}, MaxTokens: {}, DeepSearch: {}, ApiKey: {}***", 
                    url, modelConfig.getModelName(), maxTokens, enableDeepSearch, apiKey.substring(0, Math.min(10, apiKey.length())));
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);
            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("User-Agent", "Aurora-Blog/1.0");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String content = jsonNode.path("choices").get(0).path("message").path("content").asText();
                int tokens = jsonNode.path("usage").path("total_tokens").asInt();
                
                log.info("DeepSeek API调用成功 - 消耗tokens: {}", tokens);
                
                return AiChatResponseDTO.builder()
                        .success(true)
                        .content(content)
                        .type("text")
                        .model(modelConfig.getModelName())
                        .tokens(tokens)
                        .build();
            } else {
                log.error("DeepSeek API请求失败 - 状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
                return AiChatResponseDTO.builder()
                        .success(false)
                        .error("API请求失败: " + response.getStatusCode())
                        .build();
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("DeepSeek API HTTP错误 - 状态码: {}, 错误信息: {}", e.getStatusCode(), e.getResponseBodyAsString());
            
            String errorMessage;
            if (e.getStatusCode().value() == 401) {
                errorMessage = "API密钥无效或已过期，请检查DeepSeek API密钥配置";
            } else if (e.getStatusCode().value() == 429) {
                errorMessage = "API调用频率超限，请稍后重试";
            } else if (e.getStatusCode().value() == 400) {
                errorMessage = "请求参数错误: " + e.getResponseBodyAsString();
            } else {
                errorMessage = "DeepSeek API错误 (HTTP " + e.getStatusCode().value() + "): " + e.getResponseBodyAsString();
            }
            
            return AiChatResponseDTO.builder()
                    .success(false)
                    .error(errorMessage)
                    .build();
        } catch (Exception e) {
            log.error("DeepSeek API调用异常", e);
            return AiChatResponseDTO.builder()
                    .success(false)
                    .error("DeepSeek服务异常: " + e.getMessage())
                    .build();
        }
    }

    private AiChatResponseDTO chatWithGpt(String message) {
        try {
            String url = aiConfig.getGpt().getBaseUrl() + "/v1/chat/completions";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiConfig.getGpt().getChatModel());
            requestBody.put("max_tokens", aiConfig.getMaxTokens());
            requestBody.put("temperature", aiConfig.getTemperature());
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);
            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiConfig.getGpt().getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String content = jsonNode.path("choices").get(0).path("message").path("content").asText();
                int tokens = jsonNode.path("usage").path("total_tokens").asInt();
                
                return AiChatResponseDTO.builder()
                        .success(true)
                        .content(content)
                        .type("text")
                        .model(aiConfig.getGpt().getChatModel())
                        .tokens(tokens)
                        .build();
            } else {
                return AiChatResponseDTO.builder()
                        .success(false)
                        .error("API请求失败: " + response.getStatusCode())
                        .build();
            }
        } catch (Exception e) {
            log.error("GPT API调用异常", e);
            return AiChatResponseDTO.builder()
                    .success(false)
                    .error("GPT服务异常: " + e.getMessage())
                    .build();
        }
    }

    private AiChatResponseDTO generateImageWithGpt(String prompt) {
        try {
            String url = aiConfig.getGpt().getBaseUrl() + "/v1/images/generations";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiConfig.getGpt().getImageModel());
            requestBody.put("prompt", prompt);
            requestBody.put("n", 1);
            requestBody.put("size", "1024x1024");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiConfig.getGpt().getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String imageUrl = jsonNode.path("data").get(0).path("url").asText();
                
                return AiChatResponseDTO.builder()
                        .success(true)
                        .imageUrl(imageUrl)
                        .type("image")
                        .model(aiConfig.getGpt().getImageModel())
                        .build();
            } else {
                return AiChatResponseDTO.builder()
                        .success(false)
                        .error("图片生成失败: " + response.getStatusCode())
                        .type("image")
                        .build();
            }
        } catch (Exception e) {
            log.error("GPT图片生成异常", e);
            return AiChatResponseDTO.builder()
                    .success(false)
                    .error("图片生成服务异常: " + e.getMessage())
                    .type("image")
                    .build();
        }
    }

    private String getApiKey() {
        String provider = aiConfig.getProvider().toLowerCase();
        switch (provider) {
            case "deepseek":
                return aiConfig.getDeepseek().getApiKey();
            case "gpt":
                return aiConfig.getGpt().getApiKey();
            default:
                return aiConfig.getApiKey();
        }
    }
    
    /**
     * 获取DeepSeek模型配置
     */
    private AiConfigProperties.ModelConfig getDeepSeekModelConfig(String modelName) {
        AiConfigProperties.DeepSeekConfig deepseek = aiConfig.getDeepseek();
        
        // 根据模型名称返回对应配置
        switch (modelName) {
            case "deepseek-chat":
                return deepseek.getChat();
            case "deepseek-coder":
                return deepseek.getCoder();
            case "deepseek-ai/DeepSeek-V3":
                return deepseek.getV3();
            case "deepseek-ai/DeepSeek-R1":
                return deepseek.getR1();
            case "deepseek-ai/DeepSeek-R1-Distill-Qwen-32B":
                return deepseek.getR1Distill();
            case "deepseek-ai/DeepSeek-R1-Distill-Llama-8B":
                return deepseek.getR1DistillLlama();
            default:
                // 如果找不到配置，使用默认chat配置但替换模型名称
                AiConfigProperties.ModelConfig defaultConfig = new AiConfigProperties.ModelConfig();
                defaultConfig.setModelName(modelName);
                defaultConfig.setMaxTokens(aiConfig.getMaxTokens());
                defaultConfig.setTemperature(aiConfig.getTemperature());
                return defaultConfig;
        }
    }

    @Override
    public Map<String, List<String>> getAllAvailableModels() {
        Map<String, List<String>> allModels = new HashMap<>();
        
        // DeepSeek模型
        List<String> deepseekModels = new ArrayList<>();
        deepseekModels.add("deepseek-chat");
        deepseekModels.add("deepseek-coder");
        deepseekModels.add("deepseek-ai/DeepSeek-V3");
        deepseekModels.add("deepseek-ai/DeepSeek-R1");
        deepseekModels.add("deepseek-ai/DeepSeek-R1-Distill-Qwen-32B");
        deepseekModels.add("deepseek-ai/DeepSeek-R1-Distill-Llama-8B");
        allModels.put("deepseek", deepseekModels);
        
        // GPT模型
        List<String> gptModels = new ArrayList<>();
        gptModels.add("gpt-3.5-turbo");
        gptModels.add("gpt-4");
        gptModels.add("gpt-4-turbo");
        gptModels.add("gpt-4o");
        gptModels.add("gpt-4o-mini");
        allModels.put("gpt", gptModels);
        
        return allModels;
    }

    @Override
    public List<String> getAvailableModels() {
        List<String> models = new ArrayList<>();
        if ("deepseek".equals(aiConfig.getProvider().toLowerCase())) {
            models.add("deepseek-chat");
            models.add("deepseek-coder");
            models.add("deepseek-ai/DeepSeek-V3");
            models.add("deepseek-ai/DeepSeek-R1");
            models.add("deepseek-ai/DeepSeek-R1-Distill-Qwen-32B");
            models.add("deepseek-ai/DeepSeek-R1-Distill-Llama-8B");
        } else if ("gpt".equals(aiConfig.getProvider().toLowerCase())) {
            models.add("gpt-3.5-turbo");
            models.add("gpt-4");
            models.add("gpt-4-turbo");
            models.add("gpt-4o");
            models.add("gpt-4o-mini");
        }
        return models;
    }

    @Override
    public boolean switchModel(String modelName) {
        try {
            // 判断模型属于哪个提供商
            String targetProvider = determineProvider(modelName);
            if (targetProvider == null) {
                log.error("未知的模型: {}", modelName);
                return false;
            }
            
            // 如果需要切换提供商
            if (!targetProvider.equals(aiConfig.getProvider().toLowerCase())) {
                log.info("切换AI提供商从 {} 到 {}", aiConfig.getProvider(), targetProvider);
                aiConfig.setProvider(targetProvider);
            }
            
            // 设置对应提供商的模型
            if ("deepseek".equals(targetProvider)) {
                aiConfig.getDeepseek().setCurrentModel(modelName);
                log.info("已切换到DeepSeek模型: {}", modelName);
            } else if ("gpt".equals(targetProvider)) {
                aiConfig.getGpt().setChatModel(modelName);
                log.info("已切换到GPT模型: {}", modelName);
            }
            
            return true;
        } catch (Exception e) {
            log.error("切换模型失败", e);
            return false;
        }
    }
    
    /**
     * 根据模型名称判断属于哪个提供商
     */
    private String determineProvider(String modelName) {
        // DeepSeek模型
        if (modelName.startsWith("deepseek") || modelName.contains("DeepSeek")) {
            return "deepseek";
        }
        // GPT模型
        if (modelName.startsWith("gpt-")) {
            return "gpt";
        }
        return null;
    }

    @Override
    public Map<String, Object> getCurrentModelInfo() {
        Map<String, Object> info = new HashMap<>();
        String currentProvider = aiConfig.getProvider().toLowerCase();
        info.put("provider", currentProvider);
        
        if ("deepseek".equals(currentProvider)) {
            String currentModel = aiConfig.getDeepseek().getCurrentModel();
            info.put("model", currentModel);
            info.put("config", getDeepSeekModelConfig(currentModel));
        } else if ("gpt".equals(currentProvider)) {
            String currentModel = aiConfig.getGpt().getChatModel();
            info.put("model", currentModel);
            // GPT模型配置信息
            Map<String, Object> gptConfig = new HashMap<>();
            gptConfig.put("modelName", currentModel);
            gptConfig.put("maxTokens", aiConfig.getMaxTokens());
            gptConfig.put("temperature", aiConfig.getTemperature());
            info.put("config", gptConfig);
        }
        
        return info;
    }
}
