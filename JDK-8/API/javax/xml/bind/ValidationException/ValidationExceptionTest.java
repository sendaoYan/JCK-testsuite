/*
 * @test
 * @summary javax.xml.bind.ValidationException conformance: exception hierarchy and constructors.
 * @run main ValidationExceptionTest
 */


public class ValidationExceptionTest {
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
        check(javax.xml.bind.ValidationException.class.getSuperclass().getName().equals("javax.xml.bind.JAXBException"), "extends javax.xml.bind.JAXBException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
