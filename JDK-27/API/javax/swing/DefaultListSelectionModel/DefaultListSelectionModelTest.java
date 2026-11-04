/*
 * @test
 * @summary javax.swing.DefaultListSelectionModel conformance: white-box structure + black-box behavior.
 * @run main DefaultListSelectionModelTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

public class DefaultListSelectionModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultListSelectionModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.ListSelectionModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.lang.Cloneable"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.io.Serializable"), "interface 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DefaultListSelectionModel m = new DefaultListSelectionModel();
        check(m.isSelectionEmpty(), "empty initially");
        check(m.getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, "default mode");

        m.setSelectionInterval(2, 4);
        check(m.getMinSelectionIndex() == 2, "min selection");
        check(m.getMaxSelectionIndex() == 4, "max selection");
        check(m.isSelectedIndex(3), "selected middle");
        check(!m.isSelectedIndex(5), "not selected 5");
        check(Arrays.equals(new int[]{2, 3, 4}, m.getSelectedIndices()), "getSelectedIndices");
        check(m.getSelectedItemsCount() == 3, "getSelectedItemsCount");

        // anchor/lead
        m.setAnchorSelectionIndex(1);
        m.setLeadSelectionIndex(3);
        check(m.getAnchorSelectionIndex() == 1 && m.getLeadSelectionIndex() == 3, "anchor/lead");

        // interval add/remove
        m.addSelectionInterval(10, 11);
        check(m.getMaxSelectionIndex() == 11, "addSelectionInterval");
        m.removeSelectionInterval(2, 4);
        check(!m.isSelectedIndex(2), "removeSelectionInterval");

        // value adjusting + listener
        m.setValueIsAdjusting(true);
        check(m.getValueIsAdjusting(), "setValueIsAdjusting");
        final int[] fired = {0};
        m.addListSelectionListener(e -> fired[0]++);
        m.clearSelection();
        check(m.isSelectionEmpty(), "clearSelection");
        check(fired[0] >= 1, "selection listener fired");

        // clone is an independent copy
        DefaultListSelectionModel clone = (DefaultListSelectionModel) m.clone();
        check(clone != m && clone.isSelectionEmpty(), "clone independent");

        // single selection mode
        m.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        check(m.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION, "single selection mode");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
