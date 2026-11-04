/*
 * @test
 * @summary java.time.format.DecimalStyle conformance: STANDARD separators and signs.
 * @run main DecimalStyleTest
 */

import java.time.format.DecimalStyle;

public class DecimalStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDecimalStyle() {
        DecimalStyle ds = DecimalStyle.STANDARD;
        check(ds.getZeroDigit() == '0', "zeroDigit");
        check(ds.getPositiveSign() == '+', "positiveSign");
        check(ds.getNegativeSign() == '-', "negativeSign");
        check(ds.getDecimalSeparator() == '.', "decimalSeparator");
        DecimalStyle alt = ds.withDecimalSeparator(',');
        check(alt.getDecimalSeparator() == ',', "withDecimalSeparator");
        check(ds.getDecimalSeparator() == '.', "original unchanged");
        check(ds.equals(DecimalStyle.STANDARD), "equals STANDARD");
        check(ds.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testDecimalStyle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
