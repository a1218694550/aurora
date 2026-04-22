package com.aurora.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aurora.model.dto.CozeConfigDTO;
import com.aurora.model.dto.CozeChatRequestDTO;
import com.aurora.model.dto.CozeChatResponseDTO;
import com.aurora.service.CozeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

/**
 * 扣子智能体服务实现类
 *
 * @author aurora
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CozeServiceImpl implements CozeService {

    @Resource
    private RestTemplate restTemplate;

    @Value("${ai.coze.enabled:true}")
    private Boolean cozeEnabled;

    @Value("${ai.coze.apiKey:}")
    private String apiKey;

    @Value("${ai.coze.baseUrl:https://www.coze.cn/api/v1}")
    private String baseUrl;

    @Value("${ai.coze.botId:}")
    private String botId;

    @Value("${ai.coze.timeout:30}")
    private Integer timeout;

    @Value("${ai.coze.maxTokens:4096}")
    private Integer maxTokens;

    @Value("${ai.coze.ui.position.x:50}")
    private Integer defaultX;

    @Value("${ai.coze.ui.position.y:50}")
    private Integer defaultY;

    @Value("${ai.coze.ui.size.width:300}")
    private Integer defaultWidth;

    @Value("${ai.coze.ui.size.height:400}")
    private Integer defaultHeight;

    @Value("${ai.coze.ui.avatar:/images/coze-avatar.svg}")
    private String avatar;

    @Value("${ai.coze.ui.title:智能助手}")
    private String title;

    @Value("${ai.coze.ui.theme:light}")
    private String theme;

    @Value("${ai.coze.ui.draggable:true}")
    private Boolean draggable;

    // 用于存储对话历史的内存缓存（生产环境建议使用Redis）
    private final Map<String, List<Object>> conversationCache = new HashMap<>();

    @Override
    public CozeConfigDTO getCozeConfig() {
        return CozeConfigDTO.builder()
                .enabled(cozeEnabled)
                .avatar(avatar)
                .title(title)
                .theme(theme)
                .draggable(draggable)
                .position(CozeConfigDTO.PositionConfig.builder()
                        .x(defaultX)
                        .y(defaultY)
                        .build())
                .size(CozeConfigDTO.SizeConfig.builder()
                        .width(defaultWidth)
                        .height(defaultHeight)
                        .build())
                .build();
    }

    @Override
    public CozeChatResponseDTO sendMessage(CozeChatRequestDTO request) {
        try {
            if (!cozeEnabled) {
                return CozeChatResponseDTO.builder()
                        .content("智能助手暂时不可用，请稍后再试。")
                        .conversationId(request.getConversationId())
                        .messageId(UUID.randomUUID().toString())
                        .timestamp(System.currentTimeMillis())
                        .success(false)
                        .errorMessage("Coze service is disabled")
                        .build();
            }

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("bot_id", botId);
            requestBody.put("user_id", request.getUserId() != null ? request.getUserId().toString() : "anonymous");
            requestBody.put("conversation_id", request.getConversationId());
            requestBody.put("additional_messages", new Object[]{new JSONObject()
                    .fluentPut("role", "user")
                    .fluentPut("content", request.getMessage())
                    .fluentPut("content_type", "text")
            });
            requestBody.put("stream", false);
            requestBody.put("auto_save_history", true);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求到扣子API
            String url = baseUrl + "/chat";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                // 解析响应（根据扣子API的实际响应格式调整）
                String content = extractContentFromResponse(responseBody);
                String messageId = UUID.randomUUID().toString();
                
                // 保存到对话历史
                saveToConversationHistory(request.getConversationId(), request.getMessage(), content);

                return CozeChatResponseDTO.builder()
                        .content(content)
                        .conversationId(request.getConversationId())
                        .messageId(messageId)
                        .timestamp(System.currentTimeMillis())
                        .success(true)
                        .build();
            } else {
                throw new RuntimeException("Failed to get response from Coze API");
            }

        } catch (Exception e) {
            log.error("Error sending message to Coze: ", e);
            
            // 返回默认回复
            return CozeChatResponseDTO.builder()
                    .content("抱歉，我现在无法回复，请稍后再试。")
                    .conversationId(request.getConversationId())
                    .messageId(UUID.randomUUID().toString())
                    .timestamp(System.currentTimeMillis())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public List<Object> getConversation(String conversationId) {
        return conversationCache.getOrDefault(conversationId, new ArrayList<>());
    }

    @Override
    public void clearConversation(String conversationId) {
        conversationCache.remove(conversationId);
    }

    /**
     * 从扣子API响应中提取内容
     * 
     * @param responseBody API响应体
     * @return 提取的内容
     */
    private String extractContentFromResponse(Map<String, Object> responseBody) {
        try {
            // 根据扣子API的实际响应格式调整这里的解析逻辑
            if (responseBody.containsKey("messages")) {
                List<Map<String, Object>> messages = (List<Map<String, Object>>) responseBody.get("messages");
                if (!messages.isEmpty()) {
                    Map<String, Object> lastMessage = messages.get(messages.size() - 1);
                    return (String) lastMessage.get("content");
                }
            }
            
            // 如果无法解析，返回默认回复
            return "我收到了你的消息，但暂时无法给出具体回复。";
        } catch (Exception e) {
            log.error("Error extracting content from Coze response: ", e);
            return "解析回复时出现错误，请稍后再试。";
        }
    }

    /**
     * 保存对话历史
     * 
     * @param conversationId 对话ID
     * @param userMessage 用户消息
     * @param agentReply 智能体回复
     */
    private void saveToConversationHistory(String conversationId, String userMessage, String agentReply) {
        List<Object> history = conversationCache.computeIfAbsent(conversationId, k -> new ArrayList<>());
        
        // 添加用户消息
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("type", "user");
        userMsg.put("content", userMessage);
        userMsg.put("timestamp", System.currentTimeMillis());
        history.add(userMsg);
        
        // 添加智能体回复
        Map<String, Object> agentMsg = new HashMap<>();
        agentMsg.put("type", "agent");
        agentMsg.put("content", agentReply);
        agentMsg.put("timestamp", System.currentTimeMillis());
        history.add(agentMsg);
        
        // 限制历史记录数量（可配置）
        if (history.size() > 100) {
            history.subList(0, history.size() - 100).clear();
        }
    }
}
