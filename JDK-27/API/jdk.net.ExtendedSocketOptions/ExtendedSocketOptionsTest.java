/*
 * @test
 * @summary jdk.net.ExtendedSocketOptions conformance: extended socket options.
 * @run main ExtendedSocketOptionsTest
 */

public class ExtendedSocketOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(jdk.net.ExtendedSocketOptions.TCP_QUICKACK != null, "TCP_QUICKACK");
        check("TCP_QUICKACK".equals(jdk.net.ExtendedSocketOptions.TCP_QUICKACK.name()), "TCP_QUICKACK name");
        check(jdk.net.ExtendedSocketOptions.TCP_QUICKACK.type() == java.lang.Boolean.class, "TCP_QUICKACK type");
        check(jdk.net.ExtendedSocketOptions.SO_PEERCRED != null, "SO_PEERCRED");
        check(jdk.net.ExtendedSocketOptions.SO_PEERCRED.type() == jdk.net.UnixDomainPrincipal.class, "SO_PEERCRED type");
        check(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE != null, "TCP_KEEPIDLE");
        check(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE.type() == java.lang.Integer.class, "TCP_KEEPIDLE type");
        check(jdk.net.ExtendedSocketOptions.IP_DONTFRAGMENT != null, "IP_DONTFRAGMENT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
