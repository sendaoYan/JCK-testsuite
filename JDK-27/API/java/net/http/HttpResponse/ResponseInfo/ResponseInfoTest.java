/*
 * @test
 * @summary java.net.http.HttpResponse.ResponseInfo conformance: interface accessors.
 * @run main ResponseInfoTest
 */

import java.lang.reflect.Proxy;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class ResponseInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpResponse.ResponseInfo.class.isInterface(), "ResponseInfo is an interface");

        HttpResponse.ResponseInfo info = (HttpResponse.ResponseInfo) Proxy.newProxyInstance(
                ResponseInfoTest.class.getClassLoader(),
                new Class<?>[]{HttpResponse.ResponseInfo.class},
                (p, m, a) -> {
                    switch (m.getName()) {
                        case "statusCode": return 201;
                        case "headers":
                            return HttpHeaders.of(Map.of("X", List.of("v")), (k, v) -> true);
                        case "version": return HttpClient.Version.HTTP_2;
                        default: return null;
                    }
                });

        check(info.statusCode() == 201, "statusCode");
        check("v".equals(info.headers().firstValue("X").orElse(null)), "headers");
        check(info.version() == HttpClient.Version.HTTP_2, "version");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
