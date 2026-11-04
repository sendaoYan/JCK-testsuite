/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50SSSrcModule conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50SSSrcModuleTest
 */
import java.lang.reflect.*;

public class DebugVC50SSSrcModuleTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50SSSrcModule", false, DebugVC50SSSrcModuleTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.win32.coff.DebugVC50Subsection"), "interface sun.jvm.hotspot.debugger.win32.coff.DebugVC50Subsection");
        check(hasMethod(c, "getNumCodeSegments"), "method getNumCodeSegments");
        check(hasMethod(c, "getSegment"), "method getSegment");
        check(hasMethod(c, "getNumSourceFiles"), "method getNumSourceFiles");
        check(hasMethod(c, "getSourceFileDesc"), "method getSourceFileDesc");
        check(hasMethod(c, "getSegmentStartOffset"), "method getSegmentStartOffset");
        check(hasMethod(c, "getSegmentEndOffset"), "method getSegmentEndOffset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
