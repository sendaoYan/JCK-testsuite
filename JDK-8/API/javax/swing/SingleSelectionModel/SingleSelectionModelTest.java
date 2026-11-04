/*
 * @test
 * @summary javax.swing.SingleSelectionModel conformance: type shape via reflection (headless-safe).
 * @run main SingleSelectionModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SingleSelectionModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.SingleSelectionModel", false, SingleSelectionModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getSelectedIndex"), "method getSelectedIndex");
        check(names.contains("setSelectedIndex"), "method setSelectedIndex");
        check(names.contains("clearSelection"), "method clearSelection");
        check(names.contains("isSelected"), "method isSelected");
        check(names.contains("addChangeListener"), "method addChangeListener");
        check(names.contains("removeChangeListener"), "method removeChangeListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
