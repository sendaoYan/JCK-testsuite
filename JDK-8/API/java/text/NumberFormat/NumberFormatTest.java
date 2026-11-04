/*
 * @test
 * @summary java.text.NumberFormat API conformance: integer format/parse under Locale.US.
 * @run main NumberFormatTest
 */

import java.text.NumberFormat;
import java.util.Locale;

public class NumberFormatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNumberFormat() throws Exception {
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
        check("42".equals(nf.format(42)), "format 42");
        check(nf.parse("42").intValue() == 42, "parse 42");
    }

    public static void main(String[] args) throws Exception {
        testNumberFormat();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
