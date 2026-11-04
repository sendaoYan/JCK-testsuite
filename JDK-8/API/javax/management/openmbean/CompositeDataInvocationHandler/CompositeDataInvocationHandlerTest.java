/*
 * @test
 * @summary javax.management.openmbean.CompositeDataInvocationHandler conformance: concrete class reflection.
 * @run main CompositeDataInvocationHandlerTest
 */

public class CompositeDataInvocationHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.CompositeDataInvocationHandler");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("java.lang.reflect.InvocationHandler").isAssignableFrom(c), "implements/extends java.lang.reflect.InvocationHandler");
        check(hasMethod(c, "getCompositeData"), "declares getCompositeData");
        check(hasMethod(c, "invoke"), "declares invoke");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
