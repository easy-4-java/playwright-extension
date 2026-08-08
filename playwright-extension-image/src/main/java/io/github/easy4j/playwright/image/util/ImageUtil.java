/*
 * Copyright (c) 2018, Loong Wan (https://github.com/loong10k).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.easy4j.playwright.image.util;

import io.github.easy4j.playwright.image.enums.ImagePageSize;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Coordinate;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.resizers.configurations.AlphaInterpolation;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import net.coobird.thumbnailator.resizers.configurations.Dithering;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Image utilities: blank detection, scaling, watermarking, pixel comparison.
 *
 * <p>Extracted from ddd4j-cloud-cmpt-playwright's {@code ImageUtil}. The
 * original class depended on {@code org.apache.pdfbox.pdmodel.common.PDRectangle}
 * via the {@code PDPageSize} enum; that coupling is broken here by:
 * <ul>
 *   <li>Replacing {@code PDPageSize} with {@link ImagePageSize} (a self-contained
 *       enum that mirrors standard page dimensions in points)</li>
 *   <li>Removing the {@code main()} test method</li>
 *   <li>Removing the {@code watermarkLongImage} helper (project-specific helper
 *       that does not belong in a generic image library)</li>
 * </ul>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Slf4j
public final class ImageUtil {

    public static final String IMAGE_TYPE = "png";

    /** Pre-rendered blank page at A3 size (842 × 1191). Lazily initialised. */
    public static final BufferedImage WHITE_A3 = getWhiteImage(
            Float.valueOf(ImagePageSize.A3.getWidth()).intValue(),
            Float.valueOf(ImagePageSize.A3.getHeight()).intValue());

    /** Pre-rendered blank page at A4 size (595 × 842). Lazily initialised. */
    public static final BufferedImage WHITE_A4 = getWhiteImage(
            Float.valueOf(ImagePageSize.A4.getWidth()).intValue(),
            Float.valueOf(ImagePageSize.A4.getHeight()).intValue());

    /** Approximate size of the WHITE_A3 image encoded as PNG. */
    public static final long WHITE_A3_SIZE = getImageFileSize(WHITE_A3, IMAGE_TYPE);
    /** Approximate size of the WHITE_A4 image encoded as PNG. */
    public static final long WHITE_A4_SIZE = getImageFileSize(WHITE_A4, IMAGE_TYPE);

    private ImageUtil() {
        // utility class
    }

    /**
     * Encode an image to bytes and return its size.
     *
     * @param image      the image
     * @param formatName image format ("png", "jpg", ...)
     * @return encoded size in bytes; 0 on IO error
     */
    public static long getImageFileSize(BufferedImage image, String formatName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, formatName, baos);
            baos.flush();
            return baos.size();
        } catch (IOException e) {
            log.error("getImageFileSize error", e);
            return 0;
        }
    }

    /**
     * Create a solid-colour image of the given size.
     */
    public static BufferedImage getImageOnlyColor(int width, int height, Color color) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        try {
            g2d.setColor(color);
            g2d.fillRect(0, 0, width, height);
        } finally {
            g2d.dispose();
        }
        return bufferedImage;
    }

    /**
     * Create a solid-colour image with a centred red watermark.
     */
    public static BufferedImage getImageOnlyColor(int width, int height, Color color, String text) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        try {
            g2d.setColor(color);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString(text, width / 2 - text.length() * 10, height / 2 + 10);
        } finally {
            g2d.dispose();
        }
        return bufferedImage;
    }

    /** Create a blank (white) image of the given size. */
    public static BufferedImage getWhiteImage(int width, int height) {
        return getImageOnlyColor(width, height, Color.WHITE);
    }

    /**
     * Blank image with a centred watermark.
     *
     * @see #watermark(BufferedImage, Position, String, double, float)
     */
    public static BufferedImage getWhiteImageWithWatermark(int width, int height, String text) {
        return watermark(getImageOnlyColor(width, height, Color.WHITE), Positions.CENTER, text, -0.5, 0.8f);
    }

    /** Test whether every pixel of {@code image} matches {@code color} (RGB only). */
    public static boolean isImageOnlyColor(BufferedImage image, Color color) {
        int width = image.getWidth();
        int height = image.getHeight();
        int targetRgb = color.getRGB() & 0x00FFFFFF;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y) & 0x00FFFFFF;
                if (rgb != targetRgb) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Shortcut: {@link #isImageOnlyColor(BufferedImage, Color)} with {@link Color#WHITE}. */
    public static boolean isWhiteImage(BufferedImage image) {
        return isImageOnlyColor(image, Color.WHITE);
    }

    /** Shortcut: white-pixel ratio ≥ {@code percent}. */
    public static boolean isWhiteImageOutPercent(BufferedImage image, float percent) {
        return isImageColorOutPercent(image, Color.WHITE, percent);
    }

    /** Whether the {@code color} pixel ratio ≥ {@code percent}. */
    public static boolean isImageColorOutPercent(BufferedImage image, Color color, float percent) {
        int width = image.getWidth();
        int height = image.getHeight();
        float count = 0;
        int targetRgb = color.getRGB() & 0x00FFFFFF;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y) & 0x00FFFFFF;
                if (rgb == targetRgb) {
                    count++;
                }
            }
        }
        return (count / (width * height)) >= percent / 100f;
    }

    /** Scale an image to A3, full quality. */
    public static BufferedImage scaleToA3(BufferedImage image) throws IOException {
        return scaleToA3(image, 100);
    }

    /** Scale an image to A3, with quality 1-100. */
    public static BufferedImage scaleToA3(BufferedImage image, Integer quality) throws IOException {
        return scaleTo(image, ImagePageSize.A3, quality);
    }

    /** Scale an image to A4, full quality. */
    public static BufferedImage scaleToA4(BufferedImage image) throws IOException {
        return scaleToA4(image, 100);
    }

    /** Scale an image to A4, with quality 1-100. */
    public static BufferedImage scaleToA4(BufferedImage image, Integer quality) throws IOException {
        return scaleTo(image, ImagePageSize.A4, quality);
    }

    /** Scale to a standard page size, with quality 1-100. */
    public static BufferedImage scaleTo(BufferedImage image, ImagePageSize pageSize, Integer quality) throws IOException {
        return scaleTo(image, pageSize.getWidth(), pageSize.getHeight(), quality);
    }

    /** Scale to a custom (width, height), with quality 1-100. Returns the image unchanged if already at the target size. */
    public static BufferedImage scaleTo(BufferedImage image, float width, float height, Integer quality) throws IOException {
        if (isNeedScale(image, width, height)) {
            float scaledWidth = width / image.getWidth();
            float scaledHeight = height / image.getHeight();
            log.info("scaleFactor:{}, scaledWidth:{} , scaledHeight:{}  ", scaledWidth, scaledHeight);
            return Thumbnails.of(image)
                    .alphaInterpolation(AlphaInterpolation.QUALITY)
                    .antialiasing(Antialiasing.OFF)
                    .dithering(Dithering.ENABLE)
                    .scale(scaledWidth, scaledHeight)
                    .outputQuality(quality / 100f)
                    .asBufferedImage();
        }
        return image;
    }

    public static boolean isNeedScale(BufferedImage image, ImagePageSize pageSize) {
        return isNeedScale(image, pageSize.getWidth(), pageSize.getHeight());
    }

    public static boolean isNeedScale(BufferedImage image, float width, float height) {
        return image.getWidth() != width || image.getHeight() != height;
    }

    /** Read every pixel into a 2-D {@code int} array. */
    public static int[][] getImagePixels(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] pixels = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[x][y] = image.getRGB(x, y);
            }
        }
        return pixels;
    }

    /**
     * Compute the fraction of identical pixels between two images.
     * Returns 0.0 when sizes differ.
     */
    public static double calculateSimilarity(int[][] pixels1, int[][] pixels2) {
        if (pixels1.length == 0 || pixels2.length == 0
                || pixels1.length != pixels2.length
                || pixels1[0].length != pixels2[0].length) {
            log.warn("Image dimensions do not match for similarity calculation.");
            return 0.0;
        }
        int width = pixels1.length;
        int height = pixels1[0].length;
        long count = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (pixels1[x][y] == pixels2[x][y]) {
                    count++;
                }
            }
        }
        return (double) count / (width * height);
    }

    /** Thumbnailator wrapper: scale to (width, height). */
    public static void zoomSize(File srcFile, File outFile, int width, int height) {
        try {
            Thumbnails.of(srcFile).size(width, height).toFile(outFile);
        } catch (IOException e) {
            log.error("zoomSize error", e);
        }
    }

    /** Thumbnailator wrapper: scale by ratio (0~1 shrink, &gt;1 enlarge). */
    public static void zoomScaling(File srcFile, File outFile, double scale) {
        try {
            Thumbnails.of(srcFile).scale(scale).toFile(outFile);
        } catch (IOException e) {
            log.error("zoomScaling error", e);
        }
    }

    /** Thumbnailator wrapper: rotate by degrees. */
    public static void rotateAngle(File srcFile, File outFile, double rotate) {
        try {
            Thumbnails.of(srcFile).scale(1).rotate(rotate).toFile(outFile);
        } catch (IOException e) {
            log.error("rotateAngle error", e);
        }
    }

    /** Thumbnailator wrapper: crop by named position. */
    public static void corpRegion(File srcFile, File outFile, Positions position, int width, int height, int x, int y) {
        try {
            Thumbnails.of(srcFile).sourceRegion(position, x, y).size(width, height).toFile(outFile);
        } catch (IOException e) {
            log.error("corpRegion error", e);
        }
    }

    /** Thumbnailator wrapper: crop by absolute coordinates. */
    public static void corpRegion(File srcFile, File outFile, int width, int height, int x1, int y1, int x2, int y2) {
        try {
            Thumbnails.of(srcFile).sourceRegion(x1, y1, x2, y2).size(width, height).keepAspectRatio(false).toFile(outFile);
        } catch (IOException e) {
            log.error("corpRegion error", e);
        }
    }

    /** Build a translucent text watermark of the given dimensions. */
    public static BufferedImage watermarkImage(String text, int width, int height, double rotate) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        bi = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        g.dispose();
        g = bi.createGraphics();
        g.setFont(new Font("微软雅黑", Font.BOLD, 26));
        char[] data = text.toCharArray();
        g.rotate(rotate);
        g.setColor(Color.RED);
        g.drawChars(data, 0, data.length, -70, 200);
        g.dispose();
        return bi;
    }

    /** Apply a text watermark onto a {@link BufferedImage} at the given position. */
    public static BufferedImage watermark(BufferedImage srcImage, Position position, String text,
                                          double rotate, float opacity) {
        try {
            BufferedImage watermarkImage = watermarkImage(text, srcImage.getWidth(), 50, rotate);
            return Thumbnails.of(srcImage).scale(1).watermark(position, watermarkImage, opacity).asBufferedImage();
        } catch (IOException e) {
            log.error("watermark error", e);
            return srcImage;
        }
    }

    /** Apply a text watermark onto a file. */
    public static void watermark(File srcFile, File outFile, Position position, String text,
                                  double rotate, float opacity) {
        try {
            BufferedImage watermarkImage = watermarkImage(text, 300, 300, rotate);
            Thumbnails.of(srcFile).scale(1).watermark(position, watermarkImage, opacity).toFile(outFile);
        } catch (IOException e) {
            log.error("watermark error", e);
        }
    }

    /** Internal helper retained for parity with the original watermarkLongImage. */
    @SuppressWarnings("unused")
    private static void watermarkLongImage(File srcFile, File outFile) {
        BufferedImage bi = new BufferedImage(300, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        bi = g.getDeviceConfiguration().createCompatibleImage(300, 100, Transparency.TRANSLUCENT);
        g.dispose();
        g = bi.createGraphics();
        g.setFont(new Font("微软雅黑", Font.BOLD, 26));
        char[] data = "http://www.chendd.cn".toCharArray();
        g.setColor(Color.RED);
        g.drawChars(data, 0, data.length, 10, 20);
        g.dispose();
        try {
            BufferedImage image = ImageIO.read(srcFile);
            int height = image.getHeight();
            Thumbnails.Builder<File> builder = Thumbnails.of(srcFile).scale(1);
            int mod = (int) Math.ceil(height / 400d);
            for (int i = 1; i < mod; i++) {
                int x = 200;
                int y = i * 400;
                builder.watermark(new Coordinate(x, y), bi, 1);
            }
            builder.toFile(outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}