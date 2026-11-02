/*
 * @test
 * @summary sun.jvm.hotspot.gc.shared.CollectedHeapName conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.shared:+open
 * @run main CollectedHeapNameTest
 */
import java.lang.reflect.*;

public class CollectedHeapNameTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.shared.CollectedHeapName", false, CollectedHeapNameTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "SERIAL"), "field SERIAL");
        check(hasField(c, "PARALLEL"), "field PARALLEL");
        check(hasField(c, "G1"), "field G1");
        check(hasField(c, "EPSILON"), "field EPSILON");
        check(hasField(c, "Z"), "field Z");
        check(hasField(c, "SHENANDOAH"), "field SHENANDOAH");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
