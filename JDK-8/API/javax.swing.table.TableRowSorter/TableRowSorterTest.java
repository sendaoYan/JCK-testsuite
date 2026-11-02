/*
 * @test
 * @summary javax.swing.table.TableRowSorter conformance: row sorter (headless-safe).
 * @run main TableRowSorterTest
 */

import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableModel;

public class TableRowSorterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultTableModel model = new DefaultTableModel();
        TableRowSorter s = new TableRowSorter(model);
        check(s.getModel() == model, "model");
        check(s.getModelRowCount() == 0, "row count");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
