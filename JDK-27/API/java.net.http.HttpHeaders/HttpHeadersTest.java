/*
 * @test
 * @summary java.net.http.HttpHeaders conformance: white-box structure + black-box accessors/filter/immutability.
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

    static void expect(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.http.HttpHeaders");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Object.class, "superclass is Object");
    }

    static void testBlackBox() {
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
        check(!h.firstValue("X-Missing").isPresent(), "firstValue absent is empty");
        check(h.allValues("X-Missing").isEmpty(), "allValues absent is empty list");

        expect(NumberFormatException.class, () -> h.firstValueAsLong("Content-Type"),
                "firstValueAsLong on non-numeric value throws NumberFormatException");

        check(h.map().get("Content-Type").contains("text/plain"), "map() round-trip");
        check(h.map().containsKey("X-Num"), "map() has key");
        expect(UnsupportedOperationException.class, () -> h.map().put("X", List.of("y")), "map() is unmodifiable");

        String str = h.toString();
        check(str != null && str.contains("Content-Type"), "toString contains header name");

        HttpHeaders h2 = HttpHeaders.of(m, (k, v) -> true);
        check(h.equals(h2), "equals");
        check(h.hashCode() == h2.hashCode(), "hashCode consistent with equals");

        // Filtered headers: excluded keys disappear, remaining keys survive.
        HttpHeaders filtered = HttpHeaders.of(m, (k, v) -> !"X-Num".equalsIgnoreCase(k));
        check(!filtered.firstValue("X-Num").isPresent(), "filtered header removed");
        check(Optional.of("text/plain").equals(filtered.firstValue("Content-Type")), "unfiltered header kept");

        // Empty headers.
        HttpHeaders empty = HttpHeaders.of(Map.of(), (k, v) -> true);
        check(empty.map().isEmpty(), "empty headers map");
        check(!empty.firstValue("X").isPresent(), "empty headers firstValue");

        // Duplicate keys (case-insensitive) are rejected.
        Map<String, List<String>> dup = new HashMap<>();
        dup.put("X", List.of("a"));
        dup.put("x", List.of("b"));
        expect(IllegalArgumentException.class, () -> HttpHeaders.of(dup, (k, v) -> true), "duplicate keys throw");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
