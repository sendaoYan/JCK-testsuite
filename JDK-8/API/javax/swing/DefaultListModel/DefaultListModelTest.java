/*
 * @test
 * @summary javax.swing.DefaultListModel conformance: element management (headless-safe).
 * @run main DefaultListModelTest
 */

import javax.swing.DefaultListModel;

public class DefaultListModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultListModel m = new DefaultListModel();
        check(m.isEmpty(), "empty");
        m.addElement("x");
        check(m.getSize() == 1 && "x".equals(m.getElementAt(0)), "add/get");
        check(m.indexOf("x") == 0, "indexOf");
        m.removeElement("x");
        check(m.isEmpty(), "removeElement");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
