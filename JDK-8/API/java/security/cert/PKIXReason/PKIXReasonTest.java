/*
 * @test
 * @summary java.security.cert.PKIXReason conformance: enum constants.
 * @run main PKIXReasonTest
 */

public class PKIXReasonTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.PKIXReason");
        check(c.isEnum(), "is enum");
        Object[] cs = c.getEnumConstants();
        check(cs.length == 8, "constant count");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object o : cs) names.add(o.toString());
        String[] expect = { "INVALID_KEY_USAGE", "INVALID_NAME", "INVALID_POLICY", "NAME_CHAINING", "NOT_CA_CERT", "NO_TRUST_ANCHOR", "PATH_TOO_LONG", "UNRECOGNIZED_CRIT_EXT" };
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expect))), "exact constants");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
