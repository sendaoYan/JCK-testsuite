/*
 * @test
 * @summary javax.activation.UnsupportedDataTypeException conformance: exception hierarchy and constructors.
 * @run main UnsupportedDataTypeExceptionTest
 */


public class UnsupportedDataTypeExceptionTest {
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
        check(javax.activation.UnsupportedDataTypeException.class.getSuperclass().getName().equals("java.io.IOException"), "extends java.io.IOException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
