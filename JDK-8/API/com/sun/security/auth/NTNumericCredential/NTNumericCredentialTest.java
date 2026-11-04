/*
 * @test
 * @summary com.sun.security.auth.NTNumericCredential conformance: behavior of constructors, accessors and value semantics.
 * @run main NTNumericCredentialTest
 */

public class NTNumericCredentialTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.security.auth.NTNumericCredential p = new com.sun.security.auth.NTNumericCredential(12345L);
        check(p.getToken() == 12345L, "getToken");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.NTNumericCredential(12345L)), "equals");
        check(p.hashCode() == new com.sun.security.auth.NTNumericCredential(12345L).hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
