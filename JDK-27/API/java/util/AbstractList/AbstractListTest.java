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


    // ---- deepened II: 继承 iterator 走 get() / add(E) 分派 add(int,E) /
    //      hashCode 公式 / subList 写通道 / SequencedCollection 默认方法分派 -------

    static class CountingGet extends AbstractList<String> {
        int getCalls;
        public String get(int i) { getCalls++; return "e" + i; }
        public int size() { return 2; }
    }

    static class EmptyList extends AbstractList<String> {
        // next() catches IndexOutOfBoundsException from get() and converts it to NSEE
        public String get(int i) { throw new IndexOutOfBoundsException("empty"); }
        public int size() { return 0; }
    }

    static class AddOnly extends AbstractList<String> {
        final List<String> backing = new ArrayList<>(Arrays.asList("m"));
        int addCalls;
        public String get(int i) { return backing.get(i); }
        public int size() { return backing.size(); }
        public void add(int i, String v) { addCalls++; backing.add(i, v); }
    }

    static void testInheritedIteratorUsesGet() {
        CountingGet c = new CountingGet();
        StringBuilder sb = new StringBuilder();
        for (String s : c) sb.append(s);
        check(sb.toString().equals("e0e1"), "inherited iterator iterates via get()");
        check(c.getCalls == 2, "iterator drove exactly size() get() calls");
        expectThrows(() -> c.iterator().remove(), IllegalStateException.class,
                "inherited iterator remove before next ISE");
        expectThrows(() -> new EmptyList().iterator().next(), java.util.NoSuchElementException.class,
                "empty AbstractList iterator next NSEE");
        StringBuilder sb2 = new StringBuilder();
        c.iterator().forEachRemaining(sb2::append);
        check(sb2.toString().equals("e0e1"), "forEachRemaining via inherited iterator");
    }

    static void testAddDelegation() {
        AddOnly a = new AddOnly();
        check(a.add("x"), "add(E) returns true (delegates to add(int,E))");
        check(a.addCalls == 1, "add(E) invoked add(int,E) exactly once");
        check(a.equals(Arrays.asList("m", "x")), "element appended at end");
        a.add(0, "head");
        check(a.addCalls == 2 && a.get(0).equals("head"), "direct add(int,E) also counted");
        check(a.addAll(Arrays.asList("p", "q")), "addAll returns true");
        check(a.addCalls == 4, "addAll of 2 elements drove 2 more add(int,E) calls");
        check(a.equals(Arrays.asList("head", "m", "x", "p", "q")), "addAll appended in order");
    }

    static void testHashCodeFormula() {
        check(new L().hashCode() == 1, "empty hashCode 1");
        L l = new L();
        l.add("a"); l.add("b");
        check(l.hashCode() == 4066, "[a,b] hashCode 4066 (31*(31*1+97)+98)");
        l.add(null);
        check(l.hashCode() == 126046, "[a,b,null] hashCode 126046");
        check(l.hashCode() == new ArrayList<>(Arrays.asList("a", "b", null)).hashCode(),
              "AbstractList hashCode matches ArrayList");
        check(l.hashCode() - 0 == new ArrayList<>(Arrays.asList("a", "b", null)).hashCode() + 0,
              "hashCode recomputed");
        check(List.of("a", "b").hashCode() == 4066, "List.of hashCode same formula");
    }

    static void testSubListWriteChannels() {
        L l = new L();
        l.add("1"); l.add("2"); l.add("3"); l.add("4");
        List<String> sub = l.subList(1, 3);
        check(sub.equals(Arrays.asList("2", "3")), "subList content");
        sub.add(0, "99");
        check(l.equals(Arrays.asList("1", "99", "2", "3", "4")), "subList.add passes through");
        sub.remove("99");
        check(l.equals(Arrays.asList("1", "2", "3", "4")), "subList.remove(Object) passes through");
        check(sub.remove(1).equals("3"), "subList.remove(int) returns removed via subclass");
        check(l.equals(Arrays.asList("1", "2", "4")), "subList.remove(int) shrinks backing");
        check(sub.set(0, "30").equals("2"), "subList.set returns old value");
        check(l.get(1).equals("30"), "subList.set reflected in backing");
        java.util.Iterator<String> sit = sub.iterator();
        sit.next(); // "30"
        sit.remove();
        check(l.equals(Arrays.asList("1", "4")), "subList iterator.remove passes through");
        check(l.subList(0, 0).isEmpty() && l.subList(l.size(), l.size()).isEmpty(), "empty subList views");
        expectThrows(() -> l.subList(0, 99), IndexOutOfBoundsException.class, "subList(0,99) IOOBE");
        expectThrows(() -> l.subList(3, 2), IllegalArgumentException.class, "subList(3,2) IAE");
    }

    static void testSequencedDefaultsOnMinimal() {
        // getFirst/getLast defaults iterate via get(); addFirst/addLast require add(int,E)
        CountingGet c = new CountingGet();
        check(c.getFirst().equals("e0"), "getFirst default reads element 0");
        check(c.getLast().equals("e1"), "getLast default reads last element");
        expectThrows(() -> new AbstractList<String>() {
            public String get(int i) { return "x"; }
            public int size() { return 1; }
        }.addFirst("y"), UnsupportedOperationException.class,
                "addFirst default falls through to add(int,E) UOE");
        EmptyList e = new EmptyList();
        expectThrows(e::getFirst, java.util.NoSuchElementException.class, "empty getFirst NSEE");
        expectThrows(e::getLast, java.util.NoSuchElementException.class, "empty getLast NSEE");
        List<String> rev = new L().reversed();
        check(rev.isEmpty(), "reversed() default view of empty list");
        L three = new L();
        three.add("a"); three.add("b"); three.add("c");
        check(three.reversed().equals(Arrays.asList("c", "b", "a")), "reversed() default view order");
    }

    static void testEqualsCrossImpl() {
        L e = new L();
        e.add("x"); e.add(null); e.add("y");
        List<String> al = new ArrayList<>(Arrays.asList("x", null, "y"));
        java.util.LinkedList<String> ll = new java.util.LinkedList<>(Arrays.asList("x", null, "y"));
        check(e.equals(al) && al.equals(e), "AbstractList equals ArrayList (incl null)");
        check(e.equals(ll) && ll.equals(e), "AbstractList equals LinkedList");
        check(e.hashCode() == al.hashCode() && e.hashCode() == ll.hashCode(), "one hashCode across impls");
        check(!e.equals(Arrays.asList("x", null)), "prefix not equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInheritedIteratorUsesGet();
        testAddDelegation();
        testHashCodeFormula();
        testSubListWriteChannels();
        testSequencedDefaultsOnMinimal();
        testEqualsCrossImpl();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
