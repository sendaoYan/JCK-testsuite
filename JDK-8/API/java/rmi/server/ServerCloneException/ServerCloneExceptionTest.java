/*
 * @test
 * @summary java.rmi.server.ServerCloneException conformance: exception hierarchy and construction.
 * @run main ServerCloneExceptionTest
 */

public class ServerCloneExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.rmi.server.ServerCloneException x = new java.rmi.server.ServerCloneException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.lang.CloneNotSupportedException").isAssignableFrom(c), "extends java.lang.CloneNotSupportedException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
