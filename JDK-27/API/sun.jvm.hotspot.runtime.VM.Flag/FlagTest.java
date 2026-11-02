/*
 * @test
 * @summary sun.jvm.hotspot.runtime.VM.Flag conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main FlagTest
 */
import java.lang.reflect.*;

public class FlagTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.VM$Flag", false, FlagTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getInt"), "method getInt");
        check(hasMethod(c, "getDouble"), "method getDouble");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getAddress"), "method getAddress");
        check(hasMethod(c, "isDouble"), "method isDouble");
        check(hasMethod(c, "isInt"), "method isInt");
        check(hasMethod(c, "getBool"), "method getBool");
        check(hasMethod(c, "isBool"), "method isBool");
        check(hasMethod(c, "isUInt"), "method isUInt");
        check(hasMethod(c, "isIntx"), "method isIntx");
        check(hasMethod(c, "isUIntx"), "method isUIntx");
        check(hasMethod(c, "isSizet"), "method isSizet");
        check(hasMethod(c, "isCcstr"), "method isCcstr");
        check(hasMethod(c, "isCcstrlist"), "method isCcstrlist");
        check(hasMethod(c, "isUint64t"), "method isUint64t");
        check(hasMethod(c, "getUInt"), "method getUInt");
        check(hasMethod(c, "getIntx"), "method getIntx");
        check(hasMethod(c, "getUIntx"), "method getUIntx");
        check(hasMethod(c, "getSizet"), "method getSizet");
        check(hasMethod(c, "getCcstr"), "method getCcstr");
        check(hasMethod(c, "getCcstrlist"), "method getCcstrlist");
        check(hasMethod(c, "getUint64t"), "method getUint64t");
        check(hasMethod(c, "getOrigin"), "method getOrigin");
        check(hasMethod(c, "getOriginString"), "method getOriginString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
