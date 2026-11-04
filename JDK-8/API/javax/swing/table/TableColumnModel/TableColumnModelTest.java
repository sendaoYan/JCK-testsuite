/*
 * @test
 * @summary javax.swing.table.TableColumnModel conformance: type shape via reflection
 *          (headless-safe) plus interface completeness and polymorphic use.
 * @run main TableColumnModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableColumnModelListener;

public class TableColumnModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Recording implementation: every interface method is reachable through the
    // interface type and reports the arguments it was called with.
    static class RecordingModel implements javax.swing.table.TableColumnModel {
        int addCalls, removeCalls, moveCalls, marginCalls, selAllowedCalls;
        javax.swing.table.TableColumn lastAdded, lastRemoved;
        int moveFrom = -1, moveTo = -1, lastMargin = -1;
        public void addColumn(javax.swing.table.TableColumn aColumn) { addCalls++; lastAdded = aColumn; }
        public void removeColumn(javax.swing.table.TableColumn column) { removeCalls++; lastRemoved = column; }
        public void moveColumn(int columnIndex, int newIndex) { moveCalls++; moveFrom = columnIndex; moveTo = newIndex; }
        public void setColumnMargin(int newMargin) { marginCalls++; lastMargin = newMargin; }
        public int getColumnCount() { return 2; }
        public java.util.Enumeration<javax.swing.table.TableColumn> getColumns() {
            return java.util.Collections.enumeration(java.util.Arrays.asList(
                    new javax.swing.table.TableColumn(0), new javax.swing.table.TableColumn(1)));
        }
        public int getColumnIndex(Object columnIdentifier) { return 1; }
        public javax.swing.table.TableColumn getColumn(int columnIndex) { return new javax.swing.table.TableColumn(columnIndex); }
        public int getColumnMargin() { return 4; }
        public int getColumnIndexAtX(int xPosition) { return xPosition < 50 ? 0 : 1; }
        public int getTotalColumnWidth() { return 100; }
        public void setColumnSelectionAllowed(boolean flag) { selAllowedCalls++; }
        public boolean getColumnSelectionAllowed() { return false; }
        public int[] getSelectedColumns() { return new int[]{0}; }
        public int getSelectedColumnCount() { return 1; }
        public void setSelectionModel(ListSelectionModel newModel) { }
        public ListSelectionModel getSelectionModel() { return null; }
        public void addColumnModelListener(TableColumnModelListener x) { }
        public void removeColumnModelListener(TableColumnModelListener x) { }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.table.TableColumnModel", false, TableColumnModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addColumn"), "method addColumn");
        check(names.contains("removeColumn"), "method removeColumn");
        check(names.contains("moveColumn"), "method moveColumn");
        check(names.contains("setColumnMargin"), "method setColumnMargin");
        check(names.contains("getColumnCount"), "method getColumnCount");
        check(names.contains("getColumns"), "method getColumns");
        check(names.contains("getColumnIndex"), "method getColumnIndex");
        check(names.contains("getColumn"), "method getColumn");
        check(names.size() == 19, "exactly the 19 specified methods, got " + names.size() + ": " + names);
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "abstract method " + m.getName());
        }

        // DefaultTableColumnModel implements the interface.
        Class<?> impl = Class.forName("javax.swing.table.DefaultTableColumnModel", false, TableColumnModelTest.class.getClassLoader());
        check(c.isAssignableFrom(impl), "DefaultTableColumnModel is a TableColumnModel");

        // Polymorphic contract.
        javax.swing.table.TableColumnModel m = new RecordingModel();
        javax.swing.table.TableColumn tc = new javax.swing.table.TableColumn(3);
        m.addColumn(tc);
        m.moveColumn(0, 1);
        m.setColumnMargin(9);
        m.removeColumn(tc);
        RecordingModel rec = (RecordingModel) m;
        check(rec.addCalls == 1 && rec.lastAdded == tc, "addColumn through interface");
        check(rec.moveCalls == 1 && rec.moveFrom == 0 && rec.moveTo == 1, "moveColumn args");
        check(rec.marginCalls == 1 && rec.lastMargin == 9, "setColumnMargin arg");
        check(rec.removeCalls == 1 && rec.lastRemoved == tc, "removeColumn through interface");
        check(m.getColumnCount() == 2, "getColumnCount through interface");
        check(m.getColumnIndex("any") == 1, "getColumnIndex through interface");
        check(m.getColumnMargin() == 4, "getColumnMargin through interface");
        check(m.getTotalColumnWidth() == 100, "getTotalColumnWidth through interface");
        check(m.getColumnIndexAtX(10) == 0 && m.getColumnIndexAtX(60) == 1, "getColumnIndexAtX through interface");
        check(m.getSelectedColumns().length == 1 && m.getSelectedColumnCount() == 1, "selection queries through interface");
        check(!m.getColumnSelectionAllowed(), "getColumnSelectionAllowed through interface");
        check(m.getColumns().hasMoreElements(), "getColumns through interface");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
