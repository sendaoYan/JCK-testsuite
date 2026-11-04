/*
 * @test
 * @summary javax.swing.event.RowSorterEvent conformance: white-box structure + black-box behavior.
 * @run main RowSorterEventTest
 */

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class RowSorterEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class SimpleSorter extends javax.swing.RowSorter<Object> {
        public Object getModel() { return "model"; }
        public void toggleSortOrder(int column) { }
        public int convertRowIndexToModel(int index) { return index; }
        public int convertRowIndexToView(int index) { return index; }
        public void setSortKeys(List<? extends javax.swing.RowSorter.SortKey> keys) { }
        public List<? extends javax.swing.RowSorter.SortKey> getSortKeys() { return Collections.emptyList(); }
        public int getViewRowCount() { return 3; }
        public int getModelRowCount() { return 3; }
        public void modelStructureChanged() { }
        public void allRowsChanged() { }
        public void rowsInserted(int firstRow, int endRow) { }
        public void rowsDeleted(int firstRow, int endRow) { }
        public void rowsUpdated(int firstRow, int endRow) { }
        public void rowsUpdated(int firstRow, int endRow, int column) { }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.RowSorterEvent");
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
        SimpleSorter sorter = new SimpleSorter();

        javax.swing.event.RowSorterEvent orderChanged = new javax.swing.event.RowSorterEvent(sorter);
        check(orderChanged.getSource() == sorter, "single-arg getSource round-trip");
        check(orderChanged.getType() == javax.swing.event.RowSorterEvent.Type.SORT_ORDER_CHANGED,
              "single-arg type SORT_ORDER_CHANGED");
        check(orderChanged.getPreviousRowCount() == 0, "single-arg previous row count 0");
        check(orderChanged.convertPreviousRowIndexToModel(0) == -1, "single-arg convert returns -1");

        int[] previous = { 10, 20, 30 };
        javax.swing.event.RowSorterEvent sorted =
            new javax.swing.event.RowSorterEvent(sorter, javax.swing.event.RowSorterEvent.Type.SORTED, previous);
        check(sorted.getSource() == sorter, "getSource round-trip");
        check(sorted.getType() == javax.swing.event.RowSorterEvent.Type.SORTED, "type SORTED");
        check(sorted.getPreviousRowCount() == 3, "previous row count 3");
        check(sorted.convertPreviousRowIndexToModel(0) == 10, "convert index 0 -> 10");
        check(sorted.convertPreviousRowIndexToModel(1) == 20, "convert index 1 -> 20");
        check(sorted.convertPreviousRowIndexToModel(2) == 30, "convert index 2 -> 30");
        check(sorted.convertPreviousRowIndexToModel(3) == -1, "convert index 3 out of range -> -1");
        check(sorted.convertPreviousRowIndexToModel(-1) == -1, "convert index -1 -> -1");

        boolean threw = false;
        try {
            new javax.swing.event.RowSorterEvent(sorter, null, previous);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        check(threw, "null type throws IllegalArgumentException");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.RowSorterEvent", false, RowSorterEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getSource"), "method getSource");
        check(names.contains("getType"), "method getType");
        check(names.contains("convertPreviousRowIndexToModel"), "method convertPreviousRowIndexToModel");
        check(names.contains("getPreviousRowCount"), "method getPreviousRowCount");
        check(names.contains("getSource"), "method getSource");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
