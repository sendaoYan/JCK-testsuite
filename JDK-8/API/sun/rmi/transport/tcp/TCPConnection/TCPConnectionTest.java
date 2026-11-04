/*
 * @test
 * @summary sun.rmi.transport.tcp.TCPConnection conformance: class and members.
 * @run main TCPConnectionTest
 */

public class TCPConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.tcp.TCPConnection", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.tcp.TCPConnection"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.rmi.transport.Connection"), "interface sun.rmi.transport.Connection");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("expired"), "declares method expired");
        check(mnames.contains("getChannel"), "declares method getChannel");
        check(mnames.contains("getInputStream"), "declares method getInputStream");
        check(mnames.contains("getOutputStream"), "declares method getOutputStream");
        check(mnames.contains("isDead"), "declares method isDead");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
