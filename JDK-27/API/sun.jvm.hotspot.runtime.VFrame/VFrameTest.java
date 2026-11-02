/*
 * @test
 * @summary sun.jvm.hotspot.runtime.VFrame conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main VFrameTest
 */
import java.lang.reflect.*;

public class VFrameTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.VFrame", false, VFrameTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "top"), "method top");
        check(hasMethod(c, "getFrame"), "method getFrame");
        check(hasMethod(c, "sender"), "method sender");
        check(hasMethod(c, "printValueOn"), "method printValueOn");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "isInterpretedFrame"), "method isInterpretedFrame");
        check(hasMethod(c, "newVFrame"), "method newVFrame");
        check(hasMethod(c, "isJavaFrame"), "method isJavaFrame");
        check(hasMethod(c, "javaSender"), "method javaSender");
        check(hasMethod(c, "isCompiledFrame"), "method isCompiledFrame");
        check(hasMethod(c, "isEntryFrame"), "method isEntryFrame");
        check(hasMethod(c, "getRegisterMap"), "method getRegisterMap");
        check(hasMethod(c, "isTop"), "method isTop");
        check(hasMethod(c, "getThread"), "method getThread");
        check(hasMethod(c, "mayBeImpreciseDbg"), "method mayBeImpreciseDbg");
        check(hasMethod(c, "isDeoptimized"), "method isDeoptimized");
        check(hasMethod(c, "printValue"), "method printValue");
        check(hasField(c, "fr"), "field fr");
        check(hasField(c, "regMap"), "field regMap");
        check(hasField(c, "thread"), "field thread");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
