/*
 * @test
 * @summary javax.swing.RowSorter conformance: white-box structure + black-box behavior.
 * @run main RowSorterTest
 */

import java.lang.reflect.Modifier;

import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class RowSorterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.RowSorter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[][] { { "b" }, { "a" } }, new Object[] { "col" });
        RowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(model);
        check(sorter.getModel() == model, "getModel");
        check(sorter.getViewRowCount() == 2, "getViewRowCount");
        check(sorter.getModelRowCount() == 2, "getModelRowCount");

        sorter.toggleSortOrder(0);
        check(sorter.getSortKeys().size() == 1, "sort keys after toggle");
        check(sorter.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING, "ascending");
        check(sorter.convertRowIndexToModel(0) == 1, "convert view 0 to model (a at model 1)");
        check(sorter.convertRowIndexToView(1) == 0, "convert model 1 to view 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
