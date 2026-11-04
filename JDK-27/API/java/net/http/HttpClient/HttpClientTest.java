/*
 * @test
 * @summary java.net.http.HttpClient conformance: white-box structure + black-box builder/lifecycle behavior.
 * @run main HttpClientTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.http.HttpClient;
import java.time.Duration;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
