/*
 * @test
 * @summary javax.swing.BoundedRangeModel conformance: type shape via reflection (headless-safe).
 * @run main BoundedRangeModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BoundedRangeModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.BoundedRangeModel", false, BoundedRangeModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMinimum"), "method getMinimum");
        check(names.contains("setMinimum"), "method setMinimum");
        check(names.contains("getMaximum"), "method getMaximum");
        check(names.contains("setMaximum"), "method setMaximum");
        check(names.contains("getValue"), "method getValue");
        check(names.contains("setValue"), "method setValue");
        check(names.contains("setValueIsAdjusting"), "method setValueIsAdjusting");
        check(names.contains("getValueIsAdjusting"), "method getValueIsAdjusting");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
