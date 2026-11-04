/*
 * @test
 * @summary javax.swing.DefaultRowSorter conformance: white-box structure + black-box behavior.
 * @run main DefaultRowSorterTest
 */

import java.lang.reflect.Modifier;

import javax.swing.DefaultRowSorter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class DefaultRowSorterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultRowSorter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.RowSorter"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // exercise inherited DefaultRowSorter behavior via a concrete subclass
        DefaultTableModel model = new DefaultTableModel(
                new Object[][]{{"b"}, {"a"}, {"c"}},
                new Object[]{"col"});
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

        check(sorter.getModel() == model, "getModel");
        check(sorter.getModelRowCount() == 3, "modelRowCount");
        check(sorter.getViewRowCount() == 3, "viewRowCount");
        check(sorter.getMaxSortKeys() == 3, "default maxSortKeys");
        check(!sorter.getSortsOnUpdates(), "default sortsOnUpdates false");
        check(sorter.isSortable(0), "sortable by default");

        sorter.setSortable(0, false);
        check(!sorter.isSortable(0), "setSortable(false)");
        sorter.setSortable(0, true);

        // toggle + sort reorders view->model mapping
        sorter.toggleSortOrder(0);
        sorter.sort();
        check(sorter.getSortKeys().size() == 1, "sort keys set");
        RowSorter.SortKey key = sorter.getSortKeys().get(0);
        check(key.getColumn() == 0 && key.getSortOrder() == SortOrder.ASCENDING, "ascending sort key");
        check("a".equals(model.getValueAt(sorter.convertRowIndexToModel(0), 0)), "first view row is smallest");

        // explicit sort keys + maxSortKeys
        sorter.setMaxSortKeys(1);
        check(sorter.getMaxSortKeys() == 1, "setMaxSortKeys");
        sorter.setSortsOnUpdates(false);
        check(!sorter.getSortsOnUpdates(), "setSortsOnUpdates(false)");

        // convert round-trip
        int view = sorter.convertRowIndexToView(0);
        check(sorter.convertRowIndexToModel(view) == 0, "convert round trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
