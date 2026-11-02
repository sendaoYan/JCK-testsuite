/*
 * @test
 * @summary org.w3c.dom.ranges.Range conformance: interface, compare constants.
 * @run main RangeTest
 */

import org.w3c.dom.ranges.Range;

public class RangeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Range.class.isInterface(), "Range is an interface");
        check(Range.START_TO_START == 0, "START_TO_START == 0");
        check(Range.START_TO_END == 1, "START_TO_END == 1");
        check(Range.END_TO_END == 2, "END_TO_END == 2");
        check(Range.END_TO_START == 3, "END_TO_START == 3");
        check(Range.class.getMethod("getStartContainer") != null, "getStartContainer()");
        check(Range.class.getMethod("getEndContainer") != null, "getEndContainer()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
