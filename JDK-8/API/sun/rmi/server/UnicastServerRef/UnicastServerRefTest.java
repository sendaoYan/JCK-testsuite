/*
 * @test
 * @summary sun.rmi.server.UnicastServerRef conformance: class and members.
 * @run main UnicastServerRefTest
 */

public class UnicastServerRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.UnicastServerRef", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.UnicastServerRef"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.rmi.server.UnicastRef"), "superclass sun.rmi.server.UnicastRef");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.rmi.server.ServerRef"), "interface java.rmi.server.ServerRef");
        check(ifaces.contains("sun.rmi.server.Dispatcher"), "interface sun.rmi.server.Dispatcher");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("clearStackTraces"), "declares method clearStackTraces");
        check(mnames.contains("dispatch"), "declares method dispatch");
        check(mnames.contains("exportObject"), "declares method exportObject");
        check(mnames.contains("getClientHost"), "declares method getClientHost");
        check(mnames.contains("getClientRef"), "declares method getClientRef");
        check(mnames.contains("getRefClass"), "declares method getRefClass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
