/*
 * @test
 * @summary sun.rmi.transport.Endpoint conformance: interface and members.
 * @run main EndpointTest
 */

public class EndpointTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.Endpoint", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.Endpoint"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("exportObject"), "declares method exportObject");
        check(mnames.contains("getChannel"), "declares method getChannel");
        check(mnames.contains("getInboundTransport"), "declares method getInboundTransport");
        check(mnames.contains("getOutboundTransport"), "declares method getOutboundTransport");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
