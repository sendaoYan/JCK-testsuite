/*
 * @test
 * @summary sun.net.ftp.FtpDirEntry conformance: class and members.
 * @run main FtpDirEntryTest
 */

public class FtpDirEntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ftp.FtpDirEntry", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ftp.FtpDirEntry"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addFact"), "declares method addFact");
        check(mnames.contains("canExexcute"), "declares method canExexcute");
        check(mnames.contains("canRead"), "declares method canRead");
        check(mnames.contains("canWrite"), "declares method canWrite");
        check(mnames.contains("getCreated"), "declares method getCreated");
        check(mnames.contains("getFact"), "declares method getFact");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
