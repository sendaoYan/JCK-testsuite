/*
 * @test
 * @summary sun.jvm.hotspot.gc.g1.G1CollectedHeap conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.g1:+open
 * @run main G1CollectedHeapTest
 */
import java.lang.reflect.*;

public class G1CollectedHeapTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.g1.G1CollectedHeap", false, G1CollectedHeapTest.class.getClassLoader());
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
        check(hasMethod(c, "used"), "method used");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "liveRegionsIterate"), "method liveRegionsIterate");
        check(hasMethod(c, "hrm"), "method hrm");
        check(hasMethod(c, "heapRegionIterate"), "method heapRegionIterate");
        check(hasMethod(c, "n_regions"), "method n_regions");
        check(hasMethod(c, "monitoringSupport"), "method monitoringSupport");
        check(hasMethod(c, "oldSet"), "method oldSet");
        check(hasMethod(c, "humongousSet"), "method humongousSet");
        check(hasMethod(c, "heapRegionForAddress"), "method heapRegionForAddress");
        check(hasMethod(c, "printRegionDetails"), "method printRegionDetails");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
