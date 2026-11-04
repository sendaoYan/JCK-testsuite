/*
 * @test
 * @summary sun.rmi.transport.LiveRef conformance: class and members.
 * @run main LiveRefTest
 */

public class LiveRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.LiveRef", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.LiveRef"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("clone"), "declares method clone");
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("exportObject"), "declares method exportObject");
        check(mnames.contains("getChannel"), "declares method getChannel");
        check(mnames.contains("getClientSocketFactory"), "declares method getClientSocketFactory");
        check(mnames.contains("getEndpoint"), "declares method getEndpoint");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
