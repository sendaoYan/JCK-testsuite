/*
 * @test
 * @summary sun.jvm.hotspot.oops.TypeArray conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main TypeArrayTest
 */
import java.lang.reflect.*;

public class TypeArrayTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.TypeArray", false, TypeArrayTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.oops.Array".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getIntAt"), "method getIntAt");
        check(hasMethod(c, "getLongAt"), "method getLongAt");
        check(hasMethod(c, "getFloatAt"), "method getFloatAt");
        check(hasMethod(c, "getDoubleAt"), "method getDoubleAt");
        check(hasMethod(c, "iterateFields"), "method iterateFields");
        check(hasMethod(c, "printValueOn"), "method printValueOn");
        check(hasMethod(c, "isTypeArray"), "method isTypeArray");
        check(hasMethod(c, "getByteAt"), "method getByteAt");
        check(hasMethod(c, "getBooleanAt"), "method getBooleanAt");
        check(hasMethod(c, "getCharAt"), "method getCharAt");
        check(hasMethod(c, "getShortAt"), "method getShortAt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
