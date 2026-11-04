/*
 * @test
 * @summary java.util.NavigableMap conformance: white-box structure + black-box behavior.
 * @run main NavigableMapTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;

public class NavigableMapTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.NavigableMap");
        check(c.isInterface(), "is an interface");
        check(java.util.SortedMap.class.isAssignableFrom(c), "extends SortedMap");
        for (String m : new String[] {"lowerEntry", "lowerKey", "floorEntry", "floorKey",
                "ceilingEntry", "ceilingKey", "higherEntry", "higherKey"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()),
                    m + " abstract");
        for (String m : new String[] {"firstEntry", "lastEntry", "pollFirstEntry",
                "pollLastEntry", "descendingMap", "navigableKeySet", "descendingKeySet"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("reversed").getModifiers()), "reversed default");
        check(Modifier.isAbstract(c.getDeclaredMethod("lowerEntry", Object.class).getModifiers()),
                "lowerEntry abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. nearest-key queries (lower/floor/ceiling/higher, key and entry forms)
        NavigableMap<Integer, String> m = new TreeMap<>();
        m.put(1, "a");
        m.put(3, "c");
        m.put(5, "e");
        check(m.lowerKey(3) == 1, "lowerKey");
        check(m.floorKey(3) == 3, "floorKey");
        check(m.ceilingKey(3) == 3, "ceilingKey");
        check(m.higherKey(3) == 5, "higherKey");
        check(m.ceilingKey(6) == null && m.lowerKey(1) == null, "out-of-range nulls");
        check("a".equals(m.lowerEntry(3).getValue()), "lowerEntry");
        check("e".equals(m.higherEntry(3).getValue()), "higherEntry");
        check("c".equals(m.floorEntry(3).getValue()), "floorEntry");
        check("c".equals(m.ceilingEntry(3).getValue()), "ceilingEntry");

        // 2. first/last entry + key
        check(m.firstEntry().getKey() == 1 && m.lastEntry().getKey() == 5, "firstEntry/lastEntry");
        check(m.firstKey() == 1 && m.lastKey() == 5, "firstKey/lastKey");

        // 3. pollFirstEntry/pollLastEntry remove and return
        check("a".equals(m.pollFirstEntry().getValue()), "pollFirstEntry");
        check("e".equals(m.pollLastEntry().getValue()), "pollLastEntry");
        check(m.size() == 1 && m.firstKey() == 3, "size after polls");

        // 4. descendingMap / descendingKeySet / navigableKeySet
        NavigableMap<Integer, String> d = new TreeMap<>();
        d.put(1, "a");
        d.put(2, "b");
        d.put(3, "c");
        check(d.descendingMap().firstKey() == 3 && d.descendingMap().lastKey() == 1,
                "descendingMap reverses order");
        check(d.descendingKeySet().first() == 3, "descendingKeySet");
        check(d.navigableKeySet().first() == 1 && d.navigableKeySet().last() == 3,
                "navigableKeySet");

        // 5. range views (inclusive-exclusive boolean variants)
        NavigableMap<Integer, String> r = new TreeMap<>();
        for (int i = 1; i <= 6; i++) r.put(i, "" + i);
        check(r.subMap(2, true, 4, true).keySet().equals(java.util.Set.of(2, 3, 4)),
                "subMap inclusive");
        check(r.headMap(3, false).keySet().equals(java.util.Set.of(1, 2)), "headMap exclusive");
        check(r.tailMap(4, false).keySet().equals(java.util.Set.of(5, 6)), "tailMap exclusive");
        check(r.subMap(2, 4).keySet().equals(java.util.Set.of(2, 3)), "subMap [from,to)");

        // 6. reversed() view
        NavigableMap<Integer, String> rev = r.reversed();
        check(rev.firstKey() == 6 && rev.lastKey() == 1, "reversed view");
    }

    static NavigableMap<Integer, String> keys135() {
        NavigableMap<Integer, String> m = new TreeMap<>();
        m.put(1, "a");
        m.put(3, "c");
        m.put(5, "e");
        return m;
    }

    static void testNearestMatrix() {
        NavigableMap<Integer, String> m = new TreeMap<>();
        m.put(10, "ten");
        m.put(20, "twenty");
        m.put(30, "thirty");
        // matrix over probes {5,10,15,20,25,30,35}: expected nulls/keys
        Integer[][] expect = {
            // probe, lower, floor, ceiling, higher
            {5,  null, null, 10, 10},
            {10, null, 10,  10, 20},
            {15, 10,  10,  20, 20},
            {20, 10,  20,  20, 30},
            {25, 20,  20,  30, 30},
            {30, 20,  30,  30, null},
            {35, 30,  30,  null, null},
        };
        for (Integer[] row : expect) {
            int probe = row[0];
            check(m.lowerKey(probe) == row[1], "lowerKey(" + probe + ")=" + row[1]);
            check(m.floorKey(probe) == row[2], "floorKey(" + probe + ")=" + row[2]);
            check(m.ceilingKey(probe) == row[3], "ceilingKey(" + probe + ")=" + row[3]);
            check(m.higherKey(probe) == row[4], "higherKey(" + probe + ")=" + row[4]);
        }
        // entry forms carry both key and value, or null when the key form is null
        check(m.floorEntry(15).getKey() == 10 && m.floorEntry(15).getValue().equals("ten"),
                "floorEntry(15) is 10");
        check(m.ceilingEntry(15).getKey() == 20, "ceilingEntry(15) is 20");
        check(m.lowerEntry(10) == null, "lowerEntry(10) null");
        check(m.higherEntry(30) == null, "higherEntry(30) null");
        // empty map: all four are null on both forms
        NavigableMap<Integer, String> e = new TreeMap<>();
        check(e.lowerKey(7) == null && e.floorKey(7) == null, "empty lower/floor null");
        check(e.ceilingKey(7) == null && e.higherKey(7) == null, "empty ceiling/higher null");
        check(e.lowerEntry(7) == null && e.higherEntry(7) == null
                && e.floorEntry(7) == null && e.ceilingEntry(7) == null, "empty entry forms null");
    }

    static void testEntrySnapshotsImmutable() {
        NavigableMap<Integer, String> m = keys135();
        expectThrows(UnsupportedOperationException.class, () -> m.firstEntry().setValue("x"),
                "firstEntry snapshot setValue UOE");
        expectThrows(UnsupportedOperationException.class, () -> m.lastEntry().setValue("x"),
                "lastEntry snapshot setValue UOE");
        expectThrows(UnsupportedOperationException.class, () -> m.floorEntry(3).setValue("x"),
                "floorEntry snapshot setValue UOE");
        expectThrows(UnsupportedOperationException.class, () -> m.ceilingEntry(3).setValue("x"),
                "ceilingEntry snapshot setValue UOE");
        expectThrows(UnsupportedOperationException.class, () -> m.lowerEntry(3).setValue("x"),
                "lowerEntry snapshot setValue UOE");
        expectThrows(UnsupportedOperationException.class, () -> m.higherEntry(3).setValue("x"),
                "higherEntry snapshot setValue UOE");
        check(m.size() == 3, "query snapshots do not mutate map");
        // pollFirstEntry/pollLastEntry remove the entry first, then hand out an
        // immutable snapshot: setValue fails UOE on the returned entry either way.
        NavigableMap<Integer, String> p = keys135();
        expectThrows(UnsupportedOperationException.class, () -> p.pollFirstEntry().setValue("x"),
                "pollFirstEntry snapshot setValue UOE");
        check(p.size() == 2 && !p.containsKey(1), "pollFirstEntry removal happened despite UOE");
        expectThrows(UnsupportedOperationException.class, () -> p.pollLastEntry().setValue("x"),
                "pollLastEntry snapshot setValue UOE");
        check(p.size() == 1 && !p.containsKey(5), "pollLastEntry removal happened despite UOE");
    }

    static void testPollSequence() {
        NavigableMap<Integer, String> m = new TreeMap<>();
        for (int i = 1; i <= 4; i++) m.put(i, "v" + i);
        check(m.pollFirstEntry().getKey() == 1, "poll 1");
        check(m.firstKey() == 2, "firstKey advanced after poll");
        check(m.pollLastEntry().getKey() == 4, "poll 4");
        check(m.lastKey() == 3, "lastKey retreated after poll");
        check(m.pollFirstEntry().getKey() == 2 && m.pollFirstEntry().getKey() == 3,
                "drain remaining by polling first");
        check(m.isEmpty() && m.pollFirstEntry() == null && m.pollLastEntry() == null,
                "polls on empty map return null");
        // value replacement via entry snapshot poll order unchanged
        NavigableMap<Integer, String> n = keys135();
        n.put(1, "A2");
        check(n.pollFirstEntry().getValue().equals("A2"), "poll returns current value");
    }

    static void testDescendingMirror() {
        NavigableMap<Integer, String> m = keys135();
        NavigableMap<Integer, String> d = m.descendingMap();
        // under the reversed comparator the four nearest ops mirror
        check(d.floorKey(4) == 5, "desc floorKey(4)=5");
        check(d.ceilingKey(4) == 3, "desc ceilingKey(4)=3");
        check(d.lowerKey(4) == 5, "desc lowerKey(4)=5");
        check(d.higherKey(4) == 3, "desc higherKey(4)=3");
        check(d.floorKey(3) == 3 && d.ceilingKey(3) == 3, "desc floor/ceiling on existing key");
        check(d.lowerKey(5) == null, "desc lowerKey(5) null (max)");
        check(d.higherKey(1) == null, "desc higherKey(1) null (min)");
        check(d.floorEntry(4).getKey() == 5, "desc floorEntry");
        check(d.descendingMap().equals(m), "double reversal equals original");
        // descending sub-ranges use descending bounds
        check(new ArrayList<>(d.subMap(3, true, 1, true).keySet()).equals(Arrays.asList(3, 1)),
                "desc subMap(3,1] both inclusive");
        check(new ArrayList<>(d.subMap(5, false, 1, true).keySet()).equals(Arrays.asList(3, 1)),
                "desc subMap excluding absent fromKey");
        check(new ArrayList<>(d.headMap(3, true).keySet()).equals(Arrays.asList(5, 3)),
                "desc headMap inclusive");
        check(new ArrayList<>(d.headMap(3, false).keySet()).equals(Arrays.asList(5)),
                "desc headMap exclusive");
        check(new ArrayList<>(d.tailMap(3, false).keySet()).equals(Arrays.asList(1)),
                "desc tailMap exclusive");
        check(new ArrayList<>(d.tailMap(3, true).keySet()).equals(Arrays.asList(3, 1)),
                "desc tailMap inclusive");
        // live view: writes flow both ways
        d.put(4, "d");
        check(m.containsKey(4) && m.size() == 4, "descending put reaches original");
        m.remove(4);
        check(d.size() == 3, "original remove reaches descending view");
        check(d.firstKey() == 5 && d.lastKey() == 1, "desc endpoints");
        // empty descending view nearest ops null
        NavigableMap<Integer, String> ed = new TreeMap<Integer, String>().descendingMap();
        check(ed.floorKey(3) == null && ed.ceilingKey(3) == null, "empty desc floor/ceiling null");
        check(ed.pollFirstEntry() == null, "empty desc poll null");
    }

    static void testInclusiveBoundsExact() {
        NavigableMap<Integer, String> m = new TreeMap<>();
        for (int i = 1; i <= 6; i++) m.put(i, "" + i);
        NavigableMap<Integer, String> s24t = m.subMap(2, true, 4, true);
        check(s24t.firstKey() == 2 && s24t.lastKey() == 4 && s24t.size() == 3,
                "subMap [2,4] endpoints and size");
        NavigableMap<Integer, String> s24f = m.subMap(2, false, 4, false);
        check(s24f.firstKey() == 3 && s24f.lastKey() == 3 && s24f.size() == 1,
                "subMap (2,4) endpoints and size");
        NavigableMap<Integer, String> h3t = m.headMap(3, true);
        check(h3t.lastKey() == 3 && h3t.size() == 3, "headMap inclusive ends at 3");
        NavigableMap<Integer, String> h3f = m.headMap(3, false);
        check(h3f.lastKey() == 2 && h3f.size() == 2, "headMap exclusive ends before 3");
        NavigableMap<Integer, String> t3t = m.tailMap(3, true);
        check(t3t.firstKey() == 3 && t3t.size() == 4, "tailMap inclusive starts at 3");
        NavigableMap<Integer, String> t3f = m.tailMap(3, false);
        check(t3f.firstKey() == 4 && t3f.size() == 3, "tailMap exclusive starts after 3");
        // bounds that exist / do not exist in the map behave identically
        check(m.subMap(2, true, 5, true).size() == 4 && m.subMap(2, true, 5, false).size() == 3,
                "inclusive flag with existing toKey");
        check(m.headMap(7, true).size() == 6 && m.headMap(7, false).size() == 6,
                "headMap above all keys");
        check(m.tailMap(0, true).size() == 6 && m.tailMap(0, false).size() == 6,
                "tailMap below all keys");
        // write-through and bound enforcement
        expectThrows(IllegalArgumentException.class, () -> s24t.put(5, "x"),
                "subMap put above toKey IAE");
        expectThrows(IllegalArgumentException.class, () -> s24f.put(2, "x"),
                "subMap put at exclusive fromKey IAE");
        check(m.get(5).equals("5") && m.get(2).equals("2"), "failed bound puts left values intact");
        h3t.put(3, "three"); // inclusive bound accepts the boundary key
        check(h3t.size() == 3 && m.get(3).equals("three"), "put at inclusive bound lands in view");
        t3f.put(4, "four");
        check(t3f.containsKey(4), "tailMap exclusive put inside range ok");
    }

    static void testNavigableKeySetView() {
        NavigableMap<Integer, String> m = keys135();
        NavigableSet<Integer> ks = m.navigableKeySet();
        check(ks.size() == 3 && ks.contains(3), "navigableKeySet content");
        List<Integer> order = new ArrayList<>();
        for (Integer k : ks) order.add(k);
        check(order.equals(Arrays.asList(1, 3, 5)), "navigableKeySet ascending order");
        expectThrows(UnsupportedOperationException.class, () -> ks.add(9),
                "navigableKeySet.add UOE");
        check(ks.remove(3) && !m.containsKey(3), "navigableKeySet.remove reflects map");
        check(ks.pollFirst() == 1 && !m.containsKey(1), "pollFirst removes from map");
        check(ks.pollLast() == 5 && m.isEmpty(), "pollLast empties map");
        // descendingKeySet view
        NavigableMap<Integer, String> n = keys135();
        NavigableSet<Integer> dks = n.descendingKeySet();
        List<Integer> dorder = new ArrayList<>();
        for (Integer k : dks) dorder.add(k);
        check(dorder.equals(Arrays.asList(5, 3, 1)), "descendingKeySet order");
        dks.remove(5);
        check(!n.containsKey(5), "descendingKeySet.remove reflects map");
        Iterator<Integer> dit = n.descendingKeySet().iterator();
        dit.next();
        n.put(7, "seven");
        expectThrows(java.util.ConcurrentModificationException.class, dit::next,
                "descendingKeySet iterator CME after map modification");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNearestMatrix();
        testEntrySnapshotsImmutable();
        testPollSequence();
        testDescendingMirror();
        testInclusiveBoundsExact();
        testNavigableKeySetView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
