/*
 * @test
 * @summary java.util.concurrent.CopyOnWriteArrayList conformance: white-box + black-box
 *          (add/get/set/remove/indexOf/contains, addIfAbsent/addAllAbsent, iterator
 *          snapshot semantics, subList, sort/replaceAll, equals).
 * @run main CopyOnWriteArrayListTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteArrayListTest {
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
        Class<?> c = Class.forName("java.util.concurrent.CopyOnWriteArrayList");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(List.class.isAssignableFrom(c), "implements List");
        check(RandomAccess.class.isAssignableFrom(c), "implements RandomAccess");
        check(Cloneable.class.isAssignableFrom(c) && Serializable.class.isAssignableFrom(c),
              "implements Cloneable/Serializable");
        check(c.getConstructor() != null && c.getConstructor(Collection.class) != null,
              "constructors ()/(Collection)");
        check(hasMethod(c, "add", Object.class) && hasMethod(c, "get", int.class),
              "add/get");
        check(hasMethod(c, "set", int.class, Object.class) && hasMethod(c, "remove", int.class),
              "set/remove(int)");
        check(hasMethod(c, "indexOf", Object.class), "indexOf");
        check(hasMethod(c, "addIfAbsent", Object.class), "addIfAbsent");
        check(hasMethod(c, "addAllAbsent", Collection.class), "addAllAbsent");
        check(hasMethod(c, "subList", int.class, int.class), "subList");
        check(hasMethod(c, "iterator"), "iterator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testListSemantics() {
        CopyOnWriteArrayList<String> l = new CopyOnWriteArrayList<>();
        check(l.add("a") && l.add("b") && l.add("c"), "add");
        check("a".equals(l.get(0)) && "c".equals(l.get(2)), "get");
        check("a".equals(l.set(0, "A")) && "A".equals(l.get(0)), "set returns old");
        check(l.size() == 3, "size");
        check(l.indexOf("b") == 1 && l.lastIndexOf("b") == 1, "indexOf/lastIndexOf");
        check(l.contains("b") && !l.contains("a"), "contains");
        check(l.remove("b") && l.size() == 2, "remove(Object)");
        check(l.remove(1) == "c", "remove(int) returns element");
        check(l.size() == 1 && "A".equals(l.get(0)), "remaining");
        l.clear();
        check(l.isEmpty(), "clear empties");
    }

    static void testAddIfAbsent() {
        CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>();
        check(l.addIfAbsent(1), "addIfAbsent inserts new");
        check(!l.addIfAbsent(1), "addIfAbsent duplicate false");
        check(l.size() == 1, "size after duplicate");
        List<Integer> toAdd = Arrays.asList(2, 3, 1);
        check(l.addAllAbsent(toAdd) == 2, "addAllAbsent adds 2 new elements");
        check(l.size() == 3 && l.contains(1) && l.contains(2) && l.contains(3),
              "addAllAbsent result");
    }

    static void testIteratorSnapshot() {
        CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>();
        l.add(1);
        l.add(2);
        l.add(3);
        Iterator<Integer> it = l.iterator();
        // Modify the list after creating the iterator.
        l.add(4);
        l.set(0, 100);
        // Iterator still sees the snapshot from iteration time.
        List<Integer> seen = new ArrayList<>();
        while (it.hasNext()) seen.add(it.next());
        check(seen.equals(Arrays.asList(1, 2, 3)), "iterator sees snapshot, not live modifications");
        check(l.size() == 4 && l.get(0) == 100, "list reflects modifications");
    }

    static void testSubList() {
        CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>();
        l.addAll(Arrays.asList(0, 1, 2, 3, 4));
        List<Integer> sub = l.subList(1, 4);
        check(sub.equals(Arrays.asList(1, 2, 3)), "subList contents");
        check(sub.size() == 3, "subList size");
    }

    static void testSortReplaceAll() {
        CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>();
        l.add(3);
        l.add(1);
        l.add(2);
        l.sort(Integer::compareTo);
        check(l.equals(Arrays.asList(1, 2, 3)), "sort orders");
        l.replaceAll(x -> x * 10);
        check(l.equals(Arrays.asList(10, 20, 30)), "replaceAll maps");
    }

    static void testEquals() {
        CopyOnWriteArrayList<Integer> a = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3));
        CopyOnWriteArrayList<Integer> b = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3));
        check(a.equals(b), "equals order-dependent content");
        b.set(0, 9);
        check(!a.equals(b), "!equals different content");
    }

    static void testBlackBox() {
        // constructor(Object[])
        CopyOnWriteArrayList<String> fromArr = new CopyOnWriteArrayList<>(new String[]{"a", "b", "c"});
        check(fromArr.size() == 3 && "a".equals(fromArr.get(0)), "constructor(Object[])");

        // addFirst/addLast/getFirst/getLast/removeFirst/removeLast (SequencedCollection)
        CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>();
        l.addLast(2);
        l.addFirst(1);
        l.addLast(3);
        check(l.getFirst() == 1 && l.getLast() == 3, "getFirst/getLast");
        check(l.removeFirst() == 1, "removeFirst returns first");
        check(l.removeLast() == 3, "removeLast returns last");
        check(l.getFirst() == 2 && l.getLast() == 2, "remaining single element");

        // reversed()
        CopyOnWriteArrayList<Integer> r = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3));
        List<Integer> rev = r.reversed();
        check(rev.equals(Arrays.asList(3, 2, 1)), "reversed() order");

        // clone()
        Object c = r.clone();
        check(c instanceof CopyOnWriteArrayList && c != r, "clone() distinct");
        check(((CopyOnWriteArrayList<Integer>) c).equals(r), "clone() equals");

        // containsAll
        check(r.containsAll(Arrays.asList(1, 3)) && !r.containsAll(Arrays.asList(1, 9)), "containsAll");

        // forEach
        ArrayList<Integer> seen = new ArrayList<>();
        r.forEach(seen::add);
        check(seen.equals(Arrays.asList(1, 2, 3)), "forEach order");

        // hashCode
        check(r.hashCode() == Arrays.asList(1, 2, 3).hashCode(), "hashCode matches List contract");

        // listIterator() / listIterator(int)
        Iterator<Integer> li = r.listIterator();
        check(li.hasNext() && li.next() == 1, "listIterator() next");
        java.util.ListIterator<Integer> li2 = r.listIterator(1);
        check(li2.next() == 2 && li2.previous() == 2, "listIterator(int)");

        // removeAll / retainAll / removeIf
        CopyOnWriteArrayList<Integer> bulk = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        check(bulk.removeAll(Arrays.asList(1, 3, 5)), "removeAll true");
        check(bulk.equals(Arrays.asList(2, 4)), "removeAll result");
        check(bulk.retainAll(Arrays.asList(2)), "retainAll true");
        check(bulk.equals(Arrays.asList(2)), "retainAll result");
        bulk.add(3);
        bulk.removeIf(x -> x % 2 == 0);
        check(bulk.equals(Arrays.asList(3)), "removeIf removes even");

        // spliterator
        CopyOnWriteArrayList<Integer> spl = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3));
        java.util.Spliterator<Integer> sp = spl.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        ArrayList<Integer> viaSp = new ArrayList<>();
        sp.forEachRemaining(viaSp::add);
        check(viaSp.equals(Arrays.asList(1, 2, 3)), "spliterator order");

        // toArray / toArray(T[])
        Object[] arr = spl.toArray();
        check(arr.length == 3 && arr[0].equals(1), "toArray()");
        Integer[] typed = spl.toArray(new Integer[0]);
        check(typed.length == 3 && typed[2] == 3, "toArray(T[])");

        // toString
        check(spl.toString() != null && spl.toString().contains("1"), "toString non-null");
    }

    // ---- appended: contract deepening ----
    static void testNullElements() {
        CopyOnWriteArrayList<String> l = new CopyOnWriteArrayList<>();
        check(l.add(null), "add(null) legal");
        check(l.add("a"), "add after null");
        check(l.add(null), "add(null) again");
        check(l.size() == 3, "size");
        check(l.indexOf(null) == 0, "indexOf(null) precise");
        check(l.lastIndexOf(null) == 2, "lastIndexOf(null) precise");
        check(l.contains(null), "contains(null) true");
        check("a".equals(l.set(1, null)), "set(i,null) legal and returns old value");
        check(l.get(1) == null, "set stored null");
        check(l.remove(null) && l.size() == 2, "remove(null) removes exactly one");
        check(l.remove(null) && l.remove(null) && !l.contains(null), "remaining nulls removed");
        check(l.isEmpty(), "list fully drained of nulls");
    }

    static void testAddIfAbsentNull() {
        CopyOnWriteArrayList<Integer> n = new CopyOnWriteArrayList<>();
        check(n.addIfAbsent(null), "addIfAbsent(null) inserts");
        check(!n.addIfAbsent(null), "addIfAbsent(null) duplicate false");
        check(n.addAllAbsent(Arrays.asList(1, null, 1)) == 1, "addAllAbsent counts only new (1)");
        check(n.size() == 2 && n.contains(null), "null kept exactly once");
    }

    static void testIteratorSnapshotVsArrayList() {
        // core COW contract: iterator does NOT throw CME and does NOT see later adds
        CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = l.iterator();
        l.add(4);
        List<Integer> seen = new ArrayList<>();
        while (it.hasNext()) seen.add(it.next());
        check(seen.equals(Arrays.asList(1, 2, 3)), "snapshot iterator excludes post-creation add");
        // in-test contrast: ArrayList under the same scenario IS fail-fast
        ArrayList<Integer> al = new ArrayList<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> alIt = al.iterator();
        al.add(4);
        boolean cme = false;
        try { alIt.next(); } catch (java.util.ConcurrentModificationException expected) { cme = true; }
        check(cme, "contrast: ArrayList iterator throws CME, COWAL never does");
        // listIterator also iterates a snapshot
        CopyOnWriteArrayList<Integer> l2 = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3));
        java.util.ListIterator<Integer> li = l2.listIterator();
        l2.set(0, 100);
        List<Integer> seen2 = new ArrayList<>();
        while (li.hasNext()) seen2.add(li.next());
        check(seen2.equals(Arrays.asList(1, 2, 3)), "listIterator sees snapshot despite concurrent set");
    }

    static void testIteratorMutatorsUnsupported() {
        CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = l.iterator();
        it.next();
        expectThrows(() -> it.remove(), UnsupportedOperationException.class, "iterator.remove");
        java.util.ListIterator<Integer> li = l.listIterator();
        li.next();
        expectThrows(() -> li.set(9), UnsupportedOperationException.class, "listIterator.set");
        expectThrows(() -> li.add(9), UnsupportedOperationException.class, "listIterator.add");
        check(l.size() == 3, "list unchanged after unsupported mutations");
        expectThrows(() -> l.listIterator(4), IndexOutOfBoundsException.class,
                     "listIterator(4) on size 3 out of bounds");
        expectThrows(() -> l.listIterator(-1), IndexOutOfBoundsException.class,
                     "listIterator(-1) out of bounds");
    }

    static void testIndexBounds() {
        CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>(Arrays.asList(1));
        expectThrows(() -> l.get(-1), IndexOutOfBoundsException.class, "get(-1)");
        expectThrows(() -> l.get(2), IndexOutOfBoundsException.class, "get(2) size 1");
        expectThrows(() -> l.add(99, 5), IndexOutOfBoundsException.class, "add(99,x) beyond size");
        expectThrows(() -> l.remove(99), IndexOutOfBoundsException.class, "remove(99) beyond size");
        expectThrows(() -> l.set(5, 1), IndexOutOfBoundsException.class, "set(5,x) beyond size");
        expectThrows(() -> l.addAll(null), NullPointerException.class, "addAll(null)");
        expectThrows(() -> new CopyOnWriteArrayList<String>((Collection<String>) null),
                     NullPointerException.class, "ctor(null Collection)");
        expectThrows(() -> new CopyOnWriteArrayList<String>((String[]) null),
                     NullPointerException.class, "ctor(null array)");
    }

    static void testConstructors() {
        // the array is defensively copied: mutating the source does not affect the list
        String[] src = {"a", "b"};
        CopyOnWriteArrayList<String> copy = new CopyOnWriteArrayList<>(src);
        src[0] = "CHANGED";
        check("a".equals(copy.get(0)), "ctor(E[]) copies the array defensively");
        CopyOnWriteArrayList<Integer> fromColl = new CopyOnWriteArrayList<>(Arrays.asList(3, 1, 2));
        check(fromColl.equals(Arrays.asList(3, 1, 2)), "ctor(Collection) preserves order");
        check(new CopyOnWriteArrayList<Integer>().isEmpty(), "ctor() empty");
    }

    static void testSubListWriteThrough() {
        CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
        List<Integer> sub = l.subList(1, 4);
        sub.set(0, 99);
        check(l.get(1) == 99, "subList.set writes through to parent");
        sub.add(0, 77);
        check(l.equals(Arrays.asList(0, 77, 99, 2, 3, 4)), "subList.add writes through to parent");
        sub.remove(Integer.valueOf(77));
        check(l.equals(Arrays.asList(0, 99, 2, 3, 4)), "subList.remove writes through to parent");
        expectThrows(() -> l.subList(2, 1), IndexOutOfBoundsException.class, "subList(2,1)");
        expectThrows(() -> l.subList(-1, 2), IndexOutOfBoundsException.class, "subList(-1,2)");
        expectThrows(() -> l.subList(0, 99), IndexOutOfBoundsException.class, "subList(0,99)");
    }

    static void testSortStability() {
        CopyOnWriteArrayList<String> s = new CopyOnWriteArrayList<>(Arrays.asList("bb", "aa", "ab", "ba"));
        s.sort(java.util.Comparator.comparingInt(String::length));
        check(s.equals(Arrays.asList("bb", "aa", "ab", "ba")), "sort stable for equal keys");
    }

    static void testCrossImplEquals() {
        CopyOnWriteArrayList<Integer> a = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3));
        ArrayList<Integer> al = new ArrayList<>(Arrays.asList(1, 2, 3));
        check(a.equals(al) && al.equals(a), "cross-impl equals with ArrayList");
        check(a.hashCode() == al.hashCode(), "hashCode matches List contract (== ArrayList's)");
        check(a.toString().equals(al.toString()), "toString equals ArrayList's");
        check(!a.equals(Arrays.asList(3, 2, 1)), "List equality is order-sensitive");
    }

    static void testToArraySnapshot() {
        CopyOnWriteArrayList<Integer> r = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3));
        Object[] arr = r.toArray();
        r.add(7);
        check(arr.length == 3 && !Arrays.asList(arr).contains(7),
              "toArray snapshot independent of later mutations");
        r.remove(Integer.valueOf(7));
        Integer[] bigger = r.toArray(new Integer[5]);
        check(bigger.length == 5 && bigger[3] == null, "toArray(bigger) null-terminated");
    }

    static void testSpliteratorCharacteristics() {
        CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3));
        java.util.Spliterator<Integer> sp = l.spliterator();
        int ch = sp.characteristics();
        check((ch & java.util.Spliterator.ORDERED) != 0, "spliterator ORDERED");
        check((ch & java.util.Spliterator.IMMUTABLE) != 0, "spliterator IMMUTABLE (snapshot)");
        check((ch & java.util.Spliterator.SIZED) != 0, "spliterator SIZED");
        check(sp.estimateSize() == 3, "estimateSize exact");
        check(l.parallelStream().mapToInt(Integer::intValue).sum() == 6, "parallelStream sum");
    }

    static void testSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(CopyOnWriteArrayList.class).getSerialVersionUID()
              == 8673264195747942595L, "serialVersionUID locked");
        CopyOnWriteArrayList<String> l = new CopyOnWriteArrayList<>(Arrays.asList("a", null, "c"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(l);
        }
        @SuppressWarnings("unchecked")
        CopyOnWriteArrayList<String> copy = (CopyOnWriteArrayList<String>)
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.equals(l) && copy.size() == 3, "round-trip preserves content incl. null");
        check(copy.get(1) == null, "null element survives round-trip");
        check(copy.add("z") && copy.get(3).equals("z"), "resurrected list mutable");
    }

    static void testClone() {
        CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>(Arrays.asList(1, 2));
        @SuppressWarnings("unchecked")
        CopyOnWriteArrayList<Integer> c = (CopyOnWriteArrayList<Integer>) l.clone();
        check(c != l && c.equals(l), "clone equals but distinct");
        c.add(3);
        check(l.size() == 2 && c.size() == 3, "clone is structurally independent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testListSemantics();
        testAddIfAbsent();
        testIteratorSnapshot();
        testSubList();
        testSortReplaceAll();
        testEquals();
        testBlackBox();
        testNullElements();
        testAddIfAbsentNull();
        testIteratorSnapshotVsArrayList();
        testIteratorMutatorsUnsupported();
        testIndexBounds();
        testConstructors();
        testSubListWriteThrough();
        testSortStability();
        testCrossImplEquals();
        testToArraySnapshot();
        testSpliteratorCharacteristics();
        testSerialization();
        testClone();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
