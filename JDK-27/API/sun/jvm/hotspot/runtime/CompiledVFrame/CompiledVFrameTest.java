/*
 * @test
 * @summary sun.jvm.hotspot.runtime.CompiledVFrame conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main CompiledVFrameTest
 */
import java.lang.reflect.*;

public class CompiledVFrameTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.CompiledVFrame", false, CompiledVFrameTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.JavaVFrame".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getMethod"), "method getMethod");
        check(hasMethod(c, "getScope"), "method getScope");
        check(hasMethod(c, "getMonitors"), "method getMonitors");
        check(hasMethod(c, "getLocals"), "method getLocals");
        check(hasMethod(c, "getBCI"), "method getBCI");
        check(hasMethod(c, "sender"), "method sender");
        check(hasMethod(c, "isCompiledFrame"), "method isCompiledFrame");
        check(hasMethod(c, "getExpressions"), "method getExpressions");
        check(hasMethod(c, "isTop"), "method isTop");
        check(hasMethod(c, "mayBeImpreciseDbg"), "method mayBeImpreciseDbg");
        check(hasMethod(c, "isDeoptimized"), "method isDeoptimized");
        check(hasMethod(c, "getCode"), "method getCode");
        check(hasMethod(c, "getCodeUnsafe"), "method getCodeUnsafe");
        check(hasMethod(c, "getRawBCI"), "method getRawBCI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
