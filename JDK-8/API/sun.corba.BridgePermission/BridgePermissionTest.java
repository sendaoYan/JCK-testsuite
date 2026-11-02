/*
 * @test
 * @summary sun.corba.BridgePermission conformance: class and members.
 * @run main BridgePermissionTest
 */

public class BridgePermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.corba.BridgePermission", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.corba.BridgePermission"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.BasicPermission"), "superclass java.security.BasicPermission");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
