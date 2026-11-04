/*
 * @test
 * @summary javax.swing.table.AbstractTableModel conformance: type shape, default column naming,
 *          findColumn, cell edit defaults, fireTable* event precision (INSERT/UPDATE/DELETE
 *          row/column), listener registry, serialization (headless-safe).
 * @run main AbstractTableModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class AbstractTableModelTest {
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

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
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

    // Concrete, serializable model for exercising inherited behavior.
    static class Model extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        final int rows, cols;
        Model(int rows, int cols) { this.rows = rows; this.cols = cols; }
        public int getRowCount() { return rows; }
        public int getColumnCount() { return cols; }
        public Object getValueAt(int r, int c) { return r + "," + c; }
    }

    static class RecordingListener implements TableModelListener {
        TableModelEvent last;
        int count;
        public void tableChanged(TableModelEvent e) { last = e; count++; }
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.AbstractTableModel", false, AbstractTableModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.table.TableModel"), "implements javax.swing.table.TableModel");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getColumnName"), "method getColumnName");
        check(names.contains("findColumn"), "method findColumn");
        check(names.contains("getColumnClass"), "method getColumnClass");
        check(names.contains("isCellEditable"), "method isCellEditable");
        check(names.contains("setValueAt"), "method setValueAt");
        check(names.contains("addTableModelListener"), "method addTableModelListener");
        check(names.contains("removeTableModelListener"), "method removeTableModelListener");
        check(names.contains("fireTableChanged"), "method fireTableChanged");
        check(names.contains("getTableModelListeners"), "method getTableModelListeners");
        check(names.contains("getListeners"), "method getListeners");
    }

    static void testColumnNaming() {
        Model m = new Model(2, 80);
        check("A".equals(m.getColumnName(0)), "getColumnName(0)=A");
        check("B".equals(m.getColumnName(1)), "getColumnName(1)=B");
        check("Z".equals(m.getColumnName(25)), "getColumnName(25)=Z");
        check("AA".equals(m.getColumnName(26)), "getColumnName(26)=AA");
        check("AB".equals(m.getColumnName(27)), "getColumnName(27)=AB");
        check("AZ".equals(m.getColumnName(51)), "getColumnName(51)=AZ");
        check("BA".equals(m.getColumnName(52)), "getColumnName(52)=BA");
        check("".equals(m.getColumnName(-1)), "getColumnName(-1) empty");
    }

    static void testFindColumnAndDefaults() {
        Model m = new Model(3, 2);
        check(m.findColumn("A") == 0, "findColumn A");
        check(m.findColumn("B") == 1, "findColumn B");
        check(m.findColumn("ZZZ") == -1, "findColumn missing");
        checkThrows(NullPointerException.class, "findColumn null", new ThrowingRunnable() {
            public void run() { m.findColumn(null); }
        });
        check(m.getColumnClass(0) == Object.class, "getColumnClass Object");
        check(m.isCellEditable(0, 0) == false, "isCellEditable false default");
        m.setValueAt("anything", 0, 0); // documented no-op default
        check(true, "setValueAt default no-op");
    }

    static void testEvents() {
        Model m = new Model(5, 3);
        RecordingListener l = new RecordingListener();
        m.addTableModelListener(l);

        m.fireTableDataChanged();
        check(l.count == 1 && l.last.getType() == TableModelEvent.UPDATE
                && l.last.getFirstRow() == 0 && l.last.getLastRow() == Integer.MAX_VALUE
                && l.last.getColumn() == TableModelEvent.ALL_COLUMNS, "fireTableDataChanged");

        m.fireTableStructureChanged();
        check(l.last.getType() == TableModelEvent.UPDATE
                && l.last.getFirstRow() == TableModelEvent.HEADER_ROW
                && l.last.getLastRow() == TableModelEvent.HEADER_ROW
                && l.last.getColumn() == TableModelEvent.ALL_COLUMNS, "fireTableStructureChanged");

        m.fireTableRowsInserted(2, 4);
        check(l.last.getType() == TableModelEvent.INSERT
                && l.last.getFirstRow() == 2 && l.last.getLastRow() == 4
                && l.last.getColumn() == TableModelEvent.ALL_COLUMNS, "fireTableRowsInserted(2,4)");

        m.fireTableRowsDeleted(1, 1);
        check(l.last.getType() == TableModelEvent.DELETE
                && l.last.getFirstRow() == 1 && l.last.getLastRow() == 1, "fireTableRowsDeleted(1,1)");

        m.fireTableRowsUpdated(0, 2);
        check(l.last.getType() == TableModelEvent.UPDATE
                && l.last.getFirstRow() == 0 && l.last.getLastRow() == 2, "fireTableRowsUpdated(0,2)");

        m.fireTableCellUpdated(3, 1);
        check(l.last.getType() == TableModelEvent.UPDATE
                && l.last.getFirstRow() == 3 && l.last.getLastRow() == 3
                && l.last.getColumn() == 1, "fireTableCellUpdated(3,1)");

        // Custom event is passed through unchanged.
        final TableModelEvent custom = new TableModelEvent(m, 7, 9, 2, TableModelEvent.UPDATE);
        m.fireTableChanged(custom);
        check(l.last == custom, "fireTableChanged passes through");

        // Constants.
        check(TableModelEvent.INSERT == 1, "TableModelEvent.INSERT");
        check(TableModelEvent.UPDATE == 0, "TableModelEvent.UPDATE");
        check(TableModelEvent.DELETE == -1, "TableModelEvent.DELETE");
        check(TableModelEvent.HEADER_ROW == -1, "TableModelEvent.HEADER_ROW");
        check(TableModelEvent.ALL_COLUMNS == -1, "TableModelEvent.ALL_COLUMNS");
    }

    static void testListenerRegistry() {
        Model m = new Model(1, 1);
        RecordingListener a = new RecordingListener();
        RecordingListener b = new RecordingListener();
        m.addTableModelListener(a);
        m.addTableModelListener(b);
        check(m.getTableModelListeners().length == 2, "two listeners");
        check(m.getListeners(TableModelListener.class).length == 2, "getListeners two");
        m.fireTableDataChanged();
        check(a.count == 1 && b.count == 1, "both notified");
        m.removeTableModelListener(a);
        m.fireTableDataChanged();
        check(a.count == 1 && b.count == 2, "removed listener not notified");
        check(m.getTableModelListeners().length == 1, "one listener after removal");
        m.removeTableModelListener(b);
        check(m.getTableModelListeners().length == 0, "zero listeners");
    }

    static void testSerialization() throws Exception {
        Model m = new Model(3, 2);
        Model r = (Model) roundTrip(m);
        check(r.getRowCount() == 3, "round-trip rowCount");
        check(r.getColumnCount() == 2, "round-trip columnCount");
        check("1,1".equals(r.getValueAt(1, 1)), "round-trip value");
        check(r.isCellEditable(0, 0) == false, "round-trip isCellEditable");
        check(r.findColumn("B") == 1, "round-trip findColumn");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testColumnNaming();
        testFindColumnAndDefaults();
        testEvents();
        testListenerRegistry();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
