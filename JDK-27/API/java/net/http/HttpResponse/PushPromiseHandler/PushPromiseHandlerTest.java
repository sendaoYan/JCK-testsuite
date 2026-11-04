/*
 * @test
 * @summary java.net.http.HttpResponse.PushPromiseHandler conformance: of() accumulates push promises.
 * @run main PushPromiseHandlerTest
 */

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PushPromiseHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpResponse.PushPromiseHandler.class.isInterface(), "PushPromiseHandler is an interface");

        ConcurrentMap<HttpRequest, CompletableFuture<HttpResponse<String>>> pushes = new ConcurrentHashMap<>();
        HttpResponse.PushPromiseHandler<String> handler =
                HttpResponse.PushPromiseHandler.of(req -> HttpResponse.BodyHandlers.ofString(), pushes);
        check(handler != null, "of() non-null");

        HttpRequest initiating = HttpRequest.newBuilder(URI.create("https://example.com")).build();
        HttpRequest pushed = HttpRequest.newBuilder(URI.create("https://example.com/other")).build();

        handler.applyPushPromise(initiating, pushed,
                bh -> CompletableFuture.completedFuture(null));
        check(pushes.containsKey(pushed), "of() records the push promise request");

        HttpResponse.PushPromiseHandler.PushId id =
                new HttpResponse.PushPromiseHandler.PushId.Http3PushId(7L, "conn-1");
        handler.applyPushPromise(initiating, pushed, id,
                bh -> CompletableFuture.completedFuture(null));
        handler.notifyAdditionalPromise(initiating, id);

        System.out.println("PASS (" + passed + " assertions)");
    }
}
