/*
 * @test
 * @summary java.util.StringJoiner API conformance: delimiter joining, prefix/suffix, length,
 *          and setEmptyValue.
 * @run main StringJoinerTest
 */

import java.util.StringJoiner;

public class StringJoinerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDelimiter() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("a").add("b").add("c");
        check("a,b,c".equals(sj.toString()), "delimiter");
    }

    static void testPrefixSuffix() {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        sj.add("x").add("y");
        check("[x,y]".equals(sj.toString()), "prefix/suffix");
        check(sj.length() == 5, "length");
    }

    static void testEmptyValue() {
        check("empty".equals(new StringJoiner(",").setEmptyValue("empty").toString()),
              "setEmptyValue");
    }

    public static void main(String[] args) {
        testDelimiter();
        testPrefixSuffix();
        testEmptyValue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
