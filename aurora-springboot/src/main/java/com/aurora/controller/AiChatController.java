package com.aurora.controller;

import com.aurora.annotation.OptLog;
import com.aurora.model.dto.AiChatRequestDTO;
import com.aurora.model.dto.AiChatResponseDTO;
import com.aurora.model.vo.*;
import com.aurora.service.AiChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.aurora.constant.OptTypeConstant.SAVE_OR_UPDATE;

@Api(tags = "AI聊天模块")
@RestController
@RequestMapping("/admin/ai")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @OptLog(optType = SAVE_OR_UPDATE)
    @ApiOperation("AI聊天对话")
    @PostMapping("/chat")
    public ResultVO<AiChatResponseDTO> chat(@Valid @RequestBody AiChatRequestDTO request) {
        return ResultVO.ok(aiChatService.chat(request));
    }

    @ApiOperation("生成文章")
    @PostMapping("/generate/article")
    public ResultVO<AiChatResponseDTO> generateArticle(@Valid @RequestBody AiGenerateArticleVO articleVO) {
        return ResultVO.ok(aiChatService.generateArticle(
                articleVO.getKeywords(), 
                articleVO.getLength(), 
                articleVO.getStyle(), 
                articleVO.getAudience()
        ));
    }

    @ApiOperation("生成图片")
    @PostMapping("/generate/image")
    public ResultVO<AiChatResponseDTO> generateImage(@Valid @RequestBody AiGenerateImageVO imageVO) {
        return ResultVO.ok(aiChatService.generateImage(imageVO.getPrompt()));
    }

    @ApiOperation("优化文章格式")
    @PostMapping("/optimize/format")
    public ResultVO<AiChatResponseDTO> optimizeFormat(@Valid @RequestBody AiOptimizeFormatVO formatVO) {
        return ResultVO.ok(aiChatService.optimizeFormat(formatVO.getContent()));
    }

    @ApiOperation("检查AI服务状态")
    @GetMapping("/status")
    public ResultVO<Boolean> checkStatus() {
        return ResultVO.ok(aiChatService.isAvailable());
    }

    @ApiOperation("获取所有可用模型列表（按提供商分组）")
    @GetMapping("/models/all")
    public ResultVO<java.util.Map<String, java.util.List<String>>> getAllAvailableModels() {
        return ResultVO.ok(aiChatService.getAllAvailableModels());
    }

    @ApiOperation("获取当前提供商的模型列表")
    @GetMapping("/models")
    public ResultVO<java.util.List<String>> getAvailableModels() {
        return ResultVO.ok(aiChatService.getAvailableModels());
    }

    @ApiOperation("切换AI模型（支持跨提供商）")
    @PostMapping("/switch-model")
    public ResultVO<Boolean> switchModel(@Valid @RequestBody AiModelSwitchVO switchVO) {
        return ResultVO.ok(aiChatService.switchModel(switchVO.getModelName()));
    }

    @ApiOperation("获取当前模型信息")
    @GetMapping("/current-model")
    public ResultVO<java.util.Map<String, Object>> getCurrentModelInfo() {
        return ResultVO.ok(aiChatService.getCurrentModelInfo());
    }

    @ApiOperation("测试API连接")
    @PostMapping("/test-connection")
    public ResultVO<AiChatResponseDTO> testConnection() {
        AiChatRequestDTO testRequest = AiChatRequestDTO.builder()
                .message("你好，请回复'连接测试成功'")
                .chatType("general")
                .build();
        return ResultVO.ok(aiChatService.chat(testRequest));
    }
}
