package com.fivetuat.fsoftprojectserver.utils;

/**
 * Các tham số cấu hình và hằng số tính toán cho Thuật toán FSRS (Free Spaced Repetition Scheduler).
 * Mô hình DSR (Difficulty - Stability - Retrievability) với 21 tham số w0..w20 chuẩn Rust fsrs-rs.
 */
public class FsrsParameters {
    public static final double W0 = 0.2120;
    public static final double W1 = 1.2931;
    public static final double W2 = 2.3065;
    public static final double W3 = 8.2956;
    public static final double W4 = 6.4133;
    public static final double W5 = 0.8334;
    public static final double W6 = 3.0194;
    public static final double W7 = 0.0010;
    public static final double W8 = 1.8722;
    public static final double W9 = 0.1666;
    public static final double W10 = 0.7960;
    public static final double W11 = 1.4835;
    public static final double W12 = 0.0614;
    public static final double W13 = 0.2629;
    public static final double W14 = 1.6483;
    public static final double W15 = 0.6014;
    public static final double W16 = 1.8729;
    public static final double W17 = 0.5425;
    public static final double W18 = 0.0912;
    public static final double W19 = 0.0658;
    public static final double W20 = 0.1542;

    /** Hằng số chuẩn hóa FACTOR = 0.9^(-1/w20) - 1 ≈ 0.9796 */
    public static final double FACTOR = Math.pow(0.9, -1.0 / W20) - 1.0;

    /** Mục tiêu ghi nhớ mặc định (90%) */
    public static final double R_TARGET = 0.90;

    /** Giới hạn độ khó trong khoảng [1.0, 10.0] */
    public static double clampDifficulty(double d) {
        if (d < 1.0) return 1.0;
        if (d > 10.0) return 10.0;
        return d;
    }
}
