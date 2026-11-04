/*
 * @test
 * @summary java.util.regex.MatchResult API conformance: group/start/end from a matcher.
 * @run main MatchResultTest
 */

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchResultTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMatchResult() {
        Matcher m = Pattern.compile("(\\w+)=(\\d+)").matcher("x=42");
        check(m.matches(), "matches");
        MatchResult r = m.toMatchResult();
        check(r.groupCount() == 2, "groupCount");
        check("x".equals(r.group(1)), "group 1");
        check("42".equals(r.group(2)), "group 2");
        check(r.start() == 0 && r.end() == 4, "start/end");
    }

    public static void main(String[] args) {
        testMatchResult();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
