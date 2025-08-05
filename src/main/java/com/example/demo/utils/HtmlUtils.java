package com.example.demo.utils;

import java.util.regex.Pattern;

/**
 * HTML工具类，用于处理富文本内容
 */
public class HtmlUtils {

    // HTML标签正则表达式
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    // 危险标签正则表达式（简单的XSS防护）
    private static final Pattern DANGEROUS_TAG_PATTERN = Pattern.compile(
            "(?i)<\\s*(script|iframe|object|embed|form|input|button|select|textarea|style|link|meta)[^>]*>.*?</\\s*\\1\\s*>|<\\s*(script|iframe|object|embed|form|input|button|select|textarea|style|link|meta)[^>]*/?\\s*>",
            Pattern.DOTALL
    );

    // 危险属性正则表达式
    private static final Pattern DANGEROUS_ATTR_PATTERN = Pattern.compile(
            "(?i)\\s+(on\\w+|javascript:|vbscript:|data:|about:)\\s*=",
            Pattern.DOTALL
    );

    /**
     * 去除HTML标签，获取纯文本内容
     * @param html HTML内容
     * @return 纯文本内容
     */
    public static String stripHtmlTags(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        // 去除HTML标签
        String text = HTML_TAG_PATTERN.matcher(html).replaceAll("");

        // 解码HTML实体
        text = text.replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");

        // 清理多余的空白字符
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    /**
     * 生成内容预览，去除HTML标签并截取指定长度
     * @param html HTML内容
     * @param maxLength 最大长度
     * @return 预览文本
     */
    public static String generatePreview(String html, int maxLength) {
        String plainText = stripHtmlTags(html);

        if (plainText.length() <= maxLength) {
            return plainText;
        }

        return plainText.substring(0, maxLength) + "...";
    }

    /**
     * 基础的HTML内容清理，移除危险标签和属性
     * @param html 原始HTML内容
     * @return 清理后的HTML内容
     */
    public static String sanitizeHtml(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        // 移除危险标签
        String cleanHtml = DANGEROUS_TAG_PATTERN.matcher(html).replaceAll("");

        // 移除危险属性
        cleanHtml = DANGEROUS_ATTR_PATTERN.matcher(cleanHtml).replaceAll(" ");

        return cleanHtml.trim();
    }

    /**
     * 验证HTML内容是否过长
     * @param html HTML内容
     * @param maxLength 最大长度
     * @return 是否超长
     */
    public static boolean isContentTooLong(String html, int maxLength) {
        return html != null && html.length() > maxLength;
    }

    /**
     * 检查内容是否为空（去除HTML标签后）
     * @param html HTML内容
     * @return 是否为空
     */
    public static boolean isContentEmpty(String html) {
        String plainText = stripHtmlTags(html);
        return plainText.trim().isEmpty();
    }
}