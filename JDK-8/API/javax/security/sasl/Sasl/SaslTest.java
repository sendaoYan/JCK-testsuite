/*
 * @test
 * @summary javax.security.sasl.Sasl conformance: createSaslClient/getSaslClientFactories.
 * @run main SaslTest
 */

public class SaslTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.sasl.SaslClient client = javax.security.sasl.Sasl.createSaslClient(
            new String[]{"__nonexistent__"}, null, null, null, null, null);
        check(client == null, "createSaslClient returns null for unknown mechanism");
        check(javax.security.sasl.Sasl.getSaslClientFactories() != null, "getSaslClientFactories");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
