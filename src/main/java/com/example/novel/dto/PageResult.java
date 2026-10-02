package com.example.novel.dto;

import java.util.List;

/**
 * 通用分页结果: 对已排序的完整列表做内存切片,页码越界时收敛到有效范围。
 *
 * @param items      当前页数据
 * @param page       当前页码(从 1 开始)
 * @param size       每页条数
 * @param total      总条数
 * @param totalPages 总页数(至少为 1)
 * @param hasPrev    是否有上一页
 * @param hasNext    是否有下一页
 * @param <T>        列表元素类型
 */
public record PageResult<T>(List<T> items, int page, int size, long total, int totalPages,
                            boolean hasPrev, boolean hasNext) {

    /** 分页默认每页条数。 */
    public static final int DEFAULT_SIZE = 12;

    /** 每页条数上限,避免一次拉取过多。 */
    public static final int MAX_SIZE = 100;

    /** 对完整列表 all 按 (page, size) 切片;page/size 非法时自动收敛为合法值。 */
    public static <T> PageResult<T> of(List<T> all, int page, int size) {
        int safeSize = size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int total = all.size();
        int totalPages = Math.max(1, (total + safeSize - 1) / safeSize);
        int safePage = page < 1 ? 1 : Math.min(page, totalPages);
        int from = Math.min((safePage - 1) * safeSize, total);
        int to = Math.min(from + safeSize, total);
        List<T> slice = List.copyOf(all.subList(from, to));
        return new PageResult<>(slice, safePage, safeSize, total, totalPages,
                safePage > 1, safePage < totalPages);
    }
}
