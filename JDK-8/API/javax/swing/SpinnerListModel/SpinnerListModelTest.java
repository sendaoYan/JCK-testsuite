/*
 * @test
 * @summary javax.swing.SpinnerListModel conformance: list value (headless-safe).
 * @run main SpinnerListModelTest
 */

import javax.swing.SpinnerListModel;

public class SpinnerListModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        SpinnerListModel m = new SpinnerListModel(new String[] {"a", "b", "c"});
        check(m.getList().size() == 3, "size");
        check("a".equals(m.getValue()), "initial value");
        check("b".equals(m.getNextValue()), "next");
        check(m.getPreviousValue() == null, "previous at head");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
