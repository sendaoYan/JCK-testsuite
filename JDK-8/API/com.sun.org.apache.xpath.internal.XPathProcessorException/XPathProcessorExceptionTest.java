/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.XPathProcessorException conformance: exception construction.
 * @run main XPathProcessorExceptionTest
 */

public class XPathProcessorExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xpath.internal.XPathProcessorException x = new com.sun.org.apache.xpath.internal.XPathProcessorException("msg");
        check(java.lang.Throwable.class.isAssignableFrom(x.getClass()), "is Throwable");
        check(com.sun.org.apache.xpath.internal.XPathException.class.isAssignableFrom(x.getClass()), "extends com.sun.org.apache.xpath.internal.XPathException");
        check("msg".equals(x.getMessage()), "message equals msg");
        check(x.getCause() == null, "cause is null");
        check(x.toString() != null, "toString non-null");
        x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
