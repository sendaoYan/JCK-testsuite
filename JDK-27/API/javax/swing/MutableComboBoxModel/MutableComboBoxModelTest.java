/*
 * @test
 * @summary javax.swing.MutableComboBoxModel conformance: white-box structure + black-box behavior.
 * @run main MutableComboBoxModelTest
 */

import java.lang.reflect.Modifier;

import javax.swing.DefaultComboBoxModel;
import javax.swing.MutableComboBoxModel;

public class MutableComboBoxModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.MutableComboBoxModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.ComboBoxModel"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        MutableComboBoxModel<String> m = new DefaultComboBoxModel<String>();
        m.addElement("a");
        check(m.getSize() == 1, "addElement");
        m.insertElementAt("b", 0);
        check("b".equals(m.getElementAt(0)), "insertElementAt");
        m.removeElement("a");
        check(m.getSize() == 1, "removeElement");
        m.removeElementAt(0);
        check(m.getSize() == 0, "removeElementAt");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
