/*
 * @test
 * @summary com.sun.corba.se.spi.activation.ServerAlreadyRegistered conformance: exception hierarchy and construction (headless-safe).
 * @run main ServerAlreadyRegisteredTest
 */

public class ServerAlreadyRegisteredTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.corba.se.spi.activation.ServerAlreadyRegistered x = new com.sun.corba.se.spi.activation.ServerAlreadyRegistered();
        check(Throwable.class.isAssignableFrom(x.getClass()), "is a Throwable");
        check(org.omg.CORBA.UserException.class.isAssignableFrom(x.getClass()), "extends org.omg.CORBA.UserException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
