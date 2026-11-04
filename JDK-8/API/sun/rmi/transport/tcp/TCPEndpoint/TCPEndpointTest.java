/*
 * @test
 * @summary sun.rmi.transport.tcp.TCPEndpoint conformance: class and members.
 * @run main TCPEndpointTest
 */

public class TCPEndpointTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.tcp.TCPEndpoint", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.tcp.TCPEndpoint"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.rmi.transport.Endpoint"), "interface sun.rmi.transport.Endpoint");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("allKnownTransports"), "declares method allKnownTransports");
        check(mnames.contains("chooseFactory"), "declares method chooseFactory");
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("exportObject"), "declares method exportObject");
        check(mnames.contains("getBoolean"), "declares method getBoolean");
        check(mnames.contains("getChannel"), "declares method getChannel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
