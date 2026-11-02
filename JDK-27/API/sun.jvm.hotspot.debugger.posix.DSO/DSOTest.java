/*
 * @test
 * @summary sun.jvm.hotspot.debugger.posix.DSO conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.posix:+open
 * @run main DSOTest
 */
import java.lang.reflect.*;

public class DSOTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.posix.DSO", false, DSOTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.cdbg.LoadObject"), "interface sun.jvm.hotspot.debugger.cdbg.LoadObject");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getBase"), "method getBase");
        check(hasMethod(c, "newAddress"), "method newAddress");
        check(hasMethod(c, "getAddressValue"), "method getAddressValue");
        check(hasMethod(c, "getDebugInfoDataBase"), "method getDebugInfoDataBase");
        check(hasMethod(c, "debugInfoForPC"), "method debugInfoForPC");
        check(hasMethod(c, "lineNumberForPC"), "method lineNumberForPC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
