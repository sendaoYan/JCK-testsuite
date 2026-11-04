/*
 * @test
 * @summary sun.jvm.hotspot.runtime.JavaVFrame conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main JavaVFrameTest
 */
import java.lang.reflect.*;

public class JavaVFrameTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.JavaVFrame", false, JavaVFrameTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VFrame".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "getMethod"), "method getMethod");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "getMonitors"), "method getMonitors");
        check(hasMethod(c, "getLocals"), "method getLocals");
        check(hasMethod(c, "verify"), "method verify");
        check(hasMethod(c, "getBCI"), "method getBCI");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "isJavaFrame"), "method isJavaFrame");
        check(hasMethod(c, "printLockedObjectClassName"), "method printLockedObjectClassName");
        check(hasMethod(c, "getExpressions"), "method getExpressions");
        check(hasMethod(c, "printActivationOn"), "method printActivationOn");
        check(hasMethod(c, "printLockInfo"), "method printLockInfo");
        check(hasMethod(c, "printActivation"), "method printActivation");
        check(hasMethod(c, "structuralCompare"), "method structuralCompare");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
