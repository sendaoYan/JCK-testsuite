/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.nio.ch.sctp.SctpChannelImpl conformance: class and members.
 * @run main SctpChannelImplTest
 */

public class SctpChannelImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.sctp.SctpChannelImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.sctp.SctpChannelImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.nio.sctp.SctpChannel"), "superclass com.sun.nio.sctp.SctpChannel");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.nio.ch.SelChImpl"), "interface sun.nio.ch.SelChImpl");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("association"), "declares method association");
        check(mnames.contains("bind"), "declares method bind");
        check(mnames.contains("bindAddress"), "declares method bindAddress");
        check(mnames.contains("bindUnbindAddress"), "declares method bindUnbindAddress");
        check(mnames.contains("checkAssociation"), "declares method checkAssociation");
        check(mnames.contains("checkConnect"), "declares method checkConnect");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
