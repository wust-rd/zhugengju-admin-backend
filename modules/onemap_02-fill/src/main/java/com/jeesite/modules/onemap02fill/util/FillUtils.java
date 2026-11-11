package com.jeesite.modules.onemap02fill.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 填报系统日期与状态工具
 * <p>
 * 口径与产品原型 17-市政基础设施项目填报系统-1111.html 一致：
 * 节点状态、超期判定、完整度计算均按原型规则实现。
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
public final class FillUtils {

	private FillUtils() {
	}

	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA);
	private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM", Locale.CHINA);
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

	/** 原型固定演示日期，种子数据按此日期回算 */
	public static final String DEMO_TODAY = "2026-11-11";

	/** 当前日期 */
	public static LocalDate today() {
		return LocalDate.now();
	}

	/** 格式化日期 */
	public static String fmtDay(LocalDate date) {
		return date == null ? null : date.format(DAY);
	}

	/** 格式化日期（java.util.Date 重载） */
	public static String fmtDay(java.util.Date date) {
		if (date == null) {
			return null;
		}
		return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(DAY);
	}

	/** 解析日期 */
	public static LocalDate parseDay(String text) {
		if (text == null || text.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(text.trim(), DAY);
		} catch (Exception e) {
			return null;
		}
	}

	/** 格式化月份 */
	public static String fmtMonth(LocalDate date) {
		return date == null ? null : date.format(MONTH);
	}

	/** 解析月份（yyyy-MM 或 yyyy-MM-dd） */
	public static LocalDate parseMonth(String text) {
		if (text == null || text.isBlank()) {
			return null;
		}
		String value = text.trim();
		try {
			if (value.length() == 7) {
				return LocalDate.parse(value + "-01", DAY);
			}
			return LocalDate.parse(value, DAY);
		} catch (Exception e) {
			return null;
		}
	}

	/** 月份加 n 个月，返回 yyyy-MM */
	public static String mAdd(String ym, int n) {
		LocalDate date = parseMonth(ym);
		if (date == null) {
			return null;
		}
		return date.plusMonths(n).format(MONTH);
	}

	/** 月份首日 */
	public static String mFirst(String ym) {
		LocalDate date = parseMonth(ym);
		return date == null ? null : date.format(DAY);
	}

	/** 月份末日 */
	public static String mLast(String ym) {
		LocalDate date = parseMonth(ym);
		if (date == null) {
			return null;
		}
		return date.withDayOfMonth(date.lengthOfMonth()).format(DAY);
	}

	/** 两个月份相差的月数（b - a） */
	public static int mDiff(String a, String b) {
		LocalDate x = parseMonth(a);
		LocalDate y = parseMonth(b);
		if (x == null || y == null) {
			return 0;
		}
		return (y.getYear() - x.getYear()) * 12 + (y.getMonthValue() - x.getMonthValue());
	}

	/** 日期加 n 天 */
	public static String dAdd(String ds, int n) {
		LocalDate date = parseDay(ds);
		return date == null ? null : date.plusDays(n).format(DAY);
	}

	/** 两个日期相差的天数（b - a） */
	public static long dDiff(String a, String b) {
		LocalDate x = parseDay(a);
		LocalDate y = parseDay(b);
		if (x == null || y == null) {
			return 0;
		}
		return y.toEpochDay() - x.toEpochDay();
	}

	/** 当前时间戳文本 */
	public static String nowText() {
		return java.time.LocalDateTime.now().format(TIME);
	}

	/** 格式化时间戳 */
	public static String fmtDateTime(java.util.Date date) {
		if (date == null) {
			return null;
		}
		return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().format(TIME);
	}

	/** LocalDate 转 Date */
	public static java.util.Date toDate(LocalDate date) {
		if (date == null) {
			return null;
		}
		return java.util.Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
	}

	/** Date 转 LocalDate */
	public static LocalDate toLocalDate(java.util.Date date) {
		if (date == null) {
			return null;
		}
		return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * 字符串种子，与原型 seedOf 一致
	 */
	public static int seedOf(String s) {
		int h = 7;
		for (int i = 0; i < String.valueOf(s).length(); i++) {
			h = (h * 31 + String.valueOf(s).charAt(i)) % 9973;
		}
		return h;
	}

	/**
	 * 伪随机数（0~1），与原型 rnd 一致
	 */
	public static double rnd(int seed) {
		double x = Math.sin(seed * 12.9898 + seed * 0.7) * 43758.5453;
		return x - Math.floor(x);
	}

	/**
	 * 扩展名映射为文件种类
	 */
	public static String kindOf(String ext) {
		if (ext == null) {
			return "doc";
		}
		return switch (ext.toLowerCase()) {
			case "jpg", "jpeg", "png", "gif", "webp" -> "img";
			case "mp4", "mov", "avi" -> "video";
			case "pdf" -> "pdf";
			case "doc", "docx" -> "doc";
			case "xls", "xlsx", "csv" -> "xls";
			case "dwg" -> "dwg";
			case "zip", "rar" -> "zip";
			default -> "doc";
		};
	}

	/**
	 * 文件大小文本
	 */
	public static String sizeStr(long size) {
		if (size <= 0) {
			return "—";
		}
		if (size < 1024) {
			return size + " B";
		}
		if (size < 1024 * 1024) {
			return (size / 1024) + " KB";
		}
		return String.format(Locale.CHINA, "%.1f MB", size / 1024.0 / 1024.0);
	}
}
