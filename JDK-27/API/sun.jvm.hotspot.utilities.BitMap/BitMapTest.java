/*
 * @test
 * @summary sun.jvm.hotspot.utilities.BitMap conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main BitMapTest
 */
import java.lang.reflect.*;

public class BitMapTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.BitMap", false, BitMapTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "size"), "method size");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "at"), "method at");
        check(hasMethod(c, "iterate"), "method iterate");
        check(hasMethod(c, "atPut"), "method atPut");
        check(hasMethod(c, "set_size"), "method set_size");
        check(hasMethod(c, "set_map"), "method set_map");
        check(hasMethod(c, "setUnion"), "method setUnion");
        check(hasMethod(c, "setIntersection"), "method setIntersection");
        check(hasMethod(c, "setFrom"), "method setFrom");
        check(hasMethod(c, "setDifference"), "method setDifference");
        check(hasMethod(c, "isSame"), "method isSame");
        check(hasMethod(c, "getNextOneOffset"), "method getNextOneOffset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
