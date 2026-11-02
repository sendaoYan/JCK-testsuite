/*
 * @test
 * @summary sun.nio.ch.sctp.SctpStdSocketOption conformance: class and members.
 * @run main SctpStdSocketOptionTest
 */

public class SctpStdSocketOptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.sctp.SctpStdSocketOption", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.sctp.SctpStdSocketOption"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("com.sun.nio.sctp.SctpSocketOption"), "interface com.sun.nio.sctp.SctpSocketOption");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("constValue"), "declares method constValue");
        check(mnames.contains("name"), "declares method name");
        check(mnames.contains("toString"), "declares method toString");
        check(mnames.contains("type"), "declares method type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
