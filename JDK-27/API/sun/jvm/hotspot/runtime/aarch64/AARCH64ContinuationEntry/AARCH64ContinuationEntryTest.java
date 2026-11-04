/*
 * @test
 * @summary sun.jvm.hotspot.runtime.aarch64.AARCH64ContinuationEntry conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime.aarch64:+open
 * @run main AARCH64ContinuationEntryTest
 */
import java.lang.reflect.*;

public class AARCH64ContinuationEntryTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.aarch64.AARCH64ContinuationEntry", false, AARCH64ContinuationEntryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.ContinuationEntry".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toFrame"), "method toFrame");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
