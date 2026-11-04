/*
 * @test
 * @summary sun.jvm.hotspot.debugger.LongHashMap conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger:+open
 * @run main LongHashMapTest
 */
import java.lang.reflect.*;

public class LongHashMapTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.LongHashMap", false, LongHashMapTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "size"), "method size");
        check(hasMethod(c, "put"), "method put");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "containsValue"), "method containsValue");
        check(hasMethod(c, "containsKey"), "method containsKey");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
