/*
 * @test
 * @summary sun.jvm.hotspot.debugger.windbg.aarch64.WindbgAARCH64ThreadFactory conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.windbg.aarch64:+open
 * @run main WindbgAARCH64ThreadFactoryTest
 */
import java.lang.reflect.*;

public class WindbgAARCH64ThreadFactoryTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.windbg.aarch64.WindbgAARCH64ThreadFactory", false, WindbgAARCH64ThreadFactoryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.windbg.WindbgThreadFactory"), "interface sun.jvm.hotspot.debugger.windbg.WindbgThreadFactory");
        check(hasMethod(c, "createThreadWrapper"), "method createThreadWrapper");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
