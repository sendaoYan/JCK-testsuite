/*
 * @test
 * @summary javax.swing.GroupLayout.ParallelGroup conformance: type shape via reflection (headless-safe).
 * @run main ParallelGroupTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ParallelGroupTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.GroupLayout$ParallelGroup", false, ParallelGroupTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.GroupLayout$Group"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addGroup"), "method addGroup");
        check(names.contains("addComponent"), "method addComponent");
        check(names.contains("addComponent"), "method addComponent");
        check(names.contains("addGap"), "method addGap");
        check(names.contains("addGap"), "method addGap");
        check(names.contains("addGroup"), "method addGroup");
        check(names.contains("addComponent"), "method addComponent");
        check(names.contains("addComponent"), "method addComponent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
