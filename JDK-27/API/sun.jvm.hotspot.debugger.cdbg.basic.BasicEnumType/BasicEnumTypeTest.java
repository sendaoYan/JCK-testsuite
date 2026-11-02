/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.basic.BasicEnumType conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg.basic:+open
 * @run main BasicEnumTypeTest
 */
import java.lang.reflect.*;

public class BasicEnumTypeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.basic.BasicEnumType", false, BasicEnumTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.debugger.cdbg.basic.BasicIntType".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.cdbg.EnumType"), "interface sun.jvm.hotspot.debugger.cdbg.EnumType");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "visit"), "method visit");
        check(hasMethod(c, "isUnsigned"), "method isUnsigned");
        check(hasMethod(c, "iterateObject"), "method iterateObject");
        check(hasMethod(c, "asEnum"), "method asEnum");
        check(hasMethod(c, "getNumEnumerates"), "method getNumEnumerates");
        check(hasMethod(c, "getEnumName"), "method getEnumName");
        check(hasMethod(c, "getEnumValue"), "method getEnumValue");
        check(hasMethod(c, "enumNameForValue"), "method enumNameForValue");
        check(hasMethod(c, "createCVVariant"), "method createCVVariant");
        check(hasMethod(c, "addEnum"), "method addEnum");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
