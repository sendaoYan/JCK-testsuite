/*
 * @test
 * @summary sun.jvm.hotspot.runtime.VMReg conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main VMRegTest
 */
import java.lang.reflect.*;

public class VMRegTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.VMReg", false, VMRegTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "minus"), "method minus");
        check(hasMethod(c, "lessThan"), "method lessThan");
        check(hasMethod(c, "lessThanOrEqual"), "method lessThanOrEqual");
        check(hasMethod(c, "greaterThan"), "method greaterThan");
        check(hasMethod(c, "greaterThanOrEqual"), "method greaterThanOrEqual");
        check(hasMethod(c, "reg2Stack"), "method reg2Stack");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
