/*
 * @test
 * @summary java.net.http.HttpResponse conformance: interface method surface.
 * @run main HttpResponseTest
 */

import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpResponseTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Method method(String name) {
        for (Method m : HttpResponse.class.getMethods()) {
            if (m.getName().equals(name)) return m;
        }
        return null;
    }

    public static void main(String[] args) {
        check(HttpResponse.class.isInterface(), "HttpResponse is an interface");
        check(method("statusCode") != null && method("statusCode").getReturnType() == int.class,
                "statusCode returns int");
        check(method("request") != null && method("request").getReturnType() == HttpRequest.class,
                "request returns HttpRequest");
        check(method("headers") != null && method("headers").getReturnType() == HttpHeaders.class,
                "headers returns HttpHeaders");
        check(method("body") != null && method("body").getReturnType() == Object.class,
                "body returns the type parameter (erased to Object)");
        check(method("uri") != null && method("uri").getReturnType() == URI.class,
                "uri returns URI");
        check(method("version") != null && method("version").getReturnType() == HttpClient.Version.class,
                "version returns HttpClient.Version");
        check(method("connectionLabel") != null && method("connectionLabel").isDefault(),
                "connectionLabel is a default method");
        check(method("previousResponse") != null, "previousResponse present");
        check(method("sslSession") != null, "sslSession present");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
