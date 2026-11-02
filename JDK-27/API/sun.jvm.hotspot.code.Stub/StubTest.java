/*
 * @test
 * @summary sun.jvm.hotspot.code.Stub conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.code:+open
 * @run main StubTest
 */
import java.lang.reflect.*;

public class StubTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.code.Stub", false, StubTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getAddress"), "method getAddress");
        check(hasMethod(c, "verify"), "method verify");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "codeEnd"), "method codeEnd");
        check(hasMethod(c, "codeBegin"), "method codeBegin");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
