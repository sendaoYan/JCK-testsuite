/*
 * @test
 * @summary java.util.concurrent.CopyOnWriteArraySet conformance: white-box + black-box
 *          (set dedup semantics, contains/remove, addAll dedup, iterator snapshot,
 *          equals/removeAll/retainAll).
 * @run main CopyOnWriteArraySetTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public class CopyOnWriteArraySetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.CopyOnWriteArraySet");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractSet.class, "extends AbstractSet");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null && c.getConstructor(Collection.class) != null,
              "constructors ()/(Collection)");
        check(hasMethod(c, "add", Object.class) && hasMethod(c, "remove", Object.class),
              "add/remove");
        check(hasMethod(c, "contains", Object.class) && hasMethod(c, "size"), "contains/size");
        check(hasMethod(c, "addAll", Collection.class) && hasMethod(c, "containsAll", Collection.class),
              "addAll/containsAll");
        check(hasMethod(c, "removeAll", Collection.class) && hasMethod(c, "retainAll", Collection.class),
              "removeAll/retainAll");
        check(hasMethod(c, "iterator"), "iterator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testSetSemantics() {
        CopyOnWriteArraySet<String> s = new CopyOnWriteArraySet<>();
        check(s.isEmpty() && s.size() == 0, "empty initially");
        check(s.add("a"), "add first");
        check(!s.add("a"), "add duplicate returns false");
        check(s.add("b"), "add second");
        check(s.size() == 2, "size dedup");
        check(s.contains("a") && s.contains("b") && !s.contains("z"), "contains");
        check(s.remove("a") && !s.contains("a"), "remove");
        check(s.size() == 1, "size after remove");
    }

    static void testAddAllDedup() {
        CopyOnWriteArraySet<Integer> s = new CopyOnWriteArraySet<>();
        List<Integer> withDups = Arrays.asList(1, 2, 2, 3, 1);
        check(s.addAll(withDups), "addAll returns true");
        check(s.size() == 3, "addAll dedups");
        check(s.containsAll(Arrays.asList(1, 2, 3)), "containsAll");
    }

    static void testIteratorSnapshot() {
        CopyOnWriteArraySet<Integer> s = new CopyOnWriteArraySet<>();
        s.add(1);
        s.add(2);
        s.add(3);
        Iterator<Integer> it = s.iterator();
        s.add(4); // modify after iterator creation
        List<Integer> seen = new ArrayList<>();
        while (it.hasNext()) seen.add(it.next());
        seen.sort(Integer::compareTo);
        check(seen.equals(Arrays.asList(1, 2, 3)), "iterator sees snapshot");
        check(s.size() == 4, "set reflects modification");
    }

    static void testBulkOperationsAndEquals() {
        CopyOnWriteArraySet<Integer> a = new CopyOnWriteArraySet<>(Arrays.asList(1, 2, 3, 4));
        CopyOnWriteArraySet<Integer> b = new CopyOnWriteArraySet<>(Arrays.asList(1, 2, 3, 4));
        check(a.equals(b), "equals");
        check(a.removeAll(Arrays.asList(1, 2)), "removeAll true");
        check(a.equals(new CopyOnWriteArraySet<>(Arrays.asList(3, 4))), "removeAll result");
        a.retainAll(Arrays.asList(3));
        check(a.equals(new CopyOnWriteArraySet<>(Arrays.asList(3))), "retainAll result");
    }

    static void testBlackBox() {
        CopyOnWriteArraySet<Integer> s = new CopyOnWriteArraySet<>(Arrays.asList(3, 1, 2, 2));
        check(s.size() == 3, "dedup on construction");

        // toArray / toArray(T[])
        Object[] arr = s.toArray();
        ArrayList<Integer> arrSorted = new ArrayList<>();
        for (Object o : arr) arrSorted.add((Integer) o);
        arrSorted.sort(Integer::compareTo);
        check(arrSorted.equals(Arrays.asList(1, 2, 3)), "toArray() contents");
        Integer[] typed = s.toArray(new Integer[0]);
        check(typed.length == 3, "toArray(T[]) length");

        // forEach
        ArrayList<Integer> seen = new ArrayList<>();
        s.forEach(seen::add);
        seen.sort(Integer::compareTo);
        check(seen.equals(Arrays.asList(1, 2, 3)), "forEach contents");

        // removeIf
        s.removeIf(x -> x % 2 == 0);
        check(s.size() == 2 && s.contains(1) && s.contains(3) && !s.contains(2), "removeIf");

        // spliterator
        java.util.Spliterator<Integer> sp = s.spliterator();
        check(sp.estimateSize() == 2, "spliterator estimateSize");
        ArrayList<Integer> viaSp = new ArrayList<>();
        sp.forEachRemaining(viaSp::add);
        viaSp.sort(Integer::compareTo);
        check(viaSp.equals(Arrays.asList(1, 3)), "spliterator contents");

        // clear
        s.clear();
        check(s.isEmpty(), "clear empties");
    }

    // ---- appended: contract deepening ----
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

    static void testEmptyState() {
        CopyOnWriteArraySet<String> s = new CopyOnWriteArraySet<>();
        check(s.isEmpty() && s.size() == 0, "fresh set empty");
        check(!s.contains("x"), "contains empty false");
        check(!s.remove("x"), "remove empty false");
        check(!s.iterator().hasNext(), "empty iterator hasNext false");
        expectThrows(() -> s.iterator().next(), java.util.NoSuchElementException.class,
                     "empty iterator next throws NoSuchElementException");
        check(s.toString().equals("[]"), "empty toString []");
    }

    static void testIterationOrderIsInsertionOrder() {
        CopyOnWriteArraySet<Integer> s = new CopyOnWriteArraySet<>(Arrays.asList(5, 3, 1, 3, 5));
        check(s.size() == 3, "constructor dedups keeping first occurrence");
        List<Integer> order = new ArrayList<>();
        for (int x : s) order.add(x);
        check(order.equals(Arrays.asList(5, 3, 1)), "iteration order is insertion order");
        CopyOnWriteArraySet<String> t = new CopyOnWriteArraySet<>();
        check(t.addAll(Arrays.asList("c", "a", "c", "b", "a")), "addAll true");
        List<String> o2 = new ArrayList<>();
        for (String x : t) o2.add(x);
        check(o2.equals(Arrays.asList("c", "a", "b")), "addAll preserves first-occurrence order");
        check(t.toString().equals("[c, a, b]"), "toString in insertion order");
        t.add("z");
        check(t.toString().equals("[c, a, b, z]"), "add appends at the end");
    }

    static void testNullTolerance() {
        CopyOnWriteArraySet<String> s = new CopyOnWriteArraySet<>();
        check(s.add(null), "add(null) legal (backed by COWAL)");
        check(s.contains(null), "contains(null) true");
        check(!s.add(null), "second add(null) false");
        check(s.add("a") && s.size() == 2, "size with null element");
        expectThrows(() -> s.addAll(null), NullPointerException.class, "addAll(null)");
        expectThrows(() -> new CopyOnWriteArraySet<String>(null), NullPointerException.class,
                     "ctor(null Collection)");
        check(s.remove(null) && !s.contains(null), "remove(null) works");
    }

    static void testIteratorSnapshotExactOrder() {
        CopyOnWriteArraySet<Integer> s = new CopyOnWriteArraySet<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = s.iterator();
        s.add(4);
        s.remove(1);
        List<Integer> seen = new ArrayList<>();
        while (it.hasNext()) seen.add(it.next());
        check(seen.equals(Arrays.asList(1, 2, 3)), "snapshot excludes later add AND remove");
        Iterator<Integer> it2 = s.iterator();
        it2.next();
        expectThrows(() -> it2.remove(), UnsupportedOperationException.class,
                     "iterator.remove unsupported (snapshot)");
    }

    static void testCrossImplEqualsHashSet() {
        CopyOnWriteArraySet<Integer> c = new CopyOnWriteArraySet<>(Arrays.asList(1, 2, 3));
        java.util.HashSet<Integer> hs = new java.util.HashSet<>(Arrays.asList(3, 2, 1));
        check(c.equals(hs) && hs.equals(c), "cross-impl equals with HashSet");
        check(c.hashCode() == hs.hashCode(), "hashCode matches Set contract");
        hs.add(99);
        check(!c.equals(hs), "different elements not equal");
    }

    static void testSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(CopyOnWriteArraySet.class).getSerialVersionUID()
              == 5457747651344034263L, "serialVersionUID locked");
        CopyOnWriteArraySet<String> s = new CopyOnWriteArraySet<>(Arrays.asList("a", null, "b"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(s);
        }
        @SuppressWarnings("unchecked")
        CopyOnWriteArraySet<String> copy = (CopyOnWriteArraySet<String>)
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.size() == 3 && copy.contains("a") && copy.contains(null), "round-trip incl. null");
        List<String> order = new ArrayList<>();
        for (String x : copy) order.add(x);
        check(order.equals(Arrays.asList("a", null, "b")), "round-trip preserves insertion order");
        check(copy.add("c") && copy.size() == 4, "resurrected set mutable");
    }

    static void testSpliteratorCharacteristics() {
        CopyOnWriteArraySet<Integer> s = new CopyOnWriteArraySet<>(Arrays.asList(5, 3, 1));
        java.util.Spliterator<Integer> sp = s.spliterator();
        int ch = sp.characteristics();
        check((ch & java.util.Spliterator.DISTINCT) != 0, "spliterator DISTINCT");
        check((ch & java.util.Spliterator.IMMUTABLE) != 0, "spliterator IMMUTABLE");
        check((ch & java.util.Spliterator.SIZED) != 0, "spliterator SIZED");
        check(sp.estimateSize() == 3, "estimateSize exact");
        final int[] first = {-1};
        check(sp.tryAdvance(x -> first[0] = x) && first[0] == 5, "tryAdvance first in insertion order");
        check(s.spliterator().trySplit() != null, "trySplit supported");
        Integer[] bigger = s.toArray(new Integer[5]);
        check(bigger.length == 5 && bigger[3] == null, "toArray(bigger) null-terminated");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSetSemantics();
        testAddAllDedup();
        testIteratorSnapshot();
        testBulkOperationsAndEquals();
        testBlackBox();
        testEmptyState();
        testIterationOrderIsInsertionOrder();
        testNullTolerance();
        testIteratorSnapshotExactOrder();
        testCrossImplEqualsHashSet();
        testSerialization();
        testSpliteratorCharacteristics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
