/*
 * @test
 * @summary java.net.http.HttpOption conformance: white-box structure + black-box constant/type behavior.
 * @run main HttpOptionTest
 */

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.http.HttpOption;

public class HttpOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.http.HttpOption");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "HttpOption is an interface");
        check(c.getTypeParameters().length == 1, "generic type parameter present");
        check(c.getField("H3_DISCOVERY") != null, "H3_DISCOVERY constant present");
    }

    static void testBlackBox() throws Exception {
        check(HttpOption.H3_DISCOVERY != null, "H3_DISCOVERY constant non-null");
        check("H3_DISCOVERY".equals(HttpOption.H3_DISCOVERY.name()), "H3_DISCOVERY name");
        check(HttpOption.H3_DISCOVERY.type() == HttpOption.Http3DiscoveryMode.class,
                "H3_DISCOVERY type is Http3DiscoveryMode");
        check(HttpOption.H3_DISCOVERY instanceof HttpOption, "H3_DISCOVERY is an HttpOption");

        // Enum surface of Http3DiscoveryMode.
        HttpOption.Http3DiscoveryMode[] values = HttpOption.Http3DiscoveryMode.values();
        check(values.length == 3, "Http3DiscoveryMode has three values");
        check(HttpOption.Http3DiscoveryMode.valueOf("ANY") == HttpOption.Http3DiscoveryMode.ANY, "valueOf ANY");
        check(HttpOption.Http3DiscoveryMode.valueOf("ALT_SVC") == HttpOption.Http3DiscoveryMode.ALT_SVC, "valueOf ALT_SVC");
        check(HttpOption.Http3DiscoveryMode.valueOf("HTTP_3_URI_ONLY") == HttpOption.Http3DiscoveryMode.HTTP_3_URI_ONLY,
                "valueOf HTTP_3_URI_ONLY");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
