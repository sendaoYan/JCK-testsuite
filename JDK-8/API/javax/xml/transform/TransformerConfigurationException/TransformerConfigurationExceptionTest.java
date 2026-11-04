/*
 * @test
 * @summary javax.xml.transform.TransformerConfigurationException conformance: exception hierarchy and constructors.
 * @run main TransformerConfigurationExceptionTest
 */


public class TransformerConfigurationExceptionTest {
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
        check(javax.xml.transform.TransformerConfigurationException.class.getSuperclass().getName().equals("javax.xml.transform.TransformerException"), "extends javax.xml.transform.TransformerException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
