/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.Sym conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg:+open
 * @run main SymTest
 */
import java.lang.reflect.*;

public class SymTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.Sym", false, SymTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "asFunction"), "method asFunction");
        check(hasMethod(c, "isFunction"), "method isFunction");
        check(hasMethod(c, "asBlock"), "method asBlock");
        check(hasMethod(c, "asGlobal"), "method asGlobal");
        check(hasMethod(c, "asLocal"), "method asLocal");
        check(hasMethod(c, "isBlock"), "method isBlock");
        check(hasMethod(c, "isGlobal"), "method isGlobal");
        check(hasMethod(c, "isLocal"), "method isLocal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
