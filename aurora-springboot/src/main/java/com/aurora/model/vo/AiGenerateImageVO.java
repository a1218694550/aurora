package com.aurora.model.vo;

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
public class AiGenerateImageVO {

    /**
     * 图片描述提示词
     */
    @NotBlank(message = "图片描述不能为空")
    @Size(max = 1000, message = "图片描述不能超过1000字符")
    private String prompt;
}
