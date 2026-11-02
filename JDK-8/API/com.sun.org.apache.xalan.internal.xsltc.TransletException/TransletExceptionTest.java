/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.TransletException conformance: exception construction.
 * @run main TransletExceptionTest
 */

public class TransletExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xalan.internal.xsltc.TransletException x = new com.sun.org.apache.xalan.internal.xsltc.TransletException("msg");
        check(java.lang.Throwable.class.isAssignableFrom(x.getClass()), "is Throwable");
        check(org.xml.sax.SAXException.class.isAssignableFrom(x.getClass()), "extends org.xml.sax.SAXException");
        check("msg".equals(x.getMessage()), "message equals msg");
        check(x.getCause() == null, "cause is null");
        check(x.toString() != null, "toString non-null");
        x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
