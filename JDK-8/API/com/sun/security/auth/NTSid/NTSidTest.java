/*
 * @test
 * @summary com.sun.security.auth.NTSid conformance: behavior of constructors, accessors and value semantics.
 * @run main NTSidTest
 */

public class NTSidTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.security.auth.NTSid p = new com.sun.security.auth.NTSid("S-1-5-21");
        check(p instanceof java.security.Principal, "implements java.security.Principal");
        check("S-1-5-21".equals(p.getName()), "getName");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.NTSid("S-1-5-21")), "equals");
        check(p.hashCode() == new com.sun.security.auth.NTSid("S-1-5-21").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
