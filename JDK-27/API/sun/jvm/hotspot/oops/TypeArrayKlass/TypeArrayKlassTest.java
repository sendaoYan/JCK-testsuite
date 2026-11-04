/*
 * @test
 * @summary sun.jvm.hotspot.oops.TypeArrayKlass conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main TypeArrayKlassTest
 */
import java.lang.reflect.*;

public class TypeArrayKlassTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.TypeArrayKlass", false, TypeArrayKlassTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.oops.ArrayKlass".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getTypeName"), "method getTypeName");
        check(hasMethod(c, "iterateFields"), "method iterateFields");
        check(hasMethod(c, "arrayKlassImpl"), "method arrayKlassImpl");
        check(hasMethod(c, "printValueOn"), "method printValueOn");
        check(hasMethod(c, "getElementTypeName"), "method getElementTypeName");
        check(hasMethod(c, "getMaxLength"), "method getMaxLength");
        check(hasField(c, "T_BOOLEAN"), "field T_BOOLEAN");
        check(hasField(c, "T_CHAR"), "field T_CHAR");
        check(hasField(c, "T_FLOAT"), "field T_FLOAT");
        check(hasField(c, "T_DOUBLE"), "field T_DOUBLE");
        check(hasField(c, "T_BYTE"), "field T_BYTE");
        check(hasField(c, "T_SHORT"), "field T_SHORT");
        check(hasField(c, "T_INT"), "field T_INT");
        check(hasField(c, "T_LONG"), "field T_LONG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
