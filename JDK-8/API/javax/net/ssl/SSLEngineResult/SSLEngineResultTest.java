/*
 * @test
 * @summary javax.net.ssl.SSLEngineResult conformance: constructor and getters.
 * @run main SSLEngineResultTest
 */

public class SSLEngineResultTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLEngineResult r = new javax.net.ssl.SSLEngineResult(
            javax.net.ssl.SSLEngineResult.Status.OK, javax.net.ssl.SSLEngineResult.HandshakeStatus.FINISHED, 10, 20);
        check(r.getStatus() == javax.net.ssl.SSLEngineResult.Status.OK, "getStatus");
        check(r.getHandshakeStatus() == javax.net.ssl.SSLEngineResult.HandshakeStatus.FINISHED, "getHandshakeStatus");
        check(r.bytesConsumed() == 10, "bytesConsumed");
        check(r.bytesProduced() == 20, "bytesProduced");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
