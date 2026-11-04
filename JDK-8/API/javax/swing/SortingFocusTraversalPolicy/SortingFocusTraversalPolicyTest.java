/*
 * @test
 * @summary javax.swing.SortingFocusTraversalPolicy conformance: type shape via reflection (headless-safe).
 * @run main SortingFocusTraversalPolicyTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SortingFocusTraversalPolicyTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.SortingFocusTraversalPolicy", false, SortingFocusTraversalPolicyTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.InternalFrameFocusTraversalPolicy"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getComponentAfter"), "method getComponentAfter");
        check(names.contains("getComponentBefore"), "method getComponentBefore");
        check(names.contains("getFirstComponent"), "method getFirstComponent");
        check(names.contains("getLastComponent"), "method getLastComponent");
        check(names.contains("getDefaultComponent"), "method getDefaultComponent");
        check(names.contains("setImplicitDownCycleTraversal"), "method setImplicitDownCycleTraversal");
        check(names.contains("getImplicitDownCycleTraversal"), "method getImplicitDownCycleTraversal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
