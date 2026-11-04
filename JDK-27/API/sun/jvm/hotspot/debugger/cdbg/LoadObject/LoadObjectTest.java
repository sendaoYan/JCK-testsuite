/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.LoadObject conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg:+open
 * @run main LoadObjectTest
 */
import java.lang.reflect.*;

public class LoadObjectTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.LoadObject", false, LoadObjectTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getBase"), "method getBase");
        check(hasMethod(c, "getDebugInfoDataBase"), "method getDebugInfoDataBase");
        check(hasMethod(c, "debugInfoForPC"), "method debugInfoForPC");
        check(hasMethod(c, "closestSymbolToPC"), "method closestSymbolToPC");
        check(hasMethod(c, "lineNumberForPC"), "method lineNumberForPC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
