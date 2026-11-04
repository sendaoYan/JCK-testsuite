/*
 * @test
 * @summary sun.jvm.hotspot.types.Type conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.types:+open
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
        Class<?> c = Class.forName("sun.jvm.hotspot.types.Type", false, TypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getSuperclass"), "method getSuperclass");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getFields"), "method getFields");
        check(hasMethod(c, "getField"), "method getField");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "isOopType"), "method isOopType");
        check(hasMethod(c, "isCIntegerType"), "method isCIntegerType");
        check(hasMethod(c, "isCStringType"), "method isCStringType");
        check(hasMethod(c, "isJavaPrimitiveType"), "method isJavaPrimitiveType");
        check(hasMethod(c, "isPointerType"), "method isPointerType");
        check(hasMethod(c, "getJBooleanField"), "method getJBooleanField");
        check(hasMethod(c, "getJByteField"), "method getJByteField");
        check(hasMethod(c, "getJCharField"), "method getJCharField");
        check(hasMethod(c, "getJDoubleField"), "method getJDoubleField");
        check(hasMethod(c, "getJFloatField"), "method getJFloatField");
        check(hasMethod(c, "getJIntField"), "method getJIntField");
        check(hasMethod(c, "getJLongField"), "method getJLongField");
        check(hasMethod(c, "getJShortField"), "method getJShortField");
        check(hasMethod(c, "getCIntegerField"), "method getCIntegerField");
        check(hasMethod(c, "getOopField"), "method getOopField");
        check(hasMethod(c, "getNarrowOopField"), "method getNarrowOopField");
        check(hasMethod(c, "getAddressField"), "method getAddressField");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
