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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSetSemantics();
        testAddAllDedup();
        testIteratorSnapshot();
        testBulkOperationsAndEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
