package pitpilot;

import java.util.List;
import java.util.Locale;

/**
 * 测试辅助「等价逻辑」：字符串/格式化不变量校验器。
 * 把 java.lang.String 的 format/join/repeat 及 Character 大小写规范化抽成纯逻辑，
 * 供行为测试复用，并作为 PIT 的被测类自检这些辅助断言是否写实。
 */
public final class StringFormatHelper {

    private StringFormatHelper() {
    }

    /** 用 String.format("%0Nd") 生成定长十进制串（符号计入宽度）。 */
    public static String zeroPadFormat(long value, int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        return String.format(Locale.ROOT, "%0" + width + "d", value);
    }

    /** 判断 s 是否等于把 items 用 delimiter 连接的结果（String.join 不变量）。 */
    public static boolean isJoined(String s, String delimiter, List<String> items) {
        return String.join(delimiter, items).equals(s);
    }

    /** 判断 s 是否等于 text 重复 n 次（String.repeat 不变量）。 */
    public static boolean isRepeated(String s, String text, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative");
        }
        return text.repeat(n).equals(s);
    }

    /** 首字母大写、其余小写（Locale.ROOT 保证确定性），null/空串原样返回。 */
    public static String titleCase(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase(Locale.ROOT);
    }
}
