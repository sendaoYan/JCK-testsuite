/*
 * @test
 * @summary java.security.ProtectionDomain conformance: constructor and getters.
 * @run main ProtectionDomainTest
 */

public class ProtectionDomainTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.ProtectionDomain pd = new java.security.ProtectionDomain(null, null);
        check(pd.getCodeSource() == null, "getCodeSource null");
        check(pd.getPermissions() == null, "getPermissions null");
        check(pd.getClassLoader() == null, "getClassLoader null");
        check(pd.getPrincipals() != null && pd.getPrincipals().length == 0, "getPrincipals empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
