/*
 * @test
 * @summary sun.net.ConnectionResetException conformance: exception/error hierarchy and construction.
 * @run main ConnectionResetExceptionTest
 */

public class ConnectionResetExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ConnectionResetException", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ConnectionResetException"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.SocketException"), "superclass java.net.SocketException");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        sun.net.ConnectionResetException x = new sun.net.ConnectionResetException("msg");
        check(x instanceof Throwable, "is a Throwable");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
