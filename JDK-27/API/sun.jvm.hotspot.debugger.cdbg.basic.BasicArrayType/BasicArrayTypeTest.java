/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.basic.BasicArrayType conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg.basic:+open
 * @run main BasicArrayTypeTest
 */
import java.lang.reflect.*;

public class BasicArrayTypeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.basic.BasicArrayType", false, BasicArrayTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.debugger.cdbg.basic.BasicType".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.cdbg.ArrayType"), "interface sun.jvm.hotspot.debugger.cdbg.ArrayType");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "visit"), "method visit");
        check(hasMethod(c, "asArray"), "method asArray");
        check(hasMethod(c, "iterateObject"), "method iterateObject");
        check(hasMethod(c, "getElementType"), "method getElementType");
        check(hasMethod(c, "createCVVariant"), "method createCVVariant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
