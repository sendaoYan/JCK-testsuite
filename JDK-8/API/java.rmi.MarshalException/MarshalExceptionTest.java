/*
 * @test
 * @summary java.rmi.MarshalException conformance: exception hierarchy and construction.
 * @run main MarshalExceptionTest
 */

public class MarshalExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.rmi.MarshalException x = new java.rmi.MarshalException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.rmi.RemoteException").isAssignableFrom(c), "extends java.rmi.RemoteException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
