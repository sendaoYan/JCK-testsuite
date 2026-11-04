/*
 * @test
 * @summary sun.jvm.hotspot.utilities.IntervalTree conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main IntervalTreeTest
 */
import java.lang.reflect.*;

public class IntervalTreeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.IntervalTree", false, IntervalTreeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.utilities.RBTree".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "insert"), "method insert");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "verify"), "method verify");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "findAllNodesIntersecting"), "method findAllNodesIntersecting");
        check(hasMethod(c, "getNodeValue"), "method getNodeValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
