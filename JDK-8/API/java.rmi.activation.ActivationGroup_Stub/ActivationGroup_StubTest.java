/*
 * @test
 * @summary java.rmi.activation.ActivationGroup_Stub conformance: concrete class reflection.
 * @run main ActivationGroup_StubTest
 */

public class ActivationGroup_StubTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.activation.ActivationGroup_Stub");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.rmi.server.RemoteStub".equals(c.getSuperclass().getName()), "superclass java.rmi.server.RemoteStub");
        check(Class.forName("java.rmi.activation.ActivationInstantiator").isAssignableFrom(c), "implements/extends java.rmi.activation.ActivationInstantiator");
        check(Class.forName("java.rmi.Remote").isAssignableFrom(c), "implements/extends java.rmi.Remote");
        check(hasMethod(c, "newInstance"), "declares newInstance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
