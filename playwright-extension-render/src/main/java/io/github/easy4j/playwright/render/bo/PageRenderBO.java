package io.github.easy4j.playwright.render.bo;

import io.github.easy4j.playwright.render.enums.CheckState;
import io.github.easy4j.playwright.render.enums.RenderState;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * 截图缓存信息
 */
@Accessors(chain = true)
@Data
public class PageRenderBO {

    /**
     * 原始序号
     */
    private int index;
    /**
     * 唯一ID（学校代码 + 雪花算法序列值）
     */
    private String uniqueId;
    /**
     * 学校代码
     */
    private String schoolCode;
    /**
     * 年级代码
     */
    private String gradeCode;
    /**
     * 班级代码
     */
    private String classCode;
    /**
     * 学生ID
     */
    private String stuId;
    /**
     * 背景图地址
     */
    private String bgUrl;
    /**
     * 原始请求URL
     */
    private String url;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 是否保存到文件
     */
    private Boolean toFile;
    /**
     * 文件存储路径
     */
    private String path;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 截图缓存
     */
    private byte[] buffer;
    /**
     * 是否需要重新加载
     */
    private boolean needReload;
    /**
     * 当前是否是在重新加载
     */
    private boolean reload;
    /**
     * 重新超时时间（初次使用全局配置，重试会计算新的超时时间）
     */
    private Double reloadTimeout;
    /**
     * 当前页检查状态
     */
    private CheckState checkState;
    /**
     * 当前页检查失败原因
     */
    private String checkFailedReason;
    /**
     * 当前页渲染状态
     */
    private RenderState renderState;
    /**
     * 当前页渲染失败原因
     */
    private String renderFailedReason;
    /**
     * 当前页资源加载状态
     */
    private Map<String, Integer> resourceLoadState = new HashMap<>();
}

