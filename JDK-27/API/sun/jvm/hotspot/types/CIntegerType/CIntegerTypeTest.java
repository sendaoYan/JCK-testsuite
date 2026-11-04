/*
 * @test
 * @summary sun.jvm.hotspot.types.CIntegerType conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.types:+open
 * @run main CIntegerTypeTest
 */
import java.lang.reflect.*;

public class CIntegerTypeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.types.CIntegerType", false, CIntegerTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.types.Type"), "interface sun.jvm.hotspot.types.Type");
        check(hasMethod(c, "maxValue"), "method maxValue");
        check(hasMethod(c, "isUnsigned"), "method isUnsigned");
        check(hasMethod(c, "minValue"), "method minValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
