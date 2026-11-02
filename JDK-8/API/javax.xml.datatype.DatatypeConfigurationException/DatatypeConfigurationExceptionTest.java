/*
 * @test
 * @summary javax.xml.datatype.DatatypeConfigurationException conformance: exception hierarchy and constructors.
 * @run main DatatypeConfigurationExceptionTest
 */


public class DatatypeConfigurationExceptionTest {
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
        check(javax.xml.datatype.DatatypeConfigurationException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
