/*
 * @test
 * @summary java.util.regex.Pattern API conformance: compile/match/split/quote.
 * @run main PatternTest
 */

import java.util.regex.Pattern;

public class PatternTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPattern() {
        Pattern p = Pattern.compile("a+b");
        check(p.matcher("aaab").matches(), "matcher matches");
        check(!p.matcher("acb").matches(), "matcher no match");
        check(Pattern.matches("\\d+", "123"), "Pattern.matches");

        String[] parts = Pattern.compile(",").split("a,b,c");
        check(parts.length == 3 && "a".equals(parts[0]) && "b".equals(parts[1])
                  && "c".equals(parts[2]),
              "split");

        check(Pattern.compile(Pattern.quote("a+b")).matcher("a+b").matches(),
              "quote literal match");
        check(!Pattern.compile(Pattern.quote("a+b")).matcher("ab").matches(),
              "quote disables regex");
    }

    public static void main(String[] args) {
        testPattern();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
