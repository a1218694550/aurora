package com.aurora.controller;

import com.aurora.model.dto.CozeConfigDTO;
import com.aurora.model.dto.CozeChatRequestDTO;
import com.aurora.model.dto.CozeChatResponseDTO;
import com.aurora.service.CozeService;
import com.aurora.model.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 扣子智能体控制器
 *
 * @author aurora
 * @since 2024-01-01
 */
@Api(tags = "扣子智能体模块")
@RestController
@RequestMapping("/coze")
public class CozeController {

    @Autowired
    private CozeService cozeService;

    /**
     * 获取扣子智能体配置
     *
     * @return 配置信息
     */
    @ApiOperation(value = "获取扣子智能体配置")
    @GetMapping("/config")
    public ResultVO<CozeConfigDTO> getCozeConfig() {
        return ResultVO.ok(cozeService.getCozeConfig());
    }

    /**
     * 发送消息到扣子智能体
     *
     * @param request 聊天请求
     * @return 智能体回复
     */
    @ApiOperation(value = "发送消息到扣子智能体")
    @PostMapping("/chat")
    public ResultVO<CozeChatResponseDTO> sendMessage(@RequestBody CozeChatRequestDTO request) {
        return ResultVO.ok(cozeService.sendMessage(request));
    }

    /**
     * 获取对话历史
     *
     * @param conversationId 对话ID
     * @return 对话历史
     */
    @ApiOperation(value = "获取对话历史")
    @GetMapping("/conversation/{conversationId}")
    public ResultVO<?> getConversation(@PathVariable String conversationId) {
        return ResultVO.ok(cozeService.getConversation(conversationId));
    }

    /**
     * 清除对话历史
     *
     * @param conversationId 对话ID
     * @return 操作结果
     */
    @ApiOperation(value = "清除对话历史")
    @DeleteMapping("/conversation/{conversationId}")
    public ResultVO<?> clearConversation(@PathVariable String conversationId) {
        cozeService.clearConversation(conversationId);
        return ResultVO.ok();
    }
}
