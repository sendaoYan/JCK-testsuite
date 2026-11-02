/*
 * @test
 * @summary sun.jvm.hotspot.code.Location.Type conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.code:+open
 * @run main TypeTest
 */
import java.lang.reflect.*;

public class TypeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.code.Location$Type", false, TypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasField(c, "NORMAL"), "field NORMAL");
        check(hasField(c, "OOP"), "field OOP");
        check(hasField(c, "NARROWOOP"), "field NARROWOOP");
        check(hasField(c, "INT_IN_LONG"), "field INT_IN_LONG");
        check(hasField(c, "LNG"), "field LNG");
        check(hasField(c, "FLOAT_IN_DBL"), "field FLOAT_IN_DBL");
        check(hasField(c, "DBL"), "field DBL");
        check(hasField(c, "ADDR"), "field ADDR");
        check(hasField(c, "INVALID"), "field INVALID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
