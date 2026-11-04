/*
 * @test
 * @summary javax.swing.table.TableColumnModel conformance: white-box structure + black-box behavior.
 * @run main TableColumnModelTest
 */

import java.lang.reflect.Modifier;
import java.util.Enumeration;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class TableColumnModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // anonymous implementation delegating to a DefaultTableColumnModel
    static TableColumnModel makeModel() {
        return new TableColumnModel() {
            private final DefaultTableColumnModel d = new DefaultTableColumnModel();
            public void addColumn(TableColumn c) { d.addColumn(c); }
            public void removeColumn(TableColumn c) { d.removeColumn(c); }
            public void moveColumn(int a, int b) { d.moveColumn(a, b); }
            public void setColumnMargin(int m) { d.setColumnMargin(m); }
            public int getColumnCount() { return d.getColumnCount(); }
            public Enumeration<TableColumn> getColumns() { return d.getColumns(); }
            public int getColumnIndex(Object id) { return d.getColumnIndex(id); }
            public TableColumn getColumn(int i) { return d.getColumn(i); }
            public int getColumnMargin() { return d.getColumnMargin(); }
            public int getColumnIndexAtX(int x) { return d.getColumnIndexAtX(x); }
            public int getTotalColumnWidth() { return d.getTotalColumnWidth(); }
            public void setColumnSelectionAllowed(boolean b) { d.setColumnSelectionAllowed(b); }
            public boolean getColumnSelectionAllowed() { return d.getColumnSelectionAllowed(); }
            public int[] getSelectedColumns() { return d.getSelectedColumns(); }
            public int getSelectedColumnCount() { return d.getSelectedColumnCount(); }
            public void setSelectionModel(ListSelectionModel m) { d.setSelectionModel(m); }
            public ListSelectionModel getSelectionModel() { return d.getSelectionModel(); }
            public void addColumnModelListener(TableColumnModelListener l) { d.addColumnModelListener(l); }
            public void removeColumnModelListener(TableColumnModelListener l) { d.removeColumnModelListener(l); }
        };
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.TableColumnModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TableColumnModel m = makeModel();
        check(m.getColumnCount() == 0, "initial column count");
        check(m.getColumnMargin() == 1, "initial margin");

        TableColumn a = new TableColumn(0); a.setIdentifier("a"); a.setWidth(100);
        TableColumn b = new TableColumn(1); b.setIdentifier("b"); b.setWidth(50);
        m.addColumn(a);
        m.addColumn(b);
        check(m.getColumnCount() == 2, "addColumn count");
        check(m.getColumn(0) == a && m.getColumn(1) == b, "getColumn");
        check(m.getColumnIndex("a") == 0 && m.getColumnIndex("b") == 1, "getColumnIndex");
        Enumeration<TableColumn> en = m.getColumns();
        check(en.nextElement() == a && en.nextElement() == b, "getColumns order");
        check(m.getTotalColumnWidth() == 150, "getTotalColumnWidth");
        check(m.getColumnIndexAtX(99) == 0 && m.getColumnIndexAtX(100) == 1, "getColumnIndexAtX");

        m.moveColumn(1, 0);
        check(m.getColumnIndex("b") == 0 && m.getColumnIndex("a") == 1, "moveColumn reorder");

        m.setColumnMargin(5);
        check(m.getColumnMargin() == 5, "set/get columnMargin");

        m.setColumnSelectionAllowed(true);
        check(m.getColumnSelectionAllowed(), "set/get columnSelectionAllowed");
        check(m.getSelectedColumnCount() == 0, "no selection yet");
        check(m.getSelectedColumns().length == 0, "no selected columns");

        check(m.getSelectionModel() != null, "selection model present");
        m.setSelectionModel(m.getSelectionModel());
        check(m.getSelectionModel() != null, "setSelectionModel round-trip");

        m.removeColumn(b);
        check(m.getColumnCount() == 1, "removeColumn count");

        m.addColumnModelListener(new TableColumnModelListener() {
            public void columnAdded(javax.swing.event.TableColumnModelEvent e) { }
            public void columnRemoved(javax.swing.event.TableColumnModelEvent e) { }
            public void columnMoved(javax.swing.event.TableColumnModelEvent e) { }
            public void columnMarginChanged(javax.swing.event.ChangeEvent e) { }
            public void columnSelectionChanged(javax.swing.event.ListSelectionEvent e) { }
        });
        check(true, "addColumnModelListener accepted");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
