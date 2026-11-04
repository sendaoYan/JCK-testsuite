/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50SSSymbolBase conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50SSSymbolBaseTest
 */
import java.lang.reflect.*;

public class DebugVC50SSSymbolBaseTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50SSSymbolBase", false, DebugVC50SSSymbolBaseTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.win32.coff.DebugVC50Subsection"), "interface sun.jvm.hotspot.debugger.win32.coff.DebugVC50Subsection");
        check(hasMethod(c, "getSymbolIterator"), "method getSymbolIterator");
        check(hasMethod(c, "getSymHashIndex"), "method getSymHashIndex");
        check(hasMethod(c, "getAddrHashIndex"), "method getAddrHashIndex");
        check(hasMethod(c, "getSymTabSize"), "method getSymTabSize");
        check(hasMethod(c, "getSymHashSize"), "method getSymHashSize");
        check(hasMethod(c, "getAddrHashSize"), "method getAddrHashSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
