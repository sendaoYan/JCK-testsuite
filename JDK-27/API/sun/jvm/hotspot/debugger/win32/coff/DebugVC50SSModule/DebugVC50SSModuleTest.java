/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50SSModule conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50SSModuleTest
 */
import java.lang.reflect.*;

public class DebugVC50SSModuleTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50SSModule", false, DebugVC50SSModuleTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.win32.coff.DebugVC50Subsection"), "interface sun.jvm.hotspot.debugger.win32.coff.DebugVC50Subsection");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getOverlayNumber"), "method getOverlayNumber");
        check(hasMethod(c, "getLibrariesIndex"), "method getLibrariesIndex");
        check(hasMethod(c, "getNumCodeSegments"), "method getNumCodeSegments");
        check(hasMethod(c, "getDebuggingStyle"), "method getDebuggingStyle");
        check(hasMethod(c, "getSegInfo"), "method getSegInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
