/*
 * @test
 * @summary java.net.http.HttpClient conformance: abstract AutoCloseable class with sane defaults.
 * @run main HttpClientTest
 */

import java.lang.reflect.Modifier;
import java.net.http.HttpClient;

public class HttpClientTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Modifier.isAbstract(HttpClient.class.getModifiers()), "HttpClient is abstract");
        check(AutoCloseable.class.isAssignableFrom(HttpClient.class), "HttpClient implements AutoCloseable");

        HttpClient c = HttpClient.newHttpClient();
        check(c != null, "newHttpClient() non-null");
        check(c.followRedirects() == HttpClient.Redirect.NEVER, "default followRedirects is NEVER");
        check(c.version() == HttpClient.Version.HTTP_2, "default version is HTTP_2");
        check(c.sslContext() != null, "sslContext() non-null");
        check(c.sslParameters() != null, "sslParameters() non-null");
        check(!c.executor().isPresent(), "default executor absent");
        check(!c.connectTimeout().isPresent(), "default connectTimeout absent");
        check(c.newWebSocketBuilder() != null, "newWebSocketBuilder() non-null");
        check(!c.isTerminated(), "not terminated before shutdown");

        HttpClient built = HttpClient.newBuilder().build();
        check(built != null, "newBuilder().build() non-null");
        check(built.followRedirects() == HttpClient.Redirect.NEVER, "built client default redirect NEVER");

        c.close();
        built.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
