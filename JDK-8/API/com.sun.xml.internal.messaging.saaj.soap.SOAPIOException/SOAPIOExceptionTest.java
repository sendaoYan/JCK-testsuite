/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.SOAPIOException conformance: exception hierarchy and construction.
 * @run main SOAPIOExceptionTest
 */

public class SOAPIOExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.xml.internal.messaging.saaj.soap.SOAPIOException x = new com.sun.xml.internal.messaging.saaj.soap.SOAPIOException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check("java.io.IOException".equals(c.getSuperclass().getName()), "extends java.io.IOException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
