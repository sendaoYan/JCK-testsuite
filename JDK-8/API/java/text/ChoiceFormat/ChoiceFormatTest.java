/*
 * @test
 * @summary java.text.ChoiceFormat API conformance: limit-based selection.
 * @run main ChoiceFormatTest
 */

import java.text.ChoiceFormat;

public class ChoiceFormatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChoiceFormat() {
        ChoiceFormat cf = new ChoiceFormat("0#zero|1#one|2#many");
        check("zero".equals(cf.format(0)), "format 0");
        check("one".equals(cf.format(1)), "format 1");
        check("many".equals(cf.format(2)), "format 2");
        check("many".equals(cf.format(100)), "format 100");
    }

    public static void main(String[] args) {
        testChoiceFormat();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
