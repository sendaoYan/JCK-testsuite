/*
 * @test
 * @summary javax.swing.table.TableModel conformance: type shape via reflection (headless-safe)
 *          plus interface contract exercised polymorphically through a minimal implementation.
 * @run main TableModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class TableModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Minimal implementation so the interface contract is exercised through the
    // interface type itself, not through AbstractTableModel.
    static class ArrayModel implements javax.swing.table.TableModel {
        final String[][] data = {{"a0", "b0"}, {"a1", "b1"}};
        int listenerCalls;
        public int getRowCount() { return data.length; }
        public int getColumnCount() { return data[0].length; }
        public String getColumnName(int c) { return "C" + c; }
        public Class<?> getColumnClass(int c) { return String.class; }
        public boolean isCellEditable(int r, int c) { return r == c; }
        public Object getValueAt(int r, int c) { return data[r][c]; }
        public void setValueAt(Object v, int r, int c) { data[r][c] = String.valueOf(v); }
        public void addTableModelListener(TableModelListener l) { listenerCalls++; }
        public void removeTableModelListener(TableModelListener l) { listenerCalls += 2; }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.table.TableModel", false, TableModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getRowCount"), "method getRowCount");
        check(names.contains("getColumnCount"), "method getColumnCount");
        check(names.contains("getColumnName"), "method getColumnName");
        check(names.contains("getColumnClass"), "method getColumnClass");
        check(names.contains("isCellEditable"), "method isCellEditable");
        check(names.contains("getValueAt"), "method getValueAt");
        check(names.contains("setValueAt"), "method setValueAt");
        check(names.contains("addTableModelListener"), "method addTableModelListener");
        check(names.contains("removeTableModelListener"), "method removeTableModelListener");
        check(names.size() == 9, "exactly the 9 specified methods, got " + names.size() + ": " + names);

        // Every declared method is abstract (pure contract interface).
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "abstract method " + m.getName());
        }

        // Known implementors are assignment-compatible with the interface.
        Class<?> atm = Class.forName("javax.swing.table.AbstractTableModel", false, TableModelTest.class.getClassLoader());
        Class<?> dtm = Class.forName("javax.swing.table.DefaultTableModel", false, TableModelTest.class.getClassLoader());
        check(c.isAssignableFrom(atm), "AbstractTableModel is a TableModel");
        check(c.isAssignableFrom(dtm), "DefaultTableModel is a TableModel");

        // Polymorphic contract through the interface reference.
        javax.swing.table.TableModel m = new ArrayModel();
        check(m.getRowCount() == 2, "getRowCount through interface");
        check(m.getColumnCount() == 2, "getColumnCount through interface");
        check("C1".equals(m.getColumnName(1)), "getColumnName through interface");
        check(m.getColumnClass(0) == String.class, "getColumnClass through interface");
        check(m.isCellEditable(0, 0) && !m.isCellEditable(0, 1), "isCellEditable through interface");
        check("b1".equals(m.getValueAt(1, 1)), "getValueAt through interface");
        m.setValueAt("Z", 0, 0);
        check("Z".equals(m.getValueAt(0, 0)), "setValueAt then getValueAt round-trip");
        TableModelListener counting = new TableModelListener() {
            public void tableChanged(TableModelEvent e) { }
        };
        m.addTableModelListener(counting);
        m.removeTableModelListener(counting);
        check(((ArrayModel) m).listenerCalls == 3, "listener add/remove observed by impl");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
