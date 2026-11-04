/*
 * @test
 * @summary java.net.http.HttpHeaders conformance: white-box structure + black-box accessors/filter/immutability.
 * @run main HttpHeadersTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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

    static HttpHeaders of(String k, String... vs) {
        return HttpHeaders.of(Map.of(k, List.of(vs)), (a, b) -> true);
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.http.HttpHeaders");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Object.class, "superclass is Object");
        check(c.getConstructors().length == 0, "no public constructors (of() only)");
        check(!c.isInterface() && !c.isEnum() && !c.isRecord(), "plain class");
        check(Modifier.isPublic(c.getMethod("firstValue", String.class).getModifiers()), "firstValue public");
        check(Modifier.isPublic(c.getMethod("firstValueAsLong", String.class).getModifiers()),
                "firstValueAsLong public");
        check(Modifier.isPublic(c.getMethod("allValues", String.class).getModifiers()), "allValues public");
        check(Modifier.isPublic(c.getMethod("map").getModifiers()), "map public");
        check(c.getMethod("firstValue", String.class).getReturnType() == Optional.class,
                "firstValue returns Optional<String>");
        check(c.getMethod("map").getReturnType() == Map.class, "map returns Map<String,List<String>>");
        check(Modifier.isStatic(c.getMethod("of", Map.class, java.util.function.BiPredicate.class).getModifiers()),
                "of is static");
        int ofCount = 0;
        for (Method m : c.getMethods()) {
            if (m.getName().equals("of")) ofCount++;
        }
        check(ofCount == 1, "exactly one of() overload (two-arg; no single-arg in JDK-27, probed)");
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

    // ---- deepened: construction negatives (probed) ----
    static void testConstructionNegatives() {
        expect(NullPointerException.class, () -> HttpHeaders.of(null, (k, v) -> true), "of(null, f) NPE");
        expect(NullPointerException.class, () -> HttpHeaders.of(Map.of(), null), "of(map, null) NPE");
        Map<String, List<String>> nk = new HashMap<>();
        nk.put(null, List.of("v"));
        expect(NullPointerException.class, () -> HttpHeaders.of(nk, (k, v) -> true), "null key NPE");
        Map<String, List<String>> nv = new HashMap<>();
        nv.put("X", null);
        expect(NullPointerException.class, () -> HttpHeaders.of(nv, (k, v) -> true), "null value list NPE");
        Map<String, List<String>> ne = new HashMap<>();
        ne.put("X", Arrays.asList("a", null));
        expect(NullPointerException.class, () -> HttpHeaders.of(ne, (k, v) -> true), "null inside list NPE");
        expect(IllegalArgumentException.class, () -> HttpHeaders.of(Map.of("", List.of("e")), (k, v) -> true),
                "empty name IAE 'empty key' (probed)");
        expect(IllegalArgumentException.class, () -> HttpHeaders.of(Map.of("   ", List.of("v")), (k, v) -> true),
                "blank name trims to empty -> IAE 'empty key' (probed)");
        // Duplicate detection happens BEFORE filtering (probed).
        Map<String, List<String>> d = new HashMap<>();
        d.put("Foo", List.of("a"));
        d.put("FOO", List.of("b"));
        try {
            HttpHeaders.of(d, (k, v) -> !k.equals("FOO"));
            check(false, "filtered duplicate must still throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("duplicate key:"),
                    "dup message is 'duplicate key: <k>' (probed)");
        }
        // A filter that throws propagates.
        expect(IllegalStateException.class,
                () -> HttpHeaders.of(Map.of("X", List.of("v")), (k, v) -> {
                    throw new IllegalStateException("boom");
                }), "filter exception propagates (probed)");
    }

    // ---- deepened: normalization (trim) and lenient acceptance (probed) ----
    static void testNormalization() {
        HttpHeaders trimmedName = HttpHeaders.of(Map.of("  X-Y  ", List.of("v")), (k, v) -> true);
        check(trimmedName.map().keySet().equals(java.util.Set.of("X-Y")), "names are trimmed (probed)");
        check("v".equals(trimmedName.firstValue("X-Y").orElse(null)), "trimmed name lookup");

        HttpHeaders trimmedVal = HttpHeaders.of(Map.of("X", List.of("  hello  ")), (k, v) -> true);
        check(List.of("hello").equals(trimmedVal.allValues("X")), "values are trimmed (probed)");

        HttpHeaders mixed = HttpHeaders.of(Map.of("W", Arrays.asList(" a ", "  ")), (k, v) -> true);
        check(mixed.allValues("W").equals(List.of("a", "")), "each value trimmed independently (probed)");

        HttpHeaders emptyList = HttpHeaders.of(Map.of("X", List.of()), (k, v) -> true);
        check(emptyList.map().isEmpty(), "a key with an empty value list disappears (probed)");
        check(!emptyList.firstValue("X").isPresent(), "empty-list key absent from firstValue");

        HttpHeaders emptyVal = HttpHeaders.of(Map.of("E", List.of("")), (k, v) -> true);
        check("".equals(emptyVal.firstValue("E").orElse("?")), "empty value string is kept (probed)");

        // No charset validation on of(): odd names/values are accepted as-is.
        check(HttpHeaders.of(Map.of("X Y", List.of("v")), (k, v) -> true) != null,
                "name with a space accepted by of() (probed)");
        check(HttpHeaders.of(Map.of("X", List.of("a\nb")), (k, v) -> true) != null,
                "value with LF accepted by of() (probed)");
        check(HttpHeaders.of(Map.of("Xé", List.of("v")), (k, v) -> true) != null,
                "non-ASCII name accepted by of() (probed)");
        HttpHeaders htab = HttpHeaders.of(Map.of("X\tY", List.of("v")), (k, v) -> true);
        check(htab.map().containsKey("X\tY"), "name with HTAB preserved (probed)");
    }

    // ---- deepened: per-value filter semantics (probed) ----
    static void testFilterSemantics() {
        List<String> seen = new ArrayList<>();
        int[] calls = new int[1];
        HttpHeaders hh = HttpHeaders.of(
                new TreeMap<>(Map.of("A", List.of("1", "2"), "B", List.of("3"))),
                (k, v) -> { calls[0]++; seen.add(k + "=" + v); return true; });
        check(calls[0] == 3, "filter invoked once per (name, value) pair (probed)");
        check(seen.equals(List.of("A=1", "A=2", "B=3")), "filter visits values in order (probed)");

        // Dropping one value of a multi-valued header keeps the key with the rest.
        HttpHeaders partial = HttpHeaders.of(
                new TreeMap<>(Map.of("A", List.of("1", "2"), "B", List.of("3"))),
                (k, v) -> !(k.equals("A") && v.equals("2")));
        check(partial.allValues("A").equals(List.of("1")), "per-value drop keeps remaining values (probed)");
        check(partial.allValues("B").equals(List.of("3")), "untouched key unaffected");
        check(partial.map().keySet().equals(java.util.Set.of("A", "B")), "both keys retained");

        // Dropping every value of a key removes the key.
        HttpHeaders none = HttpHeaders.of(Map.of("A", List.of("1", "2")), (k, v) -> false);
        check(none.map().isEmpty(), "all values dropped -> empty headers (probed)");

        // The filter sees trimmed names and values but the original name case.
        List<String> seen2 = new ArrayList<>();
        HttpHeaders.of(Map.of("  X-P  ", List.of("  v  ")), (k, v) -> { seen2.add("[" + k + "|" + v + "]"); return true; });
        check(seen2.equals(List.of("[X-P|v]")), "filter receives trimmed name and value (probed)");
        List<String> seen3 = new ArrayList<>();
        HttpHeaders.of(Map.of("LowerCase", List.of("x")), (k, v) -> { seen3.add(k); return true; });
        check(seen3.equals(List.of("LowerCase")), "filter sees the original name case (probed)");
    }

    // ---- deepened: firstValueAsLong matrix (probed) ----
    static void testAsLong() {
        HttpHeaders nums = HttpHeaders.of(Map.of(
                "N", List.of("42"), "NEG", List.of("-7"),
                "BIG", List.of("99999999999999999999"), "TXT", List.of("abc"),
                "PAD", List.of("  99  "), "PLUS", List.of("+42"),
                "EMPTY", List.of("")), (k, v) -> true);
        check(nums.firstValueAsLong("N").getAsLong() == 42L, "asLong positive");
        check(nums.firstValueAsLong("n").getAsLong() == 42L, "asLong case-insensitive key");
        check(nums.firstValueAsLong("NEG").getAsLong() == -7L, "asLong negative");
        check(nums.firstValueAsLong("PAD").getAsLong() == 99L, "asLong trims whitespace (probed)");
        check(nums.firstValueAsLong("PLUS").getAsLong() == 42L, "asLong accepts leading + (probed)");
        check(!nums.firstValueAsLong("MISSING").isPresent(), "asLong missing -> empty");
        try {
            nums.firstValueAsLong("BIG");
            check(false, "overflow must throw");
        } catch (NumberFormatException e) {
            check("For input string: \"99999999999999999999\"".equals(e.getMessage()),
                    "asLong overflow message (probed)");
        }
        try {
            nums.firstValueAsLong("TXT");
            check(false, "text must throw");
        } catch (NumberFormatException e) {
            check("For input string: \"abc\"".equals(e.getMessage()), "asLong text message (probed)");
        }
        expect(NumberFormatException.class, () -> nums.firstValueAsLong("EMPTY"),
                "asLong on empty string NFE (probed)");
        HttpHeaders minmax = HttpHeaders.of(Map.of(
                "MIN", List.of(String.valueOf(Long.MIN_VALUE)),
                "MAX", List.of(String.valueOf(Long.MAX_VALUE))), (k, v) -> true);
        check(minmax.firstValueAsLong("MIN").getAsLong() == Long.MIN_VALUE, "asLong Long.MIN_VALUE");
        check(minmax.firstValueAsLong("MAX").getAsLong() == Long.MAX_VALUE, "asLong Long.MAX_VALUE");
        HttpHeaders firstOfTwo = HttpHeaders.of(Map.of("F", List.of("7", "8")), (k, v) -> true);
        check(firstOfTwo.firstValueAsLong("F").getAsLong() == 7L, "asLong uses only the first value");
        expect(NullPointerException.class, () -> nums.firstValueAsLong(null), "asLong(null) NPE (probed)");
    }

    // ---- deepened: multi-values and case-insensitive lookups ----
    static void testLookups() {
        HttpHeaders multi = HttpHeaders.of(Map.of("X-Multi", List.of("a", "b", "c")), (k, v) -> true);
        check(multi.allValues("x-multi").equals(List.of("a", "b", "c")),
                "allValues preserves declaration order");
        check(multi.allValues("X-MULTI").size() == 3, "allValues any case");
        check("a".equals(multi.firstValue("X-Multi").orElse(null)), "firstValue returns the first");
        check(multi.allValues("X-Multi").equals(multi.allValues("x-MuLtI")),
                "allValues equal across case variants");
        expect(NullPointerException.class, () -> multi.firstValue(null), "firstValue(null) NPE (probed)");
        expect(NullPointerException.class, () -> multi.allValues(null), "allValues(null) NPE (probed)");
        check(multi.allValues("nope").isEmpty(), "absent allValues is empty (not null)");

        // map() view: sorted keys, case-insensitive membership, value access.
        HttpHeaders sorted = HttpHeaders.of(
                new TreeMap<>(Map.of("Zeta", List.of("1"), "Alpha", List.of("2"), "Mid", List.of("3"))),
                (k, v) -> true);
        List<String> keys = new ArrayList<>(sorted.map().keySet());
        check(keys.equals(List.of("Alpha", "Mid", "Zeta")), "map() keys sorted (probed)");
        check(sorted.map().containsKey("zeta"), "map() containsKey case-insensitive");
        check(sorted.map().containsKey("ALPHA"), "map() containsKey uppercase");
        check(sorted.map().get("MID").equals(List.of("3")), "map() get with other case");
        check(sorted.map().containsValue(List.of("2")), "map() containsValue");
        check(sorted.map().equals(Map.of("Alpha", List.of("2"), "Mid", List.of("3"), "Zeta", List.of("1"))),
                "map() equals an equivalent plain map");
        check(sorted.map() == sorted.map(), "map() returns the same view each call");
        check(multi.allValues("X-Multi") == multi.allValues("X-Multi"),
                "allValues returns the same list each call (probed)");
    }

    // ---- deepened: unmodifiability matrix (probed) ----
    static void testImmutability() {
        HttpHeaders multi = HttpHeaders.of(Map.of("X-Multi", List.of("a", "b", "c")), (k, v) -> true);
        expect(UnsupportedOperationException.class, () -> multi.allValues("X-Multi").add("d"),
                "allValues add UOE");
        expect(UnsupportedOperationException.class, () -> multi.allValues("X-Multi").set(0, "z"),
                "allValues set UOE (probed)");
        expect(UnsupportedOperationException.class, () -> multi.allValues("X-Multi").remove(0),
                "allValues remove UOE");
        expect(UnsupportedOperationException.class, () -> multi.map().put("Y", List.of("z")), "map put UOE");
        expect(UnsupportedOperationException.class, () -> multi.map().remove("X-Multi"), "map remove UOE");
        expect(UnsupportedOperationException.class, () -> multi.map().get("x-MULTI").add("d"),
                "map value add UOE");
        expect(UnsupportedOperationException.class, () -> multi.map().get("X-Multi").set(0, "z"),
                "map value set UOE (probed)");
        expect(UnsupportedOperationException.class,
                () -> multi.map().entrySet().remove(multi.map().entrySet().iterator().next()),
                "entrySet remove UOE");
        expect(UnsupportedOperationException.class,
                () -> multi.map().entrySet().add(Map.entry("Z", List.of("q"))),
                "entrySet add UOE (probed)");
        expect(UnsupportedOperationException.class,
                () -> multi.map().values().iterator().next().add("d"), "values add UOE");
        expect(UnsupportedOperationException.class,
                () -> multi.map().keySet().iterator().remove(), "keySet iterator remove UOE (probed)");
        expect(UnsupportedOperationException.class, () -> multi.map().clear(), "map clear UOE");
        check(multi.map().get("X-Multi").contains("a"), "value list readable");
        check(multi.map().get("X-Multi").size() == 3, "value list size readable");
    }

    // ---- deepened: equality, hashing, toString ----
    static void testEqualsHashToString() {
        HttpHeaders c1 = of("X-A", "1");
        HttpHeaders c2 = of("x-a", "1");
        check(c1.equals(c2), "equals ignores key case (probed)");
        check(c1.hashCode() == c2.hashCode(), "hashCode ignores key case (probed)");
        check(c2.equals(c1), "equals symmetric");
        HttpHeaders c3 = of("X-A", "1", "2");
        check(!c1.equals(c3), "different value lists unequal");
        HttpHeaders v1 = of("A", "1", "2");
        HttpHeaders v2 = of("A", "2", "1");
        check(!v1.equals(v2), "value order matters for equality (probed)");
        HttpHeaders k1 = of("A", "1");
        HttpHeaders k2 = of("B", "1");
        check(!k1.equals(k2), "different keys unequal");
        HttpHeaders a1 = of("Q", "1");
        HttpHeaders a3 = of("Q", "1");
        check(a1.equals(c2) == false && a1.equals(a3), "equals consistent");
        check(!a1.equals(null), "not equal to null");
        check(!a1.equals("Q=1"), "not equal to a foreign type");

        HttpHeaders e1 = HttpHeaders.of(Map.of(), (k, v) -> true);
        HttpHeaders e2 = HttpHeaders.of(new HashMap<>(), (k, v) -> true);
        check(e1.equals(e2), "empty headers equal");
        check(e1.hashCode() == e2.hashCode(), "empty hashCode equal");
        check(e1.hashCode() == 0, "empty hashCode is 0 (probed)");
        check(e1.map().isEmpty(), "empty map view");
        check(e1 == e2, "empty headers are a cached singleton (probed)");
        check(!e1.equals(a1) && !a1.equals(e1), "empty vs non-empty unequal");

        String s = multiToString();
        check(s.startsWith("java.net.http.HttpHeaders"), "toString class prefix (probed)");
        check(s.contains("X-Multi=[a, b, c]"), "toString renders the map (probed)");
        String es = e1.toString();
        check(es.contains("{ {} }"), "empty toString shows empty map (probed)");
    }

    static String multiToString() {
        return HttpHeaders.of(Map.of("X-Multi", List.of("a", "b", "c")), (k, v) -> true).toString();
    }

    // ---- deepened: snapshot isolation (probed) ----
    static void testSnapshot() {
        Map<String, List<String>> src = new HashMap<>();
        src.put("X", List.of("1"));
        HttpHeaders snap = HttpHeaders.of(src, (k, v) -> true);
        src.put("Y", List.of("2"));
        check(snap.map().keySet().equals(java.util.Set.of("X")),
                "mutating the source map after of() is not visible (probed)");
        src.remove("X");
        check(snap.firstValue("X").isPresent(), "removing a source entry is not visible");

        List<String> vals = new ArrayList<>(List.of("1"));
        HttpHeaders snap2 = HttpHeaders.of(Map.of("X", vals), (k, v) -> true);
        vals.add("2");
        check(snap2.allValues("X").equals(List.of("1")),
                "mutating the source value list after of() is not visible (probed)");
        vals.set(0, "mutated");
        check("1".equals(snap2.firstValue("X").orElse(null)), "element mutation not visible");
    }

    // ---- deepened: larger maps keep their entries ----
    static void testBiggerMaps() {
        Map<String, List<String>> big = new TreeMap<>();
        big.put("A", List.of("1"));
        big.put("B", List.of("2", "3"));
        big.put("C", List.of());
        big.put("D", List.of("4"));
        HttpHeaders hb = HttpHeaders.of(big, (k, v) -> true);
        check(hb.map().size() == 3, "empty-valued key dropped, 3 remain");
        check(hb.map().keySet().equals(java.util.Set.of("A", "B", "D")), "keys A, B, D");
        check(hb.allValues("B").equals(List.of("2", "3")), "B has two values");
        int total = 0;
        for (Map.Entry<String, List<String>> en : hb.map().entrySet()) {
            total += en.getValue().size();
        }
        check(total == 4, "four total values across entries");
        // equals against a same-content but differently-keyed-case construction
        HttpHeaders hb2 = HttpHeaders.of(
                Map.of("a", List.of("1"), "b", List.of("2", "3"), "d", List.of("4")), (k, v) -> true);
        check(hb.equals(hb2), "equals across construction orders and key case");
    }

    // ---- deepened: numeric parsing edge cases and misc surface (probed) ----
    static void testMoreEdges() {
        check(!java.io.Serializable.class.isAssignableFrom(HttpHeaders.class),
                "HttpHeaders is not Serializable (probed)");
        HttpHeaders z = HttpHeaders.of(Map.of("Z", List.of("0")), (k, v) -> true);
        check(z.firstValueAsLong("Z").getAsLong() == 0L, "asLong zero");
        HttpHeaders hex = HttpHeaders.of(Map.of("H", List.of("0x10")), (k, v) -> true);
        expect(NumberFormatException.class, () -> hex.firstValueAsLong("H"),
                "asLong hex literal NFE (probed)");
        HttpHeaders und = HttpHeaders.of(Map.of("U", List.of("1_000")), (k, v) -> true);
        expect(NumberFormatException.class, () -> und.firstValueAsLong("U"),
                "asLong underscore digits NFE (probed)");
        HttpHeaders mid = HttpHeaders.of(Map.of("S", List.of("1 2")), (k, v) -> true);
        expect(NumberFormatException.class, () -> mid.firstValueAsLong("S"),
                "asLong embedded space NFE (probed)");

        HttpHeaders one = HttpHeaders.of(Map.of("A", List.of("1")), (k, v) -> true);
        check(one.map().get("nope") == null, "map().get of a missing key is null");
        check(one.map().entrySet().size() == 1, "entrySet size");
        check(one.map().values().size() == 1, "values size");
        check(one.firstValue("A").equals(Optional.of("1")), "Optional content equality");

        // Only the empty headers are cached; non-empty ones are distinct instances.
        Map<String, List<String>> m = Map.of("A", List.of("1"));
        HttpHeaders r1 = HttpHeaders.of(m, (k, v) -> true);
        HttpHeaders r2 = HttpHeaders.of(m, (k, v) -> true);
        check(r1 != r2, "non-empty constructions are distinct instances (probed)");
        check(r1.equals(r2), "but equal by value");

        HttpHeaders two = HttpHeaders.of(
                new TreeMap<>(Map.of("B", List.of("2"), "A", List.of("1"))), (k, v) -> true);
        check(two.toString().contains("A=[1], B=[2]"), "two-key toString in sorted order (probed)");

        HttpHeaders dropped = HttpHeaders.of(Map.of("A", List.of("x")), (k, v) -> v.equals("y"));
        check(!dropped.firstValue("A").isPresent(), "value-filtered key has no firstValue (probed)");
        check(dropped.map().isEmpty(), "value-filtered key removed from map");

        HttpHeaders ten = HttpHeaders.of(Map.of("T",
                List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")), (k, v) -> true);
        check(ten.allValues("T").size() == 10, "ten values retained in order");
        check("9".equals(ten.allValues("T").get(9)), "last value correct");
        check(ten.firstValueAsLong("T").getAsLong() == 0L, "asLong takes the first of ten");

        int[] c = new int[1];
        HttpHeaders.of(Map.of("E", List.of()), (k, v) -> { c[0]++; return true; });
        check(c[0] == 0, "filter is never called for an empty value list (probed)");

        // Empty-headers surface completeness.
        HttpHeaders emptyAgain = HttpHeaders.of(new TreeMap<>(), (k, v) -> true);
        check(emptyAgain == e1Singleton(), "empty construction reuses the singleton");
        check(emptyAgain.allValues("ANY").isEmpty(), "empty allValues");
        check(emptyAgain.map().get("ANY") == null, "empty map().get");
        check(emptyAgain.toString().startsWith("java.net.http.HttpHeaders"),
                "empty toString class prefix");
    }

    static HttpHeaders e1Singleton() {
        return HttpHeaders.of(Map.of(), (k, v) -> true);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructionNegatives();
        testNormalization();
        testFilterSemantics();
        testAsLong();
        testLookups();
        testImmutability();
        testEqualsHashToString();
        testSnapshot();
        testBiggerMaps();
        testMoreEdges();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
