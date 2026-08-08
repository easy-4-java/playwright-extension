package io.github.easy4j.playwright.render.vo;

import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.enums.RenderState;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * PDF 渲染结果
 */
@Data
@Accessors(chain = true)
public class WkhtmlRenderResultVO implements Serializable {

    /**
     * PDF渲染状态
     */
    private RenderState renderState;
    /**
     * PDF渲染失败原因
     */
    private String renderFailedReason;
    /**
     * 截图渲染结果列表，包含截图结果和截图失败原因
     */
    private List<PageRenderBO> pages;
    /**
     * PDF/Zip压缩文件唯一标识
     */
    private String fileId;
    /**
     * PDF/Zip压缩文件下载地址
     */
    private String fileUrl;
    /**
     * PDF/Zip压缩文件路径
     */
    private String filePath;
    /**
     * PDF/Zip压缩文件名称
     */
    private String fileName;
    /**
     * PDF/Zip/Image 文件字节数组
     */
    private byte[] fileBuffer;
    /**
     * PDF/Zip压缩文件大小(Kb)
     */
    private Long fileSize;

}
