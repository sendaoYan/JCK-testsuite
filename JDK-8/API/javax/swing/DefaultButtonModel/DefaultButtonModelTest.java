/*
 * @test
 * @summary javax.swing.DefaultButtonModel conformance: selection and enabled (headless-safe).
 * @run main DefaultButtonModelTest
 */

import javax.swing.DefaultButtonModel;

public class DefaultButtonModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultButtonModel m = new DefaultButtonModel();
        check(!m.isSelected(), "not selected");
        m.setSelected(true);
        check(m.isSelected(), "selected");
        m.setEnabled(false);
        check(!m.isEnabled(), "disabled");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
