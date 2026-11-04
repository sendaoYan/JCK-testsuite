/*
 * @test
 * @summary javax.swing.table.DefaultTableColumnModel conformance: columns (headless-safe).
 * @run main DefaultTableColumnModelTest
 */

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class DefaultTableColumnModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultTableColumnModel m = new DefaultTableColumnModel();
        check(m.getColumnCount() == 0, "empty");
        TableColumn col = new TableColumn(0);
        m.addColumn(col);
        check(m.getColumnCount() == 1 && m.getColumn(0) == col, "addColumn");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
