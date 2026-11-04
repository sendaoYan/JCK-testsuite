/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.XPathException conformance: exception construction.
 * @run main XPathExceptionTest
 */

public class XPathExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xpath.internal.XPathException x = new com.sun.org.apache.xpath.internal.XPathException("msg");
        check(java.lang.Throwable.class.isAssignableFrom(x.getClass()), "is Throwable");
        check(javax.xml.transform.TransformerException.class.isAssignableFrom(x.getClass()), "extends javax.xml.transform.TransformerException");
        check("msg".equals(x.getMessage()), "message equals msg");
        check(x.getCause() == null, "cause is null");
        check(x.toString() != null, "toString non-null");
        x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
