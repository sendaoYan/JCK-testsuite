/*
 * @test
 * @summary java.util.Map.Entry conformance: getKey/getValue/setValue (returns old), equals/
 *          hashCode contract, and default comparingByKey/comparingByValue (natural + custom
 *          comparator + null-argument NPE).
 * @run main EntryTest
 */

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class EntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static void testAccessors() {
        Map.Entry<String, Integer> e = new AbstractMap.SimpleEntry<>("a", 2);
        check("a".equals(e.getKey()), "getKey");
        check(e.getValue() == 2, "getValue");
        check(e.setValue(3) == 2, "setValue returns old");
        check(e.getValue() == 3, "setValue updated value");
        Map.Entry<String, Integer> nulls = new AbstractMap.SimpleEntry<>(null, null);
        check(nulls.getKey() == null && nulls.getValue() == null, "SimpleEntry allows null key/value");
    }

    static void testEqualsHashCode() {
        Map.Entry<String, Integer> a = new AbstractMap.SimpleEntry<>("k", 1);
        Map.Entry<String, Integer> b = new AbstractMap.SimpleEntry<>("k", 1);
        Map.Entry<String, Integer> c = new AbstractMap.SimpleEntry<>("k", 2);
        check(a.equals(b) && b.equals(a), "entries with equal key/value are equal");
        check(a.hashCode() == b.hashCode(), "equal entries have equal hashCode");
        check(!a.equals(c), "entries with different value are not equal");
        check(!a.equals(null), "entry not equal to null");
    }

    static void testComparingByKey() {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(new AbstractMap.SimpleEntry<>("b", 1));
        entries.add(new AbstractMap.SimpleEntry<>("a", 2));
        entries.sort(Map.Entry.comparingByKey());
        check("a".equals(entries.get(0).getKey()), "comparingByKey natural");

        entries.sort(Map.Entry.comparingByKey(Comparator.<String>reverseOrder()));
        check("b".equals(entries.get(0).getKey()), "comparingByKey custom comparator");

        expectThrows(NullPointerException.class,
                () -> Map.Entry.<String, Integer>comparingByKey(null), "comparingByKey(null) NPE");
        Comparator<Map.Entry<String, Integer>> byKey = Map.Entry.comparingByKey();
        expectThrows(NullPointerException.class,
                () -> byKey.compare(new AbstractMap.SimpleEntry<>(null, 1),
                        new AbstractMap.SimpleEntry<>("a", 1)),
                "comparingByKey natural null key NPE");
    }

    static void testComparingByValue() {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(new AbstractMap.SimpleEntry<>("b", 2));
        entries.add(new AbstractMap.SimpleEntry<>("a", 1));
        entries.sort(Map.Entry.comparingByValue());
        check(entries.get(0).getValue() == 1, "comparingByValue natural");

        entries.sort(Map.Entry.comparingByValue(Comparator.<Integer>reverseOrder()));
        check(entries.get(0).getValue() == 2, "comparingByValue custom comparator");

        expectThrows(NullPointerException.class,
                () -> Map.Entry.<String, Integer>comparingByValue(null), "comparingByValue(null) NPE");
    }

    static void testLiveEntryContract() {
        Map<String, Integer> m = new java.util.HashMap<>();
        m.put("a", 2);
        Map.Entry<String, Integer> live = m.entrySet().iterator().next();
        check("a".equals(live.getKey()) && live.getValue() == 2, "live entry accessors");
        check(live.setValue(5) == 2, "live entry setValue returns old");
        check(m.get("a") == 5, "live entry setValue writes through to map");
        check(live.getValue() == 5, "entry reflects new value");

        // cross-implementation entry equality: entrySet entry vs SimpleEntry vs SimpleImmutableEntry.
        Map.Entry<String, Integer> se = new AbstractMap.SimpleEntry<>("a", 5);
        Map.Entry<String, Integer> sie = new AbstractMap.SimpleImmutableEntry<>("a", 5);
        check(live.equals(se) && se.equals(live), "live entry equals SimpleEntry");
        check(live.equals(sie) && sie.equals(live), "live entry equals SimpleImmutableEntry");
        check(se.equals(sie) && sie.equals(se), "SimpleEntry equals SimpleImmutableEntry same content");
        check(live.hashCode() == se.hashCode() && se.hashCode() == sie.hashCode(),
                "all Entry impls share hashCode formula");
        // "a".hashCode()==97, 97^5==100.
        check(live.hashCode() == 100, "entry hashCode formula 97^5==100, got " + live.hashCode());

        // hashCode formula with nulls: null key and null value both contribute 0.
        check(new AbstractMap.SimpleEntry<String, Integer>(null, 3).hashCode() == 3,
                "null key contributes 0");
        check(new AbstractMap.SimpleEntry<String, Integer>("a", null).hashCode() == 97,
                "null value contributes 0");
        check(new AbstractMap.SimpleEntry<String, Integer>(null, null).hashCode() == 0,
                "null key and value hash 0");

        // different-key and different-value entries are never equal; symmetric hashCode not required.
        check(!se.equals(new AbstractMap.SimpleEntry<>("b", 5)), "different key unequal");
        check(!se.equals(new AbstractMap.SimpleEntry<>("a", 6)), "different value unequal");
        check(!se.equals(null) && !se.equals("a=5"), "entry vs null / non-entry");
        check(se.equals(se), "entry equals reflexive");

        // toString of both wrapper entries contains key=value.
        check(se.toString().equals("a=5"), "SimpleEntry toString");
        check(sie.toString().equals("a=5"), "SimpleImmutableEntry toString");
    }

    static void testSimpleEntryFamily() {
        AbstractMap.SimpleEntry<String, Integer> e = new AbstractMap.SimpleEntry<>("k", 1);
        check(e.setValue(2) == 1 && e.setValue(3) == 2, "SimpleEntry repeated setValue returns each old");
        check(e.getValue() == 3, "SimpleEntry final value");
        AbstractMap.SimpleEntry<String, Integer> copy = new AbstractMap.SimpleEntry<>(e);
        check(copy.equals(e) && copy.hashCode() == e.hashCode(), "SimpleEntry copy constructor equal");
        copy.setValue(9);
        check(!copy.equals(e), "copies independent after setValue");

        AbstractMap.SimpleImmutableEntry<String, Integer> i = new AbstractMap.SimpleImmutableEntry<>("k", 3);
        expectThrows(UnsupportedOperationException.class, () -> i.setValue(1),
                "SimpleImmutableEntry setValue UOE");
        check(i.getValue() == 3, "SimpleImmutableEntry unchanged after failed setValue");
        check(i.equals(e), "immutable equals mutable with same content");

        // null key/value accepted by both wrappers.
        AbstractMap.SimpleEntry<String, Integer> n = new AbstractMap.SimpleEntry<>(null, null);
        check(n.getKey() == null && n.getValue() == null, "SimpleEntry null key/value");
        check(n.setValue(7) == null && n.getValue() == 7, "SimpleEntry null old value returned");
        AbstractMap.SimpleImmutableEntry<String, Integer> ni = new AbstractMap.SimpleImmutableEntry<>(null, null);
        check(ni.getKey() == null && ni.getValue() == null, "SimpleImmutableEntry null key/value");
        check(ni.hashCode() == 0, "all-null immutable entry hash 0");
    }

    static void testEntrySetIterationEntries() {
        Map<String, Integer> m = new java.util.LinkedHashMap<>();
        m.put("x", 1);
        m.put("y", 2);
        List<Map.Entry<String, Integer>> seen = new ArrayList<>();
        for (Map.Entry<String, Integer> e : m.entrySet()) seen.add(new AbstractMap.SimpleEntry<>(e));
        check(seen.size() == 2, "captured every entry");
        check(seen.get(0).equals(new AbstractMap.SimpleImmutableEntry<>("x", 1)),
                "first snapshot equals independent entry");
        check(seen.get(1).equals(new AbstractMap.SimpleImmutableEntry<>("y", 2)),
                "second snapshot equals independent entry");
        // snapshots taken via setValue-before-copy remain live until copied:
        Map.Entry<String, Integer> live = m.entrySet().iterator().next();
        AbstractMap.SimpleEntry<String, Integer> snap = new AbstractMap.SimpleEntry<>(live);
        live.setValue(99);
        check(snap.getValue() == 1, "snapshot detached from live entry");
        check(live.getValue() == 99 && m.get("x") == 99, "live entry still writes through");
    }

    static void testComparingNullHostility() {
        Comparator<Map.Entry<String, Integer>> byVal = Map.Entry.comparingByValue();
        expectThrows(NullPointerException.class,
                () -> byVal.compare(new AbstractMap.SimpleEntry<>("a", null),
                        new AbstractMap.SimpleEntry<>("b", 1)),
                "comparingByValue natural null value NPE");
        // with a null-tolerant comparator no exception
        Comparator<Map.Entry<String, Integer>> nullsLast =
                Map.Entry.comparingByValue(Comparator.nullsLast(Comparator.naturalOrder()));
        check(nullsLast.compare(new AbstractMap.SimpleEntry<>("a", null),
                new AbstractMap.SimpleEntry<>("b", 1)) > 0, "nullsLast orders null after 1");
        Comparator<Map.Entry<String, Integer>> nullKey =
                Map.Entry.comparingByKey(Comparator.nullsFirst(Comparator.naturalOrder()));
        check(nullKey.compare(new AbstractMap.SimpleEntry<>(null, 1),
                new AbstractMap.SimpleEntry<>("a", 1)) < 0, "nullsFirst orders null key before 'a'");
        // sorting a mixed list is stable for equal values
        List<Map.Entry<String, Integer>> l = new ArrayList<>();
        l.add(new AbstractMap.SimpleEntry<>("b", 2));
        l.add(new AbstractMap.SimpleEntry<>("a", 1));
        l.add(new AbstractMap.SimpleEntry<>("c", 2));
        l.sort(Map.Entry.comparingByValue());
        check(l.get(0).getKey().equals("a"), "sort by value min first");
        check(l.get(1).getKey().equals("b") && l.get(2).getKey().equals("c"),
                "sort by value stable for equal values");
    }

    public static void main(String[] args) {
        testAccessors();
        testEqualsHashCode();
        testComparingByKey();
        testComparingByValue();
        testLiveEntryContract();
        testSimpleEntryFamily();
        testEntrySetIterationEntries();
        testComparingNullHostility();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
