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


    // ===================== 契约深化：枚举常量全集/valueOf/ordinal 稳定 =====================
    static void testLayoutContracts() {
        java.util.Formatter.BigDecimalLayoutForm[] vals =
              java.util.Formatter.BigDecimalLayoutForm.values();
        check(vals.length == 2, "exactly two constants");
        check(java.util.Formatter.BigDecimalLayoutForm.valueOf("SCIENTIFIC")
                    == java.util.Formatter.BigDecimalLayoutForm.SCIENTIFIC
              && java.util.Formatter.BigDecimalLayoutForm.valueOf("DECIMAL_FLOAT")
                    == java.util.Formatter.BigDecimalLayoutForm.DECIMAL_FLOAT,
              "valueOf both constants");
        check(java.util.Formatter.BigDecimalLayoutForm.SCIENTIFIC
                    != java.util.Formatter.BigDecimalLayoutForm.DECIMAL_FLOAT,
              "distinct constants");
        check(java.util.Formatter.BigDecimalLayoutForm.SCIENTIFIC.ordinal() == 0,
              "SCIENTIFIC declared first (ordinal 0)");
        check("SCIENTIFIC".equals(java.util.Formatter.BigDecimalLayoutForm.SCIENTIFIC.name()),
              "constant names");
    }

    public static void main(String[] args) {
        testBigDecimalLayoutForm();
        testLayoutContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
