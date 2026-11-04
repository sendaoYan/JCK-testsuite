/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50SrcModFileDesc conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50SrcModFileDescTest
 */
import java.lang.reflect.*;

public class DebugVC50SrcModFileDescTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50SrcModFileDesc", false, DebugVC50SrcModFileDescTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getSourceFileName"), "method getSourceFileName");
        check(hasMethod(c, "getLineNumberMap"), "method getLineNumberMap");
        check(hasMethod(c, "getNumCodeSegments"), "method getNumCodeSegments");
        check(hasMethod(c, "getSegmentStartOffset"), "method getSegmentStartOffset");
        check(hasMethod(c, "getSegmentEndOffset"), "method getSegmentEndOffset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
