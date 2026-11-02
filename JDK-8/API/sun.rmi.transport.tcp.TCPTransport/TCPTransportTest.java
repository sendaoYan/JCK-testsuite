/*
 * @test
 * @summary sun.rmi.transport.tcp.TCPTransport conformance: class and members.
 * @run main TCPTransportTest
 */

public class TCPTransportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.tcp.TCPTransport", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.tcp.TCPTransport"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.rmi.transport.Transport"), "superclass sun.rmi.transport.Transport");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkAcceptPermission"), "declares method checkAcceptPermission");
        check(mnames.contains("closeSocket"), "declares method closeSocket");
        check(mnames.contains("decrementExportCount"), "declares method decrementExportCount");
        check(mnames.contains("exportObject"), "declares method exportObject");
        check(mnames.contains("free"), "declares method free");
        check(mnames.contains("getChannel"), "declares method getChannel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
