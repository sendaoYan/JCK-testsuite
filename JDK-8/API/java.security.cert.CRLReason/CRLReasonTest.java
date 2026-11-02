/*
 * @test
 * @summary java.security.cert.CRLReason conformance: enum constants.
 * @run main CRLReasonTest
 */

public class CRLReasonTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.CRLReason");
        check(c.isEnum(), "is enum");
        Object[] cs = c.getEnumConstants();
        check(cs.length == 11, "constant count");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object o : cs) names.add(o.toString());
        String[] expect = { "AA_COMPROMISE", "AFFILIATION_CHANGED", "CA_COMPROMISE", "CERTIFICATE_HOLD", "CESSATION_OF_OPERATION", "KEY_COMPROMISE", "PRIVILEGE_WITHDRAWN", "REMOVE_FROM_CRL", "SUPERSEDED", "UNSPECIFIED", "UNUSED" };
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expect))), "exact constants");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
