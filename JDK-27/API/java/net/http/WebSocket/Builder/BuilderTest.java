/*
 * @test
 * @summary java.net.http.WebSocket.Builder conformance: builder methods return this and buildAsync.
 * @run main BuilderTest
 */

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class BuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
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

        client.shutdownNow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
