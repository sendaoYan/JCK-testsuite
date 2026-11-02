/*
 * @test
 * @summary sun.jvm.hotspot.gc.z.ZCollectedHeap conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.z:+open
 * @run main ZCollectedHeapTest
 */
import java.lang.reflect.*;

public class ZCollectedHeapTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.z.ZCollectedHeap", false, ZCollectedHeapTest.class.getClassLoader());
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
        check(hasMethod(c, "heap"), "method heap");
        check(hasMethod(c, "oopAddressDescription"), "method oopAddressDescription");
        check(hasMethod(c, "liveRegionsIterate"), "method liveRegionsIterate");
        check(hasMethod(c, "isInReserved"), "method isInReserved");
        check(hasMethod(c, "oop_load_at"), "method oop_load_at");
        check(hasMethod(c, "oop_load_in_native"), "method oop_load_in_native");
        check(hasMethod(c, "createBitMap"), "method createBitMap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
