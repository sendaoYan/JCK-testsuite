/*
 * @test
 * @summary javax.swing.RowSorter conformance: abstract sorter contract (headless-safe):
 *          type shape, minimal concrete subclass driving every abstract method,
 *          listener registry with protected fire* hooks, SortKey value semantics.
 * @run main RowSorterTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

public class RowSorterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** Minimal concrete sorter: identity mapping over a fixed model. */
    static class IdentitySorter extends RowSorter<String> {
        List<SortKey> keys = new ArrayList<SortKey>();
        int toggles;
        public String getModel() { return "model"; }
        public void toggleSortOrder(int column) { toggles++; }
        public int convertRowIndexToModel(int viewIndex) { return viewIndex; }
        public int convertRowIndexToView(int modelIndex) { return modelIndex; }
        public void setSortKeys(List<? extends SortKey> sortKeys) { keys = new ArrayList<SortKey>(sortKeys); }
        public List<? extends SortKey> getSortKeys() { return keys; }
        public int getViewRowCount() { return 3; }
        public int getModelRowCount() { return 3; }
        public void modelStructureChanged() { }
        public void allRowsChanged() { }
        public void rowsInserted(int firstRow, int endRow) { }
        public void rowsDeleted(int firstRow, int endRow) { }
        public void rowsUpdated(int firstRow, int endRow) { }
        public void rowsUpdated(int firstRow, int endRow, int column) { }
        void exposeSortOrderChanged() { fireSortOrderChanged(); }
        void exposeRowSorterChanged(int[] range) { fireRowSorterChanged(range); }
    }

    static class Rec implements RowSorterListener {
        int sortOrders, rowsChanged;
        RowSorterEvent last;
        public void sorterChanged(RowSorterEvent e) {
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) sortOrders++;
            else rowsChanged++;
            last = e;
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.RowSorter", false, RowSorterTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getTypeParameters().length == 1, "generic over the model type");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getModel"), "method getModel");
        check(names.contains("toggleSortOrder"), "method toggleSortOrder");
        check(names.contains("convertRowIndexToModel"), "method convertRowIndexToModel");
        check(names.contains("convertRowIndexToView"), "method convertRowIndexToView");
        check(names.contains("setSortKeys"), "method setSortKeys");
        check(names.contains("getSortKeys"), "method getSortKeys");
        check(names.contains("getViewRowCount"), "method getViewRowCount");
        check(names.contains("getModelRowCount"), "method getModelRowCount");
        check(names.contains("modelStructureChanged"), "method modelStructureChanged");
        check(names.contains("allRowsChanged"), "method allRowsChanged");
        check(names.contains("rowsInserted"), "method rowsInserted");
        check(names.contains("rowsDeleted"), "method rowsDeleted");
        check(names.contains("rowsUpdated"), "method rowsUpdated");
        check(names.contains("addRowSorterListener"), "method addRowSorterListener");
        check(names.contains("removeRowSorterListener"), "method removeRowSorterListener");
        check(names.size() == 17, "15 public method names + 2 protected fire* names, got " + names.size() + ": " + names);
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        }
        check(abstractCount == 14, "14 abstract method declarations (rowsUpdated has two overloads), got " + abstractCount);
        // The fire* hooks are protected for subclass use.
        check(Modifier.isProtected(c.getDeclaredMethod("fireSortOrderChanged").getModifiers()),
                "fireSortOrderChanged is protected");
        check(Modifier.isProtected(c.getDeclaredMethod("fireRowSorterChanged", int[].class).getModifiers()),
                "fireRowSorterChanged(int[]) is protected");
        // Public constructor.
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isPublic(ctor.getModifiers()), "public no-arg constructor");

        // Polymorphic contract through a concrete subclass.
        IdentitySorter s = new IdentitySorter();
        check("model".equals(s.getModel()), "getModel through subclass");
        check(s.convertRowIndexToModel(2) == 2 && s.convertRowIndexToView(0) == 0, "identity mapping");
        check(s.getViewRowCount() == 3 && s.getModelRowCount() == 3, "row counts");
        s.toggleSortOrder(0);
        check(s.toggles == 1, "toggleSortOrder dispatched");
        List<RowSorter.SortKey> ks = Arrays.asList(
                new RowSorter.SortKey(1, SortOrder.DESCENDING),
                new RowSorter.SortKey(0, SortOrder.ASCENDING));
        s.setSortKeys(ks);
        check(s.getSortKeys().size() == 2 && s.getSortKeys().get(0).getColumn() == 1,
                "sort keys stored");
        // Model mutation hooks are invokable on the base type.
        RowSorter<?> rs = s;
        rs.modelStructureChanged();
        rs.allRowsChanged();
        rs.rowsInserted(0, 1);
        rs.rowsDeleted(0, 1);
        rs.rowsUpdated(0, 1);
        rs.rowsUpdated(0, 1, 0);
        check(true, "model mutation hooks dispatched");

        // Listener registry and fire hooks (the base class tracks listeners itself).
        Rec rec = new Rec();
        s.addRowSorterListener(rec);
        s.exposeSortOrderChanged();
        check(rec.sortOrders == 1 && rec.last.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED,
                "SORT_ORDER_CHANGED event");
        check(rec.last.getSource() == s, "event source is the sorter");
        s.exposeRowSorterChanged(new int[]{0, 2});
        check(rec.rowsChanged == 1 && rec.last.getType() == RowSorterEvent.Type.SORTED,
                "SORTED event");
        check(rec.last.getSource() == s, "sorted event source");
        s.removeRowSorterListener(rec);
        int before = rec.sortOrders + rec.rowsChanged;
        s.exposeSortOrderChanged();
        s.exposeRowSorterChanged(null);
        check(rec.sortOrders + rec.rowsChanged == before, "removed listener not notified");

        // SortKey value semantics (covered further in SortKeyTest).
        RowSorter.SortKey k = new RowSorter.SortKey(3, SortOrder.ASCENDING);
        check(k.getColumn() == 3 && k.getSortOrder() == SortOrder.ASCENDING, "sort key accessors");
        check(k.equals(new RowSorter.SortKey(3, SortOrder.ASCENDING)), "sort key equals");
        check(!k.equals(new RowSorter.SortKey(3, SortOrder.DESCENDING)), "different order not equal");
        check(!k.equals(new RowSorter.SortKey(4, SortOrder.ASCENDING)), "different column not equal");
        check(k.hashCode() == new RowSorter.SortKey(3, SortOrder.ASCENDING).hashCode(), "hashCode consistent");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
