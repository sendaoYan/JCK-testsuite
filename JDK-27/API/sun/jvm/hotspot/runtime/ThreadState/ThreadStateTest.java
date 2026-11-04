/*
 * @test
 * @summary sun.jvm.hotspot.runtime.ThreadState conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main ThreadStateTest
 */
import java.lang.reflect.*;

public class ThreadStateTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.ThreadState", false, ThreadStateTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getPrintVal"), "method getPrintVal");
        check(hasField(c, "ALLOCATED"), "field ALLOCATED");
        check(hasField(c, "INITIALIZED"), "field INITIALIZED");
        check(hasField(c, "RUNNABLE"), "field RUNNABLE");
        check(hasField(c, "MONITOR_WAIT"), "field MONITOR_WAIT");
        check(hasField(c, "CONDVAR_WAIT"), "field CONDVAR_WAIT");
        check(hasField(c, "OBJECT_WAIT"), "field OBJECT_WAIT");
        check(hasField(c, "BREAKPOINTED"), "field BREAKPOINTED");
        check(hasField(c, "SLEEPING"), "field SLEEPING");
        check(hasField(c, "ZOMBIE"), "field ZOMBIE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
