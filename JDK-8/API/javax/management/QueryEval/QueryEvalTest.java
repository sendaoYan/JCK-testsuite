/*
 * @test
 * @summary javax.management.QueryEval conformance: abstract class reflection.
 * @run main QueryEvalTest
 */

public class QueryEvalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.QueryEval");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "getMBeanServer"), "declares getMBeanServer");
        check(hasMethod(c, "setMBeanServer"), "declares setMBeanServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
