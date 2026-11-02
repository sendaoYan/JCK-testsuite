/*
 * @test
 * @summary java.net.http.WebSocket conformance: interface constant and method surface.
 * @run main WebSocketTest
 */

import java.lang.reflect.Method;
import java.net.http.WebSocket;

public class WebSocketTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(String name) {
        for (Method m : WebSocket.class.getMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    public static void main(String[] args) {
        check(WebSocket.class.isInterface(), "WebSocket is an interface");
        check(WebSocket.NORMAL_CLOSURE == 1000, "NORMAL_CLOSURE is 1000");
        check(hasMethod("sendText"), "sendText present");
        check(hasMethod("sendBinary"), "sendBinary present");
        check(hasMethod("sendPing"), "sendPing present");
        check(hasMethod("sendPong"), "sendPong present");
        check(hasMethod("sendClose"), "sendClose present");
        check(hasMethod("request"), "request present");
        check(hasMethod("getSubprotocol"), "getSubprotocol present");
        check(hasMethod("isOutputClosed"), "isOutputClosed present");
        check(hasMethod("isInputClosed"), "isInputClosed present");
        check(hasMethod("abort"), "abort present");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
