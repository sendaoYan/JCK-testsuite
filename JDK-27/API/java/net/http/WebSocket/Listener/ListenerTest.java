/*
 * @test
 * @summary java.net.http.WebSocket.Listener conformance: default methods request the next message.
 * @run main ListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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

    public static void main(String[] args) {
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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
