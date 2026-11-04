/*
 * @test
 * @summary java.util.regex.Matcher API conformance: find/group/replaceAll/groupCount.
 * @run main MatcherTest
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcherTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMatcher() {
        Matcher m = Pattern.compile("(\\d+)").matcher("a123b456");
        check(m.groupCount() == 1, "groupCount");
        check(m.find(), "find first");
        check("123".equals(m.group()), "group first");
        check(m.find(), "find second");
        check("456".equals(m.group()), "group second");
        check(!m.find(), "no third");

        Matcher m2 = Pattern.compile("foo").matcher("foo foo");
        check("bar bar".equals(m2.replaceAll("bar")), "replaceAll");
    }

    public static void main(String[] args) {
        testMatcher();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
