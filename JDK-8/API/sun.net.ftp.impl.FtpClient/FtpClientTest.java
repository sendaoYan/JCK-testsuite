/*
 * @test
 * @summary sun.net.ftp.impl.FtpClient conformance: class and members.
 * @run main FtpClientTest
 */

public class FtpClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ftp.impl.FtpClient", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ftp.impl.FtpClient"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.ftp.FtpClient"), "superclass sun.net.ftp.FtpClient");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("abort"), "declares method abort");
        check(mnames.contains("allocate"), "declares method allocate");
        check(mnames.contains("appendFile"), "declares method appendFile");
        check(mnames.contains("changeDirectory"), "declares method changeDirectory");
        check(mnames.contains("changeToParentDirectory"), "declares method changeToParentDirectory");
        check(mnames.contains("close"), "declares method close");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
