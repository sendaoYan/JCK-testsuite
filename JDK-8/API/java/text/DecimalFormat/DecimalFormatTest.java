/*
 * @test
 * @summary java.text.DecimalFormat API conformance: grouping/rounding pattern and parse.
 * @run main DecimalFormatTest
 */

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DecimalFormatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDecimalFormat() throws Exception {
        DecimalFormat df =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        check("1,234.57".equals(df.format(1234.567)), "format pattern");
        check(df.parse("1,234.57").doubleValue() == 1234.57, "parse pattern");
    }

    public static void main(String[] args) throws Exception {
        testDecimalFormat();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
