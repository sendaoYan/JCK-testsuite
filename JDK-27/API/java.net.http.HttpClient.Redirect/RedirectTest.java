/*
 * @test
 * @summary java.net.http.HttpClient.Redirect conformance: enum constants and order.
 * @run main RedirectTest
 */

import java.net.http.HttpClient;

public class RedirectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpClient.Redirect.class.isEnum(), "Redirect is an enum");

        HttpClient.Redirect[] values = HttpClient.Redirect.values();
        check(values.length == 3, "three constants");
        check(values[0] == HttpClient.Redirect.NEVER, "NEVER is first");
        check(values[1] == HttpClient.Redirect.ALWAYS, "ALWAYS is second");
        check(values[2] == HttpClient.Redirect.NORMAL, "NORMAL is third");

        check(HttpClient.Redirect.valueOf("NORMAL") == HttpClient.Redirect.NORMAL, "valueOf NORMAL");
        check(HttpClient.Redirect.valueOf("NEVER") == HttpClient.Redirect.NEVER, "valueOf NEVER");
        check("NEVER".equals(HttpClient.Redirect.NEVER.name()), "NEVER name");
        check(HttpClient.Redirect.NEVER.ordinal() == 0, "NEVER ordinal 0");
        check(HttpClient.Redirect.NORMAL.ordinal() == 2, "NORMAL ordinal 2");

        check(HttpClient.Redirect.NEVER != HttpClient.Redirect.ALWAYS
                && HttpClient.Redirect.ALWAYS != HttpClient.Redirect.NORMAL, "constants distinct");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
