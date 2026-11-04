/*
 * @test
 * @summary java.rmi.server.RemoteStub conformance: abstract class reflection.
 * @run main RemoteStubTest
 */

public class RemoteStubTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.server.RemoteStub");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check("java.rmi.server.RemoteObject".equals(c.getSuperclass().getName()), "superclass java.rmi.server.RemoteObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
