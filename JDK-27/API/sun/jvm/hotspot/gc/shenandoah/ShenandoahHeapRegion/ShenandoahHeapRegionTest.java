/*
 * @test
 * @summary sun.jvm.hotspot.gc.shenandoah.ShenandoahHeapRegion conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.shenandoah:+open
 * @run main ShenandoahHeapRegionTest
 */
import java.lang.reflect.*;

public class ShenandoahHeapRegionTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.shenandoah.ShenandoahHeapRegion", false, ShenandoahHeapRegionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.gc.shared.LiveRegionsProvider"), "interface sun.jvm.hotspot.gc.shared.LiveRegionsProvider");
        check(hasMethod(c, "index"), "method index");
        check(hasMethod(c, "bottom"), "method bottom");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "end"), "method end");
        check(hasMethod(c, "top"), "method top");
        check(hasMethod(c, "getLiveRegions"), "method getLiveRegions");
        check(hasMethod(c, "regionSizeBytes"), "method regionSizeBytes");
        check(hasMethod(c, "regionSizeBytesShift"), "method regionSizeBytesShift");
        check(hasMethod(c, "setHeap"), "method setHeap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
