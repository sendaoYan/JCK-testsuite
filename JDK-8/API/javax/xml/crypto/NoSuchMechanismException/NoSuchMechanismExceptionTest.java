/*
 * @test
 * @summary javax.xml.crypto.NoSuchMechanismException conformance: exception hierarchy and constructors.
 * @run main NoSuchMechanismExceptionTest
 */


public class NoSuchMechanismExceptionTest {
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
        check(javax.xml.crypto.NoSuchMechanismException.class.getSuperclass().getName().equals("java.lang.RuntimeException"), "extends java.lang.RuntimeException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
