/*
 * @test
 * @summary sun.net.ftp.FtpClient conformance: abstract class and members.
 * @run main FtpClientTest
 */

public class FtpClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ftp.FtpClient", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ftp.FtpClient"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.io.Closeable"), "interface java.io.Closeable");
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
