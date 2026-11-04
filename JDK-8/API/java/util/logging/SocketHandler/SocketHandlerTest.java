/*
 * @test
 * @summary java.util.logging.SocketHandler conformance: class hierarchy (network-bound type).
 * @run main SocketHandlerTest
 */

import java.util.logging.Handler;
import java.util.logging.SocketHandler;
import java.util.logging.StreamHandler;

public class SocketHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSocketHandler() {
        check(StreamHandler.class.isAssignableFrom(SocketHandler.class),
              "SocketHandler extends StreamHandler");
        check(Handler.class.isAssignableFrom(SocketHandler.class), "SocketHandler is a Handler");
    }

    public static void main(String[] args) {
        testSocketHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
