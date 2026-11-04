/*
 * @test
 * @summary java.net.http.HttpOption.Http3DiscoveryMode conformance: enum constants and order.
 * @run main Http3DiscoveryModeTest
 */

import java.net.URI;
import java.net.http.HttpOption;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class Http3DiscoveryModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
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

        // ---- deepened: full enum shape ----
        check("ANY".equals(HttpOption.Http3DiscoveryMode.ANY.name()), "ANY name");
        check("ALT_SVC".equals(HttpOption.Http3DiscoveryMode.ALT_SVC.name()), "ALT_SVC name");
        check(HttpOption.Http3DiscoveryMode.ANY.ordinal() == 0, "ANY ordinal 0");
        check(HttpOption.Http3DiscoveryMode.ALT_SVC.ordinal() == 1, "ALT_SVC ordinal 1");
        check(HttpOption.Http3DiscoveryMode.HTTP_3_URI_ONLY.ordinal() == 2, "HTTP_3_URI_ONLY ordinal 2");
        check("ANY".equals(HttpOption.Http3DiscoveryMode.ANY.toString()), "ANY toString");
        check(HttpOption.Http3DiscoveryMode.ANY == HttpOption.Http3DiscoveryMode.valueOf("ANY"),
                "valueOf ANY");
        check(HttpOption.Http3DiscoveryMode.values() != HttpOption.Http3DiscoveryMode.values(),
                "values() returns a fresh array (probed)");
        expect(IllegalArgumentException.class, () -> HttpOption.Http3DiscoveryMode.valueOf("BOGUS"),
                "valueOf(bogus) IAE (probed)");
        expect(IllegalArgumentException.class, () -> HttpOption.Http3DiscoveryMode.valueOf("any"),
                "valueOf(lowercase) IAE");
        expect(NullPointerException.class,
                () -> HttpOption.Http3DiscoveryMode.valueOf(null), "valueOf(null) NPE");
        expect(IllegalArgumentException.class, () -> HttpOption.Http3DiscoveryMode.valueOf(""),
                "valueOf(empty) IAE");
        check(EnumSet.allOf(HttpOption.Http3DiscoveryMode.class).size() == 3, "EnumSet allOf size 3");
        check(EnumSet.range(HttpOption.Http3DiscoveryMode.ANY,
                HttpOption.Http3DiscoveryMode.ALT_SVC).size() == 2, "EnumSet range size 2");
        check(HttpOption.Http3DiscoveryMode.ANY.compareTo(HttpOption.Http3DiscoveryMode.ALT_SVC) < 0,
                "ANY < ALT_SVC");
        check(HttpOption.Http3DiscoveryMode.HTTP_3_URI_ONLY
                        .compareTo(HttpOption.Http3DiscoveryMode.ALT_SVC) > 0,
                "HTTP_3_URI_ONLY > ALT_SVC");
        check(HttpOption.Http3DiscoveryMode.ALT_SVC.compareTo(HttpOption.Http3DiscoveryMode.ALT_SVC) == 0,
                "compareTo self is 0");
        List<HttpOption.Http3DiscoveryMode> sorted = new ArrayList<>(List.of(
                HttpOption.Http3DiscoveryMode.HTTP_3_URI_ONLY, HttpOption.Http3DiscoveryMode.ANY));
        java.util.Collections.sort(sorted);
        check(sorted.equals(List.of(HttpOption.Http3DiscoveryMode.ANY,
                HttpOption.Http3DiscoveryMode.HTTP_3_URI_ONLY)), "natural order sort");
        check(HttpOption.Http3DiscoveryMode.valueOf(HttpOption.Http3DiscoveryMode.ANY.name())
                        == HttpOption.Http3DiscoveryMode.ANY, "name/valueOf round-trip");

        // ---- deepened: type() of H3_DISCOVERY is this enum; setOption integration ----
        check(HttpOption.H3_DISCOVERY.type() == HttpOption.Http3DiscoveryMode.class,
                "H3_DISCOVERY.type() is Http3DiscoveryMode");
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("https://example.com/"));
        for (HttpOption.Http3DiscoveryMode mode : values) {
            b.setOption(HttpOption.H3_DISCOVERY, mode);
            Optional<HttpOption.Http3DiscoveryMode> got =
                    b.build().getOption(HttpOption.H3_DISCOVERY);
            check(got.isPresent() && got.get() == mode, "setOption round-trip " + mode);
        }
        check(!HttpRequest.newBuilder(URI.create("https://example.com/")).build()
                        .getOption(HttpOption.H3_DISCOVERY).isPresent(),
                "no default discovery mode");

        // switch exhaustiveness over the three constants.
        int seen = 0;
        for (HttpOption.Http3DiscoveryMode mode : values) {
            switch (mode) {
                case ANY -> seen |= 1;
                case ALT_SVC -> seen |= 2;
                case HTTP_3_URI_ONLY -> seen |= 4;
            }
        }
        check(seen == 7, "switch covers all three constants");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
