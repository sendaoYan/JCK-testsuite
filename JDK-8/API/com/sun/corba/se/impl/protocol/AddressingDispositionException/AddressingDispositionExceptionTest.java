/*
 * @test
 * @summary com.sun.corba.se.impl.protocol.AddressingDispositionException conformance: exception hierarchy and construction (headless-safe).
 * @run main AddressingDispositionExceptionTest
 */

public class AddressingDispositionExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.corba.se.impl.protocol.AddressingDispositionException x = new com.sun.corba.se.impl.protocol.AddressingDispositionException((short) 0);
        check(Throwable.class.isAssignableFrom(x.getClass()), "is a Throwable");
        check(java.lang.RuntimeException.class.isAssignableFrom(x.getClass()), "extends java.lang.RuntimeException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
