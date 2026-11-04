/*
 * @test
 * @summary javax.swing.table.TableRowSorter conformance: white-box structure + black-box behavior.
 * @run main TableRowSorterTest
 */

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;

public class TableRowSorterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static DefaultTableModel intModel() {
        return new DefaultTableModel(
                new Object[][]{{3, "c"}, {1, "a"}, {2, "b"}},
                new Object[]{"num", "name"});
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.TableRowSorter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.DefaultRowSorter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultTableModel model = intModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        check(sorter.getModel() == model, "constructor sets model");
        check(sorter.getModelRowCount() == 3, "getModelRowCount 3");
        check(sorter.getViewRowCount() == 3, "getViewRowCount 3 (unsorted)");

        // force numeric ordering on Integer column to avoid locale string collation
        sorter.setComparator(0, Comparator.naturalOrder());
        check(sorter.getComparator(0) != null, "getComparator non-null after setComparator");

        // setSortKeys + sort -> ascending by Integer column
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        sorter.sort();
        check(sorter.getViewRowCount() == 3, "view row count after sort");
        check(sorter.convertRowIndexToModel(0) == 1, "ascending: view0 -> model1 (value 1)");
        check(sorter.convertRowIndexToModel(1) == 2, "ascending: view1 -> model2 (value 2)");
        check(sorter.convertRowIndexToModel(2) == 0, "ascending: view2 -> model0 (value 3)");
        check(Integer.valueOf(1).equals(model.getValueAt(sorter.convertRowIndexToModel(0), 0)), "ascending: value at view0 == 1");
        check(Integer.valueOf(3).equals(model.getValueAt(sorter.convertRowIndexToModel(2), 0)), "ascending: value at view2 == 3");

        // view <-> model round-trip after sorting
        for (int i = 0; i < sorter.getViewRowCount(); i++) {
            int modelIdx = sorter.convertRowIndexToModel(i);
            check(sorter.convertRowIndexToView(modelIdx) == i, "round-trip viewToModel(modelToView(" + i + ")) == " + i);
        }

        // getSortKeys reflects sort key
        List<? extends RowSorter.SortKey> keys = sorter.getSortKeys();
        check(keys.size() == 1, "getSortKeys size 1");
        check(keys.get(0).getColumn() == 0 && keys.get(0).getSortOrder() == SortOrder.ASCENDING, "getSortKeys column 0 ASCENDING");

        // toggleSortOrder -> DESCENDING
        sorter.toggleSortOrder(0);
        sorter.sort();
        check(sorter.convertRowIndexToModel(0) == 0, "descending: view0 -> model0 (value 3)");
        check(sorter.convertRowIndexToModel(2) == 1, "descending: view2 -> model1 (value 1)");
        check(sorter.getSortKeys().get(0).getSortOrder() == SortOrder.DESCENDING, "toggleSortOrder -> DESCENDING");

        // sortable
        check(sorter.isSortable(0), "isSortable default true");
        sorter.setSortable(0, false);
        check(!sorter.isSortable(0), "setSortable false");

        // string converter round-trip
        TableStringConverter conv = new TableStringConverter() {
            public String toString(javax.swing.table.TableModel m, int r, int c) {
                Object v = m.getValueAt(r, c);
                return v == null ? "" : v.toString();
            }
        };
        sorter.setStringConverter(conv);
        check(sorter.getStringConverter() == conv, "set/get stringConverter round-trip");

        // filtering on a fresh sorter
        DefaultTableModel fm = intModel();
        TableRowSorter<DefaultTableModel> fs = new TableRowSorter<>();
        fs.setModel(fm);
        check(fs.getModel() == fm, "setModel round-trip");
        fs.setComparator(0, Comparator.naturalOrder());
        fs.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        fs.setRowFilter(RowFilter.regexFilter("1|3", 0));
        fs.sort();
        check(fs.getModelRowCount() == 3, "filtered: model row count 3");
        check(fs.getViewRowCount() == 2, "filtered: view row count 2");
        check(Integer.valueOf(1).equals(fm.getValueAt(fs.convertRowIndexToModel(0), 0)), "filtered: view0 value 1");
        check(Integer.valueOf(3).equals(fm.getValueAt(fs.convertRowIndexToModel(1), 0)), "filtered: view1 value 3");
        check(fs.convertRowIndexToView(2) == -1, "filtered-out model row -> view -1");
        check(fs.getRowFilter() != null, "getRowFilter non-null");

        // sortable / max sort keys on DefaultRowSorter base
        check(sorter.getMaxSortKeys() > 0, "getMaxSortKeys positive");
        sorter.setMaxSortKeys(5);
        check(sorter.getMaxSortKeys() == 5, "set/get maxSortKeys");
        sorter.setSortsOnUpdates(true);
        check(sorter.getSortsOnUpdates(), "set/get sortsOnUpdates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
