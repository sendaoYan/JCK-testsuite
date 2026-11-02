/*
 * @test
 * @summary sun.net.ftp.FtpProtocolException conformance: exception/error hierarchy and construction.
 * @run main FtpProtocolExceptionTest
 */

public class FtpProtocolExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ftp.FtpProtocolException", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ftp.FtpProtocolException"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getReplyCode"), "declares method getReplyCode");
        sun.net.ftp.FtpProtocolException x = new sun.net.ftp.FtpProtocolException("msg");
        check(x instanceof Throwable, "is a Throwable");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
