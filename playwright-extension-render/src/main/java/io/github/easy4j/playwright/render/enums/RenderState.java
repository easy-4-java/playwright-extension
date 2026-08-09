package io.github.easy4j.playwright.render.enums;

import lombok.Getter;

/**
 * 渲染状态
 */
@Getter
/**
 * Enumeration of rendering states (pending, in-progress, completed, failed).
 */
public enum RenderState {

    /**
     * 等待中
     */
    WAITING(0, "等待中"),
    /**
     * 生成中
     */
    GENERATING(1, "生成中"),
    /**
     * 生成成功
     */
    SUCCESS(2, "生成成功"),
    /**
     * 生成失败
     */
    FAIL(3, "生成失败")
    ;

    private int state ;
    private String desc ;

    RenderState(int state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static RenderState getRenderState(int state) {
        for (RenderState renderState : RenderState.values()) {
            if (renderState.getState() == state) {
                return renderState;
            }
        }
        return null;
    }

}
