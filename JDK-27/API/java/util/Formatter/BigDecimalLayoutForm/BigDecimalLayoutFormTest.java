/*
 * @test
 * @summary java.util.Formatter.BigDecimalLayoutForm enum conformance.
 * @run main BigDecimalLayoutFormTest
 */

import java.util.Formatter;

public class BigDecimalLayoutFormTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBigDecimalLayoutForm() {
        check(Formatter.BigDecimalLayoutForm.values().length == 2, "2 constants");
        check(Formatter.BigDecimalLayoutForm.valueOf("SCIENTIFIC")
              == Formatter.BigDecimalLayoutForm.SCIENTIFIC, "valueOf");
        check(Formatter.BigDecimalLayoutForm.DECIMAL_FLOAT
              != Formatter.BigDecimalLayoutForm.SCIENTIFIC, "distinct constants");
    }

    public static void main(String[] args) {
        testBigDecimalLayoutForm();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
