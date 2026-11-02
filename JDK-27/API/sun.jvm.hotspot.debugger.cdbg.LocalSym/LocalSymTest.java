/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.LocalSym conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg:+open
 * @run main LocalSymTest
 */
import java.lang.reflect.*;

public class LocalSymTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.LocalSym", false, LocalSymTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.cdbg.Sym"), "interface sun.jvm.hotspot.debugger.cdbg.Sym");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getFrameOffset"), "method getFrameOffset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
