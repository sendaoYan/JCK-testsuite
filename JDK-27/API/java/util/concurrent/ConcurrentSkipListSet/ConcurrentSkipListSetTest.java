/*
 * @test
 * @summary java.util.concurrent.ConcurrentSkipListSet conformance: white-box + black-box
 *          (sorted set add/first/last, duplicate rejection, ceiling/floor/higher/lower,
 *          pollFirst/pollLast, comparator, descending views, subSet/headSet/tailSet,
 *          null rejection).
 * @run main ConcurrentSkipListSetTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class ConcurrentSkipListSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.ConcurrentSkipListSet");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractSet.class, "extends AbstractSet");
        check(NavigableSet.class.isAssignableFrom(c), "implements NavigableSet");
        check(Cloneable.class.isAssignableFrom(c) && Serializable.class.isAssignableFrom(c),
              "implements Cloneable/Serializable");
        check(c.getConstructor() != null && c.getConstructor(Comparator.class) != null
              && c.getConstructor(Collection.class) != null, "constructors ()/(Comparator)/(Collection)");
        check(hasMethod(c, "first") && hasMethod(c, "last"), "first/last");
        check(hasMethod(c, "ceiling", Object.class) && hasMethod(c, "floor", Object.class)
              && hasMethod(c, "higher", Object.class) && hasMethod(c, "lower", Object.class),
              "ceiling/floor/higher/lower");
        check(hasMethod(c, "pollFirst") && hasMethod(c, "pollLast"), "pollFirst/pollLast");
        check(hasMethod(c, "descendingIterator") && hasMethod(c, "descendingSet"),
              "descendingIterator/descendingSet");
        check(hasMethod(c, "subSet", Object.class, boolean.class, Object.class, boolean.class),
              "subSet(E,boolean,E,boolean)");
        check(hasMethod(c, "comparator"), "comparator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testSortedAddAndNavigation() {
        ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>();
        check(s.add(3) && s.add(1) && s.add(5) && s.add(2), "add");
        check(!s.add(2), "duplicate rejected");
        check(s.size() == 4, "size");
        check(s.first() == 1 && s.last() == 5, "first/last");
        check(s.contains(2) && !s.contains(9), "contains");
        check(s.ceiling(2) == 2 && s.ceiling(4) == 5, "ceiling");
        check(s.floor(4) == 3 && s.floor(2) == 2, "floor");
        check(s.higher(2) == 3 && s.lower(2) == 1, "higher/lower");
    }

    static void testPollFirstLast() {
        ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>();
        s.add(1);
        s.add(2);
        s.add(3);
        check(s.pollFirst() == 1, "pollFirst");
        check(s.pollLast() == 3, "pollLast");
        check(s.first() == 2 && s.size() == 1, "remaining");
        check(s.pollFirst() == 2 && s.isEmpty(), "empty after all polls");
    }

    static void testComparator() {
        ConcurrentSkipListSet<Integer> natural = new ConcurrentSkipListSet<>();
        check(natural.comparator() == null, "natural comparator null");
        ConcurrentSkipListSet<Integer> rev = new ConcurrentSkipListSet<>(Comparator.reverseOrder());
        rev.add(1);
        rev.add(2);
        check(rev.first() == 2 && rev.last() == 1, "reverse order first/last");
    }

    static void testDescending() {
        ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>();
        s.add(1);
        s.add(2);
        s.add(3);
        Iterator<Integer> it = s.descendingIterator();
        check(it.next() == 3 && it.next() == 2 && it.next() == 1, "descendingIterator order");
        NavigableSet<Integer> ds = s.descendingSet();
        check(ds.first() == 3 && ds.last() == 1, "descendingSet first/last");
    }

    static void testSubSets() {
        ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>();
        for (int i = 1; i <= 10; i++) s.add(i);
        check(s.subSet(3, true, 7, false).size() == 4, "subSet inclusive/exclusive");
        check(s.headSet(4).size() == 3 && s.headSet(4).last() == 3, "headSet exclusive");
        check(s.tailSet(8).size() == 3 && s.tailSet(8).first() == 8, "tailSet inclusive");
    }

    static void testNullRejection() {
        ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>();
        expectThrows(() -> s.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> s.contains(null), NullPointerException.class, "contains(null)");
        expectThrows(() -> s.remove(null), NullPointerException.class, "remove(null)");
    }

    static void testBlackBox() {
        // constructors (Collection)/(SortedSet)
        ConcurrentSkipListSet<Integer> s1 = new ConcurrentSkipListSet<>(java.util.Arrays.asList(3, 1, 2));
        check(s1.size() == 3 && s1.first() == 1 && s1.last() == 3, "constructor(Collection) sorts");
        java.util.TreeSet<Integer> ts = new java.util.TreeSet<>(java.util.Arrays.asList(5, 2, 9));
        ConcurrentSkipListSet<Integer> s2 = new ConcurrentSkipListSet<>(ts);
        check(s2.first() == 2 && s2.last() == 9, "constructor(SortedSet) keeps order");

        // addFirst / addLast (SequencedSet) always throw UnsupportedOperationException
        ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>();
        s.add(3);
        s.add(5);
        expectThrows(() -> s.addFirst(1), UnsupportedOperationException.class,
                     "addFirst throws UnsupportedOperationException");
        expectThrows(() -> s.addLast(7), UnsupportedOperationException.class,
                     "addLast throws UnsupportedOperationException");

        // equals
        ConcurrentSkipListSet<Integer> eq = new ConcurrentSkipListSet<>(java.util.Arrays.asList(3, 5));
        check(s.equals(eq) && eq.equals(s), "equals content");
        eq.add(99);
        check(!s.equals(eq), "!equals different content");

        // iterator (sorted order)
        Iterator<Integer> it = s.iterator();
        check(it.next() == 3 && it.next() == 5, "iterator sorted");

        // removeAll
        check(s.removeAll(java.util.Arrays.asList(3)), "removeAll true");
        check(s.size() == 1 && s.contains(5), "removeAll result");

        // spliterator
        ConcurrentSkipListSet<Integer> sps = new ConcurrentSkipListSet<>(java.util.Arrays.asList(3, 1, 2));
        java.util.Spliterator<Integer> sp = sps.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        java.util.ArrayList<Integer> viaSp = new java.util.ArrayList<>();
        sp.forEachRemaining(viaSp::add);
        check(viaSp.equals(java.util.Arrays.asList(1, 2, 3)), "spliterator sorted order");

        // clone() (covariant and Object)
        ConcurrentSkipListSet<Integer> copy = sps.clone();
        check(copy.equals(sps) && copy != sps, "clone() equals but distinct");
        Object oCopy = sps.clone();
        check(oCopy instanceof ConcurrentSkipListSet, "clone() Object return type");

        // clear
        sps.clear();
        check(sps.isEmpty(), "clear empties");
    }

    // ---- appended: contract deepening ----
    static void expectThrowsMsg(Runnable r, Class<? extends Throwable> type, String fragment,
                                String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                         + " but got " + t.getClass().getSimpleName(), t);
            check(t.getMessage() != null && t.getMessage().contains(fragment),
                  msg + " message contains '" + fragment + "' (got: " + t.getMessage() + ")");
            passed++;
        }
    }

    static ConcurrentSkipListSet<Integer> fixedSet() {
        ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>();
        s.add(10);
        s.add(20);
        s.add(30);
        s.add(40);
        return s;
    }

    static void testEmptyState() {
        ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>();
        check(s.isEmpty() && s.size() == 0, "fresh set empty");
        check(s.pollFirst() == null && s.pollLast() == null, "poll empty null");
        expectThrows(() -> s.first(), java.util.NoSuchElementException.class, "first empty NSEE");
        expectThrows(() -> s.last(), java.util.NoSuchElementException.class, "last empty NSEE");
        expectThrows(() -> s.iterator().next(), java.util.NoSuchElementException.class, "empty iterator NSEE");
        check(s.toString().equals("[]"), "empty toString []");
    }

    static void testNavigationMatrix() {
        ConcurrentSkipListSet<Integer> s = fixedSet();
        check(s.ceiling(10) == 10, "ceiling equal");
        check(s.ceiling(11) == 20, "ceiling between");
        check(s.ceiling(41) == null, "ceiling above all null");
        check(s.floor(10) == 10, "floor equal");
        check(s.floor(19) == 10, "floor between");
        check(s.floor(9) == null, "floor below all null");
        check(s.higher(10) == 20, "higher equal-key");
        check(s.higher(40) == null, "higher last null");
        check(s.lower(10) == null, "lower first null");
        check(s.lower(25) == 20, "lower between");
        check(s.pollLast() == 40 && s.size() == 3, "pollLast removes last");
    }

    static void testSubSetsFull() {
        ConcurrentSkipListSet<Integer> s = fixedSet();
        check(s.subSet(10, true, 40, true).size() == 4, "subSet inclusive both");
        check(s.subSet(10, false, 40, false).size() == 2, "subSet exclusive both");
        check(s.headSet(30).size() == 2, "headSet exclusive");
        check(s.headSet(30, true).size() == 3, "headSet inclusive");
        check(s.tailSet(30).size() == 2, "tailSet inclusive");
        check(s.tailSet(30, false).size() == 1, "tailSet exclusive");
        // views are live: parent adds appear
        NavigableSet<Integer> head = s.headSet(30);
        s.add(5);
        check(head.contains(5) && head.size() == 3, "headSet live view sees parent add");
        s.remove(5);
        // view writes go through
        NavigableSet<Integer> tail = s.tailSet(30);
        tail.add(35);
        check(s.contains(35), "tailSet add writes through");
        tail.remove(35);
        check(!s.contains(35), "tailSet remove writes through");
        // out-of-range adds are rejected with "key out of range"
        expectThrowsMsg(() -> s.headSet(20).add(25), IllegalArgumentException.class,
                        "key out of range", "headSet add above bound");
        expectThrowsMsg(() -> s.tailSet(30).add(25), IllegalArgumentException.class,
                        "key out of range", "tailSet add below bound");
        expectThrowsMsg(() -> s.subSet(20, true, 30, true).add(35), IllegalArgumentException.class,
                        "key out of range", "subSet add above high bound");
        expectThrowsMsg(() -> s.subSet(20, true, 30, true).add(15), IllegalArgumentException.class,
                        "key out of range", "subSet add below low bound");
        check(s.size() == 4, "set intact after rejected out-of-range adds");
    }

    static void testDescendingWriteThrough() {
        ConcurrentSkipListSet<Integer> s = fixedSet();
        NavigableSet<Integer> d = s.descendingSet();
        d.remove(20);
        check(!s.contains(20), "descendingSet remove writes through");
        s.add(20);
        java.util.ArrayList<Integer> order = new java.util.ArrayList<>();
        for (int x : d) order.add(x);
        check(order.equals(java.util.Arrays.asList(40, 30, 20, 10)), "descendingSet iteration reversed");
        check(d.descendingSet().first() == 10, "descendingSet twice restores order");
    }

    static void testIteratorContract() {
        ConcurrentSkipListSet<Integer> s = fixedSet();
        Iterator<Integer> it = s.iterator();
        expectThrows(() -> it.remove(), IllegalStateException.class,
                     "iterator.remove before next throws IllegalStateException");
        check(it.next() == 10, "iterator ascending");
        it.remove();
        check(s.size() == 3 && !s.contains(10), "iterator.remove writes through");
        expectThrows(() -> it.remove(), IllegalStateException.class,
                     "second consecutive iterator.remove throws IllegalStateException");
        // weak consistency: structural change mid-iteration does not throw CME
        ConcurrentSkipListSet<Integer> w = fixedSet();
        Iterator<Integer> wit = w.iterator();
        w.add(99);
        int n = 0;
        while (wit.hasNext()) { wit.next(); n++; }
        check(n >= 4, "iterator completes after concurrent add (no CME)");
        java.util.ArrayList<Integer> full = new java.util.ArrayList<>();
        for (int x : fixedSet()) full.add(x);
        check(full.equals(java.util.Arrays.asList(10, 20, 30, 40)), "iteration ascending full order");
        check(fixedSet().toString().equals("[10, 20, 30, 40]"), "toString ascending");
    }

    static void testNullFullRejection() {
        ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>();
        expectThrows(() -> s.ceiling(null), NullPointerException.class, "ceiling(null)");
        expectThrows(() -> s.floor(null), NullPointerException.class, "floor(null)");
        expectThrows(() -> s.higher(null), NullPointerException.class, "higher(null)");
        expectThrows(() -> s.lower(null), NullPointerException.class, "lower(null)");
        expectThrows(() -> s.addAll(null), NullPointerException.class, "addAll(null)");
        expectThrows(() -> new ConcurrentSkipListSet<Integer>((java.util.Collection<Integer>) null),
                     NullPointerException.class, "ctor(null Collection)");
        expectThrows(() -> s.addAll(java.util.Arrays.asList(1, null)), NullPointerException.class,
                     "addAll(Collection with null)");
    }

    static void testConstructorsAndAddAll() {
        ConcurrentSkipListSet<Integer> fromColl = new ConcurrentSkipListSet<>(java.util.Arrays.asList(5, 1, 3, 1));
        check(fromColl.size() == 3 && fromColl.first() == 1 && fromColl.last() == 5,
              "ctor(Collection) sorts and dedups");
        java.util.TreeSet<Integer> ts = new java.util.TreeSet<>(Comparator.reverseOrder());
        ts.add(2);
        ts.add(9);
        ts.add(5);
        ConcurrentSkipListSet<Integer> fromSorted = new ConcurrentSkipListSet<>(ts);
        check(fromSorted.first() == 9 && fromSorted.comparator() != null,
              "ctor(SortedSet) keeps comparator and order");
        ConcurrentSkipListSet<Integer> ad = new ConcurrentSkipListSet<>(java.util.Arrays.asList(1, 2));
        check(ad.addAll(java.util.Arrays.asList(2, 3, 3)), "addAll true when at least one new");
        check(ad.size() == 3, "addAll dedups");
        check(!ad.addAll(java.util.Arrays.asList(1, 2, 3)), "addAll false when nothing new");
    }

    static void testBulkAndEqualsTreeSet() {
        ConcurrentSkipListSet<Integer> a = fixedSet();
        java.util.TreeSet<Integer> t = new java.util.TreeSet<>(java.util.Arrays.asList(40, 30, 20, 10));
        check(a.equals(t) && t.equals(a), "cross-impl equals with TreeSet");
        check(a.hashCode() == t.hashCode(), "hashCode matches Set contract");
        t.add(99);
        check(!a.equals(t), "different elements not equal");
        ConcurrentSkipListSet<Integer> b = fixedSet();
        Object[] arr = b.toArray();
        check(java.util.Arrays.asList(arr).equals(java.util.Arrays.asList(10, 20, 30, 40)),
              "toArray ascending order");
        Integer[] bigger = b.toArray(new Integer[6]);
        check(bigger.length == 6 && bigger[4] == null, "toArray(bigger) null-terminated");
        check(b.removeAll(java.util.Arrays.asList(10, 30)) && b.size() == 2, "removeAll");
        check(b.retainAll(java.util.Arrays.asList(20)) && b.size() == 1, "retainAll");
        b.removeIf(x -> x == 20);
        check(b.isEmpty(), "removeIf empties");
    }

    static void testSpliteratorCharacteristics() {
        ConcurrentSkipListSet<Integer> s = fixedSet();
        java.util.Spliterator<Integer> sp = s.spliterator();
        int ch = sp.characteristics();
        check((ch & java.util.Spliterator.ORDERED) != 0, "spliterator ORDERED");
        check((ch & java.util.Spliterator.SORTED) != 0, "spliterator SORTED");
        check((ch & java.util.Spliterator.NONNULL) != 0, "spliterator NONNULL");
        check((ch & java.util.Spliterator.CONCURRENT) != 0, "spliterator CONCURRENT");
        check((ch & java.util.Spliterator.SIZED) == 0, "spliterator not SIZED");
        final int[] first = {-1};
        check(sp.tryAdvance(x -> first[0] = x) && first[0] == 10, "tryAdvance first ascending");
        java.util.ArrayList<Integer> rest = new java.util.ArrayList<>();
        sp.forEachRemaining(rest::add);
        check(rest.equals(java.util.Arrays.asList(20, 30, 40)), "forEachRemaining ascending");
        check(s.stream().count() == 4, "stream count");
    }

    static void testSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ConcurrentSkipListSet.class).getSerialVersionUID()
              == -2479143111061671589L, "serialVersionUID locked");
        ConcurrentSkipListSet<Integer> s = fixedSet();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(s);
        }
        @SuppressWarnings("unchecked")
        ConcurrentSkipListSet<Integer> copy = (ConcurrentSkipListSet<Integer>)
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.equals(s) && copy.first() == 10 && copy.last() == 40, "natural round-trip");
        check(copy.add(50) && copy.size() == 5, "resurrected set mutable");
        // serializable comparator round-trip preserves order and comparator
        ConcurrentSkipListSet<Integer> rev = new ConcurrentSkipListSet<>(Comparator.reverseOrder());
        rev.add(1);
        rev.add(2);
        rev.add(3);
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos2)) {
            oos.writeObject(rev);
        }
        @SuppressWarnings("unchecked")
        ConcurrentSkipListSet<Integer> revCopy = (ConcurrentSkipListSet<Integer>)
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos2.toByteArray())).readObject();
        check(revCopy.first() == 3 && revCopy.last() == 1, "reverse comparator round-trip order");
        check(revCopy.comparator() != null, "comparator survives round-trip");
        check(revCopy.equals(new java.util.TreeSet<>(java.util.Arrays.asList(1, 2, 3))),
              "reverse comparator round-trip content");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSortedAddAndNavigation();
        testPollFirstLast();
        testComparator();
        testDescending();
        testSubSets();
        testNullRejection();
        testBlackBox();
        testEmptyState();
        testNavigationMatrix();
        testSubSetsFull();
        testDescendingWriteThrough();
        testIteratorContract();
        testNullFullRejection();
        testConstructorsAndAddAll();
        testBulkAndEqualsTreeSet();
        testSpliteratorCharacteristics();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
