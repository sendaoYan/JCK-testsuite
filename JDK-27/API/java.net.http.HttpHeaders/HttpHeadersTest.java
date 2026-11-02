/*
 * @test
 * @summary java.net.http.HttpHeaders conformance: value accessors and equality.
 * @run main HttpHeadersTest
 */

import java.lang.reflect.Modifier;
import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HttpHeadersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Modifier.isFinal(HttpHeaders.class.getModifiers()), "HttpHeaders is final");

        Map<String, List<String>> m = new HashMap<>();
        m.put("Content-Type", List.of("text/plain"));
        m.put("X-Num", List.of("42"));

        HttpHeaders h = HttpHeaders.of(m, (k, v) -> true);
        check(h != null, "of() non-null");
        check(Optional.of("text/plain").equals(h.firstValue("Content-Type")), "firstValue");
        check(Optional.of("text/plain").equals(h.firstValue("content-type")), "firstValue case-insensitive");
        check(List.of("42").equals(h.allValues("X-Num")), "allValues");
        check(h.firstValueAsLong("X-Num").getAsLong() == 42L, "firstValueAsLong");
        check(!h.firstValueAsLong("X-Missing").isPresent(), "firstValueAsLong absent is empty");

        boolean nfe = false;
        try {
            h.firstValueAsLong("Content-Type");
        } catch (NumberFormatException expected) {
            nfe = true;
        }
        check(nfe, "firstValueAsLong on non-numeric value throws NumberFormatException");

        check(h.map().get("Content-Type").contains("text/plain"), "map() round-trip");
        check(h.map().containsKey("X-Num"), "map() has key");

        HttpHeaders h2 = HttpHeaders.of(m, (k, v) -> true);
        check(h.equals(h2), "equals");
        check(h.hashCode() == h2.hashCode(), "hashCode consistent with equals");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
