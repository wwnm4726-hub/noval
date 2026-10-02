package com.example.novel.dto;

/**
 * 运营后台数据概览指标。
 *
 * @param novelCount   小说总数
 * @param chapterCount 章节总数
 * @param userCount    注册用户数
 * @param commentCount 评论总数
 * @param ratingCount  评分次数
 */
public record DashboardStats(long novelCount, long chapterCount, long userCount,
                             long commentCount, long ratingCount) {
}
