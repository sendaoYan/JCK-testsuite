/*
 * @test
 * @summary sun.jvm.hotspot.types.basic.BasicType conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.types.basic:+open
 * @run main BasicTypeTest
 */
import java.lang.reflect.*;

public class BasicTypeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.types.basic.BasicType", false, BasicTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.types.Type"), "interface sun.jvm.hotspot.types.Type");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "getSuperclass"), "method getSuperclass");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getFields"), "method getFields");
        check(hasMethod(c, "getField"), "method getField");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "setSize"), "method setSize");
        check(hasMethod(c, "setSuperclass"), "method setSuperclass");
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
        check(hasMethod(c, "addField"), "method addField");
        check(hasMethod(c, "setIsJavaPrimitiveType"), "method setIsJavaPrimitiveType");
        check(hasMethod(c, "setIsOopType"), "method setIsOopType");
        check(hasMethod(c, "removeField"), "method removeField");
        check(hasField(c, "db"), "field db");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
