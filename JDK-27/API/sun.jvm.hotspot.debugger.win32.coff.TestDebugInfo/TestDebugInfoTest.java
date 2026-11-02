/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.TestDebugInfo conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main TestDebugInfoTest
 */
import java.lang.reflect.*;

public class TestDebugInfoTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.TestDebugInfo", false, TestDebugInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 3, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.win32.coff.DebugVC50SubsectionTypes"), "interface sun.jvm.hotspot.debugger.win32.coff.DebugVC50SubsectionTypes");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.win32.coff.DebugVC50SymbolTypes"), "interface sun.jvm.hotspot.debugger.win32.coff.DebugVC50SymbolTypes");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.win32.coff.DebugVC50TypeLeafIndices"), "interface sun.jvm.hotspot.debugger.win32.coff.DebugVC50TypeLeafIndices");
        check(hasMethod(c, "main"), "method main");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
