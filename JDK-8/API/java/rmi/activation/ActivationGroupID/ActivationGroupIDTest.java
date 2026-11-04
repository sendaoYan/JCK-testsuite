/*
 * @test
 * @summary java.rmi.activation.ActivationGroupID conformance: concrete class reflection.
 * @run main ActivationGroupIDTest
 */

public class ActivationGroupIDTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.activation.ActivationGroupID");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getSystem"), "declares getSystem");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
