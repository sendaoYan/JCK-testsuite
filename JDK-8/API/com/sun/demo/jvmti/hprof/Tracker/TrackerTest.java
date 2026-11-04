/*
 * @test
 * @summary com.sun.demo.jvmti.hprof.Tracker conformance: type shape via reflection (headless-safe).
 * @run main TrackerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TrackerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.demo.jvmti.hprof.Tracker", false, TrackerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.demo.jvmti.hprof.Tracker"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("ObjectInit"), "method ObjectInit");
        check(names.contains("NewArray"), "method NewArray");
        check(names.contains("CallSite"), "method CallSite");
        check(names.contains("ReturnSite"), "method ReturnSite");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
