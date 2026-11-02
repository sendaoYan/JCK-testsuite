/*
 * @test
 * @summary sun.jvm.hotspot.asm.Register conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.asm:+open
 * @run main RegisterTest
 */
import java.lang.reflect.*;

public class RegisterTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.asm.Register", false, RegisterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.asm.ImmediateOrRegister".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "getNumber"), "method getNumber");
        check(hasMethod(c, "isValid"), "method isValid");
        check(hasMethod(c, "isFloat"), "method isFloat");
        check(hasMethod(c, "isStackPointer"), "method isStackPointer");
        check(hasMethod(c, "isFramePointer"), "method isFramePointer");
        check(hasMethod(c, "isRegister"), "method isRegister");
        check(hasMethod(c, "getNumberOfRegisters"), "method getNumberOfRegisters");
        check(hasField(c, "number"), "field number");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
