/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.basic.BasicCFrame conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg.basic:+open
 * @run main BasicCFrameTest
 */
import java.lang.reflect.*;

public class BasicCFrameTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.basic.BasicCFrame", false, BasicCFrameTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.cdbg.CFrame"), "interface sun.jvm.hotspot.debugger.cdbg.CFrame");
        check(hasMethod(c, "dbg"), "method dbg");
        check(hasMethod(c, "closestSymbolToPC"), "method closestSymbolToPC");
        check(hasMethod(c, "loadObjectForPC"), "method loadObjectForPC");
        check(hasMethod(c, "blockForPC"), "method blockForPC");
        check(hasMethod(c, "iterateLocals"), "method iterateLocals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
