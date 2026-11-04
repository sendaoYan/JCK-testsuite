/*
 * @test
 * @summary sun.rmi.transport.proxy.RMIHttpToPortSocketFactory conformance: class and members.
 * @run main RMIHttpToPortSocketFactoryTest
 */

public class RMIHttpToPortSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.proxy.RMIHttpToPortSocketFactory", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.proxy.RMIHttpToPortSocketFactory"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.rmi.server.RMISocketFactory"), "superclass java.rmi.server.RMISocketFactory");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createServerSocket"), "declares method createServerSocket");
        check(mnames.contains("createSocket"), "declares method createSocket");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
