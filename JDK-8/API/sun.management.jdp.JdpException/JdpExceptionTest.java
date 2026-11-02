/*
 * @test
 * @summary sun.management.jdp.JdpException conformance: exception/error hierarchy and construction.
 * @run main JdpExceptionTest
 */

public class JdpExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.jdp.JdpException", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.jdp.JdpException"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        sun.management.jdp.JdpException x = new sun.management.jdp.JdpException("msg");
        check(x instanceof Throwable, "is a Throwable");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
