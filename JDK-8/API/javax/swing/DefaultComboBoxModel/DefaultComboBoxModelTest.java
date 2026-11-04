/*
 * @test
 * @summary javax.swing.DefaultComboBoxModel conformance: element management (headless-safe).
 * @run main DefaultComboBoxModelTest
 */

import javax.swing.DefaultComboBoxModel;

public class DefaultComboBoxModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultComboBoxModel m = new DefaultComboBoxModel();
        check(m.getSize() == 0, "empty");
        m.addElement("a");
        m.addElement("b");
        check(m.getSize() == 2, "size");
        check("a".equals(m.getElementAt(0)), "getElementAt");
        check(m.getIndexOf("b") == 1, "getIndexOf");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
