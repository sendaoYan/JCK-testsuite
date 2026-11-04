/*
 * @test
 * @summary sun.rmi.transport.proxy.CGIHandler conformance: class and members.
 * @run main CGIHandlerTest
 */

public class CGIHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.proxy.CGIHandler", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.proxy.CGIHandler"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("main"), "declares method main");
        check(mnames.contains("returnClientError"), "declares method returnClientError");
        check(mnames.contains("returnServerError"), "declares method returnServerError");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
