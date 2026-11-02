/*
 * @test
 * @summary javax.swing.DefaultSingleSelectionModel conformance: single selection (headless-safe).
 * @run main DefaultSingleSelectionModelTest
 */

import javax.swing.DefaultSingleSelectionModel;

public class DefaultSingleSelectionModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultSingleSelectionModel m = new DefaultSingleSelectionModel();
        check(m.getSelectedIndex() == -1, "none");
        m.setSelectedIndex(2);
        check(m.getSelectedIndex() == 2 && m.isSelected(), "selected");
        m.clearSelection();
        check(!m.isSelected(), "cleared");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
