/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException conformance: safe value/exception behavior.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.exceptions:+open
 * @run main XMLSecurityExceptionTest
 */
public class XMLSecurityExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException e = new com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException("alg.generic");
        check(e.getMsgID().equals("alg.generic"), "getMsgID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
