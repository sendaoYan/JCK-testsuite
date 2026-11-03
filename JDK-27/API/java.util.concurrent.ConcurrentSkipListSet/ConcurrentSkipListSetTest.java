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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSortedAddAndNavigation();
        testPollFirstLast();
        testComparator();
        testDescending();
        testSubSets();
        testNullRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
