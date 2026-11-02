/*
 * @test
 * @summary sun.jvm.hotspot.oops.FieldType conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main FieldTypeTest
 */
import java.lang.reflect.*;

public class FieldTypeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.FieldType", false, FieldTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "isArray"), "method isArray");
        check(hasMethod(c, "getSignature"), "method getSignature");
        check(hasMethod(c, "isFloat"), "method isFloat");
        check(hasMethod(c, "isObject"), "method isObject");
        check(hasMethod(c, "isOop"), "method isOop");
        check(hasMethod(c, "isByte"), "method isByte");
        check(hasMethod(c, "isChar"), "method isChar");
        check(hasMethod(c, "isDouble"), "method isDouble");
        check(hasMethod(c, "isInt"), "method isInt");
        check(hasMethod(c, "isLong"), "method isLong");
        check(hasMethod(c, "isShort"), "method isShort");
        check(hasMethod(c, "isBoolean"), "method isBoolean");
        check(hasMethod(c, "getArrayInfo"), "method getArrayInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
