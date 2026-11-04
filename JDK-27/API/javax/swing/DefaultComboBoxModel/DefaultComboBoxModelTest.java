/*
 * @test
 * @summary javax.swing.DefaultComboBoxModel conformance: white-box structure + black-box behavior.
 * @run main DefaultComboBoxModelTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;

public class DefaultComboBoxModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultComboBoxModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractListModel"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.MutableComboBoxModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>(new String[]{"a", "b", "c"});
        check(m.getSize() == 3, "array ctor size");
        check("b".equals(m.getElementAt(1)), "getElementAt");
        check(m.getIndexOf("c") == 2, "getIndexOf");

        m.setSelectedItem("b");
        check("b".equals(m.getSelectedItem()), "setSelectedItem");

        // mutations
        m.addElement("d");
        check(m.getSize() == 4 && "d".equals(m.getElementAt(3)), "addElement");
        m.insertElementAt("x", 0);
        check("x".equals(m.getElementAt(0)), "insertElementAt");
        m.removeElement("b");
        check(m.getIndexOf("b") == -1, "removeElement");
        m.removeElementAt(0);
        check(!"x".equals(m.getElementAt(0)), "removeElementAt");

        // vector ctor + addAll
        DefaultComboBoxModel<Integer> v = new DefaultComboBoxModel<>(new java.util.Vector<>(Arrays.asList(1, 2)));
        v.addAll(Arrays.asList(3, 4));
        check(v.getSize() == 4, "addAll");

        // empty ctor starts empty
        DefaultComboBoxModel<String> e = new DefaultComboBoxModel<>();
        check(e.getSize() == 0 && e.getSelectedItem() == null, "empty ctor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
