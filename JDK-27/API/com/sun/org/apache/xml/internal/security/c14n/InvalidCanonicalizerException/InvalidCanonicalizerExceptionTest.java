/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.InvalidCanonicalizerException conformance: safe value/exception behavior.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.c14n:+open
 * @run main InvalidCanonicalizerExceptionTest
 */
public class InvalidCanonicalizerExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xml.internal.security.c14n.InvalidCanonicalizerException e = new com.sun.org.apache.xml.internal.security.c14n.InvalidCanonicalizerException("c14n.invalid");
        check(e.getMsgID().equals("c14n.invalid"), "getMsgID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
