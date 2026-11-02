/*
 * @test
 * @summary sun.rmi.transport.Connection conformance: interface and members.
 * @run main ConnectionTest
 */

public class ConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.Connection", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.Connection"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("getChannel"), "declares method getChannel");
        check(mnames.contains("getInputStream"), "declares method getInputStream");
        check(mnames.contains("getOutputStream"), "declares method getOutputStream");
        check(mnames.contains("isReusable"), "declares method isReusable");
        check(mnames.contains("releaseInputStream"), "declares method releaseInputStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
