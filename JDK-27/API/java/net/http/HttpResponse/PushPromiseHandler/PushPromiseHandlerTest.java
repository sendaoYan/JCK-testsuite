/*
 * @test
 * @summary java.net.http.HttpResponse.PushPromiseHandler conformance: of() accumulates push promises.
 * @run main PushPromiseHandlerTest
 */

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PushPromiseHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpResponse.PushPromiseHandler.class.isInterface(), "PushPromiseHandler is an interface");
        check(HttpResponse.PushPromiseHandler.class.getTypeParameters().length == 1,
                "PushPromiseHandler has one type parameter");

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

        // ---- deepened: interface shape (JDK-27 surface) ----
        java.util.List<String> names = new ArrayList<>();
        boolean threeArgDefault = false, fourArgDefault = false, notifyDefault = false;
        try {
            threeArgDefault = HttpResponse.PushPromiseHandler.class
                    .getMethod("applyPushPromise", HttpRequest.class, HttpRequest.class,
                            java.util.function.Function.class).isDefault();
            fourArgDefault = HttpResponse.PushPromiseHandler.class
                    .getMethod("applyPushPromise", HttpRequest.class, HttpRequest.class,
                            HttpResponse.PushPromiseHandler.PushId.class,
                            java.util.function.Function.class).isDefault();
            notifyDefault = HttpResponse.PushPromiseHandler.class
                    .getMethod("notifyAdditionalPromise", HttpRequest.class,
                            HttpResponse.PushPromiseHandler.PushId.class).isDefault();
        } catch (NoSuchMethodException e) {
            check(false, "unexpected: " + e);
        }
        check(threeArgDefault == false, "3-arg applyPushPromise is abstract");
        check(fourArgDefault, "4-arg applyPushPromise(Http3) is a default method");
        check(notifyDefault, "notifyAdditionalPromise is a default method");
        check(HttpResponse.PushPromiseHandler.class.getDeclaredMethods().length >= 3,
                "declares 3+ methods");

        // ---- deepened: of() acceptor semantics (probed) ----
        ConcurrentMap<HttpRequest, CompletableFuture<HttpResponse<String>>> map2 = new ConcurrentHashMap<>();
        HttpResponse.PushPromiseHandler<String> h2 = HttpResponse.PushPromiseHandler.of(
                req -> HttpResponse.BodyHandlers.ofString(), map2);
        AtomicInteger acceptorCalls = new AtomicInteger();
        CompletableFuture<HttpResponse<String>> future = new CompletableFuture<>();
        h2.applyPushPromise(initiating, pushed, bh -> {
            acceptorCalls.incrementAndGet();
            return future;
        });
        check(acceptorCalls.get() == 1, "acceptor invoked exactly once");
        check(map2.get(pushed) == future,
                "the future stored in the map is the one returned by the acceptor");

        // A repeated push of the same request re-invokes the acceptor; the map
        // keeps a single entry per request (put overwrites, probed).
        CompletableFuture<HttpResponse<String>> future2 = new CompletableFuture<>();
        h2.applyPushPromise(initiating, pushed, bh -> { acceptorCalls.incrementAndGet(); return future2; });
        check(acceptorCalls.get() == 2, "second push invokes the acceptor again");
        check(map2.size() == 1, "map holds one entry per request");
        check(map2.get(pushed) == future2, "latest future replaces the previous one");

        // A pre-existing map entry is overwritten by the accepted push (probed).
        ConcurrentMap<HttpRequest, CompletableFuture<HttpResponse<String>>> mapPre =
                new ConcurrentHashMap<>();
        CompletableFuture<HttpResponse<String>> pre = new CompletableFuture<>();
        mapPre.put(pushed, pre);
        HttpResponse.PushPromiseHandler<String> hPre = HttpResponse.PushPromiseHandler.of(
                req -> HttpResponse.BodyHandlers.ofString(), mapPre);
        hPre.applyPushPromise(initiating, pushed, bh -> future2);
        check(mapPre.get(pushed) == future2, "pre-existing entry overwritten (probed)");

        // 4-arg applyPushPromise through the of() handler records identically (probed).
        HttpResponse.PushPromiseHandler.PushId pid0 =
                new HttpResponse.PushPromiseHandler.PushId.Http3PushId(9L, "c0");
        h2.applyPushPromise(initiating, pushed, pid0,
                bh -> { acceptorCalls.incrementAndGet(); return future2; });
        check(acceptorCalls.get() == 3, "4-arg form also reaches the acceptor (probed)");
        check(map2.get(pushed) == future2, "4-arg form stores the future");

        // An acceptor returning null fails the ConcurrentHashMap put with NPE (probed).
        try {
            map2.clear();
            h2.applyPushPromise(initiating, pushed, bh -> null);
            check(false, "null-returning acceptor must fail");
        } catch (NullPointerException e) {
            check(true, "null-returning acceptor throws NPE at the map put (probed)");
        }

        // An acceptor that throws propagates synchronously to the caller (probed).
        try {
            h2.applyPushPromise(initiating, pushed, bh -> { throw new RuntimeException("acc-boom"); });
            check(false, "throwing acceptor must propagate");
        } catch (RuntimeException e) {
            check("acc-boom".equals(e.getMessage()), "throwing acceptor propagates synchronously (probed)");
        }

        // ---- deepened: of() filters pushes by origin (probed: same scheme+host+port) ----
        HttpRequest upperHost = HttpRequest.newBuilder(URI.create("HTTPS://EXAMPLE.COM/p2")).build();
        HttpRequest diffHost = HttpRequest.newBuilder(URI.create("https://other.example.org/p3")).build();
        HttpRequest diffPort = HttpRequest.newBuilder(URI.create("https://example.com:8443/p4")).build();
        HttpRequest httpScheme = HttpRequest.newBuilder(URI.create("http://example.com/p5")).build();
        h2.applyPushPromise(initiating, upperHost, bh -> { acceptorCalls.incrementAndGet(); return future2; });
        h2.applyPushPromise(initiating, diffHost, bh -> { acceptorCalls.incrementAndGet(); return future2; });
        h2.applyPushPromise(initiating, diffPort, bh -> { acceptorCalls.incrementAndGet(); return future2; });
        h2.applyPushPromise(initiating, httpScheme, bh -> { acceptorCalls.incrementAndGet(); return future2; });
        check(acceptorCalls.get() == 4, "only same-origin pushes reach the acceptor (probed: 4 = 3 prior + 1 same-origin)");
        check(map2.containsKey(upperHost), "host comparison is case-insensitive");
        check(!map2.containsKey(diffHost), "different host rejected");
        check(!map2.containsKey(diffPort), "different port rejected");
        check(!map2.containsKey(httpScheme), "different scheme rejected");

        // ---- deepened: factory null tolerance (probed JDK-27: of() does not null-check) ----
        check(HttpResponse.PushPromiseHandler.of(null, map2) != null,
                "of(null, map) tolerated by the factory (probed)");
        check(HttpResponse.PushPromiseHandler.of(req -> HttpResponse.BodyHandlers.ofString(), null) != null,
                "of(fn, null) tolerated by the factory (probed)");
        try {
            HttpResponse.PushPromiseHandler.of(null, map2)
                    .applyPushPromise(initiating, pushed, bh -> new CompletableFuture<>());
            check(false, "of(null,..) apply must NPE");
        } catch (NullPointerException e) {
            check(true, "of(null,..).applyPushPromise NPEs at handler creation time (probed)");
        }
        try {
            HttpResponse.PushPromiseHandler.of(req -> HttpResponse.BodyHandlers.ofString(), null)
                    .applyPushPromise(initiating, pushed, bh -> new CompletableFuture<>());
            check(false, "of(..,null) apply must NPE");
        } catch (NullPointerException e) {
            check(true, "of(..,null).applyPushPromise NPEs at the map put (probed)");
        }

        // ---- deepened: default 4-arg applyPushPromise delegates to the abstract 3-arg ----
        List<String> log = new ArrayList<>();
        List<CompletableFuture<HttpResponse<String>>> futuresSeen = new ArrayList<>();
        HttpResponse.PushPromiseHandler<String> custom = new HttpResponse.PushPromiseHandler<>() {
            public void applyPushPromise(HttpRequest init, HttpRequest push,
                    java.util.function.Function<HttpResponse.BodyHandler<String>,
                            CompletableFuture<HttpResponse<String>>> acceptor) {
                log.add("3arg:" + push.uri());
                futuresSeen.add(acceptor.apply(HttpResponse.BodyHandlers.ofString()));
            }
        };
        HttpResponse.PushPromiseHandler.PushId pid =
                new HttpResponse.PushPromiseHandler.PushId.Http3PushId(1L, "c");
        custom.applyPushPromise(initiating, pushed, pid, bh -> {
            log.add("acceptor");
            return new CompletableFuture<>();
        });
        check(log.size() == 2 && "3arg:https://example.com/other".equals(log.get(0))
                && "acceptor".equals(log.get(1)),
                "4-arg default delegates to the 3-arg method (probed)");

        // notifyAdditionalPromise default is a no-op: must not throw.
        custom.notifyAdditionalPromise(initiating, pid);
        check(true, "notifyAdditionalPromise default no-op");

        // A lambda implementation of the single abstract method works.
        HttpResponse.PushPromiseHandler<String> lambda = (i, p, acc) -> acc.apply(
                HttpResponse.BodyHandlers.replacing("x"));
        check(lambda != null, "lambda implementation possible");

        // ---- deepened: PushId passthrough and functional shape ----
        // An implementation overriding the 4-arg method receives the exact id instance.
        final java.util.List<Object> seenIds = new ArrayList<>();
        HttpResponse.PushPromiseHandler<String> capturing = new HttpResponse.PushPromiseHandler<>() {
            public void applyPushPromise(HttpRequest init, HttpRequest push,
                    java.util.function.Function<HttpResponse.BodyHandler<String>,
                            CompletableFuture<HttpResponse<String>>> acceptor) {
                acceptor.apply(HttpResponse.BodyHandlers.ofString());
            }
            @Override
            public void applyPushPromise(HttpRequest initiatingRequest, HttpRequest pushPromiseRequest,
                    HttpResponse.PushPromiseHandler.PushId pushId,
                    java.util.function.Function<HttpResponse.BodyHandler<String>,
                            CompletableFuture<HttpResponse<String>>> acceptor) {
                seenIds.add(pushId);
            }
        };
        HttpResponse.PushPromiseHandler.PushId pid2 =
                new HttpResponse.PushPromiseHandler.PushId.Http3PushId(11L, "cx");
        capturing.applyPushPromise(initiating, pushed, pid2, bh -> new CompletableFuture<>());
        check(seenIds.size() == 1 && seenIds.get(0) == pid2,
                "overridden 4-arg method receives the exact PushId instance");

        // applyPushPromise is void; notifyAdditionalPromise is void.
        try {
            check(HttpResponse.PushPromiseHandler.class
                            .getMethod("applyPushPromise", HttpRequest.class, HttpRequest.class,
                                    java.util.function.Function.class).getReturnType() == void.class,
                    "3-arg applyPushPromise returns void");
            check(HttpResponse.PushPromiseHandler.class
                            .getMethod("applyPushPromise", HttpRequest.class, HttpRequest.class,
                                    HttpResponse.PushPromiseHandler.PushId.class,
                                    java.util.function.Function.class).getReturnType() == void.class,
                    "4-arg applyPushPromise returns void");
            check(HttpResponse.PushPromiseHandler.class
                            .getMethod("notifyAdditionalPromise", HttpRequest.class,
                                    HttpResponse.PushPromiseHandler.PushId.class).getReturnType() == void.class,
                    "notifyAdditionalPromise returns void");
        } catch (NoSuchMethodException e) {
            check(false, "unexpected: " + e);
        }

        // of() returns a fresh handler per call (no shared caching).
        check(HttpResponse.PushPromiseHandler.of(req -> HttpResponse.BodyHandlers.ofString(), map2)
                        != HttpResponse.PushPromiseHandler.of(req -> HttpResponse.BodyHandlers.ofString(), map2),
                "of() returns distinct instances");

        // The handler works for any body type, e.g. Void via discarding.
        ConcurrentMap<HttpRequest, CompletableFuture<HttpResponse<Void>>> vmap = new ConcurrentHashMap<>();
        HttpResponse.PushPromiseHandler<Void> vh = HttpResponse.PushPromiseHandler.of(
                req -> HttpResponse.BodyHandlers.discarding(), vmap);
        vh.applyPushPromise(initiating, pushed, bh -> new CompletableFuture<>());
        check(vmap.containsKey(pushed), "of() parameterized for Void works");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
