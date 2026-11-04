/*
 * @test
 * @summary java.net.http.WebSocket conformance: white-box structure + black-box builder/listener defaults.
 * @run main WebSocketTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;

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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
