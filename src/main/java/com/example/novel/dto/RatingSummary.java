package com.example.novel.dto;

/**
 * 小说评分摘要(用于 REST 接口)。
 *
 * @param novelId 小说主键
 * @param average 平均分(无评分时为 null)
 * @param count   评分人数
 */
public record RatingSummary(Long novelId, Double average, long count) {
}
