/*
 * @test
 * @summary java.rmi.activation.ActivationGroup conformance: abstract class reflection.
 * @run main ActivationGroupTest
 */

public class ActivationGroupTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.activation.ActivationGroup");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check("java.rmi.server.UnicastRemoteObject".equals(c.getSuperclass().getName()), "superclass java.rmi.server.UnicastRemoteObject");
        check(Class.forName("java.rmi.activation.ActivationInstantiator").isAssignableFrom(c), "implements/extends java.rmi.activation.ActivationInstantiator");
        check(hasMethod(c, "activeObject"), "declares activeObject");
        check(hasMethod(c, "createGroup"), "declares createGroup");
        check(hasMethod(c, "currentGroupID"), "declares currentGroupID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
