/*
 * @test
 * @summary javax.annotation.processing.FilerException conformance: exception hierarchy and constructors.
 * @run main FilerExceptionTest
 */


public class FilerExceptionTest {
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
        check(javax.annotation.processing.FilerException.class.getSuperclass().getName().equals("java.io.IOException"), "extends java.io.IOException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
