/*
 * @test
 * @summary javax.swing.SpinnerDateModel conformance: date value (headless-safe).
 * @run main SpinnerDateModelTest
 */

import javax.swing.SpinnerDateModel;

public class SpinnerDateModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        SpinnerDateModel m = new SpinnerDateModel(new java.util.Date(0L), null, null, java.util.Calendar.DAY_OF_MONTH);
        check(m.getValue() instanceof java.util.Date, "value is Date");
        java.util.Date d = new java.util.Date(5000L);
        m.setValue(d);
        check(d.equals(m.getValue()), "setValue");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
