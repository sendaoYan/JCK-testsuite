/*
 * @test
 * @summary java.net.http.HttpOption conformance: white-box structure + black-box constant/type behavior.
 * @run main HttpOptionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpOption;
import java.net.http.HttpRequest;
import java.util.Optional;

public class HttpOptionTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.http.HttpOption");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "HttpOption is an interface");
        check(c.getTypeParameters().length == 1, "generic type parameter present");
        check(c.getField("H3_DISCOVERY") != null, "H3_DISCOVERY constant present");

        // ---- deepened: sealed hierarchy and method surface (probed) ----
        check(c.isSealed(), "HttpOption is sealed (JDK-27, probed)");
        Class<?>[] permitted = c.getPermittedSubclasses();
        check(permitted != null && permitted.length == 1, "one permitted subclass");
        check(permitted != null && permitted[0].getName().equals("java.net.http.HttpRequestOptionImpl"),
                "permits java.net.http.HttpRequestOptionImpl (probed)");
        check(!Modifier.isPublic(permitted[0].getModifiers()) || permitted[0].getPackageName().startsWith("java.net.http"),
                "permitted impl lives in java.net.http");
        Method[] declared = c.getDeclaredMethods();
        check(declared.length == 2, "exactly two declared methods (probed)");
        Method name = c.getMethod("name");
        Method type = c.getMethod("type");
        check(name.getReturnType() == String.class, "name() returns String");
        check(type.getReturnType() == Class.class, "type() returns Class<?>");
        check(Modifier.isAbstract(name.getModifiers()), "name() abstract");
        check(Modifier.isAbstract(type.getModifiers()), "type() abstract");
        check(Modifier.isPublic(c.getField("H3_DISCOVERY").getModifiers()), "H3_DISCOVERY public");
        check(Modifier.isStatic(c.getField("H3_DISCOVERY").getModifiers()), "H3_DISCOVERY static");
        check(Modifier.isFinal(c.getField("H3_DISCOVERY").getModifiers()), "H3_DISCOVERY final");
        check(c.getField("H3_DISCOVERY").getGenericType().getTypeName()
                        .startsWith("java.net.http.HttpOption<java.net.http.HttpOption$Http3DiscoveryMode>"),
                "H3_DISCOVERY is HttpOption<Http3DiscoveryMode>");
        check(c.getInterfaces().length == 0, "HttpOption extends nothing");
        check(!c.isAnnotationPresent(FunctionalInterface.class), "not a FunctionalInterface");
    }

    static void testBlackBox() throws Exception {
        check(HttpOption.H3_DISCOVERY != null, "H3_DISCOVERY constant non-null");
        check("H3_DISCOVERY".equals(HttpOption.H3_DISCOVERY.name()), "H3_DISCOVERY name");
        check(HttpOption.H3_DISCOVERY.type() == HttpOption.Http3DiscoveryMode.class,
                "H3_DISCOVERY type is Http3DiscoveryMode");
        check(HttpOption.H3_DISCOVERY instanceof HttpOption, "H3_DISCOVERY is an HttpOption");
        check("H3_DISCOVERY".equals(HttpOption.H3_DISCOVERY.toString()), "toString is the name (probed)");
        check(HttpOption.H3_DISCOVERY.type().isEnum(), "its type is the discovery-mode enum");

        // Enum surface of Http3DiscoveryMode.
        HttpOption.Http3DiscoveryMode[] values = HttpOption.Http3DiscoveryMode.values();
        check(values.length == 3, "Http3DiscoveryMode has three values");
        check(HttpOption.Http3DiscoveryMode.valueOf("ANY") == HttpOption.Http3DiscoveryMode.ANY, "valueOf ANY");
        check(HttpOption.Http3DiscoveryMode.valueOf("ALT_SVC") == HttpOption.Http3DiscoveryMode.ALT_SVC, "valueOf ALT_SVC");
        check(HttpOption.Http3DiscoveryMode.valueOf("HTTP_3_URI_ONLY") == HttpOption.Http3DiscoveryMode.HTTP_3_URI_ONLY,
                "valueOf HTTP_3_URI_ONLY");

        // ---- deepened: HttpRequest.Builder setOption integration (probed) ----
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("https://example.com/"));
        check(!b.build().getOption(HttpOption.H3_DISCOVERY).isPresent(),
                "option absent by default (probed)");
        b.setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ALT_SVC);
        Optional<HttpOption.Http3DiscoveryMode> got = b.build().getOption(HttpOption.H3_DISCOVERY);
        check(got.isPresent() && got.get() == HttpOption.Http3DiscoveryMode.ALT_SVC,
                "setOption round-trip through build() (probed)");
        b.setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ANY);
        check(b.build().getOption(HttpOption.H3_DISCOVERY).orElseThrow()
                        == HttpOption.Http3DiscoveryMode.ANY,
                "setOption overwrites the previous value");
        b.setOption(HttpOption.H3_DISCOVERY, null);
        check(!b.build().getOption(HttpOption.H3_DISCOVERY).isPresent(),
                "setOption(..., null) removes the option (probed)");
        b.setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.HTTP_3_URI_ONLY);
        check(b.build().getOption(HttpOption.H3_DISCOVERY).isPresent(), "re-set after removal works");

        // setOption(null) NPE; wrong value type IAE (probed).
        expect(NullPointerException.class,
                () -> HttpRequest.newBuilder(URI.create("https://example.com/"))
                        .setOption(null, HttpOption.Http3DiscoveryMode.ANY),
                "setOption(null, v) NPE");
        setBadOptionType();
        passed++; // setBadOptionType throws on failure

        // Builder.copy() carries options (probed).
        HttpRequest withOpt = HttpRequest.newBuilder(URI.create("https://example.com/"))
                .setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ALT_SVC)
                .build();
        check(HttpRequest.newBuilder(URI.create("https://example.com/"))
                        .setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ALT_SVC)
                        .copy().build().getOption(HttpOption.H3_DISCOVERY)
                        .orElseThrow() == HttpOption.Http3DiscoveryMode.ALT_SVC,
                "Builder.copy() keeps options (probed)");
        check(withOpt.getOption(HttpOption.H3_DISCOVERY).orElseThrow()
                        == HttpOption.Http3DiscoveryMode.ALT_SVC,
                "request.getOption() accessor matches");

        // The generic option type is invariant: a raw read yields the enum type.
        HttpOption<HttpOption.Http3DiscoveryMode> typed = HttpOption.H3_DISCOVERY;
        check(typed.type() == HttpOption.Http3DiscoveryMode.class, "typed variable keeps its type");

        // ---- deepened: getOption lookup negatives (probed) ----
        HttpRequest plain = HttpRequest.newBuilder(URI.create("https://example.com/")).build();
        expect(NullPointerException.class, () -> plain.getOption(null), "getOption(null) NPE (probed)");
        check(!plain.getOption(HttpOption.H3_DISCOVERY).isPresent(), "absent option is Optional.empty");
        check(plain.getOption(HttpOption.H3_DISCOVERY).equals(Optional.empty()),
                "absent option equals Optional.empty");
    }

    /** Wrong-typed value for the option must be rejected (probed IAE). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void setBadOptionType() {
        HttpOption raw = HttpOption.H3_DISCOVERY;
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("https://example.com/"));
        try {
            b.setOption(raw, (Object) "notAMode");
            throw new AssertionError("setOption with wrong type must throw");
        } catch (IllegalArgumentException expected) {
            // probed: IllegalArgumentException
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
