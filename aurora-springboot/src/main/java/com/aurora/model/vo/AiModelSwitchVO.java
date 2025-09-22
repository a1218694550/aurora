package com.aurora.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelSwitchVO {

    /**
     * 要切换的模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String modelName;
}
