/*
 * @test
 * @summary java.net.http.HttpResponse.BodyHandler conformance: interface applies to a BodySubscriber.
 * @run main BodyHandlerTest
 */

import java.lang.reflect.Proxy;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class BodyHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpResponse.BodyHandler.class.isInterface(), "BodyHandler is an interface");

        HttpResponse.ResponseInfo info = (HttpResponse.ResponseInfo) Proxy.newProxyInstance(
                BodyHandlerTest.class.getClassLoader(),
                new Class<?>[]{HttpResponse.ResponseInfo.class},
                (p, m, a) -> {
                    switch (m.getName()) {
                        case "statusCode": return 200;
                        case "headers":
                            return HttpHeaders.of(Map.of("X", List.of("1")), (k, v) -> true);
                        case "version": return HttpClient.Version.HTTP_1_1;
                        default: return null;
                    }
                });

        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        check(handler != null, "BodyHandlers.ofString() non-null");
        HttpResponse.BodySubscriber<String> subscriber = handler.apply(info);
        check(subscriber != null, "apply(ResponseInfo) returns a BodySubscriber");
        check(subscriber.getBody() != null, "BodySubscriber.getBody() non-null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
