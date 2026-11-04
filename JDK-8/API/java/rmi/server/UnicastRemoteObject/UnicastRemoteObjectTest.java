/*
 * @test
 * @summary java.rmi.server.UnicastRemoteObject conformance: concrete class reflection.
 * @run main UnicastRemoteObjectTest
 */

public class UnicastRemoteObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.server.UnicastRemoteObject");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.rmi.server.RemoteServer".equals(c.getSuperclass().getName()), "superclass java.rmi.server.RemoteServer");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "exportObject"), "declares exportObject");
        check(hasMethod(c, "unexportObject"), "declares unexportObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
