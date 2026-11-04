/*
 * @test
 * @summary sun.jvm.hotspot.code.Location conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.code:+open
 * @run main LocationTest
 */
import java.lang.reflect.*;

public class LocationTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.code.Location", false, LocationTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "isRegister"), "method isRegister");
        check(hasMethod(c, "getWhere"), "method getWhere");
        check(hasMethod(c, "isIllegal"), "method isIllegal");
        check(hasMethod(c, "getStackOffset"), "method getStackOffset");
        check(hasMethod(c, "getRegisterNumber"), "method getRegisterNumber");
        check(hasMethod(c, "isStack"), "method isStack");
        check(hasMethod(c, "holdsOop"), "method holdsOop");
        check(hasMethod(c, "holdsNarrowOop"), "method holdsNarrowOop");
        check(hasMethod(c, "holdsInt"), "method holdsInt");
        check(hasMethod(c, "holdsLong"), "method holdsLong");
        check(hasMethod(c, "holdsFloat"), "method holdsFloat");
        check(hasMethod(c, "holdsDouble"), "method holdsDouble");
        check(hasMethod(c, "holdsAddr"), "method holdsAddr");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
