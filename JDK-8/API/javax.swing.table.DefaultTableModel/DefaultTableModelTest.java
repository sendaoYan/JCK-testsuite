/*
 * @test
 * @summary javax.swing.table.DefaultTableModel conformance: rows/columns (headless-safe).
 * @run main DefaultTableModelTest
 */

import javax.swing.table.DefaultTableModel;

public class DefaultTableModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultTableModel m = new DefaultTableModel();
        check(m.getRowCount() == 0 && m.getColumnCount() == 0, "empty");
        m.addColumn("C");
        check(m.getColumnCount() == 1, "column");
        m.addRow(new Object[] {"v"});
        check(m.getRowCount() == 1 && "v".equals(m.getValueAt(0, 0)), "row");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
