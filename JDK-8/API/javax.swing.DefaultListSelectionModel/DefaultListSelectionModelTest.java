/*
 * @test
 * @summary javax.swing.DefaultListSelectionModel conformance: selection intervals (headless-safe).
 * @run main DefaultListSelectionModelTest
 */

import javax.swing.DefaultListSelectionModel;

public class DefaultListSelectionModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultListSelectionModel m = new DefaultListSelectionModel();
        check(m.isSelectionEmpty(), "empty");
        m.setSelectionInterval(1, 3);
        check(!m.isSelectionEmpty(), "not empty");
        check(m.getMinSelectionIndex() == 1 && m.getMaxSelectionIndex() == 3, "range");
        check(m.isSelectedIndex(2), "isSelectedIndex");
        m.clearSelection();
        check(m.isSelectionEmpty(), "cleared");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
