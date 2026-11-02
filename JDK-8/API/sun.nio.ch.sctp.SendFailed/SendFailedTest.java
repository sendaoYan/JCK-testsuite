/*
 * @test
 * @summary sun.nio.ch.sctp.SendFailed conformance: class and members.
 * @run main SendFailedTest
 */

public class SendFailedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.sctp.SendFailed", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.sctp.SendFailed"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.nio.sctp.SendFailedNotification"), "superclass com.sun.nio.sctp.SendFailedNotification");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.nio.ch.sctp.SctpNotification"), "interface sun.nio.ch.sctp.SctpNotification");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("address"), "declares method address");
        check(mnames.contains("assocId"), "declares method assocId");
        check(mnames.contains("association"), "declares method association");
        check(mnames.contains("buffer"), "declares method buffer");
        check(mnames.contains("errorCode"), "declares method errorCode");
        check(mnames.contains("setAssociation"), "declares method setAssociation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
