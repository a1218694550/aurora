package com.aurora.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扣子智能体配置DTO
 *
 * @author aurora
 * @since 2024-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CozeConfigDTO {

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 智能体头像
     */
    private String avatar;

    /**
     * 智能体标题
     */
    private String title;

    /**
     * 主题
     */
    private String theme;

    /**
     * 是否可拖动
     */
    private Boolean draggable;

    /**
     * 位置配置
     */
    private PositionConfig position;

    /**
     * 尺寸配置
     */
    private SizeConfig size;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PositionConfig {
        private Integer x;
        private Integer y;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SizeConfig {
        private Integer width;
        private Integer height;
    }
}
