package io.github.easy4j.playwright.render.enums;

import lombok.Getter;

/**
 * 检查状态
 */
@Getter
/**
 * Enumeration of element check states for form interactions.
 */
public enum CheckState {

    /**
     * 检查通过
     */
    SUCCESS(1, "检查通过"),
    /**
     * 前端检查不通过
     */
     WEB_CHECK_FAIL(2, "前端检查不通过"),
    /**
     * 后端检查不通过
     */
    IMG_CHECK_FAIL(3, "图片检查不通过"),
    ;

    private int state ;
    private String desc ;

    CheckState(int state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static CheckState getRenderState(int state) {
        for (CheckState renderState : CheckState.values()) {
            if (renderState.getState() == state) {
                return renderState;
            }
        }
        return null;
    }

}
