/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException conformance: safe value/exception behavior.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.c14n:+open
 * @run main CanonicalizationExceptionTest
 */
public class CanonicalizationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException e = new com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException("c14n.generic");
        check(e.getMsgID().equals("c14n.generic"), "getMsgID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
