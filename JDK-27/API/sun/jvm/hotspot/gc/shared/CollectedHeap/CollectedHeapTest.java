/*
 * @test
 * @summary sun.jvm.hotspot.gc.shared.CollectedHeap conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.shared:+open
 * @run main CollectedHeapTest
 */
import java.lang.reflect.*;

public class CollectedHeapTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.shared.CollectedHeap", false, CollectedHeapTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "kind"), "method kind");
        check(hasMethod(c, "start"), "method start");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "capacity"), "method capacity");
        check(hasMethod(c, "used"), "method used");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "oopAddressDescription"), "method oopAddressDescription");
        check(hasMethod(c, "isIn"), "method isIn");
        check(hasMethod(c, "reservedRegion"), "method reservedRegion");
        check(hasMethod(c, "liveRegionsIterate"), "method liveRegionsIterate");
        check(hasMethod(c, "isInReserved"), "method isInReserved");
        check(hasMethod(c, "oop_load_at"), "method oop_load_at");
        check(hasMethod(c, "oop_load_in_native"), "method oop_load_in_native");
        check(hasMethod(c, "createBitMap"), "method createBitMap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
