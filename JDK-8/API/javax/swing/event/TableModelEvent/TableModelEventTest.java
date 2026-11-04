/*
 * @test
 * @summary javax.swing.event.TableModelEvent conformance: white-box structure + black-box behavior.
 * @run main TableModelEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;

public class TableModelEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TableModelEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        check(javax.swing.event.TableModelEvent.INSERT == 1, "INSERT == 1");
        check(javax.swing.event.TableModelEvent.UPDATE == 0, "UPDATE == 0");
        check(javax.swing.event.TableModelEvent.DELETE == -1, "DELETE == -1");
        check(javax.swing.event.TableModelEvent.HEADER_ROW == -1, "HEADER_ROW == -1");
        check(javax.swing.event.TableModelEvent.ALL_COLUMNS == -1, "ALL_COLUMNS == -1");

        TableModel model = new DefaultTableModel();

        // Single-arg: whole table changed, all columns, UPDATE.
        javax.swing.event.TableModelEvent all = new javax.swing.event.TableModelEvent(model);
        check(all.getSource() == model, "single-arg getSource");
        check(all.getFirstRow() == 0, "single-arg firstRow 0");
        check(all.getLastRow() == Integer.MAX_VALUE, "single-arg lastRow Integer.MAX_VALUE");
        check(all.getColumn() == javax.swing.event.TableModelEvent.ALL_COLUMNS, "single-arg column ALL_COLUMNS");
        check(all.getType() == javax.swing.event.TableModelEvent.UPDATE, "single-arg type UPDATE");

        // Two-arg: a single row updated.
        javax.swing.event.TableModelEvent row = new javax.swing.event.TableModelEvent(model, 4);
        check(row.getFirstRow() == 4 && row.getLastRow() == 4, "two-arg row range");
        check(row.getColumn() == javax.swing.event.TableModelEvent.ALL_COLUMNS, "two-arg column ALL_COLUMNS");
        check(row.getType() == javax.swing.event.TableModelEvent.UPDATE, "two-arg type UPDATE");

        // Three-arg: row range updated.
        javax.swing.event.TableModelEvent range = new javax.swing.event.TableModelEvent(model, 1, 3);
        check(range.getFirstRow() == 1 && range.getLastRow() == 3, "three-arg range");
        check(range.getColumn() == javax.swing.event.TableModelEvent.ALL_COLUMNS, "three-arg column ALL_COLUMNS");

        // Four-arg: specific column updated.
        javax.swing.event.TableModelEvent col = new javax.swing.event.TableModelEvent(model, 1, 3, 2);
        check(col.getFirstRow() == 1 && col.getLastRow() == 3, "four-arg range");
        check(col.getColumn() == 2, "four-arg column 2");
        check(col.getType() == javax.swing.event.TableModelEvent.UPDATE, "four-arg type UPDATE");

        // Five-arg: full control, e.g. row insertion.
        javax.swing.event.TableModelEvent insert =
            new javax.swing.event.TableModelEvent(model, 5, 6, javax.swing.event.TableModelEvent.ALL_COLUMNS,
                                                 javax.swing.event.TableModelEvent.INSERT);
        check(insert.getFirstRow() == 5 && insert.getLastRow() == 6, "five-arg range");
        check(insert.getColumn() == javax.swing.event.TableModelEvent.ALL_COLUMNS, "five-arg column");
        check(insert.getType() == javax.swing.event.TableModelEvent.INSERT, "five-arg type INSERT");

        javax.swing.event.TableModelEvent delete =
            new javax.swing.event.TableModelEvent(model, 0, javax.swing.event.TableModelEvent.HEADER_ROW,
                                                 javax.swing.event.TableModelEvent.ALL_COLUMNS,
                                                 javax.swing.event.TableModelEvent.DELETE);
        check(delete.getFirstRow() == 0 && delete.getLastRow() == javax.swing.event.TableModelEvent.HEADER_ROW,
              "delete header row range");
        check(delete.getType() == javax.swing.event.TableModelEvent.DELETE, "delete type DELETE");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        TableModelEvent e = new TableModelEvent(new javax.swing.table.DefaultTableModel(), 1, 3, 2, TableModelEvent.UPDATE);
        check(e.getFirstRow() == 1 && e.getLastRow() == 3, "rows");
        check(e.getColumn() == 2 && e.getType() == TableModelEvent.UPDATE, "column/type");
    }



    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
