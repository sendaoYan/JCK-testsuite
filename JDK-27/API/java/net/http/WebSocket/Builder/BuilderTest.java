/*
 * @test
 * @summary java.net.http.WebSocket.Builder conformance: builder methods return this and buildAsync.
 * @run main BuilderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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

    static final URI INVALID = URI.create("ws://127.0.0.1:1/handshake-invalid");
    static final WebSocket.Listener LISTENER = new WebSocket.Listener() { };

    /**
     * buildAsync with a parameter that provably fails validation BEFORE any I/O
     * (probed: OpeningHandshake's constructor throws while creating the future).
     */
    static String failedWith(WebSocket.Builder b) {
        try {
            CompletableFuture<WebSocket> f = b.buildAsync(INVALID, LISTENER);
            try {
                f.get(5, TimeUnit.SECONDS);
                return "completed normally";
            } catch (ExecutionException e) {
                return "cause:" + e.getCause().getClass().getSimpleName()
                        + ":" + e.getCause().getMessage();
            }
        } catch (Throwable e) {
            return "sync:" + e.getClass().getSimpleName() + ":" + e.getMessage();
        }
    }

    static void checkFailed(WebSocket.Builder b, String prefix, String expected, String msg) {
        String r = failedWith(b);
        check(r.equals(expected), msg + " (got '" + r + "', probed " + prefix + ")");
    }

    public static void main(String[] args) throws Exception {
        check(WebSocket.Builder.class.isInterface(), "Builder is an interface");

        HttpClient client = HttpClient.newHttpClient();
        WebSocket.Builder b = client.newWebSocketBuilder();
        check(b != null, "newWebSocketBuilder() non-null");
        check(b.header("X-One", "1") == b, "header returns this");
        check(b.connectTimeout(Duration.ofSeconds(5)) == b, "connectTimeout returns this");
        check(b.subprotocols("chat") == b, "subprotocols returns this");

        WebSocket.Listener listener = new WebSocket.Listener() { };
        CompletableFuture<WebSocket> future =
                b.buildAsync(URI.create("ws://127.0.0.1:1"), listener);
        check(future != null, "buildAsync returns a non-null CompletableFuture");

        // ---- deepened: interface shape ----
        check(Modifier.isPublic(WebSocket.Builder.class.getModifiers()), "Builder public");
        check(!WebSocket.Builder.class.isSealed(), "Builder user-implementable");
        check(WebSocket.Builder.class.getDeclaredMethods().length == 4,
                "four declared methods (probed)");
        Method header = WebSocket.Builder.class.getMethod("header", String.class, String.class);
        Method ct = WebSocket.Builder.class.getMethod("connectTimeout", Duration.class);
        Method sub = WebSocket.Builder.class.getMethod("subprotocols", String.class, String[].class);
        Method build = WebSocket.Builder.class.getMethod("buildAsync", URI.class, WebSocket.Listener.class);
        check(header.getReturnType() == WebSocket.Builder.class, "header returns Builder");
        check(ct.getReturnType() == WebSocket.Builder.class, "connectTimeout returns Builder");
        check(sub.getReturnType() == WebSocket.Builder.class, "subprotocols returns Builder");
        check(build.getReturnType() == CompletableFuture.class, "buildAsync returns CompletableFuture");
        check(Modifier.isAbstract(header.getModifiers())
                        && Modifier.isAbstract(ct.getModifiers())
                        && Modifier.isAbstract(sub.getModifiers())
                        && Modifier.isAbstract(build.getModifiers()),
                "all four methods abstract");
        check(sub.isVarArgs(), "subprotocols is varargs");

        // ---- deepened: synchronous NPEs from buildAsync (probed) ----
        WebSocket.Builder n1 = client.newWebSocketBuilder();
        expect(NullPointerException.class, () -> n1.buildAsync(null, LISTENER),
                "buildAsync(null, l) NPE 'uri' (probed)");
        WebSocket.Builder n2 = client.newWebSocketBuilder();
        expect(NullPointerException.class, () -> n2.buildAsync(INVALID, null),
                "buildAsync(uri, null) NPE 'listener' (probed)");

        // ---- deepened: setter NPEs (probed) ----
        expect(NullPointerException.class, () -> b.header(null, "v"), "header(null, v) NPE");
        expect(NullPointerException.class, () -> b.header("k", null), "header(k, null) NPE");
        expect(NullPointerException.class, () -> b.connectTimeout(null), "connectTimeout(null) NPE");
        expect(NullPointerException.class, () -> b.subprotocols(null), "subprotocols(null) NPE");

        // ---- deepened: validation failures surface as failed futures (probed) ----
        // Header name/value syntax.
        WebSocket.Builder h1 = client.newWebSocketBuilder();
        h1.header("X Y", "v");
        checkFailed(h1, "bad header name",
                "cause:IllegalArgumentException:invalid header name: \"X Y\"",
                "bad header name fails the future");
        WebSocket.Builder h2 = client.newWebSocketBuilder();
        h2.header("X", "a\nb");
        checkFailed(h2, "bad header value",
                "cause:IllegalArgumentException:invalid header value: \"a\nb\"",
                "bad header value fails the future");
        // Restricted WebSocket handshake headers.
        for (String bad : new String[]{"Sec-WebSocket-Version", "Sec-WebSocket-Protocol",
                "Sec-WebSocket-Key", "Sec-WebSocket-Accept", "Sec-WebSocket-Extensions"}) {
            WebSocket.Builder hb = client.newWebSocketBuilder();
            hb.header(bad, "x");
            checkFailed(hb, "restricted header",
                    "cause:IllegalArgumentException:Illegal header: " + bad,
                    "restricted header " + bad + " fails the future");
        }
        // connectTimeout bounds.
        WebSocket.Builder c1 = client.newWebSocketBuilder();
        c1.connectTimeout(Duration.ZERO);
        checkFailed(c1, "connectTimeout ZERO",
                "cause:IllegalArgumentException:Invalid duration: PT0S",
                "connectTimeout ZERO fails the future");
        WebSocket.Builder c2 = client.newWebSocketBuilder();
        c2.connectTimeout(Duration.ofMillis(-1));
        checkFailed(c2, "connectTimeout negative",
                "cause:IllegalArgumentException:Invalid duration: PT-0.001S",
                "connectTimeout -1ms fails the future");
        // Subprotocol syntax.
        WebSocket.Builder s1 = client.newWebSocketBuilder();
        s1.subprotocols("a b");
        checkFailed(s1, "bad subprotocol",
                "cause:IllegalArgumentException:Bad subprotocol syntax: a b",
                "subprotocol with a space fails the future");
        WebSocket.Builder s2 = client.newWebSocketBuilder();
        s2.subprotocols("");
        checkFailed(s2, "empty subprotocol",
                "cause:IllegalArgumentException:Bad subprotocol syntax: ",
                "empty subprotocol fails the future");
        WebSocket.Builder s3 = client.newWebSocketBuilder();
        s3.subprotocols("x", "x");
        checkFailed(s3, "duplicate subprotocol",
                "cause:IllegalArgumentException:Duplicating subprotocol: x",
                "duplicate subprotocol fails the future");
        WebSocket.Builder s4 = client.newWebSocketBuilder();
        s4.subprotocols("good", "bad\tone");
        checkFailed(s4, "bad lesser subprotocol",
                "cause:IllegalArgumentException:Bad subprotocol syntax: bad\tone",
                "bad lesser subprotocol fails the future");

        // ---- deepened: URI validation (probed) ----
        List<String[]> uriCases = List.of(
                new String[]{"http://127.0.0.1:1/", "cause:IllegalArgumentException:invalid URI scheme: http"},
                new String[]{"https://127.0.0.1:1/", "cause:IllegalArgumentException:invalid URI scheme: https"},
                new String[]{"/no-scheme", "cause:IllegalArgumentException:invalid URI scheme: null"},
                new String[]{"ws://127.0.0.1:1/#f", "cause:IllegalArgumentException:URI must not contain a fragment: ws://127.0.0.1:1/#f"},
                new String[]{"ws:///path", "cause:IllegalArgumentException:URI must contain a host: ws:///path"});
        for (String[] uc : uriCases) {
            String r;
            try {
                CompletableFuture<WebSocket> f = client.newWebSocketBuilder()
                        .buildAsync(URI.create(uc[0]), LISTENER);
                try {
                    f.get(5, TimeUnit.SECONDS);
                    r = "completed normally";
                } catch (ExecutionException e) {
                    r = "cause:" + e.getCause().getClass().getSimpleName()
                            + ":" + e.getCause().getMessage();
                }
            } catch (Throwable e) {
                r = "sync:" + e.getClass().getSimpleName() + ":" + e.getMessage();
            }
            check(r.equals(uc[1]), "URI " + uc[0] + " rejected with '" + uc[1] + "' (got '" + r + "', probed)");
        }

        // ---- deepened: builders are independent (probed) ----
        // A restricted header on one builder does not leak into a fresh one:
        // the fresh builder's failure below is caused only by its own bad value.
        WebSocket.Builder polluted = client.newWebSocketBuilder();
        polluted.header("Sec-WebSocket-Key", "k");
        WebSocket.Builder otherBad = client.newWebSocketBuilder();
        otherBad.subprotocols("dup", "dup");
        checkFailed(otherBad, "independent builders",
                "cause:IllegalArgumentException:Duplicating subprotocol: dup",
                "builder state is per-instance (probed)");

        // Multiple valid header calls chain freely; full-chain returns this.
        WebSocket.Builder chain = client.newWebSocketBuilder();
        check(chain.header("A", "1").header("B", "2").connectTimeout(Duration.ofSeconds(1))
                        .subprotocols("one", "two") == chain, "full chain returns the same builder");

        client.shutdownNow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
