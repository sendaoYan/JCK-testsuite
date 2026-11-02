/*
 * @test
 * @summary sun.jvm.hotspot.code.VMRegImpl conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.code:+open
 * @run main VMRegImplTest
 */
import java.lang.reflect.*;

public class VMRegImplTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.code.VMRegImpl", false, VMRegImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getRegisterName"), "method getRegisterName");
        check(hasMethod(c, "getStack0"), "method getStack0");
        check(hasMethod(c, "getStackSlotSize"), "method getStackSlotSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
