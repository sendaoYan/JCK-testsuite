/*
 * @test
 * @summary java.util.concurrent.ConcurrentNavigableMap conformance: white-box
 *          structure + black-box behavior. Contract level: subMap/headMap/tailMap
 *          (incl. 4-arg boolean inclusive boundaries) as LIVE bidirectionally linked
 *          views driven via ConcurrentSkipListMap (view put -> main map, main map put
 *          -> view, out-of-range view put -> IllegalArgumentException, reversed range
 *          -> IllegalArgumentException), descendingMap reverse order + linkage +
 *          involution, navigableKeySet/keySet/descendingKeySet views, endpoint ops
 *          (firstKey/lastKey/firstEntry/lastEntry/pollFirstEntry/pollLastEntry),
 *          view equals/hashCode per Map contract, interface hierarchy + generics.
 * @run main ConcurrentNavigableMapTest
 */

import java.util.*;
import java.util.concurrent.*;

public class ConcurrentNavigableMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    static void expectMsg(Runnable r, Class<? extends Throwable> type, String expected, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                         + " but got " + t.getClass().getSimpleName(), t);
            if (expected != null && !expected.equals(t.getMessage()))
                throw new AssertionError(msg + ": expected message <" + expected
                                         + "> but got <" + t.getMessage() + ">", t);
            passed++;
        }
    }

    static ConcurrentSkipListMap<Integer, String> fresh() {
        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        m.put(10, "a");
        m.put(20, "b");
        m.put(30, "c");
        return m;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ConcurrentNavigableMap");
        check(c.isInterface(), "ConcurrentNavigableMap is an interface");
        check(ConcurrentMap.class.isAssignableFrom(c), "extends ConcurrentMap");
        check(NavigableMap.class.isAssignableFrom(c), "extends NavigableMap");
        hasMethod(c, "subMap", Object.class, boolean.class, Object.class, boolean.class);
        hasMethod(c, "headMap", Object.class, boolean.class);
        hasMethod(c, "tailMap", Object.class, boolean.class);
        hasMethod(c, "descendingMap");
        hasMethod(c, "navigableKeySet");
        hasMethod(c, "descendingKeySet");

        // deepened structural contract
        check(Map.class.isAssignableFrom(c), "is a Map");
        Class<?>[] supers = c.getInterfaces();
        check(supers.length == 2, "exactly two direct superinterfaces");
        List<Class<?>> sl = Arrays.asList(supers);
        check(sl.contains(ConcurrentMap.class) && sl.contains(NavigableMap.class),
              "direct superinterfaces are ConcurrentMap and NavigableMap");
        // the returning-view signatures are narrowed to ConcurrentNavigableMap
        check(c.getMethod("subMap", Object.class, boolean.class, Object.class, boolean.class)
                .getReturnType() == ConcurrentNavigableMap.class, "subMap(4-arg) returns ConcurrentNavigableMap");
        check(c.getMethod("headMap", Object.class, boolean.class)
                .getReturnType() == ConcurrentNavigableMap.class, "headMap(k,b) returns ConcurrentNavigableMap");
        check(c.getMethod("tailMap", Object.class, boolean.class)
                .getReturnType() == ConcurrentNavigableMap.class, "tailMap(k,b) returns ConcurrentNavigableMap");
        check(c.getMethod("subMap", Object.class, Object.class)
                .getReturnType() == ConcurrentNavigableMap.class, "subMap(2-arg) returns ConcurrentNavigableMap");
        check(c.getMethod("headMap", Object.class)
                .getReturnType() == ConcurrentNavigableMap.class, "headMap(k) returns ConcurrentNavigableMap");
        check(c.getMethod("tailMap", Object.class)
                .getReturnType() == ConcurrentNavigableMap.class, "tailMap(k) returns ConcurrentNavigableMap");
        check(c.getMethod("descendingMap").getReturnType() == ConcurrentNavigableMap.class,
              "descendingMap returns ConcurrentNavigableMap");
        check(c.getMethod("navigableKeySet").getReturnType() == NavigableSet.class,
              "navigableKeySet returns NavigableSet");
        check(c.getMethod("keySet").getReturnType() == NavigableSet.class,
              "keySet returns NavigableSet");
        check(c.getMethod("descendingKeySet").getReturnType() == NavigableSet.class,
              "descendingKeySet returns NavigableSet");
        check(c.getTypeParameters().length == 2, "two type parameters K,V");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ConcurrentNavigableMap<Integer, String> m = new ConcurrentSkipListMap<>();
        m.put(1, "a");
        m.put(3, "c");
        m.put(5, "e");
        check("a".equals(m.firstEntry().getValue()), "firstEntry");
        check("e".equals(m.lastEntry().getValue()), "lastEntry");
        check(m.ceilingKey(2) == 3, "ceilingKey");
        check(m.floorKey(4) == 3, "floorKey");
        check(m.subMap(1, 5).size() == 2, "subMap(1,5) size 2");
        check(m.headMap(3).size() == 1, "headMap(3) size 1");
        check(m.tailMap(3).size() == 2, "tailMap(3) size 2");
        check(m.descendingMap().firstKey() == 5, "descendingMap firstKey is max");
        NavigableSet<Integer> ks = m.navigableKeySet();
        check(ks.first() == 1 && ks.last() == 5, "navigableKeySet ordered");
        check(m.descendingKeySet().first() == 5, "descendingKeySet");
    }

    // ---- Part 3: 4-arg boolean boundary matrix ----
    static void testSubMapBoundaries() {
        ConcurrentSkipListMap<Integer, String> m = fresh();
        check(m.subMap(10, true, 20, true).size() == 2, "subMap(lo,T,hi,T) includes both endpoints");
        check(m.subMap(10, false, 20, false).size() == 0, "subMap(lo,F,hi,F) excludes both endpoints");
        check(m.subMap(10, false, 20, true).size() == 1, "subMap(lo,F,hi,T) excludes lo includes hi");
        check(m.subMap(10, true, 20, false).size() == 1, "subMap(lo,T,hi,F) includes lo excludes hi");
        check(m.subMap(10, true, 20, true).firstKey() == 10, "subMap inclusive firstKey is lo");
        check(m.subMap(10, true, 20, true).lastKey() == 20, "subMap inclusive lastKey is hi");
        check(m.subMap(10, false, 20, true).firstKey() == 20, "subMap lo-exclusive firstKey skips lo");
        check(m.subMap(10, true, 20, false).lastKey() == 10, "subMap hi-exclusive lastKey skips hi");

        // 2-arg forms: lo inclusive, hi exclusive
        check(m.subMap(10, 20).size() == 1, "subMap(lo,hi) is [lo,hi)");
        check(m.subMap(10, 21).size() == 2, "subMap reaching hi via epsilon");
        check(m.headMap(20).size() == 1, "headMap(hi) is [min,hi)");
        check(m.headMap(20, true).size() == 2, "headMap(hi,T) is [min,hi]");
        check(m.headMap(5).size() == 0, "headMap below all keys is empty");
        check(m.headMap(50).size() == 3, "headMap above all keys is whole map");
        check(m.tailMap(20).size() == 2, "tailMap(lo) is [lo,max]");
        check(m.tailMap(20, true).size() == 2, "tailMap(lo,T) is [lo,max]");
        check(m.tailMap(20, false).size() == 1, "tailMap(lo,F) is (lo,max]");
        check(m.tailMap(25).size() == 1, "tailMap between keys skips lower");
        check(m.tailMap(5).size() == 3, "tailMap below all keys is whole map");

        // empty-range views are valid but empty
        check(m.subMap(21, 25).isEmpty(), "subMap of empty interior range");
        check(m.headMap(10).isEmpty(), "headMap ending at first key (exclusive) is empty");
        // views over empty map
        ConcurrentNavigableMap<Integer, String> em = new ConcurrentSkipListMap<>();
        check(em.subMap(1, true, 9, true).isEmpty(), "subMap of empty map");
        check(em.headMap(5).isEmpty() && em.tailMap(5).isEmpty(), "head/tailMap of empty map");
        check(em.descendingMap().isEmpty(), "descendingMap of empty map");
    }

    // ---- Part 4: view <-> main bidirectional linkage ----
    static void testViewWriteLinkage() {
        ConcurrentSkipListMap<Integer, String> m = fresh();
        ConcurrentNavigableMap<Integer, String> sub = m.subMap(10, true, 20, true);
        // put through the view is visible in the main map
        sub.put(15, "mid");
        check(m.containsKey(15) && m.get(15).equals("mid"), "view put reflected in main map");
        check(m.size() == 4, "main map size grew by view put");
        // put into the main map inside the range is visible in the view
        m.put(12, "x");
        check(sub.containsKey(12) && sub.get(12).equals("x"), "main put inside range visible in view");
        check(sub.size() == 4, "view size counts later in-range insert");
        // put into the main map outside the range is NOT visible in the view
        m.put(35, "out");
        check(!sub.containsKey(35), "main put outside range invisible in view");
        check(sub.size() == 4, "view size unchanged by out-of-range insert");
        // removal links both directions
        m.remove(10);
        check(!sub.containsKey(10), "main remove visible in view");
        sub.remove(15);
        check(!m.containsKey(15), "view remove reflected in main map");
        check(m.containsKey(12) && m.containsKey(20) && m.containsKey(30) && m.containsKey(35),
              "survivors intact after linked removals");
        // get through the view reads current main-map state
        check("b".equals(sub.get(20)), "view get reads live mapping");
        check(sub.get(19) == null, "view get of absent key");
        // clear through the view clears only the in-range portion
        ConcurrentSkipListMap<Integer, String> m2 = fresh();
        ConcurrentNavigableMap<Integer, String> sub2 = m2.subMap(10, true, 20, true);
        sub2.clear();
        check(sub2.isEmpty(), "view cleared itself");
        check(m2.size() == 1 && m2.containsKey(30), "view clear left out-of-range mappings");
    }

    // ---- Part 5: range enforcement on view writes ----
    static void testViewRangeEnforcement() {
        final ConcurrentSkipListMap<Integer, String> m = fresh();
        final ConcurrentNavigableMap<Integer, String> sub = m.subMap(10, true, 20, true);
        expectMsg(() -> sub.put(5, "below"),
                  IllegalArgumentException.class, "key out of range", "subMap put below range");
        expectMsg(() -> sub.put(25, "above"),
                  IllegalArgumentException.class, "key out of range", "subMap put above range");
        check(!m.containsKey(5) && !m.containsKey(25), "rejected puts left no mapping");
        check(m.size() == 3, "map unchanged after rejected view puts");

        final ConcurrentNavigableMap<Integer, String> head = m.headMap(20, true);
        expectMsg(() -> head.put(25, "x"),
                  IllegalArgumentException.class, "key out of range", "headMap put above bound");
        final ConcurrentNavigableMap<Integer, String> tail = m.tailMap(10, true);
        expectMsg(() -> tail.put(5, "x"),
                  IllegalArgumentException.class, "key out of range", "tailMap put below bound");

        // endpoints exactly on the boundary follow the inclusive flag
        sub.put(10, "lo");   // inclusive lo: allowed
        sub.put(20, "hi");   // inclusive hi: allowed
        check(m.get(10).equals("lo") && m.get(20).equals("hi"), "boundary puts allowed in inclusive view");
        final ConcurrentNavigableMap<Integer, String> subEx = m.subMap(10, false, 20, false);
        expectMsg(() -> subEx.put(10, "x"),
                  IllegalArgumentException.class, "key out of range", "exclusive-lo view rejects lo");
        expectMsg(() -> subEx.put(20, "x"),
                  IllegalArgumentException.class, "key out of range", "exclusive-hi view rejects hi");

        // reversed range is rejected up front (lo > hi)
        expectMsg(() -> m.subMap(30, true, 10, true),
                  IllegalArgumentException.class, "inconsistent range", "subMap with lo>hi rejected");
        // null key in view put -> NPE (SkipListMap keys never null)
        expectMsg(() -> sub.put(null, "x"),
                  NullPointerException.class, null, "view put null key");
    }

    // ---- Part 6: descendingMap ----
    static void testDescendingMap() {
        ConcurrentSkipListMap<Integer, String> m = fresh();
        ConcurrentNavigableMap<Integer, String> desc = m.descendingMap();
        List<Integer> dkeys = new ArrayList<>(desc.keySet());
        check(dkeys.equals(Arrays.asList(30, 20, 10)), "descendingMap iterates keys in reverse order");
        check(desc.firstKey() == 30, "descendingMap firstKey is the max key");
        check(desc.lastKey() == 10, "descendingMap lastKey is the min key");
        check("c".equals(desc.firstEntry().getValue()) && "a".equals(desc.lastEntry().getValue()),
              "descendingMap firstEntry/lastEntry swap");
        // linkage: put through descending view lands in the main map
        desc.put(25, "dd");
        check(m.containsKey(25) && m.get(25).equals("dd"), "descending put reflected in main map");
        check(desc.containsKey(25), "descending put visible in its own view");
        // main map put visible in descending view
        m.put(5, "low");
        check(desc.containsKey(5) && desc.lastKey() == 5 && desc.firstKey() == 30,
              "main put visible in descending view, endpoints stay max/min");
        // navigation inside the descending view is reversed
        check(desc.ceilingKey(21) == 20, "descending ceilingKey searches downward");
        check(desc.floorKey(21) == 25, "descending floorKey searches upward-reversed");
        // involution: descending twice restores ascending order
        List<Integer> dd = new ArrayList<>(desc.descendingMap().keySet());
        check(dd.equals(Arrays.asList(5, 10, 20, 25, 30)), "descendingMap twice restores ascending order");
        // equals/hashCode follow the Map contract regardless of direction
        check(desc.equals(m) && m.equals(desc), "descendingMap equals main map both ways");
        check(desc.hashCode() == m.hashCode(), "descendingMap hashCode equals main map");
        // descending sub-range view is also live and reversed
        ConcurrentNavigableMap<Integer, String> dsub = desc.subMap(25, true, 10, true);
        check(dsub.firstKey() == 25 && dsub.lastKey() == 10, "descending subMap endpoints reversed");
    }

    // ---- Part 7: key set views ----
    static void testKeySetViews() {
        ConcurrentSkipListMap<Integer, String> m = fresh();
        NavigableSet<Integer> ks = m.navigableKeySet();
        check(ks instanceof NavigableSet, "navigableKeySet is a NavigableSet");
        check(!(ks instanceof Map), "keySet is not a Map");
        List<Integer> order = new ArrayList<>(ks);
        check(order.equals(Arrays.asList(10, 20, 30)), "navigableKeySet ascending order");
        check(ks.first() == 10 && ks.last() == 30, "navigableKeySet first/last");
        // iterator removal writes through to the backing map
        Iterator<Integer> it = ks.iterator();
        check(it.next() == 10, "iterator first element");
        it.remove();
        check(!m.containsKey(10) && m.size() == 2, "iterator.remove removed the mapping");
        // keySet() is the same live navigable view
        NavigableSet<Integer> ks2 = m.keySet();
        check(ks2 instanceof NavigableSet, "keySet() returns NavigableSet");
        check(ks2.size() == 2 && ks2.contains(20), "keySet live content");
        // removal through keySet view removes the mapping
        check(ks2.remove(20), "keySet.remove returns true for present key");
        check(!m.containsKey(20), "keySet.remove removed the mapping");
        check(!ks2.remove(99), "keySet.remove returns false for absent key");
        // descendingKeySet reverses iteration
        m.put(10, "a");
        List<Integer> dks = new ArrayList<>(m.descendingKeySet());
        check(dks.equals(Arrays.asList(30, 10)), "descendingKeySet reverse order");
        // adding keys through the key view is unsupported
        try {
            ks2.add(99);
            check(false, "keySet.add must be unsupported");
        } catch (UnsupportedOperationException e) {
            check(true, "keySet.add throws UnsupportedOperationException");
        }
        check(!m.containsKey(99), "failed keySet.add left no mapping");
    }

    // ---- Part 8: endpoint operations ----
    static void testEndpoints() {
        ConcurrentSkipListMap<Integer, String> m = fresh();
        check(m.firstKey() == 10 && m.lastKey() == 30, "firstKey/lastKey");
        Map.Entry<Integer, String> fe = m.firstEntry();
        Map.Entry<Integer, String> le = m.lastEntry();
        check(fe.getKey() == 10 && "a".equals(fe.getValue()), "firstEntry key/value");
        check(le.getKey() == 30 && "c".equals(le.getValue()), "lastEntry key/value");
        check(m.size() == 3, "firstEntry/lastEntry do not remove");

        Map.Entry<Integer, String> pf = m.pollFirstEntry();
        check(pf.getKey() == 10 && "a".equals(pf.getValue()), "pollFirstEntry returns first entry");
        check(!m.containsKey(10) && m.size() == 2, "pollFirstEntry removed the mapping");
        Map.Entry<Integer, String> pl = m.pollLastEntry();
        check(pl.getKey() == 30 && "c".equals(pl.getValue()), "pollLastEntry returns last entry");
        check(!m.containsKey(30) && m.size() == 1, "pollLastEntry removed the mapping");
        // entries handed out are immutable snapshots
        try {
            pf.setValue("Z");
            check(false, "entry.setValue must be unsupported");
        } catch (UnsupportedOperationException e) {
            check(true, "pollFirstEntry entry is immutable");
        }

        // empty-map endpoint behaviors
        ConcurrentSkipListMap<Integer, String> em = new ConcurrentSkipListMap<>();
        check(em.firstEntry() == null && em.lastEntry() == null, "firstEntry/lastEntry null on empty");
        check(em.pollFirstEntry() == null && em.pollLastEntry() == null, "poll entries null on empty");
        try {
            em.firstKey();
            check(false, "firstKey must throw on empty map");
        } catch (NoSuchElementException e) {
            check(true, "firstKey throws NoSuchElementException on empty");
        }
        try {
            em.lastKey();
            check(false, "lastKey must throw on empty map");
        } catch (NoSuchElementException e) {
            check(true, "lastKey throws NoSuchElementException on empty");
        }
    }

    // ---- Part 9: view equals/hashCode per Map contract ----
    static void testViewEqualsHashCode() {
        ConcurrentSkipListMap<Integer, String> m = fresh();
        ConcurrentNavigableMap<Integer, String> sub = m.subMap(10, true, 20, true);
        HashMap<Integer, String> hm = new HashMap<>();
        hm.put(10, "a");
        hm.put(20, "b");
        check(sub.equals(hm) && hm.equals(sub), "subMap view equals plain Map both ways");
        check(sub.hashCode() == hm.hashCode(), "subMap view hashCode equals plain Map");
        // a view differing by one mapping is unequal
        hm.put(30, "c");
        check(!sub.equals(hm) && !hm.equals(sub), "subMap view unequal once content diverges");
        // descending view equality ignores direction
        check(m.descendingMap().equals(m) && m.equals(m.descendingMap()),
              "descendingMap equal in both directions");
        check(m.descendingMap().hashCode() == m.hashCode(), "descendingMap hashCode equal");
        // equality against another ordered implementation
        TreeMap<Integer, String> tm = new TreeMap<>(m);
        check(m.equals(tm) && tm.equals(m), "ConcurrentSkipListMap equals TreeMap with same mappings");
        check(m.hashCode() == tm.hashCode(), "hashCode matches TreeMap");
        // unequal values make maps unequal even with equal key sets
        m.put(20, "changed");
        check(!m.equals(tm), "value change breaks equality");
    }

    // ---- Part 10: hierarchy and generics ----
    static void testHierarchyAndGenerics() {
        ConcurrentSkipListMap<Integer, String> m = fresh();
        check(m instanceof ConcurrentNavigableMap, "ConcurrentSkipListMap implements ConcurrentNavigableMap");
        check(m instanceof ConcurrentMap, "ConcurrentSkipListMap implements ConcurrentMap");
        check(m instanceof NavigableMap, "ConcurrentSkipListMap implements NavigableMap");
        check(m instanceof SortedMap, "ConcurrentSkipListMap implements SortedMap");
        check(m instanceof Map, "ConcurrentSkipListMap implements Map");
        check(ConcurrentNavigableMap.class.isAssignableFrom(ConcurrentSkipListMap.class),
              "ConcurrentNavigableMap.isAssignableFrom(ConcurrentSkipListMap)");
        check(ConcurrentMap.class.isAssignableFrom(ConcurrentNavigableMap.class),
              "ConcurrentNavigableMap is a ConcurrentMap");
        // views share the concrete map's interface type (javadoc: submaps return ConcurrentNavigableMap)
        check(m.subMap(10, 30) instanceof ConcurrentNavigableMap, "subMap view implements ConcurrentNavigableMap");
        check(m.headMap(20) instanceof ConcurrentNavigableMap, "headMap view implements ConcurrentNavigableMap");
        check(m.tailMap(20) instanceof ConcurrentNavigableMap, "tailMap view implements ConcurrentNavigableMap");
        check(m.descendingMap() instanceof ConcurrentNavigableMap, "descendingMap implements ConcurrentNavigableMap");
        // usable through every supertype reference
        ConcurrentNavigableMap<Integer, String> cnm = m;
        NavigableMap<Integer, String> asNav = cnm;
        SortedMap<Integer, String> asSorted = asNav;
        Map<Integer, String> asMap = asSorted;
        check(asNav.firstKey() == 10, "firstKey through NavigableMap reference");
        check(asSorted.firstKey() == 10, "firstKey through SortedMap reference");
        check(asMap.size() == 3 && asMap.get(20).equals("b"), "size/get through Map reference");
        Map<?, ?> wildcard = cnm;
        check(Integer.valueOf(10).equals(wildcard.keySet().iterator().next()), "wildcard iteration ascending");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSubMapBoundaries();
        testViewWriteLinkage();
        testViewRangeEnforcement();
        testDescendingMap();
        testKeySetViews();
        testEndpoints();
        testViewEqualsHashCode();
        testHierarchyAndGenerics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
