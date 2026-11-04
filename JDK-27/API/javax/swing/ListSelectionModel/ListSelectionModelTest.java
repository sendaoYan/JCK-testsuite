/*
 * @test
 * @summary javax.swing.ListSelectionModel conformance: white-box structure + black-box behavior.
 * @run main ListSelectionModelTest
 */

import java.lang.reflect.Modifier;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

public class ListSelectionModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ListSelectionModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(ListSelectionModel.SINGLE_SELECTION == 0, "SINGLE_SELECTION constant");
        check(ListSelectionModel.SINGLE_INTERVAL_SELECTION == 1, "SINGLE_INTERVAL_SELECTION constant");
        check(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION == 2, "MULTIPLE_INTERVAL_SELECTION constant");

        ListSelectionModel m = new DefaultListSelectionModel();
        check(m.getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, "default mode");
        m.setSelectionInterval(0, 2);
        check(m.isSelectedIndex(1), "isSelectedIndex");
        check(m.getMinSelectionIndex() == 0 && m.getMaxSelectionIndex() == 2, "min/max");
        m.clearSelection();
        check(m.isSelectionEmpty(), "isSelectionEmpty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
