/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException conformance: safe value/exception behavior.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.exceptions:+open
 * @run main Base64DecodingExceptionTest
 */
public class Base64DecodingExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException e = new com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException("xml.base64");
        check(e.getMsgID().equals("xml.base64"), "getMsgID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
