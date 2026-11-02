/*
 * @test
 * @summary sun.net.ftp.impl.DefaultFtpClientProvider conformance: class and members.
 * @run main DefaultFtpClientProviderTest
 */

public class DefaultFtpClientProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ftp.impl.DefaultFtpClientProvider", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ftp.impl.DefaultFtpClientProvider"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.ftp.FtpClientProvider"), "superclass sun.net.ftp.FtpClientProvider");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createFtpClient"), "declares method createFtpClient");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
