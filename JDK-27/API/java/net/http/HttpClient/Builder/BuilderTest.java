/*
 * @test
 * @summary java.net.http.HttpClient.Builder conformance: builder methods return this and persist state.
 * @run main BuilderTest
 */

import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;

public class BuilderTest {
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

    public static void main(String[] args) throws Exception {
        check(HttpClient.Builder.class.isInterface(), "Builder is an interface");
        check(HttpClient.Builder.NO_PROXY != null, "NO_PROXY constant non-null");
        check(HttpClient.Builder.NO_PROXY instanceof ProxySelector, "NO_PROXY is a ProxySelector");
        check(Mod_public("NO_PROXY"), "NO_PROXY is public");

        HttpClient.Builder b = HttpClient.newBuilder();
        check(b != null, "newBuilder() non-null");
        check(b.followRedirects(HttpClient.Redirect.ALWAYS) == b, "followRedirects returns this");
        check(b.version(HttpClient.Version.HTTP_1_1) == b, "version returns this");
        check(b.connectTimeout(Duration.ofSeconds(5)) == b, "connectTimeout returns this");
        check(b.priority(7) == b, "priority returns this");
        check(b.localAddress(InetAddress.getLoopbackAddress()) == b, "localAddress returns this");

        HttpClient c = b.build();
        check(c != null, "build() non-null");
        check(c.followRedirects() == HttpClient.Redirect.ALWAYS, "redirect ALWAYS persisted");
        check(c.version() == HttpClient.Version.HTTP_1_1, "version HTTP_1_1 persisted");
        check(c.connectTimeout().isPresent()
                && Duration.ofSeconds(5).equals(c.connectTimeout().get()), "connectTimeout persisted");
        check(!c.authenticator().isPresent(), "authenticator empty by default");
        check(!c.cookieHandler().isPresent(), "cookieHandler empty by default");

        // ---- deepened: fluent "returns this" for every setter (JDK-27 surface) ----
        HttpClient.Builder f = HttpClient.newBuilder();
        check(f.cookieHandler(new CookieManager()) == f, "cookieHandler returns this");
        check(f.sslContext(javax.net.ssl.SSLContext.getDefault()) == f, "sslContext returns this");
        check(f.sslParameters(new javax.net.ssl.SSLParameters()) == f, "sslParameters returns this");
        check(f.executor(Runnable::run) == f, "executor returns this");
        check(f.proxy(ProxySelector.of(new java.net.InetSocketAddress("127.0.0.1", 1))) == f,
                "proxy returns this");
        check(f.authenticator(new Authenticator() { }) == f, "authenticator returns this");
        HttpClient fc = f.build();
        check(fc.cookieHandler().isPresent(), "cookieHandler persisted");
        check(fc.authenticator().isPresent(), "authenticator persisted");
        check(fc.proxy().isPresent(), "proxy persisted");
        check(fc.executor().isPresent(), "executor persisted");
        fc.close();

        // ---- deepened: null arguments are rejected with NPE (probed) ----
        HttpClient.Builder n = HttpClient.newBuilder();
        expect(NullPointerException.class, () -> n.cookieHandler(null), "cookieHandler(null) NPE");
        expect(NullPointerException.class, () -> n.connectTimeout(null), "connectTimeout(null) NPE");
        expect(NullPointerException.class, () -> n.sslContext(null), "sslContext(null) NPE");
        expect(NullPointerException.class, () -> n.sslParameters(null), "sslParameters(null) NPE");
        expect(NullPointerException.class, () -> n.executor(null), "executor(null) NPE");
        expect(NullPointerException.class, () -> n.followRedirects(null), "followRedirects(null) NPE");
        expect(NullPointerException.class, () -> n.version(null), "version(null) NPE");
        expect(NullPointerException.class, () -> n.proxy(null), "proxy(null) NPE");
        expect(NullPointerException.class, () -> n.authenticator(null), "authenticator(null) NPE");

        // ---- deepened: duration boundaries (probed messages) ----
        expect(IllegalArgumentException.class, () -> HttpClient.newBuilder().connectTimeout(Duration.ZERO),
                "connectTimeout(ZERO) IAE");
        expect(IllegalArgumentException.class,
                () -> HttpClient.newBuilder().connectTimeout(Duration.ofMillis(-1)),
                "connectTimeout(negative) IAE");
        HttpClient tiny = HttpClient.newBuilder().connectTimeout(Duration.ofNanos(1)).build();
        check(Duration.ofNanos(1).equals(tiny.connectTimeout().get()), "connectTimeout(1ns) accepted");
        tiny.close();

        // ---- deepened: priority range is [1, 256] inclusive (HTTP/2 weight, probed) ----
        expect(IllegalArgumentException.class, () -> HttpClient.newBuilder().priority(0),
                "priority(0) IAE");
        expect(IllegalArgumentException.class, () -> HttpClient.newBuilder().priority(257),
                "priority(257) IAE");
        expect(IllegalArgumentException.class, () -> HttpClient.newBuilder().priority(-1),
                "priority(-1) IAE");
        expect(IllegalArgumentException.class, () -> HttpClient.newBuilder().priority(Integer.MIN_VALUE),
                "priority(MIN_VALUE) IAE");
        expect(IllegalArgumentException.class, () -> HttpClient.newBuilder().priority(Integer.MAX_VALUE),
                "priority(MAX_VALUE) IAE");
        HttpClient lo = HttpClient.newBuilder().priority(1).build();
        lo.close();
        check(true, "priority(1) accepted");
        HttpClient hi = HttpClient.newBuilder().priority(256).build();
        hi.close();
        check(true, "priority(256) accepted");

        // ---- deepened: exact reference round-trips ----
        javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getDefault();
        HttpClient idc = HttpClient.newBuilder().sslContext(ctx).build();
        check(idc.sslContext() == ctx, "sslContext identity round-trip");
        idc.close();
        CookieHandler cm = new CookieManager();
        Authenticator au = new Authenticator() { };
        java.util.concurrent.Executor ex = Runnable::run;
        ProxySelector ps = ProxySelector.of(new java.net.InetSocketAddress("127.0.0.1", 1));
        HttpClient ids = HttpClient.newBuilder().cookieHandler(cm).authenticator(au).executor(ex).proxy(ps).build();
        check(ids.cookieHandler().get() == cm, "cookieHandler identity round-trip");
        check(ids.authenticator().get() == au, "authenticator identity round-trip");
        check(ids.executor().get() == ex, "executor identity round-trip");
        check(ids.proxy().get() == ps, "proxy identity round-trip");
        ids.close();

        // sslParameters: copied, but content preserved.
        javax.net.ssl.SSLParameters sp = new javax.net.ssl.SSLParameters(
                new String[]{"TLS_AES_128_GCM_SHA256"}, new String[]{"TLSv1.3"});
        HttpClient spp = HttpClient.newBuilder().sslParameters(sp).build();
        check(spp.sslParameters() != sp, "sslParameters defensively copied");
        check(Arrays.equals(spp.sslParameters().getCipherSuites(), sp.getCipherSuites()),
                "sslParameters cipher content preserved");
        check(Arrays.equals(spp.sslParameters().getProtocols(), sp.getProtocols()),
                "sslParameters protocols content preserved");
        spp.close();

        // NO_PROXY reads back as the exact constant.
        HttpClient np = HttpClient.newBuilder().proxy(HttpClient.Builder.NO_PROXY).build();
        check(np.proxy().get() == HttpClient.Builder.NO_PROXY, "proxy(NO_PROXY) round-trip");
        np.close();

        // ---- deepened: builder reuse and independence (probed) ----
        HttpClient.Builder shared = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL);
        HttpClient first = shared.build();
        HttpClient second = shared.version(HttpClient.Version.HTTP_3).build();
        try {
            check(first != second, "two build() calls give distinct clients");
            check(first.followRedirects() == HttpClient.Redirect.NORMAL,
                    "first client unaffected by later builder mutation");
            check(second.followRedirects() == HttpClient.Redirect.NORMAL,
                    "settings persist across build() calls");
            check(second.version() == HttpClient.Version.HTTP_3,
                    "builder still mutable after build (version applied to second)");
            check(first.version() == HttpClient.Version.HTTP_2,
                    "first client keeps default version");
        } finally {
            first.close();
            second.close();
        }

        // localAddress(null): JDK-27 default method tolerates null (probed: no exception).
        HttpClient.Builder la = HttpClient.newBuilder();
        check(la.localAddress(null) == la, "localAddress(null) tolerated by default method");
        HttpClient laClient = HttpClient.newBuilder()
                .localAddress(InetAddress.getLoopbackAddress()).build();
        check(laClient != null, "localAddress(loopback) builds");
        laClient.close();

        // HTTP_3 as builder version is accepted at build time (probed).
        HttpClient h3 = HttpClient.newBuilder().version(HttpClient.Version.HTTP_3).build();
        check(h3.version() == HttpClient.Version.HTTP_3, "version(HTTP_3) round-trip");
        h3.close();

        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean Mod_public(String field) {
        try {
            int m = HttpClient.Builder.class.getField(field).getModifiers();
            return java.lang.reflect.Modifier.isPublic(m)
                    && java.lang.reflect.Modifier.isStatic(m)
                    && java.lang.reflect.Modifier.isFinal(m);
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
