/*
 * @test
 * @summary java.net.http.HttpClient.Builder conformance: builder methods return this and persist state.
 * @run main BuilderTest
 */

import java.net.InetAddress;
import java.net.http.HttpClient;
import java.time.Duration;

public class BuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(HttpClient.Builder.class.isInterface(), "Builder is an interface");
        check(HttpClient.Builder.NO_PROXY != null, "NO_PROXY constant non-null");

        HttpClient.Builder b = HttpClient.newBuilder();
        check(b != null, "newBuilder() non-null");
        check(b.followRedirects(HttpClient.Redirect.ALWAYS) == b, "followRedirects returns this");
        check(b.version(HttpClient.Version.HTTP_1_1) == b, "version returns this");
        check(b.connectTimeout(Duration.ofSeconds(5)) == b, "connectTimeout returns this");
        check(b.priority(7) == b, "priority returns this");
        check(b.localAddress(InetAddress.getLoopbackAddress()) == b, "localAddress returns this");

        HttpClient c = b.build();
        check(c != null, "build() non-null");
        check(c.followRedirects() == HttpClient.Redirect.ALWAYS, "redirect ALWAYS persisted");
        check(c.version() == HttpClient.Version.HTTP_1_1, "version HTTP_1_1 persisted");
        check(c.connectTimeout().isPresent()
                && Duration.ofSeconds(5).equals(c.connectTimeout().get()), "connectTimeout persisted");
        check(!c.authenticator().isPresent(), "authenticator empty by default");
        check(!c.cookieHandler().isPresent(), "cookieHandler empty by default");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
