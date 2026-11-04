/*
 * @test
 * @summary java.util.AbstractList conformance: white-box structure + black-box behavior.
 * @run main AbstractListTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class AbstractListTest {
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

    /** Minimal concrete subclass: get + size + positional mutators. */
    static class L extends AbstractList<String> {
        final List<String> backing = new ArrayList<>();
        @Override public String get(int i) { return backing.get(i); }
        @Override public int size() { return backing.size(); }
        @Override public void add(int i, String e) { backing.add(i, e); modCount++; }
        @Override public String set(int i, String e) { return backing.set(i, e); }
        @Override public String remove(int i) { String r = backing.remove(i); modCount++; return r; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.AbstractList");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(AbstractList.class.isAssignableFrom(c), "extends AbstractCollection");
        check(List.class.isAssignableFrom(c), "implements List");
        Method get = c.getDeclaredMethod("get", int.class);
        check(Modifier.isAbstract(get.getModifiers()), "get(int) abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("set", int.class, Object.class).getModifiers()),
                "set(int,E) concrete (default throws)");
        check(!Modifier.isAbstract(c.getDeclaredMethod("add", int.class, Object.class).getModifiers()),
                "add(int,E) concrete");
        check(c.getDeclaredField("modCount") != null, "protected transient modCount present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. add(E) appends; add(int,E) inserts; get/set/remove positional
        L l = new L();
        check(l.add("a") && l.add("b"), "add(E) appends");
        l.add(1, "x");
        check(l.equals(Arrays.asList("a", "x", "b")), "add(int,E) inserts");
        check("x".equals(l.set(1, "y")), "set returns old");
        check(l.get(1).equals("y"), "set updates value");
        check("y".equals(l.remove(1)), "remove(int) returns removed");
        check(l.size() == 2, "size after remove");

        // 2. indexOf/lastIndexOf via equals
        L l2 = new L();
        l2.add("a");
        l2.add("b");
        l2.add("a");
        check(l2.indexOf("a") == 0 && l2.lastIndexOf("a") == 2, "indexOf/lastIndexOf");
        check(l2.indexOf("z") == -1 && l2.lastIndexOf("z") == -1, "missing index is -1");

        // 3. iterator fail-fast: structural modification after iterator creation
        L l3 = new L();
        l3.add("a");
        l3.add("b");
        l3.add("c");
        Iterator<String> it = l3.iterator();
        it.next();
        l3.add(0, "z");   // structural modification
        expectThrows(it::next, ConcurrentModificationException.class, "iterator fail-fast");

        // 4. listIterator navigation + add/set/remove
        L l4 = new L();
        l4.add("a");
        l4.add("c");
        ListIterator<String> li = l4.listIterator();
        check(li.next().equals("a"), "listIterator next");
        li.add("b");
        check(l4.equals(Arrays.asList("a", "b", "c")), "listIterator add");
        li.next();
        li.set("C");
        check(l4.equals(Arrays.asList("a", "b", "C")), "listIterator set");
        li.previous();
        li.remove();
        check(l4.equals(Arrays.asList("a", "b")), "listIterator remove");

        // 5. subList is a live view
        L l5 = new L();
        l5.add("0");
        l5.add("1");
        l5.add("2");
        List<String> sub = l5.subList(1, 3);
        sub.set(0, "X");
        check("X".equals(l5.get(1)), "subList mutates the backing list");
        check(sub.size() == 2, "subList size");
        l5.add(3, "3");
        expectThrows(() -> sub.size(), ConcurrentModificationException.class,
                "subList fail-fast after structural change");

        // 6. equals/hashCode are order-sensitive
        L a = new L(); a.add("1"); a.add("2");
        L b = new L(); b.add("1"); b.add("2");
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode for equal lists");
        b.set(0, "9");
        check(!a.equals(b), "equals order/values sensitive");

        // 7. read-only positional mutators throw UnsupportedOperationException
        AbstractList<String> ro = new AbstractList<>() {
            @Override public String get(int i) { return "x"; }
            @Override public int size() { return 1; }
        };
        expectThrows(() -> ro.set(0, "y"), UnsupportedOperationException.class, "default set throws");
        expectThrows(() -> ro.add(0, "y"), UnsupportedOperationException.class, "default add(int) throws");
        expectThrows(() -> ro.remove(0), UnsupportedOperationException.class, "default remove(int) throws");

        // 8. clear() removes all
        l5.clear();
        check(l5.isEmpty(), "clear empties");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
