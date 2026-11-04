/*
 * @test
 * @summary sun.jvm.hotspot.gc.shenandoah.ShenandoahHeap conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.shenandoah:+open
 * @run main ShenandoahHeapTest
 */
import java.lang.reflect.*;

public class ShenandoahHeapTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.shenandoah.ShenandoahHeap", false, ShenandoahHeapTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.gc.shared.CollectedHeap".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "kind"), "method kind");
        check(hasMethod(c, "capacity"), "method capacity");
        check(hasMethod(c, "getRegion"), "method getRegion");
        check(hasMethod(c, "used"), "method used");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "liveRegionsIterate"), "method liveRegionsIterate");
        check(hasMethod(c, "createBitMap"), "method createBitMap");
        check(hasMethod(c, "regionAtOffset"), "method regionAtOffset");
        check(hasMethod(c, "getLogMinObjAlignmentInBytes"), "method getLogMinObjAlignmentInBytes");
        check(hasMethod(c, "committed"), "method committed");
        check(hasMethod(c, "numOfRegions"), "method numOfRegions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
