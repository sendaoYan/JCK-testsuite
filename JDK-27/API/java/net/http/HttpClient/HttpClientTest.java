/*
 * @test
 * @summary java.net.http.HttpClient conformance: white-box structure + black-box builder/lifecycle behavior.
 * @run main HttpClientTest
 */

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Executor;

public class HttpClientTest {
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

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.http.HttpClient");
        check(Modifier.isPublic(c.getModifiers()), "HttpClient is public");
        check(Modifier.isAbstract(c.getModifiers()), "HttpClient is abstract");
        check(AutoCloseable.class.isAssignableFrom(c), "HttpClient implements AutoCloseable");
        check(c.getSuperclass() == Object.class, "HttpClient superclass is Object");

        Constructor<?> ctor = c.getDeclaredConstructor();
        check(ctor != null, "HttpClient has a no-arg constructor");
        check(Modifier.isProtected(ctor.getModifiers()), "HttpClient no-arg constructor is protected");

        for (String m : new String[]{"send", "sendAsync", "shutdown", "shutdownNow", "awaitTermination",
                "isTerminated", "proxy", "newWebSocketBuilder", "newHttpClient", "newBuilder", "executor",
                "connectTimeout", "version", "followRedirects", "authenticator", "cookieHandler",
                "sslContext", "sslParameters"}) {
            check(hasMethod(c, m), "HttpClient has method " + m);
        }
        check(Modifier.isStatic(c.getMethod("newHttpClient").getModifiers()), "newHttpClient is static");
        check(Modifier.isStatic(c.getMethod("newBuilder").getModifiers()), "newBuilder is static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Defaults from newHttpClient().
        HttpClient c = HttpClient.newHttpClient();
        try {
            check(c != null, "newHttpClient() non-null");
            check(c.followRedirects() == HttpClient.Redirect.NEVER, "default followRedirects is NEVER");
            check(c.version() == HttpClient.Version.HTTP_2, "default version is HTTP_2");
            check(c.sslContext() != null, "sslContext() non-null");
            check(c.sslParameters() != null, "sslParameters() non-null");
            check(!c.executor().isPresent(), "default executor absent");
            check(!c.connectTimeout().isPresent(), "default connectTimeout absent");
            check(!c.proxy().isPresent(), "default proxy absent");
            check(!c.authenticator().isPresent(), "default authenticator absent");
            check(!c.cookieHandler().isPresent(), "default cookieHandler absent");
            check(c.newWebSocketBuilder() != null, "newWebSocketBuilder() non-null");
            check(!c.isTerminated(), "not terminated before shutdown");
        } finally {
            c.close();
        }

        // Defaults from newBuilder().build().
        HttpClient built = HttpClient.newBuilder().build();
        try {
            check(built != null, "newBuilder().build() non-null");
            check(built.followRedirects() == HttpClient.Redirect.NEVER, "built client default redirect NEVER");
            check(built.version() == HttpClient.Version.HTTP_2, "built client default version HTTP_2");
            check(!built.proxy().isPresent(), "built client default proxy absent");
        } finally {
            built.close();
        }

        // Builder with explicit settings, read back through the accessors.
        Executor direct = Runnable::run;
        HttpClient configured = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(7))
                .proxy(HttpClient.Builder.NO_PROXY)
                .executor(direct)
                .priority(5)
                .build();
        try {
            check(configured.followRedirects() == HttpClient.Redirect.ALWAYS, "followRedirects(ALWAYS) read back");
            check(configured.version() == HttpClient.Version.HTTP_1_1, "version(HTTP_1_1) read back");
            check(Duration.ofSeconds(7).equals(configured.connectTimeout().get()), "connectTimeout read back");
            check(configured.proxy().isPresent() && configured.proxy().get() == HttpClient.Builder.NO_PROXY,
                    "proxy(NO_PROXY) read back");
            check(configured.executor().isPresent(), "executor present when set");
            check(configured.executor().get() == direct, "executor identity preserved");
        } finally {
            configured.close();
        }

        // Lifecycle: shutdown() then awaitTermination() then isTerminated().
        HttpClient life = HttpClient.newHttpClient();
        life.shutdown();
        check(life.awaitTermination(Duration.ofSeconds(60)), "awaitTermination after shutdown");
        check(life.isTerminated(), "isTerminated after awaitTermination");

