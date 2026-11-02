/*
 * @test
 * @summary sun.rmi.transport.proxy.HttpReceiveSocket conformance: class and members.
 * @run main HttpReceiveSocketTest
 */

public class HttpReceiveSocketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.proxy.HttpReceiveSocket", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.proxy.HttpReceiveSocket"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.rmi.transport.proxy.WrappedSocket"), "superclass sun.rmi.transport.proxy.WrappedSocket");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.rmi.transport.proxy.RMISocketInfo"), "interface sun.rmi.transport.proxy.RMISocketInfo");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("getInetAddress"), "declares method getInetAddress");
        check(mnames.contains("getInputStream"), "declares method getInputStream");
        check(mnames.contains("getLocalAddress"), "declares method getLocalAddress");
        check(mnames.contains("getLocalPort"), "declares method getLocalPort");
        check(mnames.contains("getOutputStream"), "declares method getOutputStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
