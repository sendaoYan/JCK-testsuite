/*
 * @test
 * @summary java.rmi.server.SocketSecurityException conformance: exception hierarchy and construction.
 * @run main SocketSecurityExceptionTest
 */

public class SocketSecurityExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.rmi.server.SocketSecurityException x = new java.rmi.server.SocketSecurityException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.rmi.server.ExportException").isAssignableFrom(c), "extends java.rmi.server.ExportException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
