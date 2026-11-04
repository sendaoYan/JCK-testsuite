/*
 * @test
 * @summary javax.swing.table.TableRowSorter conformance: row sorter (headless-safe):
 *          model wiring, toggleSortOrder two-state cycle, sort keys validation,
 *          view/model index conversion with filters, comparators, string converter,
 *          row-change notifications, boundary exception matrix.
 * @run main TableRowSorterTest
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;
import javax.swing.RowSorter.SortKey;

public class TableRowSorterTest {
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

    static DefaultTableModel model() {
        return new DefaultTableModel(new Object[][]{{"b", 2}, {"a", 1}, {"c", 3}}, new Object[]{"S", "I"});
    }

    static String keys(TableRowSorter<?> rs) {
        StringBuilder s = new StringBuilder();
        for (SortKey k : rs.getSortKeys()) s.append(k.getColumn()).append(":").append(k.getSortOrder()).append(",");
        return s.toString();
    }

    static void testModelWiring() {
        DefaultTableModel model = model();
        TableRowSorter<DefaultTableModel> s = new TableRowSorter<DefaultTableModel>(model);
        check(s.getModel() == model, "model");
        check(s.getModelRowCount() == 3, "row count");
        check(s.getViewRowCount() == 3, "view row count unsorted");
        check(s.getSortKeys().isEmpty(), "no sort keys initially");
        check(s.getStringConverter() == null, "no string converter by default");
        // No-arg constructor leaves the model unset; setModel wires it later.
        TableRowSorter<DefaultTableModel> s2 = new TableRowSorter<DefaultTableModel>();
        check(s2.getModel() == null, "no-arg model is null");
        s2.setModel(model);
        check(s2.getModel() == model && s2.getModelRowCount() == 3, "setModel wiring");
        // setModel(null) is accepted.
        s2.setModel(null);
        check(s2.getModel() == null, "setModel(null) accepted");
    }

    static void testToggleCycle() {
        DefaultTableModel model = model();
        TableRowSorter<DefaultTableModel> s = new TableRowSorter<DefaultTableModel>(model);
        s.toggleSortOrder(0);
        check("0:ASCENDING,".equals(keys(s)), "first toggle ASCENDING on column 0");
        check(s.convertRowIndexToModel(0) == 1 && s.convertRowIndexToModel(1) == 0 && s.convertRowIndexToModel(2) == 2,
                "ascending mapping a,b,c");
        s.toggleSortOrder(0);
        check("0:DESCENDING,".equals(keys(s)), "second toggle DESCENDING");
        check(s.convertRowIndexToModel(0) == 2, "descending starts with c");
        s.toggleSortOrder(0);
        check("0:ASCENDING,".equals(keys(s)), "third toggle cycles back to ASCENDING (two-state cycle)");
        // Toggling another column promotes it to primary, demoting the previous key.
        s.toggleSortOrder(1);
        List<? extends SortKey> ks = s.getSortKeys();
        check(ks.size() == 2 && ks.get(0).getColumn() == 1 && ks.get(0).getSortOrder() == SortOrder.ASCENDING,
                "new column becomes primary");
        check(ks.get(1).getColumn() == 0, "old column demoted to secondary");
    }

    static void testSortKeys() {
        DefaultTableModel model = model();
        TableRowSorter<DefaultTableModel> s = new TableRowSorter<DefaultTableModel>(model);
        // Keys passed in are honoured verbatim (multi-key sort).
        List<SortKey> two = new ArrayList<SortKey>();
        two.add(new SortKey(1, SortOrder.ASCENDING));
        two.add(new SortKey(0, SortOrder.DESCENDING));
        s.setSortKeys(two);
        check(s.getSortKeys().size() == 2, "two keys installed");
        // rows: (b,2),(a,1),(c,3) sorted by column1 asc then column0 desc -> (a,1),(b,2),(c,3)
        check(s.convertRowIndexToModel(0) == 1 && s.convertRowIndexToModel(1) == 0 && s.convertRowIndexToModel(2) == 2,
                "multi-key sort result");
        // The returned list is an unmodifiable snapshot.
        List<? extends SortKey> got = s.getSortKeys();
        checkThrows(UnsupportedOperationException.class, "returned sort keys are unmodifiable",
                new ThrowingRunnable() { public void run() { got.clear(); } });
        check(s.getSortKeys().size() == 2, "sorter unaffected by external mutation attempt");
        // null clears to empty.
        s.setSortKeys(null);
        check(s.getSortKeys().isEmpty(), "setSortKeys(null) clears");
        // Empty list also unsorts and restores identity mapping.
        s.setSortKeys(new ArrayList<SortKey>());
        check(s.convertRowIndexToModel(0) == 0, "identity mapping with empty keys");
        // Validation.
        checkThrows(IllegalArgumentException.class, "setSortKeys invalid column",
                new ThrowingRunnable() { public void run() { s.setSortKeys(Collections.singletonList(new SortKey(9, SortOrder.ASCENDING))); } });
        List<SortKey> withNull = new ArrayList<SortKey>();
        withNull.add(null);
        checkThrows(IllegalArgumentException.class, "setSortKeys null element",
                new ThrowingRunnable() { public void run() { s.setSortKeys(withNull); } });
        checkThrows(IndexOutOfBoundsException.class, "toggleSortOrder invalid column",
                new ThrowingRunnable() { public void run() { s.toggleSortOrder(9); } });
        checkThrows(IndexOutOfBoundsException.class, "toggleSortOrder negative column",
                new ThrowingRunnable() { public void run() { s.toggleSortOrder(-1); } });
    }

    static void testFilters() {
        DefaultTableModel model = model();
        TableRowSorter<DefaultTableModel> s = new TableRowSorter<DefaultTableModel>(model);
        s.setRowFilter(RowFilter.regexFilter("a|c", 0));
        check(s.getViewRowCount() == 2, "filter keeps a and c");
        check(s.getModelRowCount() == 3, "model row count unchanged by filter");
        check(s.convertRowIndexToModel(0) == 1 && s.convertRowIndexToModel(1) == 2, "filtered mapping");
        check(s.convertRowIndexToView(1) == 0, "model->view of included row");
        check(s.convertRowIndexToView(0) == -1, "model->view of filtered-out row is -1");
        checkThrows(IndexOutOfBoundsException.class, "convertRowIndexToModel beyond view rows",
                new ThrowingRunnable() { public void run() { s.convertRowIndexToModel(99); } });
        s.setRowFilter(null);
        check(s.getViewRowCount() == 3, "removing filter restores view");
    }

    static void testComparatorsAndConverter() {
        DefaultTableModel model = new DefaultTableModel(new Object[][]{{"b", 3}, {"a", 1}, {"c", 2}}, new Object[]{"S", "I"}) {
            public Class<?> getColumnClass(int c) { return c == 1 ? Integer.class : String.class; }
        };
        TableRowSorter<DefaultTableModel> s = new TableRowSorter<DefaultTableModel>(model);
        check(s.getComparator(0) != null, "String column has a collator comparator");
        check(s.getComparator(1) != null && s.getComparator(1) != s.getComparator(0), "Integer column has its own comparator");
        // Numeric sort on the Integer column: 1,2,3 -> model rows 1,2,0.
        s.toggleSortOrder(1);
        check(s.convertRowIndexToModel(0) == 1 && s.convertRowIndexToModel(1) == 2 && s.convertRowIndexToModel(2) == 0,
                "numeric ascending order");
        // Custom comparator takes over.
        s.setComparator(1, new Comparator<Integer>() { public int compare(Integer x, Integer y) { return y - x; } });
        check(s.getComparator(1) != null, "custom comparator installed");
        s.toggleSortOrder(1); // now DESCENDING under reversed comparator = 1,2,3 again
        check(s.convertRowIndexToModel(0) == 1, "custom comparator drives order");
        s.setComparator(1, null);
        check(s.getComparator(1) != null, "null resets to default comparator");

        // String converter participates in sorting.
        final int[] calls = {0};
        final TableStringConverter conv = new TableStringConverter() {
            public String toString(TableModel m, int r, int c) { calls[0]++; return String.valueOf(m.getValueAt(r, c)); }
        };
        DefaultTableModel m2 = new DefaultTableModel(new Object[][]{{"b"}, {"a"}, {"c"}}, new Object[]{"S"});
        TableRowSorter<DefaultTableModel> s2 = new TableRowSorter<DefaultTableModel>(m2);
        s2.setStringConverter(conv);
        check(s2.getStringConverter() == conv, "string converter round-trip");
        int before = calls[0];
        s2.toggleSortOrder(0);
        check(calls[0] > before, "string converter consulted during sort");
        check(s2.convertRowIndexToModel(0) == 1, "converter-driven sort order");
        // Converter values also feed regex filters.
        s2.setRowFilter(RowFilter.regexFilter("a|c"));
        check(s2.getViewRowCount() == 2, "filter uses converter strings");
    }

    static void testRowChangeNotifications() {
        DefaultTableModel model = model();
        TableRowSorter<DefaultTableModel> s = new TableRowSorter<DefaultTableModel>(model);
        // Structure/data change entry points stay consistent with the model.
        model.addRow(new Object[]{"d", 4});
        s.rowsInserted(3, 3);
        check(s.getViewRowCount() == 4, "rowsInserted updates view count");
        model.removeRow(3);
        s.rowsDeleted(3, 3);
        check(s.getViewRowCount() == 3, "rowsDeleted updates view count");
        s.allRowsChanged();
        check(s.getViewRowCount() == 3, "allRowsChanged keeps count");
        s.modelStructureChanged();
        check(s.getViewRowCount() == 3 && s.getSortKeys().isEmpty(), "modelStructureChanged clears sort keys");
        s.toggleSortOrder(0);
        s.rowsUpdated(0, 0);
        check(s.getViewRowCount() == 3, "rowsUpdated keeps mapping valid");
        s.rowsUpdated(0, 0, 0);
        check(s.getViewRowCount() == 3, "rowsUpdated(col) keeps mapping valid");
        checkThrows(IndexOutOfBoundsException.class, "rowsUpdated invalid column",
                new ThrowingRunnable() { public void run() { s.rowsUpdated(0, 0, 9); } });
    }

    public static void main(String[] args) throws Exception {
        testModelWiring();
        testToggleCycle();
        testSortKeys();
        testFilters();
        testComparatorsAndConverter();
        testRowChangeNotifications();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
