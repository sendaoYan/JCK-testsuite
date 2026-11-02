/*
 * @test
 * @summary org.ietf.jgss.GSSException conformance: GSS exception major/minor status.
 * @run main GSSExceptionTest
 */

import org.ietf.jgss.*;

public class GSSExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        GSSException e = new GSSException(GSSException.BAD_NAME);
        check(e.getMajor() == GSSException.BAD_NAME, "getMajor");
        check(e.getMajorString() != null, "getMajorString non-null");
        GSSException e2 = new GSSException(GSSException.BAD_MECH, 5, "detail");
        check(e2.getMinor() == 5, "getMinor");
        check("detail".equals(e2.getMinorString()), "getMinorString");
        check(e2.getMessage() != null, "getMessage non-null");
        e2.setMinor(7, "new");
        check(e2.getMinor() == 7, "setMinor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
