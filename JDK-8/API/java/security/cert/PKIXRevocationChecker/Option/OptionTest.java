/*
 * @test
 * @summary java.security.cert.PKIXRevocationChecker.Option conformance: enum constants.
 * @run main OptionTest
 */

public class OptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.PKIXRevocationChecker$Option");
        check(c.isEnum(), "is enum");
        Object[] cs = c.getEnumConstants();
        check(cs.length == 4, "constant count");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object o : cs) names.add(o.toString());
        String[] expect = { "NO_FALLBACK", "ONLY_END_ENTITY", "PREFER_CRLS", "SOFT_FAIL" };
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expect))), "exact constants");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
