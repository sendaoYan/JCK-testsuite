/*
 * @test
 * @summary sun.jvm.hotspot.utilities.RBNode conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main RBNodeTest
 */
import java.lang.reflect.*;

public class RBNodeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.RBNode", false, RBNodeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "update"), "method update");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "setParent"), "method setParent");
        check(hasMethod(c, "setColor"), "method setColor");
        check(hasMethod(c, "copyFrom"), "method copyFrom");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "getColor"), "method getColor");
        check(hasMethod(c, "getLeft"), "method getLeft");
        check(hasMethod(c, "getRight"), "method getRight");
        check(hasMethod(c, "setLeft"), "method setLeft");
        check(hasMethod(c, "setRight"), "method setRight");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
