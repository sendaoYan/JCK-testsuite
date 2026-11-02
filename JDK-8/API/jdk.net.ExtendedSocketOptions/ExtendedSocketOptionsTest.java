/*
 * @test
 * @summary jdk.net.ExtendedSocketOptions conformance: extended socket options.
 * @run main ExtendedSocketOptionsTest
 */

public class ExtendedSocketOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(jdk.net.ExtendedSocketOptions.SO_FLOW_SLA != null, "SO_FLOW_SLA");
        check(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE != null, "TCP_KEEPIDLE");
        check(jdk.net.ExtendedSocketOptions.TCP_KEEPINTERVAL != null, "TCP_KEEPINTERVAL");
        check(jdk.net.ExtendedSocketOptions.TCP_KEEPCOUNT != null, "TCP_KEEPCOUNT");
        check(!jdk.net.ExtendedSocketOptions.SO_FLOW_SLA.equals(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE), "distinct options");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
