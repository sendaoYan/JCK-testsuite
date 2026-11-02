/*
 * @test
 * @summary sun.jvm.hotspot.gc.g1.G1HeapRegionClosure conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.g1:+open
 * @run main G1HeapRegionClosureTest
 */
import java.lang.reflect.*;

public class G1HeapRegionClosureTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.g1.G1HeapRegionClosure", false, G1HeapRegionClosureTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "doHeapRegion"), "method doHeapRegion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
