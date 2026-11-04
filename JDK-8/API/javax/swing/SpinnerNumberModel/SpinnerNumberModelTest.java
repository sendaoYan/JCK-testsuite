/*
 * @test
 * @summary javax.swing.SpinnerNumberModel conformance: numeric value (headless-safe).
 * @run main SpinnerNumberModelTest
 */

import javax.swing.SpinnerNumberModel;

public class SpinnerNumberModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        SpinnerNumberModel m = new SpinnerNumberModel(5, 0, 10, 1);
        check(((Number) m.getValue()).intValue() == 5, "value");
        check(m.getNumber().intValue() == 5, "number");
        check(m.getStepSize().intValue() == 1, "step");
        m.setValue(7);
        check(m.getNumber().intValue() == 7, "setValue");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
