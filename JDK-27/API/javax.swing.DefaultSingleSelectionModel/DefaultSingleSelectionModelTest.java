/*
 * @test
 * @summary javax.swing.DefaultSingleSelectionModel conformance: white-box structure + black-box behavior.
 * @run main DefaultSingleSelectionModelTest
 */

import java.lang.reflect.Modifier;

import javax.swing.DefaultSingleSelectionModel;
import javax.swing.event.ChangeListener;

public class DefaultSingleSelectionModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultSingleSelectionModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.SingleSelectionModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultSingleSelectionModel m = new DefaultSingleSelectionModel();
        check(m.getSelectedIndex() == -1, "default no selection");
        check(!m.isSelected(), "default not selected");

        m.setSelectedIndex(3);
        check(m.getSelectedIndex() == 3, "setSelectedIndex");
        check(m.isSelected(), "isSelected after set");

        final int[] fired = {0};
        ChangeListener l = e -> fired[0]++;
        m.addChangeListener(l);
        m.setSelectedIndex(5);
        check(fired[0] >= 1, "change fired on selection change");

        m.clearSelection();
        check(m.getSelectedIndex() == -1, "clearSelection");
        check(!m.isSelected(), "not selected after clear");
        check(m.getChangeListeners().length == 1, "listener count");
        m.removeChangeListener(l);
        check(m.getChangeListeners().length == 0, "listener removed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
