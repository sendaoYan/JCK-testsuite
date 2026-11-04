/*
 * @test
 * @summary sun.jvm.hotspot.runtime.BasicType conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.BasicType", false, BasicTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getTObject"), "method getTObject");
        check(hasMethod(c, "getTConflict"), "method getTConflict");
        check(hasMethod(c, "charToBasicType"), "method charToBasicType");
        check(hasMethod(c, "getTBoolean"), "method getTBoolean");
        check(hasMethod(c, "getTChar"), "method getTChar");
        check(hasMethod(c, "getTFloat"), "method getTFloat");
        check(hasMethod(c, "getTDouble"), "method getTDouble");
        check(hasMethod(c, "getTByte"), "method getTByte");
        check(hasMethod(c, "getTShort"), "method getTShort");
        check(hasMethod(c, "getTInt"), "method getTInt");
        check(hasMethod(c, "getTLong"), "method getTLong");
        check(hasMethod(c, "getTArray"), "method getTArray");
        check(hasMethod(c, "getTVoid"), "method getTVoid");
        check(hasMethod(c, "getTAddress"), "method getTAddress");
        check(hasMethod(c, "getTNarrowOop"), "method getTNarrowOop");
        check(hasMethod(c, "getTMetadata"), "method getTMetadata");
        check(hasMethod(c, "getTNarrowKlass"), "method getTNarrowKlass");
        check(hasMethod(c, "getTIllegal"), "method getTIllegal");
        check(hasMethod(c, "intToBasicType"), "method intToBasicType");
        check(hasMethod(c, "charToType"), "method charToType");
        check(hasField(c, "T_BOOLEAN"), "field T_BOOLEAN");
        check(hasField(c, "T_CHAR"), "field T_CHAR");
        check(hasField(c, "T_FLOAT"), "field T_FLOAT");
        check(hasField(c, "T_DOUBLE"), "field T_DOUBLE");
        check(hasField(c, "T_BYTE"), "field T_BYTE");
        check(hasField(c, "T_SHORT"), "field T_SHORT");
        check(hasField(c, "T_INT"), "field T_INT");
        check(hasField(c, "T_LONG"), "field T_LONG");
        check(hasField(c, "T_OBJECT"), "field T_OBJECT");
        check(hasField(c, "T_ARRAY"), "field T_ARRAY");
        check(hasField(c, "T_VOID"), "field T_VOID");
        check(hasField(c, "T_ADDRESS"), "field T_ADDRESS");
        check(hasField(c, "T_NARROWOOP"), "field T_NARROWOOP");
        check(hasField(c, "T_METADATA"), "field T_METADATA");
        check(hasField(c, "T_NARROWKLASS"), "field T_NARROWKLASS");
        check(hasField(c, "T_CONFLICT"), "field T_CONFLICT");
        check(hasField(c, "T_ILLEGAL"), "field T_ILLEGAL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
