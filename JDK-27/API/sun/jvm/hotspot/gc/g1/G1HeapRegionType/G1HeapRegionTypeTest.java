/*
 * @test
 * @summary sun.jvm.hotspot.gc.g1.G1HeapRegionType conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.g1:+open
 * @run main G1HeapRegionTypeTest
 */
import java.lang.reflect.*;

public class G1HeapRegionTypeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.g1.G1HeapRegionType", false, G1HeapRegionTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "isFree"), "method isFree");
        check(hasMethod(c, "isYoung"), "method isYoung");
        check(hasMethod(c, "isHumongous"), "method isHumongous");
        check(hasMethod(c, "isOld"), "method isOld");
        check(hasMethod(c, "typeAnnotation"), "method typeAnnotation");
        check(hasMethod(c, "isEden"), "method isEden");
        check(hasMethod(c, "isSurvivor"), "method isSurvivor");
        check(hasMethod(c, "isStartsHumongous"), "method isStartsHumongous");
        check(hasMethod(c, "isContinuesHumongous"), "method isContinuesHumongous");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
