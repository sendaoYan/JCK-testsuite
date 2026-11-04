/*
 * @test
 * @summary sun.jvm.hotspot.gc.shenandoah.ShenandoahBitMap conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.shenandoah:+open
 * @run main ShenandoahBitMapTest
 */
import java.lang.reflect.*;

public class ShenandoahBitMapTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.shenandoah.ShenandoahBitMap", false, ShenandoahBitMapTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.utilities.BitMapInterface"), "interface sun.jvm.hotspot.utilities.BitMapInterface");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "at"), "method at");
        check(hasMethod(c, "atPut"), "method atPut");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
