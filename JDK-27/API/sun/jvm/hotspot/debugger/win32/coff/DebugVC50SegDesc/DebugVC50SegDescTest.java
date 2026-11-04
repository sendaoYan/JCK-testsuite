/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50SegDesc conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50SegDescTest
 */
import java.lang.reflect.*;

public class DebugVC50SegDescTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50SegDesc", false, DebugVC50SegDescTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getClassName"), "method getClassName");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "getFrame"), "method getFrame");
        check(hasMethod(c, "getFlags"), "method getFlags");
        check(hasMethod(c, "getGroup"), "method getGroup");
        check(hasMethod(c, "getOverlayNum"), "method getOverlayNum");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
