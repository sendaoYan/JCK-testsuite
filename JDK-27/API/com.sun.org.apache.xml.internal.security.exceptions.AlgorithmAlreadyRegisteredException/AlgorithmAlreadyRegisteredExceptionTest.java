/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.exceptions.AlgorithmAlreadyRegisteredException conformance: safe value/exception behavior.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.exceptions:+open
 * @run main AlgorithmAlreadyRegisteredExceptionTest
 */
public class AlgorithmAlreadyRegisteredExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xml.internal.security.exceptions.AlgorithmAlreadyRegisteredException e = new com.sun.org.apache.xml.internal.security.exceptions.AlgorithmAlreadyRegisteredException("xml.algo.already");
        check(e.getMsgID().equals("xml.algo.already"), "getMsgID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
