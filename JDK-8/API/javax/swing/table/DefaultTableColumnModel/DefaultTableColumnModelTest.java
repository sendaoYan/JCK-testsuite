/*
 * @test
 * @summary javax.swing.table.DefaultTableColumnModel conformance: columns (headless-safe):
 *          defaults, add/remove/move semantics and events, X/width mapping, selection
 *          wiring, null/out-of-range exception matrix, serialization.
 * @run main DefaultTableColumnModelTest
 */

import java.util.Enumeration;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class DefaultTableColumnModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> ex, String m, ThrowingRunnable r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + ": expected " + ex.getName() + " but got " + t.getClass().getName() + ": " + t.getMessage());
        }
        throw new AssertionError(m + ": expected " + ex.getName() + " but no exception");
    }

    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object r = ois.readObject();
        ois.close();
        return r;
    }

    static class Recorder implements TableColumnModelListener {
        int[] added = {-2, -2}, removed = {-2, -2}, moved = {-2, -2};
        int marginChanges, selectionChanges;
        public void columnAdded(TableColumnModelEvent e) { added = new int[]{e.getFromIndex(), e.getToIndex()}; }
        public void columnRemoved(TableColumnModelEvent e) { removed = new int[]{e.getFromIndex(), e.getToIndex()}; }
        public void columnMoved(TableColumnModelEvent e) { moved = new int[]{e.getFromIndex(), e.getToIndex()}; }
        public void columnMarginChanged(ChangeEvent e) { marginChanges++; }
        public void columnSelectionChanged(ListSelectionEvent e) { selectionChanges++; }
    }

    static String order(DefaultTableColumnModel m) {
        StringBuilder s = new StringBuilder();
        Enumeration<TableColumn> e = m.getColumns();
        while (e.hasMoreElements()) s.append(e.nextElement().getModelIndex()).append(",");
        return s.toString();
    }

    static void test() throws Exception {
        DefaultTableColumnModel m = new DefaultTableColumnModel();
        check(m.getColumnCount() == 0, "empty");
        check(m.getColumnMargin() == 1, "default columnMargin 1");
        check(m.getTotalColumnWidth() == 0, "default totalColumnWidth 0");
        check(m.getColumnSelectionAllowed() == false, "default columnSelectionAllowed false");
        check(m.getSelectionModel() != null, "default selection model non-null");
        check(m.getSelectionModel() instanceof DefaultListSelectionModel, "default selection model type");
        check(m.getColumnModelListeners().length == 0, "no listeners initially");
        check(!m.getColumns().hasMoreElements(), "no columns enumerated");

        Recorder rec = new Recorder();
        m.addColumnModelListener(rec);

        TableColumn col = new TableColumn(0);
        col.setWidth(100);
        m.addColumn(col);
        check(m.getColumnCount() == 1 && m.getColumn(0) == col, "addColumn");
        check(rec.added[0] == 0 && rec.added[1] == 0, "columnAdded(0,0) for first column");
        check(m.getTotalColumnWidth() == 100, "totalColumnWidth after first column");
        // getColumnIndex by identifier: headerValue fallback identifier
        TableColumn named = new TableColumn(7);
        named.setHeaderValue("SEVEN");
        m.addColumn(named);
        check(m.getColumnIndex("SEVEN") == 1, "getColumnIndex by headerValue identifier");
        check(rec.added[0] == 0 && rec.added[1] == 1, "columnAdded(0,1) for second column");
        // named still has the TableColumn default width of 75.
        check(m.getTotalColumnWidth() == 175, "totalColumnWidth sums current widths (100+75)");
        named.setWidth(60);
        check(m.getTotalColumnWidth() == 160, "totalColumnWidth live update after setWidth");
        check(m.getColumn(1) == named, "getColumn(1)");

        // Enumeration order is insertion order.
        check("0,7,".equals(order(m)), "enumeration order by modelIndex: " + order(m));

        // moveColumn
        m.moveColumn(1, 0);
        check("7,0,".equals(order(m)), "moveColumn(1,0) order");
        check(rec.moved[0] == 1 && rec.moved[1] == 0, "columnMoved(1,0)");
        m.moveColumn(0, 1);
        check("0,7,".equals(order(m)), "moveColumn back");
        m.moveColumn(0, 0);
        check("0,7,".equals(order(m)), "self moveColumn is a no-op");

        // getColumnIndexAtX boundaries: col0 spans [0,100), col1 [100,160).
        check(m.getColumnIndexAtX(0) == 0, "x=0 in first column");
        check(m.getColumnIndexAtX(99) == 0, "x=99 in first column");
        check(m.getColumnIndexAtX(100) == 1, "x=100 starts second column");
        check(m.getColumnIndexAtX(159) == 1, "x=159 in second column");
        check(m.getColumnIndexAtX(160) == -1, "x beyond total is -1");
        check(m.getColumnIndexAtX(-1) == -1, "negative x is -1");

        // margin events and round-trip (negative accepted verbatim).
        m.setColumnMargin(7);
        check(m.getColumnMargin() == 7, "columnMargin round-trip");
        check(rec.marginChanges >= 1, "setColumnMargin fires columnMarginChanged");
        m.setColumnMargin(-1);
        check(m.getColumnMargin() == -1, "negative columnMargin stored verbatim");

        // selection wiring.
        m.setColumnSelectionAllowed(true);
        check(m.getColumnSelectionAllowed(), "columnSelectionAllowed round-trip");
        ListSelectionModel sel = m.getSelectionModel();
        sel.setSelectionInterval(1, 1);
        check(m.getSelectedColumnCount() == 1, "selected count");
        check(m.getSelectedColumns().length == 1 && m.getSelectedColumns()[0] == 1, "selected columns content");
        check(rec.selectionChanges >= 1, "selection change fires columnSelectionChanged");
        DefaultListSelectionModel replacement = new DefaultListSelectionModel();
        m.setSelectionModel(replacement);
        check(m.getSelectionModel() == replacement, "setSelectionModel replaces");
        check(m.getSelectedColumnCount() == 0, "selection cleared by replacement model");
        checkThrows(IllegalArgumentException.class, "setSelectionModel(null)",
                new ThrowingRunnable() { public void run() { m.setSelectionModel(null); } });

        // removeColumn
        m.removeColumn(named);
        check(m.getColumnCount() == 1, "removeColumn count");
        check("0,".equals(order(m)), "removeColumn order");
        check(rec.removed[0] == 1, "columnRemoved from index of removed column");
        // Removing an absent column is a silent no-op; removing null likewise.
        m.removeColumn(named);
        check(m.getColumnCount() == 1, "removing absent column is a no-op");
        m.removeColumn(null);
        check(m.getColumnCount() == 1, "removing null column is a no-op");
        m.removeColumn(col);
        check(m.getColumnCount() == 0 && m.getTotalColumnWidth() == 0, "remove last column");

        // Exception matrix.
        checkThrows(IllegalArgumentException.class, "addColumn(null)",
                new ThrowingRunnable() { public void run() { m.addColumn(null); } });
        checkThrows(IllegalArgumentException.class, "getColumnIndex(null)",
                new ThrowingRunnable() { public void run() { m.getColumnIndex(null); } });
        checkThrows(IllegalArgumentException.class, "getColumnIndex(missing)",
                new ThrowingRunnable() { public void run() { m.getColumnIndex("no-such-identifier"); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "getColumn(5) empty model",
                new ThrowingRunnable() { public void run() { m.getColumn(5); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "getColumn(-1)",
                new ThrowingRunnable() { public void run() { m.getColumn(-1); } });
        final DefaultTableColumnModel m2 = new DefaultTableColumnModel();
        m2.addColumn(new TableColumn(0));
        checkThrows(IllegalArgumentException.class, "moveColumn(0,5) beyond range",
                new ThrowingRunnable() { public void run() { m2.moveColumn(0, 5); } });
        checkThrows(IllegalArgumentException.class, "moveColumn(-1,0)",
                new ThrowingRunnable() { public void run() { m2.moveColumn(-1, 0); } });

        // Listener registry.
        m.removeColumnModelListener(rec);
        check(m.getColumnModelListeners().length == 0, "listener removed");
        check(m.getListeners(TableColumnModelListener.class).length == 0, "getListeners empty");
        m.addColumnModelListener(rec);
        check(m.getListeners(TableColumnModelListener.class).length == 1, "getListeners one");
    }

    static void testSerialization() throws Exception {
        DefaultTableColumnModel m = new DefaultTableColumnModel();
        TableColumn a = new TableColumn(0), b = new TableColumn(1);
        m.addColumn(a); m.addColumn(b);
        m.setColumnMargin(9);
        m.setColumnSelectionAllowed(true);
        DefaultTableColumnModel back = (DefaultTableColumnModel) roundTrip(m);
        check(back.getColumnCount() == 2, "round-trip column count");
        check(back.getColumn(0).getModelIndex() == 0, "round-trip column 0");
        check(back.getColumn(1).getModelIndex() == 1, "round-trip column 1");
        check(back.getColumnMargin() == 9, "round-trip margin");
        check(back.getColumnSelectionAllowed(), "round-trip selection allowed");
        check(back.getSelectionModel() != null, "round-trip selection model");
    }

    public static void main(String[] args) throws Exception {
        test();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
