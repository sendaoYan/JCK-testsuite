/*
 * @test
 * @summary sun.jvm.hotspot.interpreter.BytecodeInvoke conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.interpreter:+open
 * @run main BytecodeInvokeTest
 */
import java.lang.reflect.*;

public class BytecodeInvokeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.interpreter.BytecodeInvoke", false, BytecodeInvokeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.interpreter.BytecodeWithCPIndex".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "signature"), "method signature");
        check(hasMethod(c, "name"), "method name");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "at"), "method at");
        check(hasMethod(c, "resultType"), "method resultType");
        check(hasMethod(c, "verify"), "method verify");
        check(hasMethod(c, "isValid"), "method isValid");
        check(hasMethod(c, "atCheck"), "method atCheck");
        check(hasMethod(c, "isInvokestatic"), "method isInvokestatic");
        check(hasMethod(c, "isInvokedynamic"), "method isInvokedynamic");
        check(hasMethod(c, "adjustedInvokeCode"), "method adjustedInvokeCode");
        check(hasMethod(c, "isInvokeinterface"), "method isInvokeinterface");
        check(hasMethod(c, "isInvokevirtual"), "method isInvokevirtual");
        check(hasMethod(c, "isInvokespecial"), "method isInvokespecial");
        check(hasMethod(c, "getInvokedMethod"), "method getInvokedMethod");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
