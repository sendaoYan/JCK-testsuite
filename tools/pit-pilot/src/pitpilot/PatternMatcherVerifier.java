package pitpilot;

import java.util.regex.Pattern;

/**
 * 测试辅助「等价逻辑」：正则匹配校验器。行为测试里常用「这一批字符串都该匹配该正则」，
 * 抽成纯逻辑后可被 PIT 自检。
 */
public final class PatternMatcherVerifier {

    private PatternMatcherVerifier() {
    }

    public static boolean matchesAll(String regex, String... inputs) {
        Pattern p = Pattern.compile(regex);
        for (String s : inputs) {
            if (!p.matcher(s).matches()) {
                return false;
            }
        }
        return true;
    }

    public static int countMatches(String regex, String input) {
        Pattern p = Pattern.compile(regex);
        java.util.regex.Matcher m = p.matcher(input);
        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }

    public static boolean matchesAny(String regex, String... inputs) {
        for (String s : inputs) {
            if (s != null && s.matches(regex)) {
                return true;
            }
        }
        return false;
    }
}
