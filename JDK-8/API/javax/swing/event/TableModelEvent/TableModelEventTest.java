/*
 * @test
 * @summary javax.swing.event.TableModelEvent conformance: event (headless-safe).
 * @run main TableModelEventTest
 */

import javax.swing.event.TableModelEvent;

public class TableModelEventTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        TableModelEvent e = new TableModelEvent(new javax.swing.table.DefaultTableModel(), 1, 3, 2, TableModelEvent.UPDATE);
        check(e.getFirstRow() == 1 && e.getLastRow() == 3, "rows");
        check(e.getColumn() == 2 && e.getType() == TableModelEvent.UPDATE, "column/type");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
