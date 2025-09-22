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
public class AiOptimizeFormatVO {

    /**
     * 要优化的文章内容
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;
}
