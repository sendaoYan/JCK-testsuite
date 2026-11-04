/*
 * @test
 * @summary org.ietf.jgss.GSSException conformance: constructors and getters.
 * @run main GSSExceptionTest
 */

public class GSSExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.ietf.jgss.GSSException e = new org.ietf.jgss.GSSException(org.ietf.jgss.GSSException.DEFECTIVE_TOKEN);
        check(e.getMajor() == org.ietf.jgss.GSSException.DEFECTIVE_TOKEN, "getMajor");
        check(e.getMajorString() != null, "getMajorString");
        org.ietf.jgss.GSSException e2 = new org.ietf.jgss.GSSException(org.ietf.jgss.GSSException.BAD_MECH, 0, "custom");
        check(e2.getMinor() == 0, "getMinor");
        check(e2.getMessage() != null, "getMessage non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
