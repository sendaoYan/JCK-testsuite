/*
 * @test
 * @summary javax.swing.RepaintManager conformance: type shape via reflection (headless-safe).
 * @run main RepaintManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RepaintManagerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.RepaintManager", false, RepaintManagerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("currentManager"), "method currentManager");
        check(names.contains("currentManager"), "method currentManager");
        check(names.contains("setCurrentManager"), "method setCurrentManager");
        check(names.contains("addInvalidComponent"), "method addInvalidComponent");
        check(names.contains("removeInvalidComponent"), "method removeInvalidComponent");
        check(names.contains("addDirtyRegion"), "method addDirtyRegion");
        check(names.contains("addDirtyRegion"), "method addDirtyRegion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
