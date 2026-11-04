/*
 * @test
 * @summary javax.net.ssl.SSLEngineResult.Status conformance: enum constants.
 * @run main StatusTest
 */

public class StatusTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLEngineResult$Status");
        check(c.isEnum(), "is enum");
        Object[] cs = c.getEnumConstants();
        check(cs.length == 4, "constant count");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object o : cs) names.add(o.toString());
        String[] expect = { "BUFFER_OVERFLOW", "BUFFER_UNDERFLOW", "CLOSED", "OK" };
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expect))), "exact constants");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
