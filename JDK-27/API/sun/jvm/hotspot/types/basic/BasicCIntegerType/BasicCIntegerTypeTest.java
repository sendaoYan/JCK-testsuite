/*
 * @test
 * @summary sun.jvm.hotspot.types.basic.BasicCIntegerType conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.types.basic:+open
 * @run main BasicCIntegerTypeTest
 */
import java.lang.reflect.*;

public class BasicCIntegerTypeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.types.basic.BasicCIntegerType", false, BasicCIntegerTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.types.basic.BasicType".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.types.CIntegerType"), "interface sun.jvm.hotspot.types.CIntegerType");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "maxValue"), "method maxValue");
        check(hasMethod(c, "isUnsigned"), "method isUnsigned");
        check(hasMethod(c, "isCIntegerType"), "method isCIntegerType");
        check(hasMethod(c, "setIsUnsigned"), "method setIsUnsigned");
        check(hasMethod(c, "minValue"), "method minValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
