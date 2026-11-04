/*
 * @test
 * @summary com.sun.net.httpserver.Headers conformance: white-box structure + black-box behavior.
 * @run main HeadersTest
 */

import java.lang.reflect.*;
import java.util.*;

public class HeadersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.net.httpserver.Headers");
        check(!Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "concrete class");
        check(java.util.Map.class.isAssignableFrom(c), "implements Map<String,List<String>>");
        Set<String> m = methodNames(c);
        String[] names = {"add","get","getFirst","set","containsKey","remove","of","put","putAll","clear","keySet","values","entrySet","replaceAll","size","isEmpty"};
        for (String n : names) check(m.contains(n), n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        com.sun.net.httpserver.Headers h = new com.sun.net.httpserver.Headers();
        check(h.isEmpty(), "init empty");
        h.add("X-Test", "a");
        h.add("X-Test", "b");
        check(h.get("X-Test").size() == 2, "two values");
        check(h.getFirst("X-Test").equals("a"), "getFirst preserves order");
        check(h.size() == 1, "size counts distinct keys");

        // case-insensitive key normalization (first char upper, rest lower)
        check(h.getFirst("x-test").equals("a"), "getFirst case-insensitive");
        check(h.containsKey("X-TEST"), "containsKey case-insensitive");
        h.set("x-test", "v");
        check(h.getFirst("X-Test").equals("v"), "set normalizes key and overwrites");
        check(h.get("X-Test").size() == 1, "set collapses to single value");

        h.set("X-Single", "w");
        check(h.containsKey("X-Single"), "containsKey set");
        h.remove("x-single");
        check(!h.containsKey("X-Single"), "remove case-insensitive");
        check(h.get("X-Missing") == null, "get missing key null");
        check(h.getFirst("X-Missing") == null, "getFirst missing key null");

        // put / putAll / clear
        com.sun.net.httpserver.Headers h2 = new com.sun.net.httpserver.Headers();
        h2.put("A", new ArrayList<>(Arrays.asList("1", "2")));
        check(h2.getFirst("A").equals("1"), "put");
        Map<String, List<String>> src = new HashMap<>();
        src.put("B", Arrays.asList("x"));
        h2.putAll(src);
        check(h2.getFirst("B").equals("x"), "putAll");
        check(h2.keySet().containsAll(Arrays.asList("A", "B")), "keySet");
        check(h2.values().size() == 2, "values size");
        check(h2.entrySet().size() == 2, "entrySet size");
        h2.replaceAll((k, v) -> v);  // no-op revalidation
        h2.clear();
        check(h2.isEmpty(), "clear empties");

        // equals / hashCode / toString
        com.sun.net.httpserver.Headers e1 = new com.sun.net.httpserver.Headers();
        com.sun.net.httpserver.Headers e2 = new com.sun.net.httpserver.Headers();
        e1.add("K", "v");
        e2.add("k", "v");
        check(e1.equals(e2), "equals case-insensitive");
        check(e1.hashCode() == e2.hashCode(), "hashCode consistent with equals");
        check(e1.toString().contains("Headers"), "toString mentions Headers");

        // of(String...) : immutable, multi-value, odd-length error, empty
        com.sun.net.httpserver.Headers im = com.sun.net.httpserver.Headers.of("K", "v1", "K", "v2");
        check(im.get("K").size() == 2, "of accumulates duplicate names");
        check(im.getFirst("K").equals("v1"), "of getFirst");
        boolean threw = false;
        try { im.add("K", "v3"); } catch (UnsupportedOperationException e) { threw = true; }
        check(threw, "of(String...) is immutable on add");
        threw = false;
        try { im.set("K", "v3"); } catch (UnsupportedOperationException e) { threw = true; }
        check(threw, "of(String...) is immutable on set");
        threw = false;
        try { com.sun.net.httpserver.Headers.of("K"); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "of odd number of strings throws IAE");
        check(com.sun.net.httpserver.Headers.of().isEmpty(), "of() empty");

        // of(Map) immutable
        Map<String, List<String>> m = new HashMap<>();
        m.put("M", Arrays.asList("1", "2"));
        com.sun.net.httpserver.Headers im2 = com.sun.net.httpserver.Headers.of(m);
        check(im2.get("M").size() == 2, "of(Map) copies values");
        threw = false;
        try { im2.remove("M"); } catch (UnsupportedOperationException e) { threw = true; }
        check(threw, "of(Map) is immutable on remove");

        // Map copy constructor yields a mutable, independent copy
        com.sun.net.httpserver.Headers copy = new com.sun.net.httpserver.Headers(m);
        copy.add("M", "3");
        check(copy.get("M").size() == 3, "copy constructor is mutable");
        check(im2.get("M").size() == 2, "copy constructor does not alias source");

        // null rejection
        threw = false;
        try { new com.sun.net.httpserver.Headers().add(null, "v"); } catch (NullPointerException e) { threw = true; }
        check(threw, "add null key throws NPE");
        threw = false;
        try { new com.sun.net.httpserver.Headers().containsKey(null); } catch (NullPointerException e) { threw = true; }
        check(threw, "containsKey null throws NPE");
        threw = false;
        try { new com.sun.net.httpserver.Headers().add("K", "v\r\nX: y"); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "add value with CR/LF throws IAE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
