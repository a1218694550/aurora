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
public class AiGenerateArticleVO {

    /**
     * 文章关键词
     */
    @NotBlank(message = "关键词不能为空")
    private String keywords;

    /**
     * 文章长度
     */
    private String length;

    /**
     * 写作风格
     */
    private String style;

    /**
     * 目标读者
     */
    private String audience;
}
