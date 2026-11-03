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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testListSemantics();
        testAddIfAbsent();
        testIteratorSnapshot();
        testSubList();
        testSortReplaceAll();
        testEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
