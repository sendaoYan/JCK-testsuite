/*
 * @test
 * @summary java.rmi.server.ServerRef conformance: interface reflection.
 * @run main ServerRefTest
 */

public class ServerRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.server.ServerRef");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.rmi.server.RemoteRef").isAssignableFrom(c), "implements/extends java.rmi.server.RemoteRef");
        check(hasMethod(c, "exportObject"), "declares exportObject");
        check(hasMethod(c, "getClientHost"), "declares getClientHost");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
