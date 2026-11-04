/*
 * @test
 * @summary sun.jvm.hotspot.runtime.Threads conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main ThreadsTest
 */
import java.lang.reflect.*;

public class ThreadsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.Threads", false, ThreadsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getWaitingThreads"), "method getWaitingThreads");
        check(hasMethod(c, "owningThreadFromMonitor"), "method owningThreadFromMonitor");
        check(hasMethod(c, "oopsDo"), "method oopsDo");
        check(hasMethod(c, "getNumberOfThreads"), "method getNumberOfThreads");
        check(hasMethod(c, "getJavaThreadAt"), "method getJavaThreadAt");
        check(hasMethod(c, "createJavaThreadWrapper"), "method createJavaThreadWrapper");
        check(hasMethod(c, "getPendingThreads"), "method getPendingThreads");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
