/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.fastinfoset.EncodingAlgorithmException conformance: exception hierarchy and construction.
 * @run main EncodingAlgorithmExceptionTest
 */

public class EncodingAlgorithmExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.xml.internal.org.jvnet.fastinfoset.EncodingAlgorithmException x = new com.sun.xml.internal.org.jvnet.fastinfoset.EncodingAlgorithmException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check("com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetException".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
