/*
 * @test
 * @summary java.net.http.HttpClient.Redirect conformance: enum constants and order.
 * @run main RedirectTest
 */

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class RedirectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpClient.Redirect.class.isEnum(), "Redirect is an enum");
        check(HttpClient.Redirect.NEVER.getDeclaringClass() == HttpClient.Redirect.class,
                "getDeclaringClass is Redirect");

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

        // ---- deepened: full shape ----
        check(HttpClient.Redirect.valueOf("ALWAYS") == HttpClient.Redirect.ALWAYS, "valueOf ALWAYS");
        check("ALWAYS".equals(HttpClient.Redirect.ALWAYS.name()), "ALWAYS name");
        check("NORMAL".equals(HttpClient.Redirect.NORMAL.name()), "NORMAL name");
        check(HttpClient.Redirect.ALWAYS.ordinal() == 1, "ALWAYS ordinal 1");
        check(Enum.valueOf(HttpClient.Redirect.class, "NORMAL") == HttpClient.Redirect.NORMAL,
                "Enum.valueOf NORMAL");

        // values() yields a defensive fresh array.
        HttpClient.Redirect[] a1 = HttpClient.Redirect.values();
        HttpClient.Redirect[] a2 = HttpClient.Redirect.values();
        check(a1 != a2, "values() fresh array");
        a1[0] = null;
        check(HttpClient.Redirect.values()[0] == HttpClient.Redirect.NEVER,
                "mutating values() copy does not affect enum");

        // compareTo ordering.
        check(HttpClient.Redirect.NEVER.compareTo(HttpClient.Redirect.ALWAYS) < 0, "NEVER < ALWAYS");
        check(HttpClient.Redirect.ALWAYS.compareTo(HttpClient.Redirect.NORMAL) < 0, "ALWAYS < NORMAL");
        check(HttpClient.Redirect.NORMAL.compareTo(HttpClient.Redirect.NEVER) > 0, "NORMAL > NEVER");
        check(HttpClient.Redirect.NORMAL.compareTo(HttpClient.Redirect.NORMAL) == 0, "NORMAL == NORMAL");

        List<HttpClient.Redirect> shuffled = new ArrayList<>(Arrays.asList(
                HttpClient.Redirect.NORMAL, HttpClient.Redirect.ALWAYS, HttpClient.Redirect.NEVER));
        java.util.Collections.sort(shuffled);
        check(shuffled.equals(Arrays.asList(HttpClient.Redirect.values())),
                "sorted order == declaration order");

        // equals/hashCode.
        check(HttpClient.Redirect.ALWAYS.equals(HttpClient.Redirect.valueOf("ALWAYS")), "equals same");
        check(!HttpClient.Redirect.NEVER.equals(HttpClient.Redirect.NORMAL), "NEVER != NORMAL");
        check(!HttpClient.Redirect.ALWAYS.equals(null), "not equal to null");
        check(!HttpClient.Redirect.ALWAYS.equals("ALWAYS"), "not equal to String");
        check(HttpClient.Redirect.NORMAL.hashCode() == HttpClient.Redirect.valueOf("NORMAL").hashCode(),
                "hashCode consistent");

        // toString is the name.
        check("NEVER".equals(HttpClient.Redirect.NEVER.toString()), "toString is name");

        // No getter exposes semantics: shape-only assertions above.
        try {
            HttpClient.Redirect.valueOf("SOMETIMES");
            check(false, "valueOf(SOMETIMES) must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("SOMETIMES"),
                    "valueOf(unknown) IAE message contains name");
        }
        try {
            HttpClient.Redirect.valueOf(null);
            check(false, "valueOf(null) must throw");
        } catch (NullPointerException e) {
            check(true, "valueOf(null) NPE");
        }
        try {
            HttpClient.Redirect.valueOf("never");
            check(false, "valueOf(lowercase) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "valueOf(lowercase) IAE (case-sensitive)");
        }

        // EnumSet interop.
        check(EnumSet.allOf(HttpClient.Redirect.class).size() == 3, "EnumSet.allOf size 3");
        check(EnumSet.range(HttpClient.Redirect.NEVER, HttpClient.Redirect.ALWAYS).size() == 2,
                "EnumSet.range NEVER..ALWAYS");

        // Default and explicit policies round-trip through a client (no network).
        try (HttpClient c = HttpClient.newHttpClient()) {
            check(c.followRedirects() == HttpClient.Redirect.NEVER, "client default is NEVER");
        }
        for (HttpClient.Redirect r : HttpClient.Redirect.values()) {
            try (HttpClient c = HttpClient.newBuilder().followRedirects(r).build()) {
                check(c.followRedirects() == r, "followRedirects(" + r + ") round-trip");
            }
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
