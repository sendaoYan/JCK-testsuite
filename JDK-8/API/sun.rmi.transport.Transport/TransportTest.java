/*
 * @test
 * @summary sun.rmi.transport.Transport conformance: abstract class and members.
 * @run main TransportTest
 */

public class TransportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.Transport", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.Transport"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkAcceptPermission"), "declares method checkAcceptPermission");
        check(mnames.contains("currentTransport"), "declares method currentTransport");
        check(mnames.contains("exportObject"), "declares method exportObject");
        check(mnames.contains("free"), "declares method free");
        check(mnames.contains("getChannel"), "declares method getChannel");
        check(mnames.contains("getLogLevel"), "declares method getLogLevel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
