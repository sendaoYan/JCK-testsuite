/*
 * @test
 * @summary java.net.http.HttpOption.Http3DiscoveryMode conformance: enum constants and order.
 * @run main Http3DiscoveryModeTest
 */

import java.net.http.HttpOption;

public class Http3DiscoveryModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpOption.Http3DiscoveryMode.class.isEnum(), "Http3DiscoveryMode is an enum");

        HttpOption.Http3DiscoveryMode[] values = HttpOption.Http3DiscoveryMode.values();
        check(values.length == 3, "three constants");
        check(values[0] == HttpOption.Http3DiscoveryMode.ANY, "ANY is first");
        check(values[1] == HttpOption.Http3DiscoveryMode.ALT_SVC, "ALT_SVC is second");
        check(values[2] == HttpOption.Http3DiscoveryMode.HTTP_3_URI_ONLY, "HTTP_3_URI_ONLY is third");

        check(HttpOption.Http3DiscoveryMode.valueOf("ALT_SVC") == HttpOption.Http3DiscoveryMode.ALT_SVC,
                "valueOf ALT_SVC");
        check("HTTP_3_URI_ONLY".equals(HttpOption.Http3DiscoveryMode.HTTP_3_URI_ONLY.name()),
                "HTTP_3_URI_ONLY name");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
