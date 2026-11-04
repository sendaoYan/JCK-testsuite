/*
 * @test
 * @summary java.util.concurrent.CopyOnWriteArraySet conformance: white-box + black-box
 *          (add dedup, iteration order == first-occurrence insertion order, null element
 *          tolerance, snapshot iterator, cross-impl equals with HashSet, serialization,
 *          spliterator, bulk operations).
 * @run main CopyOnWriteArraySetTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public class CopyOnWriteArraySetTest {
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
    static void testCopyOnWriteArraySet() {
        CopyOnWriteArraySet<String> s = new CopyOnWriteArraySet<String>();
        check(s.add("a"), "add first");
        check(!s.add("a"), "add duplicate returns false");
        check(s.add("b"), "add second");
        check(s.size() == 2, "size");
        check(s.contains("a") && s.contains("b"), "contains");
        check(s.remove("a") && s.size() == 1, "remove");
        check(s.containsAll(Arrays.asList("b")), "containsAll");
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
        check(s.toArray().length == 0, "empty toArray");
    }

    static void testIterationOrderIsInsertionOrder() {
        // iteration order == first-occurrence insertion order (backing COWAL addIfAbsent)
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
        // add appends at the end
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

    static void testIteratorSnapshot() {
        CopyOnWriteArraySet<Integer> s = new CopyOnWriteArraySet<>();
        s.add(1);
        s.add(2);
        s.add(3);
        Iterator<Integer> it = s.iterator();
        s.add(4); // modify after iterator creation
        List<Integer> seen = new ArrayList<>();
        while (it.hasNext()) seen.add(it.next());
        check(seen.equals(Arrays.asList(1, 2, 3)), "iterator sees snapshot, excludes later add");
        check(s.size() == 4, "set reflects modification");
        // iterator mutators are unsupported (snapshot semantics)
        Iterator<Integer> it2 = s.iterator();
        it2.next();
        expectThrows(() -> it2.remove(), UnsupportedOperationException.class,
                     "iterator.remove unsupported (snapshot)");
    }

    static void testBulkOperationsAndEquals() {
        CopyOnWriteArraySet<Integer> a = new CopyOnWriteArraySet<>(Arrays.asList(1, 2, 3, 4));
        CopyOnWriteArraySet<Integer> b = new CopyOnWriteArraySet<>(Arrays.asList(1, 2, 3, 4));
        check(a.equals(b), "equals");
        check(a.removeAll(Arrays.asList(1, 2)), "removeAll true");
        check(a.equals(new CopyOnWriteArraySet<>(Arrays.asList(3, 4))), "removeAll result");
        a.retainAll(Arrays.asList(3));
        check(a.equals(new CopyOnWriteArraySet<>(Arrays.asList(3))), "retainAll result");
        a.removeIf(x -> x == 3);
        check(a.isEmpty(), "removeIf empties");
        // cross-implementation equality with HashSet (Set contract: size + containsAll)
        CopyOnWriteArraySet<Integer> c = new CopyOnWriteArraySet<>(Arrays.asList(1, 2, 3));
        HashSet<Integer> hs = new HashSet<>(Arrays.asList(3, 2, 1));
        check(c.equals(hs) && hs.equals(c), "cross-impl equals with HashSet");
        check(c.hashCode() == hs.hashCode(), "hashCode matches Set contract");
        hs.add(99);
        check(!c.equals(hs), "different elements not equal");
    }

    static void testToArrayAndSpliterator() {
        CopyOnWriteArraySet<Integer> s = new CopyOnWriteArraySet<>(Arrays.asList(5, 3, 1));
        Object[] arr = s.toArray();
        check(arr.length == 3, "toArray length");
        List<Object> l = Arrays.asList(arr);
        check(l.containsAll(Arrays.asList(1, 3, 5)), "toArray contents");
        Integer[] typed = s.toArray(new Integer[0]);
        check(typed.length == 3 && typed[0] == 5, "toArray(T[]) preserves insertion order");
        Integer[] bigger = s.toArray(new Integer[5]);
        check(bigger.length == 5 && bigger[3] == null, "toArray(bigger) null-terminated");
        java.util.Spliterator<Integer> sp = s.spliterator();
        int ch = sp.characteristics();
        check((ch & java.util.Spliterator.DISTINCT) != 0, "spliterator DISTINCT");
        check((ch & java.util.Spliterator.IMMUTABLE) != 0, "spliterator IMMUTABLE");
        check((ch & java.util.Spliterator.SIZED) != 0, "spliterator SIZED");
        check(sp.estimateSize() == 3, "estimateSize exact");
        final int[] first = {-1};
        check(sp.tryAdvance(x -> first[0] = x) && first[0] == 5, "tryAdvance first in insertion order");
        java.util.Spliterator<Integer> sp2 = s.spliterator();
        check(sp2.trySplit() != null, "trySplit supported");
        check(s.stream().count() == 3, "stream count");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCopyOnWriteArraySet();
        testEmptyState();
        testIterationOrderIsInsertionOrder();
        testNullTolerance();
        testIteratorSnapshot();
        testBulkOperationsAndEquals();
        testToArrayAndSpliterator();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
