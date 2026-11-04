/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50SSFileIndex conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50SSFileIndexTest
 */
import java.lang.reflect.*;

public class DebugVC50SSFileIndexTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50SSFileIndex", false, DebugVC50SSFileIndexTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.win32.coff.DebugVC50Subsection"), "interface sun.jvm.hotspot.debugger.win32.coff.DebugVC50Subsection");
        check(hasMethod(c, "getNumModules"), "method getNumModules");
        check(hasMethod(c, "getNumReferences"), "method getNumReferences");
        check(hasMethod(c, "getModStart"), "method getModStart");
        check(hasMethod(c, "getRefCount"), "method getRefCount");
        check(hasMethod(c, "getNameRef"), "method getNameRef");
        check(hasMethod(c, "getNames"), "method getNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
