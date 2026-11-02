/*
 * @test
 * @summary sun.jvm.hotspot.interpreter.BytecodeGetPut conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.interpreter:+open
 * @run main BytecodeGetPutTest
 */
import java.lang.reflect.*;

public class BytecodeGetPutTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.interpreter.BytecodeGetPut", false, BytecodeGetPutTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.interpreter.BytecodeWithCPIndex".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "signature"), "method signature");
        check(hasMethod(c, "name"), "method name");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "isStatic"), "method isStatic");
        check(hasMethod(c, "getField"), "method getField");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
