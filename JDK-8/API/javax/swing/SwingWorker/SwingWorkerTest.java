/*
 * @test
 * @summary javax.swing.SwingWorker conformance: type shape via reflection (headless-safe).
 * @run main SwingWorkerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SwingWorkerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.SwingWorker", false, SwingWorkerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.util.concurrent.RunnableFuture"), "implements java.util.concurrent.RunnableFuture");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("run"), "method run");
        check(names.contains("getProgress"), "method getProgress");
        check(names.contains("execute"), "method execute");
        check(names.contains("cancel"), "method cancel");
        check(names.contains("isCancelled"), "method isCancelled");
        check(names.contains("isDone"), "method isDone");
        check(names.contains("get"), "method get");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
