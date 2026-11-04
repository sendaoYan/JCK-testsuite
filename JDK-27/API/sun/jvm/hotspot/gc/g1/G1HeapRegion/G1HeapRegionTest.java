/*
 * @test
 * @summary sun.jvm.hotspot.gc.g1.G1HeapRegion conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.g1:+open
 * @run main G1HeapRegionTest
 */
import java.lang.reflect.*;

public class G1HeapRegionTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.g1.G1HeapRegion", false, G1HeapRegionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.gc.shared.ContiguousSpace".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.gc.shared.LiveRegionsProvider"), "interface sun.jvm.hotspot.gc.shared.LiveRegionsProvider");
        check(hasMethod(c, "bottom"), "method bottom");
        check(hasMethod(c, "end"), "method end");
        check(hasMethod(c, "isPinned"), "method isPinned");
        check(hasMethod(c, "top"), "method top");
        check(hasMethod(c, "free"), "method free");
        check(hasMethod(c, "used"), "method used");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "getLiveRegions"), "method getLiveRegions");
        check(hasMethod(c, "usedRegion"), "method usedRegion");
        check(hasMethod(c, "isInRegion"), "method isInRegion");
        check(hasMethod(c, "grainBytes"), "method grainBytes");
        check(hasMethod(c, "isFree"), "method isFree");
        check(hasMethod(c, "isYoung"), "method isYoung");
        check(hasMethod(c, "isHumongous"), "method isHumongous");
        check(hasMethod(c, "isOld"), "method isOld");
        check(hasMethod(c, "getPointerSize"), "method getPointerSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
