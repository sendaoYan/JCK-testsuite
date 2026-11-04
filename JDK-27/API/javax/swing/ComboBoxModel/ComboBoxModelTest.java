/*
 * @test
 * @summary javax.swing.ComboBoxModel conformance: white-box structure + black-box behavior.
 * @run main ComboBoxModelTest
 */

import java.lang.reflect.Modifier;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

public class ComboBoxModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ComboBoxModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.ListModel"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // exercise the interface contract through a concrete implementation
        ComboBoxModel<String> m = new DefaultComboBoxModel<>(new String[]{"a", "b", "c"});

        check(m.getSize() == 3, "getSize");
        check("b".equals(m.getElementAt(1)), "getElementAt");
        check("a".equals(m.getSelectedItem()), "default selects first item");

        m.setSelectedItem("b");
        check("b".equals(m.getSelectedItem()), "setSelectedItem/getSelectedItem");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
