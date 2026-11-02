/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.SOAPExceptionImpl conformance: exception hierarchy and construction.
 * @run main SOAPExceptionImplTest
 */

public class SOAPExceptionImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.xml.internal.messaging.saaj.SOAPExceptionImpl x = new com.sun.xml.internal.messaging.saaj.SOAPExceptionImpl("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check("javax.xml.soap.SOAPException".equals(c.getSuperclass().getName()), "extends javax.xml.soap.SOAPException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
