/*
 * @test
 * @summary sun.net.ftp.FtpLoginException conformance: exception/error hierarchy and construction.
 * @run main FtpLoginExceptionTest
 */

public class FtpLoginExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ftp.FtpLoginException", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ftp.FtpLoginException"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.IOException"), "superclass java.io.IOException");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        sun.net.ftp.FtpLoginException x = new sun.net.ftp.FtpLoginException("msg");
        check(x instanceof Throwable, "is a Throwable");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
