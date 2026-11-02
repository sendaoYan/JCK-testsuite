/*
 * @test
 * @summary java.rmi.server.RemoteRef conformance: interface reflection.
 * @run main RemoteRefTest
 */

public class RemoteRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.server.RemoteRef");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Externalizable").isAssignableFrom(c), "implements/extends java.io.Externalizable");
        check(hasMethod(c, "done"), "declares done");
        check(hasMethod(c, "getRefClass"), "declares getRefClass");
        check(hasMethod(c, "invoke"), "declares invoke");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
