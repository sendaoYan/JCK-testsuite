/*
 * @test
 * @summary javax.net.ssl.SSLEngineResult.HandshakeStatus conformance: enum constants.
 * @run main HandshakeStatusTest
 */

public class HandshakeStatusTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLEngineResult$HandshakeStatus");
        check(c.isEnum(), "is enum");
        Object[] cs = c.getEnumConstants();
        check(cs.length == 5, "constant count");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object o : cs) names.add(o.toString());
        String[] expect = { "FINISHED", "NEED_TASK", "NEED_UNWRAP", "NEED_WRAP", "NOT_HANDSHAKING" };
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expect))), "exact constants");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
