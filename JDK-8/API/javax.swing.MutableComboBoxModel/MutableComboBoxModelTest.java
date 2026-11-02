/*
 * @test
 * @summary javax.swing.MutableComboBoxModel conformance: type shape via reflection (headless-safe).
 * @run main MutableComboBoxModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MutableComboBoxModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.MutableComboBoxModel", false, MutableComboBoxModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "javax.swing.ComboBoxModel"), "implements javax.swing.ComboBoxModel");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addElement"), "method addElement");
        check(names.contains("removeElement"), "method removeElement");
        check(names.contains("insertElementAt"), "method insertElementAt");
        check(names.contains("removeElementAt"), "method removeElementAt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
