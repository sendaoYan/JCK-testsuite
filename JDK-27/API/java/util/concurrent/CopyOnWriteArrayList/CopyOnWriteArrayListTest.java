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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testListSemantics();
        testAddIfAbsent();
        testIteratorSnapshot();
        testSubList();
        testSortReplaceAll();
        testEquals();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
