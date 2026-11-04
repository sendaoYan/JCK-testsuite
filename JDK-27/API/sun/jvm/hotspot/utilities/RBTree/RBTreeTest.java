/*
 * @test
 * @summary sun.jvm.hotspot.utilities.RBTree conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main RBTreeTest
 */
import java.lang.reflect.*;

public class RBTreeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.RBTree", false, RBTreeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "main"), "method main");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "getRoot"), "method getRoot");
        check(hasMethod(c, "verify"), "method verify");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "insertNode"), "method insertNode");
        check(hasMethod(c, "getNodeValue"), "method getNodeValue");
        check(hasMethod(c, "deleteNode"), "method deleteNode");
        check(hasField(c, "DEBUGGING"), "field DEBUGGING");
        check(hasField(c, "VERBOSE"), "field VERBOSE");
        check(hasField(c, "REALLY_VERBOSE"), "field REALLY_VERBOSE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
