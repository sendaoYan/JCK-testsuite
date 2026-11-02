/*
 * @test
 * @summary java.net.http.HttpClient.Version conformance: enum constants and order.
 * @run main VersionTest
 */

import java.net.http.HttpClient;

public class VersionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpClient.Version.class.isEnum(), "Version is an enum");

        HttpClient.Version[] values = HttpClient.Version.values();
        check(values.length == 3, "three constants");
        check(values[0] == HttpClient.Version.HTTP_1_1, "HTTP_1_1 is first");
        check(values[1] == HttpClient.Version.HTTP_2, "HTTP_2 is second");
        check(values[2] == HttpClient.Version.HTTP_3, "HTTP_3 is third");

        check(HttpClient.Version.valueOf("HTTP_2") == HttpClient.Version.HTTP_2, "valueOf HTTP_2");
        check(HttpClient.Version.valueOf("HTTP_3") == HttpClient.Version.HTTP_3, "valueOf HTTP_3");
        check("HTTP_1_1".equals(HttpClient.Version.HTTP_1_1.name()), "HTTP_1_1 name");
        check(HttpClient.Version.HTTP_1_1.ordinal() == 0, "HTTP_1_1 ordinal 0");
        check(HttpClient.Version.HTTP_3.ordinal() == 2, "HTTP_3 ordinal 2");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
