/*
 * @test
 * @summary java.net.http.WebSocket conformance: white-box structure + black-box builder/listener defaults.
 * @run main WebSocketTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebSocketTest {
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

    static void testWhiteBox() throws Exception {
        check(WebSocket.class.isInterface(), "WebSocket is an interface");
        check(Modifier.isPublic(WebSocket.class.getModifiers()), "public");
        check(WebSocket.class.getField("NORMAL_CLOSURE") != null, "NORMAL_CLOSURE constant present");
        check(WebSocket.NORMAL_CLOSURE == 1000, "NORMAL_CLOSURE is 1000");
        for (String m : new String[]{"sendText", "sendBinary", "sendPing", "sendPong", "sendClose",
                "request", "getSubprotocol", "isOutputClosed", "isInputClosed", "abort"}) {
            check(hasMethod(WebSocket.class, m), "WebSocket has method " + m);
        }
        check(WebSocket.Builder.class.isInterface(), "WebSocket.Builder is an interface");
        check(WebSocket.Listener.class.isInterface(), "WebSocket.Listener is an interface");

        // ---- deepened: full member signatures (JDK-27 surface) ----
        check(!WebSocket.class.isSealed(), "WebSocket is user-implementable");
        check(WebSocket.class.getTypeParameters().length == 0, "non-generic");
        int fMods = WebSocket.class.getField("NORMAL_CLOSURE").getModifiers();
        check(Modifier.isPublic(fMods) && Modifier.isStatic(fMods) && Modifier.isFinal(fMods),
                "NORMAL_CLOSURE is public static final int (probed)");
        check(WebSocket.class.getField("NORMAL_CLOSURE").getType() == int.class,
                "NORMAL_CLOSURE type int");
        check(WebSocket.class.getDeclaredFields().length == 1, "only one declared field");
        check(WebSocket.class.getMethod("sendText", CharSequence.class, boolean.class).getReturnType()
                        == CompletableFuture.class, "sendText returns CompletableFuture<WebSocket>");
        check(WebSocket.class.getMethod("sendBinary", ByteBuffer.class, boolean.class).getReturnType()
                        == CompletableFuture.class, "sendBinary returns CompletableFuture");
        check(WebSocket.class.getMethod("sendPing", ByteBuffer.class).getReturnType()
                        == CompletableFuture.class, "sendPing returns CompletableFuture");
        check(WebSocket.class.getMethod("sendPong", ByteBuffer.class).getReturnType()
                        == CompletableFuture.class, "sendPong returns CompletableFuture");
        check(WebSocket.class.getMethod("sendClose", int.class, String.class).getReturnType()
                        == CompletableFuture.class, "sendClose returns CompletableFuture");
        check(WebSocket.class.getMethod("request", long.class).getReturnType() == void.class,
                "request returns void");
        check(WebSocket.class.getMethod("getSubprotocol").getReturnType() == String.class,
                "getSubprotocol returns String");
        check(WebSocket.class.getMethod("isOutputClosed").getReturnType() == boolean.class,
                "isOutputClosed returns boolean");
        check(WebSocket.class.getMethod("isInputClosed").getReturnType() == boolean.class,
                "isInputClosed returns boolean");
        check(WebSocket.class.getMethod("abort").getReturnType() == void.class, "abort returns void");
        check(WebSocket.class.getDeclaredMethods().length == 10, "exactly ten declared methods");
        check(WebSocket.Listener.class.getDeclaredMethods().length == 7,
                "Listener declares seven methods (probed)");
        int defaults = 0;
        for (Method m : WebSocket.Listener.class.getDeclaredMethods()) {
            if (m.isDefault()) defaults++;
        }
        check(defaults == 7, "all seven Listener methods are defaults (probed)");
    }

    static void testBlackBox() throws Exception {
        check(WebSocket.NORMAL_CLOSURE == 1000, "NORMAL_CLOSURE is 1000");

        HttpClient client = HttpClient.newHttpClient();
        try {
            WebSocket.Builder b = client.newWebSocketBuilder();
            check(b != null, "newWebSocketBuilder non-null");
            check(b.header("X-Test", "1") == b, "header returns this");
            check(b.connectTimeout(Duration.ofSeconds(3)) == b, "connectTimeout returns this");
            check(b.subprotocols("chat", "superchat") == b, "subprotocols returns this");
            expect(NullPointerException.class, () -> b.header(null, "v"), "header(null) throws NPE");
            expect(NullPointerException.class, () -> b.header("k", null), "header(k,null) throws NPE");
            expect(NullPointerException.class, () -> b.connectTimeout(null), "connectTimeout(null) throws NPE");
            expect(NullPointerException.class, () -> b.subprotocols(null), "subprotocols(null) throws NPE");
        } finally {
            client.close();
        }

        // Listener default methods call webSocket.request(1) and return null; exercise them via a proxy mock
        // so no real WebSocket (and hence no network) is required.
        WebSocket ws = (WebSocket) java.lang.reflect.Proxy.newProxyInstance(
                WebSocketTest.class.getClassLoader(),
                new Class<?>[]{WebSocket.class},
                (proxy, method, args) -> null);

        WebSocket.Listener l = new WebSocket.Listener() {};
        l.onOpen(ws); // must not throw
        check(l.onText(ws, "hi", true) == null, "onText default returns null");
        check(l.onBinary(ws, null, false) == null, "onBinary default returns null");
        check(l.onPing(ws, null) == null, "onPing default returns null");
        check(l.onPong(ws, null) == null, "onPong default returns null");
        check(l.onClose(ws, 1000, "bye") == null, "onClose default returns null");
        l.onError(ws, new Exception("boom")); // must not throw

        // ---- deepened: which defaults invoke request(1) (probed) ----
        final long[] reqs = new long[1];
        final List<String> calls = new ArrayList<>();
        WebSocket recording = (WebSocket) java.lang.reflect.Proxy.newProxyInstance(
                WebSocketTest.class.getClassLoader(),
                new Class<?>[]{WebSocket.class},
                (proxy, method, args) -> {
                    calls.add(method.getName());
                    if (method.getName().equals("request")) reqs[0] = (Long) args[0];
                    return null;
                });
        WebSocket.Listener d = new WebSocket.Listener() {};
        reqs[0] = -1; d.onOpen(recording);
        check(reqs[0] == 1, "onOpen default calls request(1) (probed)");
        reqs[0] = -1; d.onText(recording, "x", true);
        check(reqs[0] == 1, "onText default calls request(1) (probed)");
        reqs[0] = -1; d.onBinary(recording, ByteBuffer.wrap(new byte[1]), true);
        check(reqs[0] == 1, "onBinary default calls request(1) (probed)");
        reqs[0] = -1; d.onPing(recording, null);
        check(reqs[0] == 1, "onPing default calls request(1) (probed)");
        reqs[0] = -1; d.onPong(recording, null);
        check(reqs[0] == 1, "onPong default calls request(1) (probed)");
        reqs[0] = -1; d.onClose(recording, 1000, "r");
        check(reqs[0] == -1, "onClose default does NOT call request (probed)");
        reqs[0] = -1; d.onError(recording, new Exception());
        check(reqs[0] == -1, "onError default does NOT call request (probed)");

        // ---- deepened: CompletionStage-returning Listener methods (probed) ----
        for (Method m : WebSocket.Listener.class.getDeclaredMethods()) {
            switch (m.getName()) {
                case "onOpen", "onError" ->
                        check(m.getReturnType() == void.class, m.getName() + " returns void (probed)");
                case "onClose" ->
                        check(m.getReturnType() == CompletionStage.class, "onClose returns CompletionStage (probed)");
                default ->
                        check(m.getReturnType() == CompletionStage.class,
                                m.getName() + " returns CompletionStage (probed)");
            }
        }

        // ---- deepened: a user Listener overriding onText controls the stage ----
        final CompletionStage<?> mine = new CompletableFuture<>();
        WebSocket.Listener custom = new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                return mine;
            }
        };
        check(custom.onText(recording, "payload", true) == mine,
                "user onText result is returned verbatim");
        check(d.onText(recording, "payload", true) == null,
                "default onText yields null (no additional demand beyond request(1))");

        // A listener counting deliveries through the proxy websocket.
        final List<String> received = new ArrayList<>();
        WebSocket.Listener counting = new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                received.add("open");
                WebSocket.Listener.super.onOpen(webSocket);
            }
            @Override
            public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
                received.add("ping");
                return WebSocket.Listener.super.onPing(webSocket, message);
            }
        };
        counting.onOpen(recording);
        counting.onPing(recording, null);
        check(received.equals(List.of("open", "ping")), "custom overrides run in order");
        check(reqs[0] == 1, "calling the super default re-issues request(1)");

        System.out.println("PASS (" + passed + " assertions)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
    }
}
