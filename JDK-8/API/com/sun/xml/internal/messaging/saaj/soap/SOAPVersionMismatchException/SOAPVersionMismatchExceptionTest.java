/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.SOAPVersionMismatchException conformance: exception hierarchy and construction.
 * @run main SOAPVersionMismatchExceptionTest
 */

public class SOAPVersionMismatchExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.xml.internal.messaging.saaj.soap.SOAPVersionMismatchException x = new com.sun.xml.internal.messaging.saaj.soap.SOAPVersionMismatchException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check("com.sun.xml.internal.messaging.saaj.SOAPExceptionImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.SOAPExceptionImpl");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
