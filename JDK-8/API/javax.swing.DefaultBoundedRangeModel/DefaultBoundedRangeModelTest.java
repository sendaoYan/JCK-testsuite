/*
 * @test
 * @summary javax.swing.DefaultBoundedRangeModel conformance: range properties (headless-safe).
 * @run main DefaultBoundedRangeModelTest
 */

import javax.swing.DefaultBoundedRangeModel;

public class DefaultBoundedRangeModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultBoundedRangeModel m = new DefaultBoundedRangeModel(50, 10, 0, 100);
        check(m.getValue() == 50, "value");
        check(m.getExtent() == 10, "extent");
        check(m.getMinimum() == 0 && m.getMaximum() == 100, "min/max");
        m.setValue(80);
        check(m.getValue() == 80, "setValue");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
