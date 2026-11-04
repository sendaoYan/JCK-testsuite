/*
 * @test
 * @summary javax.xml.bind.TypeConstraintException conformance: exception hierarchy and constructors.
 * @run main TypeConstraintExceptionTest
 */


public class TypeConstraintExceptionTest {
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
        check(javax.xml.bind.TypeConstraintException.class.getSuperclass().getName().equals("java.lang.RuntimeException"), "extends java.lang.RuntimeException");
        javax.xml.bind.TypeConstraintException te = new javax.xml.bind.TypeConstraintException("msg", "code");
        check("code".equals(te.getErrorCode()), "error code");
        java.lang.Throwable le = new java.lang.Exception("linked");
        te.setLinkedException(le);
        check(te.getLinkedException() == le, "linked exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
