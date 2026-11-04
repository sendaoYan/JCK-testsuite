/*
 * @test
 * @summary sun.management.jmxremote.LocalRMIServerSocketFactory conformance: class and members.
 * @run main LocalRMIServerSocketFactoryTest
 */

public class LocalRMIServerSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.jmxremote.LocalRMIServerSocketFactory", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.jmxremote.LocalRMIServerSocketFactory"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.rmi.server.RMIServerSocketFactory"), "interface java.rmi.server.RMIServerSocketFactory");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createServerSocket"), "declares method createServerSocket");
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("hashCode"), "declares method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