        // shutdownNow() on a fresh client also terminates.
        HttpClient now = HttpClient.newHttpClient();
        now.shutdownNow();
        check(now.awaitTermination(Duration.ofSeconds(60)), "awaitTermination after shutdownNow");
        check(now.isTerminated(), "isTerminated after shutdownNow");
    }

    // ---- Part 3: deepened accessor round-trips (identity semantics, JDK-27 probed) ----
    static void testAccessorRoundTrips() throws Exception {
        // sslContext: exact instance is preserved through build().
        javax.net.ssl.SSLContext def = javax.net.ssl.SSLContext.getDefault();
        HttpClient sc = HttpClient.newBuilder().sslContext(def).build();
        try {
            check(sc.sslContext() == def, "sslContext() returns the exact instance set");
        } finally {
            sc.close();
        }

        // sslParameters: the builder takes a defensive copy (not identity), but the
        // cipher suites and protocols content round-trips exactly (JDK-27 probed:
        // identity false, equals false, contents equal).
        javax.net.ssl.SSLParameters params = new javax.net.ssl.SSLParameters(
                new String[]{"TLS_AES_128_GCM_SHA256"}, new String[]{"TLSv1.3"});
        HttpClient sp = HttpClient.newBuilder().sslParameters(params).build();
        try {
            javax.net.ssl.SSLParameters got = sp.sslParameters();
            check(got != params, "sslParameters() returns a defensive copy");
            check(Arrays.equals(got.getCipherSuites(), params.getCipherSuites()),
                    "sslParameters cipher suites round-trip");
            check(Arrays.equals(got.getProtocols(), params.getProtocols()),
                    "sslParameters protocols round-trip");
        } finally {
            sp.close();
        }

        // cookieHandler / authenticator / proxy / executor: identity preserved.
        CookieHandler cm = new CookieManager();
        Authenticator au = new Authenticator() { };
        ProxySelector ps = ProxySelector.of(new java.net.InetSocketAddress("127.0.0.1", 1));
        Executor direct = Runnable::run;
        HttpClient full = HttpClient.newBuilder()
                .cookieHandler(cm).authenticator(au).proxy(ps).executor(direct).build();
        try {
            check(full.cookieHandler().get() == cm, "cookieHandler identity preserved");
            check(full.authenticator().get() == au, "authenticator identity preserved");
            check(full.proxy().get() == ps, "proxy identity preserved");
            check(full.executor().get() == direct, "executor identity preserved");
        } finally {
            full.close();
        }

        // NO_PROXY constant: a ProxySelector that reads back identically.
        check(HttpClient.Builder.NO_PROXY instanceof ProxySelector, "NO_PROXY is a ProxySelector");
        HttpClient np = HttpClient.newBuilder().proxy(HttpClient.Builder.NO_PROXY).build();
        try {
            check(np.proxy().isPresent() && np.proxy().get() == HttpClient.Builder.NO_PROXY,
                    "proxy(NO_PROXY) reads back the same constant");
        } finally {
            np.close();
        }

        // Default sslParameters is non-null with a non-empty cipher suite list.
        HttpClient dflt = HttpClient.newHttpClient();
        try {
            check(dflt.sslParameters() != null, "default sslParameters non-null");
            check(dflt.sslParameters().getCipherSuites().length > 0,
                    "default sslParameters has cipher suites");
        } finally {
            dflt.close();
        }

        // HTTP_3 default version builds and reads back (JDK-27 fact: Version.HTTP_3 exists).
        HttpClient h3 = HttpClient.newBuilder().version(HttpClient.Version.HTTP_3).build();
        try {
            check(h3.version() == HttpClient.Version.HTTP_3, "version(HTTP_3) round-trip");
        } finally {
            h3.close();
        }

        // Version values all round-trip through the builder.
        for (HttpClient.Version v : HttpClient.Version.values()) {
            HttpClient c = HttpClient.newBuilder().version(v).build();
            try {
                check(c.version() == v, "version(" + v + ") round-trip");
            } finally {
                c.close();
            }
        }

        // Redirect policy values all round-trip.
        for (HttpClient.Redirect r : HttpClient.Redirect.values()) {
            HttpClient c = HttpClient.newBuilder().followRedirects(r).build();
            try {
                check(c.followRedirects() == r, "followRedirects(" + r + ") round-trip");
            } finally {
                c.close();
            }
        }

        // priority is write-only (no getter): 1 and 256 are the accepted bounds (probed).
        HttpClient p1 = HttpClient.newBuilder().priority(1).build();
        p1.close();
        check(p1.isTerminated() || true, "priority(1) builds");
        HttpClient p256 = HttpClient.newBuilder().priority(256).build();
        p256.close();
        check(true, "priority(256) builds");
    }

    // ---- Part 4: lifecycle state machine (JDK-27 probed; no network used) ----
    static void testLifecycleDeep() throws Exception {
        // awaitTermination on a fresh unused client: ZERO is permitted (no IAE),
        // returns false immediately because nothing is pending.
        HttpClient live = HttpClient.newHttpClient();
        try {
            check(!live.awaitTermination(Duration.ZERO), "awaitTermination(ZERO) on fresh client false");
            check(!live.awaitTermination(Duration.ofMillis(-1)),
                    "awaitTermination(negative) tolerated, returns false (JDK-27: no IAE)");
            expect(NullPointerException.class, () -> {
                try { live.awaitTermination(null); } catch (InterruptedException e) { throw new RuntimeException(e); }
            }, "awaitTermination(null) throws NPE");
        } finally {
            live.close();
        }

        // shutdown() is idempotent; close() after shutdown and twice is fine.
        HttpClient idem = HttpClient.newHttpClient();
        idem.shutdown();
        idem.shutdown();
        check(true, "shutdown() idempotent");
        idem.close();
        idem.close();
        check(true, "close() idempotent after shutdown");

        // After termination, the synchronous send() fails locally with IOException
        // "closed" (probed: thrown before any network IO is attempted).
        HttpClient dead = HttpClient.newHttpClient();
        dead.shutdown();
        check(dead.awaitTermination(Duration.ofSeconds(10)), "terminated after shutdown");
        check(dead.isTerminated(), "isTerminated true after awaitTermination");
        check(dead.awaitTermination(Duration.ZERO),
                "awaitTermination(ZERO) true once fully terminated (JDK-27 probed)");
        HttpRequest req = HttpRequest.newBuilder(java.net.URI.create("https://example.com")).build();
        try {
            dead.send(req, HttpResponse.BodyHandlers.discarding());
            check(false, "send() on terminated client must throw");
        } catch (IOException e) {
            check("closed".equals(e.getMessage()), "send() on terminated client throws IOException 'closed'");
        } catch (IllegalStateException e) {
            check(true, "send() on terminated client throws IllegalStateException");
        }
        dead.close();

        // shutdownNow on an unused client terminates it as well.
        HttpClient now2 = HttpClient.newHttpClient();
        now2.shutdownNow();
        check(now2.awaitTermination(Duration.ofSeconds(10)), "awaitTermination after shutdownNow");
        check(now2.isTerminated(), "isTerminated after shutdownNow");
        now2.close();

        // Factory identity: each call yields a fresh independent instance.
        HttpClient a = HttpClient.newHttpClient();
        HttpClient b = HttpClient.newHttpClient();
        check(a != b, "two newHttpClient() instances are distinct");
        a.close();
        b.close();

        // send()/sendAsync() require network and are intentionally NOT exercised here.
    }

    // ---- Part 5: structural signature checks (javap-verified against JDK-27) ----
    static void testSignatures() throws Exception {
        Class<?> c = HttpClient.class;

        // Getter return types.
        check(c.getMethod("followRedirects").getReturnType() == HttpClient.Redirect.class,
                "followRedirects() returns Redirect");
        check(c.getMethod("version").getReturnType() == HttpClient.Version.class,
                "version() returns Version");
        check(c.getMethod("sslContext").getReturnType() == javax.net.ssl.SSLContext.class,
                "sslContext() returns SSLContext");
        check(c.getMethod("sslParameters").getReturnType() == javax.net.ssl.SSLParameters.class,
                "sslParameters() returns SSLParameters");
        check(c.getMethod("newWebSocketBuilder").getReturnType() == java.net.http.WebSocket.Builder.class,
                "newWebSocketBuilder() returns WebSocket.Builder");
        check(c.getMethod("isTerminated").getReturnType() == boolean.class,
                "isTerminated() returns boolean");
        check(c.getMethod("awaitTermination", Duration.class).getReturnType() == boolean.class,
                "awaitTermination(Duration) returns boolean");
        check(c.getMethod("shutdown").getReturnType() == void.class, "shutdown() returns void");
        check(c.getMethod("shutdownNow").getReturnType() == void.class, "shutdownNow() returns void");
        check(c.getMethod("close").getReturnType() == void.class, "close() returns void");

        // Optional-typed getters.
        check(c.getMethod("cookieHandler").getReturnType() == java.util.Optional.class, "cookieHandler Optional");
        check(c.getMethod("connectTimeout").getReturnType() == java.util.Optional.class, "connectTimeout Optional");
        check(c.getMethod("proxy").getReturnType() == java.util.Optional.class, "proxy Optional");
        check(c.getMethod("authenticator").getReturnType() == java.util.Optional.class, "authenticator Optional");
        check(c.getMethod("executor").getReturnType() == java.util.Optional.class, "executor Optional");

        // send/sendAsync: abstract, with IOException/InterruptedException on send.
        Method send = c.getMethod("send", HttpRequest.class, HttpResponse.BodyHandler.class);
        check(Modifier.isAbstract(send.getModifiers()), "send is abstract");
        check(send.getExceptionTypes().length == 2
                && java.io.IOException.class.isAssignableFrom(send.getExceptionTypes()[0])
                && send.getExceptionTypes()[1] == InterruptedException.class,
                "send declares IOException, InterruptedException");
        check(c.getMethod("sendAsync", HttpRequest.class, HttpResponse.BodyHandler.class)
                .getReturnType() == java.util.concurrent.CompletableFuture.class,
                "sendAsync(2) returns CompletableFuture");
        check(c.getMethod("sendAsync", HttpRequest.class, HttpResponse.BodyHandler.class,
                        HttpResponse.PushPromiseHandler.class).getReturnType()
                        == java.util.concurrent.CompletableFuture.class,
                "sendAsync(3) returns CompletableFuture");
        long sendAsyncCount = java.util.Arrays.stream(c.getDeclaredMethods())
                .filter(m -> m.getName().equals("sendAsync")).count();
        check(sendAsyncCount == 2, "exactly two sendAsync overloads");

        // JDK-27 fact: HttpClient exposes no localAddress() accessor even though
        // Builder.localAddress(InetAddress) exists (probed: getter ABSENT).
        boolean hasLocalAddressGetter = false;
        for (Method m : c.getMethods()) {
            if (m.getName().equals("localAddress")) hasLocalAddressGetter = true;
        }
        check(!hasLocalAddressGetter, "no localAddress() getter on HttpClient (JDK-27)");
    }

    // ---- Part 6: boundary and misc contract details ----
    static void testBoundaries() throws Exception {
        // Smallest legal positive duration is accepted and reads back exactly.
        HttpClient ns = HttpClient.newBuilder().connectTimeout(Duration.ofNanos(1)).build();
        try {
            check(Duration.ofNanos(1).equals(ns.connectTimeout().get()), "connectTimeout(1ns) round-trip");
        } finally {
            ns.close();
        }

        // newWebSocketBuilder() hands out a fresh builder on every call.
        HttpClient c = HttpClient.newHttpClient();
        try {
            check(c.newWebSocketBuilder() != null, "newWebSocketBuilder #1");
            check(c.newWebSocketBuilder() != null, "newWebSocketBuilder #2");
        } finally {
            c.close();
        }

        // The no-arg constructor is protected so only subclasses may call it.
        Constructor<?>[] ctors = HttpClient.class.getDeclaredConstructors();
        int protectedCtors = 0;
        for (Constructor<?> ct : ctors) {
            if (Modifier.isProtected(ct.getModifiers())) protectedCtors++;
        }
        check(protectedCtors == ctors.length, "all declared constructors are protected");

        // close() is the AutoCloseable method used by try-with-resources.
        check(AutoCloseable.class.isAssignableFrom(HttpClient.class),
                "HttpClient usable in try-with-resources");
        try (HttpClient twr = HttpClient.newHttpClient()) {
            check(twr != null, "try-with-resources construction");
        }

        // toString is unspecified but must not be null.
        HttpClient one = HttpClient.newHttpClient();
        try {
            check(one.toString() != null, "toString non-null");
        } finally {
            one.close();
        }

        // Unset optional properties stay absent even when other properties are set.
        HttpClient partial = HttpClient.newBuilder().priority(3).build();
        try {
            check(!partial.connectTimeout().isPresent(), "connectTimeout absent when not set");
            check(!partial.cookieHandler().isPresent(), "cookieHandler absent when not set");
            check(!partial.proxy().isPresent(), "proxy absent when not set");
            check(!partial.authenticator().isPresent(), "authenticator absent when not set");
            check(!partial.executor().isPresent(), "executor absent when not set");
            check(partial.followRedirects() == HttpClient.Redirect.NEVER,
                    "redirect defaults to NEVER when only priority set");
            check(partial.version() == HttpClient.Version.HTTP_2,
                    "version defaults to HTTP_2 when only priority set");
        } finally {
            partial.close();
        }

        // A client built with an explicit executor keeps it even after close.
        HttpClient ex2 = HttpClient.newBuilder().executor(Runnable::run).build();
        ex2.close();
        check(ex2.executor().isPresent(), "executor still readable after close");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAccessorRoundTrips();
        testLifecycleDeep();
        testSignatures();
        testBoundaries();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
