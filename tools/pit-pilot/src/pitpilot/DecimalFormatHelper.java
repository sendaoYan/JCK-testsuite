package pitpilot;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 测试辅助「等价逻辑」：格式化与数值容差断言辅助。
 * formatCurrency 是典型的「把 double 拆成符号 + 分」逻辑，内含分支与算术，
 * 适合用来演示「只断言非空 vs 断言内容」对突变分数的影响。
 */
public final class DecimalFormatHelper {

    private DecimalFormatHelper() {
    }

    /** 把金额拆成「符号 + 分（四舍五入到百分位）」的字符串。 */
    public static String formatCurrency(double value) {
        double abs = Math.abs(value);
        long cents = Math.round(abs * 100.0);
        if (value < 0) {
            return "-" + cents;
        }
        return Long.toString(cents);
    }

    /** 容差比较：|actual - expected| <= epsilon，epsilon 必须非负。 */
    public static boolean withinTolerance(double actual, double expected, double epsilon) {
        if (epsilon < 0) {
            throw new IllegalArgumentException("epsilon must be non-negative");
        }
        return Math.abs(actual - expected) <= epsilon;
    }

    /** 百分比格式化：value * 100，非负时带正号前缀。故意保留分支与算术，作为「弱断言」演示位。 */
    public static String formatPercent(double value) {
        double pct = value * 100.0;
        if (pct >= 0) {
            return "+" + pct;
        }
        return Long.toString(Math.round(pct));
    }

    /** 按指定 scale 与舍入模式做定点舍入，scale 必须非负。 */
    public static double roundToScale(double value, int scale, RoundingMode mode) {
        if (scale < 0) {
            throw new IllegalArgumentException("scale must be non-negative");
        }
        return BigDecimal.valueOf(value).setScale(scale, mode).doubleValue();
    }
}
