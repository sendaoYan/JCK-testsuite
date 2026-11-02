/*
 * @test
 * @summary javax.swing.plaf.metal.MetalScrollButton conformance: type shape via reflection (headless-safe).
 * @run main MetalScrollButtonTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MetalScrollButtonTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalScrollButton", false, MetalScrollButtonTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicArrowButton"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setFreeStanding"), "method setFreeStanding");
        check(names.contains("paint"), "method paint");
        check(names.contains("getPreferredSize"), "method getPreferredSize");
        check(names.contains("getMinimumSize"), "method getMinimumSize");
        check(names.contains("getMaximumSize"), "method getMaximumSize");
        check(names.contains("getButtonWidth"), "method getButtonWidth");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
