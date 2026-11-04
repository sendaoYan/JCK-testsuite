/*
 * @test
 * @summary java.net.http.HttpClient.Version conformance: enum constants and order.
 * @run main VersionTest
 */

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class VersionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpClient.Version.class.isEnum(), "Version is an enum");
        check(HttpClient.Version.HTTP_1_1.getDeclaringClass() == HttpClient.Version.class,
                "getDeclaringClass is Version");

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

        // ---- deepened: full shape of all three constants ----
        check("HTTP_2".equals(HttpClient.Version.HTTP_2.name()), "HTTP_2 name");
        check("HTTP_3".equals(HttpClient.Version.HTTP_3.name()), "HTTP_3 name");
        check(HttpClient.Version.HTTP_2.ordinal() == 1, "HTTP_2 ordinal 1");
        check(HttpClient.Version.valueOf("HTTP_1_1") == HttpClient.Version.HTTP_1_1, "valueOf HTTP_1_1");

        // JDK-27 fact: HTTP_3 exists (planned long-term; present in this release).
        check(Enum.valueOf(HttpClient.Version.class, "HTTP_3") == HttpClient.Version.HTTP_3,
                "Enum.valueOf HTTP_3");

        // values() returns a fresh array each call; mutating it does not affect the enum.
        HttpClient.Version[] v1 = HttpClient.Version.values();
        HttpClient.Version[] v2 = HttpClient.Version.values();
        check(v1 != v2, "values() returns a fresh array");
        v1[0] = null;
        check(HttpClient.Version.values()[0] == HttpClient.Version.HTTP_1_1,
                "mutating values() array does not affect the enum");

        // compareTo ordering follows declaration order.
        check(HttpClient.Version.HTTP_1_1.compareTo(HttpClient.Version.HTTP_2) < 0, "1_1 < 2");
        check(HttpClient.Version.HTTP_2.compareTo(HttpClient.Version.HTTP_3) < 0, "2 < 3");
        check(HttpClient.Version.HTTP_3.compareTo(HttpClient.Version.HTTP_1_1) > 0, "3 > 1_1");
        check(HttpClient.Version.HTTP_2.compareTo(HttpClient.Version.HTTP_2) == 0, "2 == 2");
        check(HttpClient.Version.HTTP_1_1.ordinal() < HttpClient.Version.HTTP_2.ordinal()
                && HttpClient.Version.HTTP_2.ordinal() < HttpClient.Version.HTTP_3.ordinal(),
                "ordinals strictly increasing");

        // Sorting a list of the constants yields declaration order.
        List<HttpClient.Version> shuffled = new ArrayList<>(Arrays.asList(
                HttpClient.Version.HTTP_3, HttpClient.Version.HTTP_1_1, HttpClient.Version.HTTP_2));
        java.util.Collections.sort(shuffled);
        check(shuffled.equals(Arrays.asList(HttpClient.Version.values())), "sorted order == declaration order");

        // equals/hashCode identity semantics.
        check(HttpClient.Version.HTTP_2.equals(HttpClient.Version.valueOf("HTTP_2")), "equals same");
        check(!HttpClient.Version.HTTP_2.equals(HttpClient.Version.HTTP_3), "differs from HTTP_3");
        check(!HttpClient.Version.HTTP_3.equals(null), "not equal to null");
        check(!HttpClient.Version.HTTP_3.equals("HTTP_3"), "not equal to a String");
        check(HttpClient.Version.HTTP_2.hashCode() == HttpClient.Version.valueOf("HTTP_2").hashCode(),
                "hashCode consistent");

        // toString returns the constant name.
        check("HTTP_2".equals(HttpClient.Version.HTTP_2.toString()), "toString is name");
        check("HTTP_3".equals(HttpClient.Version.HTTP_3.toString()), "HTTP_3 toString");

        // valueOf with unknown / null names.
        try {
            HttpClient.Version.valueOf("HTTP_4");
            check(false, "valueOf(HTTP_4) must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("HTTP_4"),
                    "valueOf(HTTP_4) IAE message contains name");
        }
        try {
            HttpClient.Version.valueOf(null);
            check(false, "valueOf(null) must throw");
        } catch (NullPointerException e) {
            check(true, "valueOf(null) NPE");
        }
        try {
            HttpClient.Version.valueOf("http_2");
            check(false, "valueOf(lowercase) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "valueOf(lowercase) IAE (case-sensitive)");
        }

        // EnumSet interop.
        check(EnumSet.allOf(HttpClient.Version.class).size() == 3, "EnumSet.allOf size 3");
        check(EnumSet.of(HttpClient.Version.HTTP_2).contains(HttpClient.Version.HTTP_2), "EnumSet.of contains");
        check(!EnumSet.of(HttpClient.Version.HTTP_2).contains(HttpClient.Version.HTTP_3),
                "EnumSet.of excludes others");

        // Enum constants usable through the client builder without any network.
        try (HttpClient c = HttpClient.newBuilder().version(HttpClient.Version.HTTP_3).build()) {
            check(c.version() == HttpClient.Version.HTTP_3, "HTTP_3 round-trips through HttpClient");
        }
        try (HttpClient c = HttpClient.newHttpClient()) {
            check(c.version() == HttpClient.Version.HTTP_2, "client default version is HTTP_2");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
