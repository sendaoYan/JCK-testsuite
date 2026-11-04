/*
 * @test
 * @summary sun.rmi.transport.tcp.TCPChannel conformance: class and members.
 * @run main TCPChannelTest
 */

public class TCPChannelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.tcp.TCPChannel", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.tcp.TCPChannel"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.rmi.transport.Channel"), "interface sun.rmi.transport.Channel");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("acceptMultiplexConnection"), "declares method acceptMultiplexConnection");
        check(mnames.contains("checkConnectPermission"), "declares method checkConnectPermission");
        check(mnames.contains("createConnection"), "declares method createConnection");
        check(mnames.contains("free"), "declares method free");
        check(mnames.contains("freeCachedConnections"), "declares method freeCachedConnections");
        check(mnames.contains("getEndpoint"), "declares method getEndpoint");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
