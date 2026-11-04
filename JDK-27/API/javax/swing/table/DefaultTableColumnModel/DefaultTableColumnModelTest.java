/*
 * @test
 * @summary javax.swing.table.DefaultTableColumnModel conformance: white-box structure + black-box behavior.
 * @run main DefaultTableColumnModelTest
 */

import java.lang.reflect.Modifier;
import java.util.Enumeration;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class DefaultTableColumnModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.DefaultTableColumnModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 4, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.table.TableColumnModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.beans.PropertyChangeListener"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.event.ListSelectionListener"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("java.io.Serializable"), "interface 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultTableColumnModel cm = new DefaultTableColumnModel();
        check(cm.getColumnCount() == 0, "new: 0 columns");
        check(cm.getColumnMargin() == 1, "new: margin 1");
        check(!cm.getColumnSelectionAllowed(), "new: column selection not allowed");

        TableColumn a = new TableColumn(0); a.setIdentifier("a");
        TableColumn b = new TableColumn(1); b.setIdentifier("b");
        TableColumn c = new TableColumn(2); c.setIdentifier("c");
        cm.addColumn(a);
        cm.addColumn(b);
        cm.addColumn(c);
        check(cm.getColumnCount() == 3, "addColumn x3 count");
        check(cm.getColumn(0) == a, "getColumn(0)");
        check(cm.getColumn(2) == c, "getColumn(2)");
        check(cm.getColumnIndex("a") == 0, "getColumnIndex a == 0");
        check(cm.getColumnIndex("b") == 1, "getColumnIndex b == 1");
        check(cm.getColumnIndex("c") == 2, "getColumnIndex c == 2");

        // getColumns order
        Enumeration<TableColumn> en = cm.getColumns();
        check(en.nextElement() == a && en.nextElement() == b && en.nextElement() == c, "getColumns order a,b,c");
        check(!en.hasMoreElements(), "getColumns exhausted");

        // moveColumn reorders
        cm.moveColumn(2, 0);
        check(cm.getColumnIndex("c") == 0, "moveColumn: c to front");
        check(cm.getColumnIndex("a") == 1, "moveColumn: a shifted");
        check(cm.getColumnIndex("b") == 2, "moveColumn: b shifted");
        check(cm.getColumn(0) == c && cm.getColumn(1) == a && cm.getColumn(2) == b, "moveColumn: getColumn order");

        // removeColumn
        cm.removeColumn(a);
        check(cm.getColumnCount() == 2, "removeColumn: count");
        check(cm.getColumnIndex("b") == 1, "removeColumn: b index");

        // getColumnIndex not-found throws IllegalArgumentException
        boolean threw = false;
        try { cm.getColumnIndex("nope"); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "getColumnIndex unknown throws IllegalArgumentException");
        threw = false;
        try { cm.getColumnIndex(null); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "getColumnIndex null throws IllegalArgumentException");

        // width helpers: default width 75 each
        TableColumn w1 = new TableColumn(0); w1.setIdentifier("w1"); w1.setWidth(100);
        TableColumn w2 = new TableColumn(1); w2.setIdentifier("w2"); w2.setWidth(50);
        DefaultTableColumnModel cm2 = new DefaultTableColumnModel();
        cm2.addColumn(w1);
        cm2.addColumn(w2);
        check(cm2.getTotalColumnWidth() == 150, "getTotalColumnWidth sum");
        check(cm2.getColumnIndexAtX(-1) == -1, "getColumnIndexAtX negative == -1");
        check(cm2.getColumnIndexAtX(0) == 0, "getColumnIndexAtX 0 == 0");
        check(cm2.getColumnIndexAtX(99) == 0, "getColumnIndexAtX 99 == 0");
        check(cm2.getColumnIndexAtX(100) == 1, "getColumnIndexAtX 100 == 1");
        check(cm2.getColumnIndexAtX(150) == -1, "getColumnIndexAtX past end == -1");

        // column margin + selection allowed
        cm2.setColumnMargin(7);
        check(cm2.getColumnMargin() == 7, "set/get columnMargin");
        cm2.setColumnSelectionAllowed(true);
        check(cm2.getColumnSelectionAllowed(), "set/get columnSelectionAllowed true");

        // listener firing: added/removed/moved/marginChanged
        final int[] added = {0}; final int[] removed = {0}; final int[] moved = {0}; final int[] margin = {0};
        final TableColumnModelEvent[] lastAdded = new TableColumnModelEvent[1];
        final TableColumnModelEvent[] lastRemoved = new TableColumnModelEvent[1];
        final TableColumnModelEvent[] lastMoved = new TableColumnModelEvent[1];
        TableColumnModelListener l = new TableColumnModelListener() {
            public void columnAdded(TableColumnModelEvent e) { added[0]++; lastAdded[0] = e; }
            public void columnRemoved(TableColumnModelEvent e) { removed[0]++; lastRemoved[0] = e; }
            public void columnMoved(TableColumnModelEvent e) { moved[0]++; lastMoved[0] = e; }
            public void columnMarginChanged(ChangeEvent e) { margin[0]++; }
            public void columnSelectionChanged(ListSelectionEvent e) { }
        };
        DefaultTableColumnModel cm3 = new DefaultTableColumnModel();
        cm3.addColumnModelListener(l);
        TableColumn x = new TableColumn(0); x.setIdentifier("x");
        TableColumn y = new TableColumn(1); y.setIdentifier("y");
        cm3.addColumn(x);
        check(added[0] == 1, "addColumn fires columnAdded");
        check(lastAdded[0].getFromIndex() == 0 && lastAdded[0].getToIndex() == 0, "columnAdded event indices");
        cm3.addColumn(y);
        check(added[0] == 2, "addColumn fires columnAdded again");
        cm3.moveColumn(1, 0);
        check(moved[0] == 1, "moveColumn fires columnMoved");
        check(lastMoved[0].getFromIndex() == 1 && lastMoved[0].getToIndex() == 0, "columnMoved event indices");
        cm3.setColumnMargin(9);
        check(margin[0] == 1, "setColumnMargin fires columnMarginChanged");
        cm3.removeColumn(x);
        check(removed[0] == 1, "removeColumn fires columnRemoved");
        check(lastRemoved[0].getFromIndex() == 1 && lastRemoved[0].getToIndex() == 0, "columnRemoved event indices");
        cm3.removeColumnModelListener(l);
        int aBefore = added[0];
        cm3.addColumn(new TableColumn(2));
        check(added[0] == aBefore, "removeColumnModelListener stops delivery");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
