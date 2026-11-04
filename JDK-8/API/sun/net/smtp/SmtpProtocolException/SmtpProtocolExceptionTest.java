/*
 * @test
 * @summary sun.net.smtp.SmtpProtocolException conformance: exception/error hierarchy and construction.
 * @run main SmtpProtocolExceptionTest
 */

public class SmtpProtocolExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.smtp.SmtpProtocolException", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.smtp.SmtpProtocolException"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.IOException"), "superclass java.io.IOException");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
