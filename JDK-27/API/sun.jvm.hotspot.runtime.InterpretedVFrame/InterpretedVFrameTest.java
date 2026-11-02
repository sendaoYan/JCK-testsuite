/*
 * @test
 * @summary sun.jvm.hotspot.runtime.InterpretedVFrame conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main InterpretedVFrameTest
 */
import java.lang.reflect.*;

public class InterpretedVFrameTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.InterpretedVFrame", false, InterpretedVFrameTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.JavaVFrame".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getMethod"), "method getMethod");
        check(hasMethod(c, "getMonitors"), "method getMonitors");
        check(hasMethod(c, "getLocals"), "method getLocals");
        check(hasMethod(c, "verify"), "method verify");
        check(hasMethod(c, "getBCI"), "method getBCI");
        check(hasMethod(c, "isInterpretedFrame"), "method isInterpretedFrame");
        check(hasMethod(c, "getExpressions"), "method getExpressions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
