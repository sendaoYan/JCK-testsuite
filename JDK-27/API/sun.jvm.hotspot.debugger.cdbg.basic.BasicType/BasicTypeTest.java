/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.basic.BasicType conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg.basic:+open
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.basic.BasicType", false, BasicTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 2, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.cdbg.Type"), "interface sun.jvm.hotspot.debugger.cdbg.Type");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.cdbg.CVAttributes"), "interface sun.jvm.hotspot.debugger.cdbg.CVAttributes");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "isArray"), "method isArray");
        check(hasMethod(c, "isEnum"), "method isEnum");
        check(hasMethod(c, "setName"), "method setName");
        check(hasMethod(c, "isVolatile"), "method isVolatile");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "isVoid"), "method isVoid");
        check(hasMethod(c, "visit"), "method visit");
        check(hasMethod(c, "isFloat"), "method isFloat");
        check(hasMethod(c, "asArray"), "method asArray");
        check(hasMethod(c, "isDouble"), "method isDouble");
        check(hasMethod(c, "isInt"), "method isInt");
        check(hasMethod(c, "iterateObject"), "method iterateObject");
        check(hasMethod(c, "asBit"), "method asBit");
        check(hasMethod(c, "asInt"), "method asInt");
        check(hasMethod(c, "asEnum"), "method asEnum");
        check(hasMethod(c, "asFloat"), "method asFloat");
        check(hasMethod(c, "asDouble"), "method asDouble");
        check(hasMethod(c, "asPointer"), "method asPointer");
        check(hasMethod(c, "asRef"), "method asRef");
        check(hasMethod(c, "asCompound"), "method asCompound");
        check(hasMethod(c, "asFunction"), "method asFunction");
        check(hasMethod(c, "asMemberFunction"), "method asMemberFunction");
        check(hasMethod(c, "asVoid"), "method asVoid");
        check(hasMethod(c, "isBit"), "method isBit");
        check(hasMethod(c, "isPointer"), "method isPointer");
        check(hasMethod(c, "isRef"), "method isRef");
        check(hasMethod(c, "isCompound"), "method isCompound");
        check(hasMethod(c, "isFunction"), "method isFunction");
        check(hasMethod(c, "isMemberFunction"), "method isMemberFunction");
        check(hasMethod(c, "isConst"), "method isConst");
        check(hasMethod(c, "isLazy"), "method isLazy");
        check(hasMethod(c, "createCVVariant"), "method createCVVariant");
        check(hasMethod(c, "findCVVariant"), "method findCVVariant");
        check(hasMethod(c, "addCVVariant"), "method addCVVariant");
        check(hasMethod(c, "getCVVariant"), "method getCVVariant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
