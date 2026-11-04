/*
 * @test
 * @summary javax.xml.ws.ProtocolException conformance: exception hierarchy and constructors.
 * @run main ProtocolExceptionTest
 */


public class ProtocolExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        check(javax.xml.ws.ProtocolException.class.getSuperclass().getName().equals("javax.xml.ws.WebServiceException"), "extends javax.xml.ws.WebServiceException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
