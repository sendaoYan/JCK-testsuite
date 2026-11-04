/*
 * @test
 * @summary java.net.http.HttpResponse.PushPromiseHandler.PushId conformance: sealed marker interface.
 * @run main PushIdTest
 */

import java.net.http.HttpResponse;

public class PushIdTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Class<?> c = HttpResponse.PushPromiseHandler.PushId.class;
        check(c.isInterface(), "PushId is an interface");
        check(c.getDeclaredMethods().length == 0,
                "PushId declares no methods (marker interface)");
        check(c.getDeclaredFields().length == 0, "PushId declares no fields");
        check(c.getTypeParameters().length == 0, "PushId is non-generic");
        check(c.getInterfaces().length == 0, "PushId extends no other interfaces");

        // ---- deepened: JDK-27 sealed hierarchy (probed) ----
        check(c.isSealed(), "PushId is sealed (JDK-27)");
        Class<?>[] permitted = c.getPermittedSubclasses();
        check(permitted != null && permitted.length == 1,
                "exactly one permitted subclass");
        check(permitted != null && permitted[0].getName()
                        .equals("java.net.http.HttpResponse$PushPromiseHandler$PushId$Http3PushId"),
                "sole permitted subclass is Http3PushId");
        check(HttpResponse.PushPromiseHandler.PushId.Http3PushId.class.isRecord(),
                "Http3PushId is a record");
        check(!HttpResponse.PushPromiseHandler.PushId.Http3PushId.class.isSealed(),
                "Http3PushId is not itself sealed");
        check(HttpResponse.PushPromiseHandler.PushId.Http3PushId.class.getNestHost()
                        == HttpResponse.PushPromiseHandler.PushId.class.getNestHost(),
                "PushId and Http3PushId share the same nest");

        // ---- deepened: Http3PushId is-a PushId ----
        HttpResponse.PushPromiseHandler.PushId id =
                new HttpResponse.PushPromiseHandler.PushId.Http3PushId(3L, "conn");
        check(id instanceof HttpResponse.PushPromiseHandler.PushId, "Http3PushId is a PushId");
        check(id.equals(new HttpResponse.PushPromiseHandler.PushId.Http3PushId(3L, "conn")),
                "value equality between equal ids");
        check(!id.equals(new HttpResponse.PushPromiseHandler.PushId.Http3PushId(4L, "conn")),
                "inequality on different pushId");
        check(!id.equals(new HttpResponse.PushPromiseHandler.PushId.Http3PushId(3L, "other")),
                "inequality on different connectionLabel");
        check(id.hashCode() == new HttpResponse.PushPromiseHandler.PushId.Http3PushId(3L, "conn").hashCode(),
                "equal ids have equal hashCodes");

        // ---- deepened: usable wherever PushId is expected (default methods) ----
        java.util.ArrayList<String> log = new java.util.ArrayList<>();
        HttpResponse.PushPromiseHandler<String> h = new HttpResponse.PushPromiseHandler<>() {
            public void applyPushPromise(java.net.http.HttpRequest init, java.net.http.HttpRequest push,
                    java.util.function.Function<HttpResponse.BodyHandler<String>,
                            java.util.concurrent.CompletableFuture<HttpResponse<String>>> acceptor) {
                log.add("3");
            }
        };
        h.applyPushPromise(null, null, id, null);
        check(log.equals(java.util.List.of("3")),
                "default 4-arg method forwards to the 3-arg method");
        h.notifyAdditionalPromise(null, id);
        check(log.size() == 1, "notifyAdditionalPromise default is a no-op");

        // Polymorphic list of ids.
        java.util.List<HttpResponse.PushPromiseHandler.PushId> ids =
                java.util.List.of(id, new HttpResponse.PushPromiseHandler.PushId.Http3PushId(4L, "c2"));
        check(ids.size() == 2 && ids.get(1) instanceof HttpResponse.PushPromiseHandler.PushId,
                "PushId values compose polymorphically");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
