/*
 * @test
 * @summary sun.jvm.hotspot.runtime.PerfMemory conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main PerfMemoryTest
 */
import java.lang.reflect.*;

public class PerfMemoryTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.PerfMemory", false, PerfMemoryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "end"), "method end");
        check(hasMethod(c, "start"), "method start");
        check(hasMethod(c, "capacity"), "method capacity");
        check(hasMethod(c, "top"), "method top");
        check(hasMethod(c, "initialized"), "method initialized");
        check(hasMethod(c, "iterate"), "method iterate");
        check(hasMethod(c, "prologue"), "method prologue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
