/*
 * @test
 * @summary com.sun.java.swing.SwingUtilities3 conformance: type shape via reflection (headless-safe).
 * @run main SwingUtilities3Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SwingUtilities3Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.SwingUtilities3", false, SwingUtilities3Test.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.swing.SwingUtilities3"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("setDelegateRepaintManager"), "method setDelegateRepaintManager");
        check(names.contains("setVsyncRequested"), "method setVsyncRequested");
        check(names.contains("isVsyncRequested"), "method isVsyncRequested");
        check(names.contains("getDelegateRepaintManager"), "method getDelegateRepaintManager");
        check(names.contains("setEventQueueDelegate"), "method setEventQueueDelegate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
