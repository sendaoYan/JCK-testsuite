/*
 * @test
 * @summary java.security.cert.CertPathValidatorException.BasicReason conformance: enum constants.
 * @run main BasicReasonTest
 */

public class BasicReasonTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.CertPathValidatorException$BasicReason");
        check(c.isEnum(), "is enum");
        Object[] cs = c.getEnumConstants();
        check(cs.length == 7, "constant count");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object o : cs) names.add(o.toString());
        String[] expect = { "ALGORITHM_CONSTRAINED", "EXPIRED", "INVALID_SIGNATURE", "NOT_YET_VALID", "REVOKED", "UNDETERMINED_REVOCATION_STATUS", "UNSPECIFIED" };
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expect))), "exact constants");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
