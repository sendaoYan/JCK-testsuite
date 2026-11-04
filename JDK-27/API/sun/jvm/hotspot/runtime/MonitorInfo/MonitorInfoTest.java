/*
 * @test
 * @summary sun.jvm.hotspot.runtime.MonitorInfo conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main MonitorInfoTest
 */
import java.lang.reflect.*;

public class MonitorInfoTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.MonitorInfo", false, MonitorInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "lock"), "method lock");
        check(hasMethod(c, "owner"), "method owner");
        check(hasMethod(c, "eliminated"), "method eliminated");
        check(hasMethod(c, "ownerIsScalarReplaced"), "method ownerIsScalarReplaced");
        check(hasMethod(c, "ownerKlass"), "method ownerKlass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
