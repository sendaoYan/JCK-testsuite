/*
 * @test
 * @summary java.net.http.HttpOption conformance: interface with H3_DISCOVERY constant.
 * @run main HttpOptionTest
 */

import java.net.http.HttpOption;

public class HttpOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpOption.class.isInterface(), "HttpOption is an interface");
        check(HttpOption.H3_DISCOVERY != null, "H3_DISCOVERY constant non-null");
        check("H3_DISCOVERY".equals(HttpOption.H3_DISCOVERY.name()), "H3_DISCOVERY name");
        check(HttpOption.H3_DISCOVERY.type() == HttpOption.Http3DiscoveryMode.class,
                "H3_DISCOVERY type is Http3DiscoveryMode");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
