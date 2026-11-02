/*
 * @test
 * @summary sun.jvm.hotspot.runtime.StackValue conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main StackValueTest
 */
import java.lang.reflect.*;

public class StackValueTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.StackValue", false, StackValueTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getInteger"), "method getInteger");
        check(hasMethod(c, "getObject"), "method getObject");
        check(hasMethod(c, "printOn"), "method printOn");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
