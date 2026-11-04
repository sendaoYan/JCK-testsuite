/*
 * @test
 * @summary java.net.http.WebSocket.Listener conformance: default methods request the next message.
 * @run main ListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Method method(String name) {
        for (Method m : WebSocket.Listener.class.getMethods()) {
            if (m.getName().equals(name)) return m;
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        check(WebSocket.Listener.class.isInterface(), "Listener is an interface");

        for (String name : new String[]{
                "onOpen", "onText", "onBinary", "onPing", "onPong", "onClose", "onError"}) {
            Method m = method(name);
            check(m != null && m.isDefault(), name + " is a default method");
        }

        final long[] requests = new long[]{0};
        final long[] requested = new long[]{0};
        WebSocket ws = (WebSocket) Proxy.newProxyInstance(
                ListenerTest.class.getClassLoader(),
                new Class<?>[]{WebSocket.class},
                (p, m, a) -> {
                    if (m.getName().equals("request")) {
                        requests[0]++;
                        requested[0] = (long) a[0];
                    }
                    return null;
                });

        WebSocket.Listener listener = new WebSocket.Listener() { };
        ByteBuffer buffer = ByteBuffer.wrap("ping".getBytes(StandardCharsets.UTF_8));

        listener.onOpen(ws);
        check(requests[0] == 1 && requested[0] == 1L, "onOpen default requests one message");

        check(listener.onText(ws, "text", true) == null, "onText default returns null");
        check(requests[0] == 2, "onText default requests one more message");

        check(listener.onBinary(ws, buffer, true) == null, "onBinary default returns null");
        check(requests[0] == 3, "onBinary default requests one more message");

        check(listener.onPing(ws, buffer) == null, "onPing default returns null");
        check(requests[0] == 4, "onPing default requests one more message");

        check(listener.onPong(ws, buffer) == null, "onPong default returns null");
        check(requests[0] == 5, "onPong default requests one more message");

        check(listener.onClose(ws, 1000, "bye") == null, "onClose default returns null");
        check(requests[0] == 5, "onClose default does not request");

        listener.onError(ws, new RuntimeException());
        check(requests[0] == 5, "onError default does not request");

        // ---- deepened: exact signatures of all seven callbacks ----
        check(Modifier.isPublic(WebSocket.Listener.class.getModifiers()), "Listener is public");
        check(!WebSocket.Listener.class.isSealed(), "Listener is user-implementable");
        check(WebSocket.Listener.class.getTypeParameters().length == 0, "non-generic");
        check(WebSocket.Listener.class.getDeclaredMethods().length == 7,
                "exactly seven declared methods (probed)");
        check(method("onOpen").getParameterCount() == 1
                        && method("onOpen").getParameterTypes()[0] == WebSocket.class,
                "onOpen(WebSocket)");
        check(method("onText").getParameterTypes().length == 3
                        && method("onText").getParameterTypes()[0] == WebSocket.class
                        && method("onText").getParameterTypes()[1] == CharSequence.class
                        && method("onText").getParameterTypes()[2] == boolean.class,
                "onText(WebSocket, CharSequence, boolean)");
        check(method("onBinary").getParameterTypes().length == 3
                        && method("onBinary").getParameterTypes()[1] == ByteBuffer.class,
                "onBinary(WebSocket, ByteBuffer, boolean)");
        check(method("onPing").getParameterTypes().length == 2
                        && method("onPing").getParameterTypes()[1] == ByteBuffer.class,
                "onPing(WebSocket, ByteBuffer)");
        check(method("onPong").getParameterTypes().length == 2
                        && method("onPong").getParameterTypes()[1] == ByteBuffer.class,
                "onPong(WebSocket, ByteBuffer)");
        check(method("onClose").getParameterTypes().length == 3
                        && method("onClose").getParameterTypes()[1] == int.class
                        && method("onClose").getParameterTypes()[2] == String.class,
                "onClose(WebSocket, int, String)");
        check(method("onError").getParameterTypes().length == 2
                        && method("onError").getParameterTypes()[1] == Throwable.class,
                "onError(WebSocket, Throwable)");
        check(method("onOpen").getReturnType() == void.class, "onOpen returns void");
        check(method("onError").getReturnType() == void.class, "onError returns void");
        for (String n : new String[]{"onText", "onBinary", "onPing", "onPong", "onClose"}) {
            check(method(n).getReturnType() == CompletionStage.class, n + " returns CompletionStage");
        }

        // ---- deepened: defaults are idempotent per call and use the passed socket ----
        requests[0] = 0;
        listener.onText(ws, "a", false);
        listener.onText(ws, "b", true);
        check(requests[0] == 2, "each onText invocation requests exactly one more (probed)");
        listener.onBinary(ws, buffer, false);
        listener.onPing(ws, buffer);
        listener.onPong(ws, buffer);
        check(requests[0] == 5, "binary/ping/pong each request one more");
        listener.onClose(ws, 1000, "");
        listener.onError(ws, new Exception());
        check(requests[0] == 5, "close/error never request");

        // ---- deepened: subclass overriding only some methods inherits the rest ----
        final List<String> log = new ArrayList<>();
        WebSocket.Listener partial = new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                log.add("text:" + data + ":" + last);
                return null;
            }
            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                log.add("error:" + error.getClass().getSimpleName());
            }
        };
        requests[0] = 0;
        partial.onOpen(ws);
        check(requests[0] == 1, "onOpen default inherited by partial subclass");
        check(partial.onText(ws, "hello", true) == null, "overridden onText returns its own value");
        check(log.equals(List.of("text:hello:true")), "overridden onText records the payload");
        partial.onError(ws, new IllegalStateException());
        check(log.equals(List.of("text:hello:true", "error:IllegalStateException")),
                "overridden onError records the error class");
        check(partial.onPing(ws, buffer) == null, "onPing default inherited");

        // ---- deepened: a listener returning a controlled stage ----
        final CompletionStage<?> stage = new CompletableFuture<>();
        WebSocket.Listener staged = new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer message, boolean last) {
                return stage;
            }
        };
        check(staged.onBinary(ws, buffer, true) == stage, "custom stage returned verbatim");
        requests[0] = 0;
        staged.onBinary(ws, buffer, true);
        check(requests[0] == 0, "overridden onBinary does not auto-request");

        // The listener interface is a SAM-free type: it cannot be a lambda.
        // (All methods have defaults, so an empty implementation is valid, verified above.)

        // Repeated onError/onClose with different codes do not request.
        listener.onClose(ws, 1001, "going away");
        listener.onClose(ws, 1000, null);
        listener.onError(ws, new java.io.IOException());
        check(requests[0] == 0, "still no requests from close/error");
        check(listener.onText(ws, "", false) == null, "empty text chunk accepted by the default");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
