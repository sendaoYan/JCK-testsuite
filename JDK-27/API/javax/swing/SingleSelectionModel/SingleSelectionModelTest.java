/*
 * @test
 * @summary javax.swing.SingleSelectionModel conformance: white-box structure + black-box behavior.
 * @run main SingleSelectionModelTest
 */

import java.lang.reflect.Modifier;

import javax.swing.DefaultSingleSelectionModel;
import javax.swing.SingleSelectionModel;

public class SingleSelectionModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SingleSelectionModel");
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
        SingleSelectionModel m = new DefaultSingleSelectionModel();
        check(m.getSelectedIndex() == -1, "initial -1");
        m.setSelectedIndex(3);
        check(m.getSelectedIndex() == 3, "setSelectedIndex");
        check(m.isSelected(), "isSelected");
        m.clearSelection();
        check(!m.isSelected(), "cleared");
        check(m.getSelectedIndex() == -1, "cleared index");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
