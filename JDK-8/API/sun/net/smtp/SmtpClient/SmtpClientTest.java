/*
 * @test
 * @summary sun.net.smtp.SmtpClient conformance: class and members.
 * @run main SmtpClientTest
 */

public class SmtpClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.smtp.SmtpClient", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.smtp.SmtpClient"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.TransferProtocolClient"), "superclass sun.net.TransferProtocolClient");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("closeMessage"), "declares method closeMessage");
        check(mnames.contains("closeServer"), "declares method closeServer");
        check(mnames.contains("from"), "declares method from");
        check(mnames.contains("getEncoding"), "declares method getEncoding");
        check(mnames.contains("getMailHost"), "declares method getMailHost");
        check(mnames.contains("issueCommand"), "declares method issueCommand");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
