/*
 * @test
 * @summary java.util.AbstractSequentialList conformance: white-box structure + black-box behavior.
 * @run main AbstractSequentialListTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class AbstractSequentialListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal concrete subclass: implements only size() + listIterator(int). */
    static class SL extends AbstractSequentialList<String> {
        final List<String> backing = new ArrayList<>();
        @Override public int size() { return backing.size(); }
        @Override public ListIterator<String> listIterator(int index) { return backing.listIterator(index); }
        void fill(String... items) { backing.addAll(Arrays.asList(items)); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.AbstractSequentialList");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.util.AbstractList.class.isAssignableFrom(c), "extends AbstractList");
        Method li = c.getDeclaredMethod("listIterator", int.class);
        check(Modifier.isAbstract(li.getModifiers()), "listIterator(int) abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("get", int.class).getModifiers()), "get concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("set", int.class, Object.class).getModifiers()),
                "set concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("add", int.class, Object.class).getModifiers()),
                "add(int) concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("remove", int.class).getModifiers()),
                "remove(int) concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SL sl = new SL();
        sl.fill("a", "b", "c", "d");

        // 1. get via listIterator(i).next(); set via listIterator(i).set
        check("a".equals(sl.get(0)) && "d".equals(sl.get(3)), "get by index");
        check("b".equals(sl.set(1, "B")), "set returns old");
        check("B".equals(sl.get(1)), "set updates value");

        // 2. add(int) inserts; remove(int) removes
        sl.add(0, "head");
        check("head".equals(sl.get(0)) && sl.size() == 5, "add(int) inserts");
        check("head".equals(sl.remove(0)), "remove(int) returns removed");
        check(sl.size() == 4 && "a".equals(sl.get(0)), "remove(int) shrinks");

        // 3. addAll(int, collection) inserts in order
        sl.addAll(1, Arrays.asList("x", "y"));
        check(sl.equals(Arrays.asList("a", "x", "y", "B", "c", "d")), "addAll(int, collection)");

        // 4. iterator() and full listIterator() both drive the sequence
        SL sl2 = new SL();
        sl2.fill("1", "2", "3");
        StringBuilder sb = new StringBuilder();
        for (String s : sl2) sb.append(s);
        check("123".contentEquals(sb), "iterator() via listIterator");
        ListIterator<String> li = sl2.listIterator();
        check(li.next().equals("1") && li.next().equals("2") && li.previous().equals("2"),
                "listIterator bidirectional");
        check(li.nextIndex() == 1 && li.previousIndex() == 0, "listIterator indexes");

        // 5. inherited List methods (indexOf/subList) work
        SL sl3 = new SL();
        sl3.fill("p", "q", "p");
        check(sl3.indexOf("p") == 0 && sl3.lastIndexOf("p") == 2, "indexOf/lastIndexOf");
        check(sl3.subList(1, 3).equals(Arrays.asList("q", "p")), "subList");
    }


    // ---- deepened II: 计数器证明全操作经 listIterator / 只读子类 UOE /
    //      hashCode 公式 / subList 写通道 / 边界 ----------------------------------

    static class Recording extends AbstractSequentialList<String> {
        final List<String> backing = new ArrayList<>(Arrays.asList("a", "b", "c"));
        int iteratorCreations;
        public ListIterator<String> listIterator(int index) {
            iteratorCreations++;
            return backing.listIterator(index);
        }
        public int size() { return backing.size(); }
    }

    static void testAllOperationsRouteThroughListIterator() {
        Recording r = new Recording();
        int before = r.iteratorCreations;
        check(r.get(1).equals("b"), "get works");
        check(r.iteratorCreations == before + 1, "get(1) created exactly one listIterator");
        r.set(1, "B");
        check(r.iteratorCreations == before + 2, "set(int,E) went through listIterator");
        r.add(1, "X");
        check(r.iteratorCreations == before + 3, "add(int,E) went through listIterator");
        check(r.remove(2).equals("B"), "remove(int) returns element");
        check(r.iteratorCreations == before + 4, "remove(int) went through listIterator");
        // the equals() check itself also routes through listIterator(): +1
        check(r.equals(Arrays.asList("a", "X", "c")), "state after routed ops");
        check(r.iteratorCreations == before + 5, "equals() also drove one listIterator");
        r.indexOf("a");
        check(r.iteratorCreations == before + 6, "indexOf drove listIterator");
        r.lastIndexOf("c");
        check(r.iteratorCreations == before + 7, "lastIndexOf drove listIterator");
        java.util.Iterator<String> it = r.iterator();
        check(it.next().equals("a"), "iterator() routes to listIterator(0)");
        check(r.iteratorCreations == before + 8, "iterator() created one listIterator");
        r.add("z");
        check(r.get(r.size() - 1).equals("z"), "add(E) appended");
        r.clear();
        check(r.isEmpty() && r.size() == 0, "clear via iterator remove");
    }

    static class ReadOnlyIterator extends AbstractSequentialList<String> {
        final List<String> backing = List.of("a", "b");
        public ListIterator<String> listIterator(int index) { return backing.listIterator(index); }
        public int size() { return backing.size(); }
    }

    static void testReadOnlySubclassUOE() throws Exception {
        ReadOnlyIterator ro = new ReadOnlyIterator();
        try { ro.set(0, "x"); throw new AssertionError("set UOE expected"); }
        catch (UnsupportedOperationException e) { passed++; }
        try { ro.add(0, "x"); throw new AssertionError("add UOE expected"); }
        catch (UnsupportedOperationException e) { passed++; }
        try { ro.remove(0); throw new AssertionError("remove UOE expected"); }
        catch (UnsupportedOperationException e) { passed++; }
        try { ro.add("x"); throw new AssertionError("add(E) UOE expected"); }
        catch (UnsupportedOperationException e) { passed++; }
        try { ro.clear(); throw new AssertionError("clear UOE expected"); }
        catch (UnsupportedOperationException e) { passed++; }
        check(ro.get(0).equals("a") && ro.size() == 2, "reads still work");
    }

    static void testEqualsHashCodeListContract() {
        SL m = new SL();
        m.fill("a", "b");
        check(m.hashCode() == 4066, "[a,b] hashCode 4066 (AbstractList formula inherited)");
        SL mn = new SL();
        mn.fill("a", "b", null);
        check(mn.hashCode() == 126046, "[a,b,null] hashCode 126046");
        check(new SL().hashCode() == 1, "empty hashCode 1");
        check(m.equals(new ArrayList<>(Arrays.asList("a", "b"))), "equals ArrayList");
        check(m.equals(List.of("a", "b")) && List.of("a", "b").equals(m), "equals List.of");
        check(!m.equals(Arrays.asList("b", "a")), "order sensitive");
    }

    static void testSubListWriteChannels() {
        SL m = new SL();
        m.fill("1", "2", "3", "4");
        List<String> sub = m.subList(1, 3);
        check(sub.equals(Arrays.asList("2", "3")), "subList content");
        sub.add("x");
        check(m.equals(Arrays.asList("1", "2", "3", "x", "4")), "subList.add passes through");
        sub.remove("x");
        check(m.equals(Arrays.asList("1", "2", "3", "4")), "subList.remove passes through");
        check(sub.set(0, "20").equals("2"), "subList.set returns old");
        check(m.get(1).equals("20"), "subList.set reflected");
        java.util.Iterator<String> sit = sub.iterator();
        sit.next();
        sit.remove();
        check(m.equals(Arrays.asList("1", "3", "4")), "subList iterator.remove passes through");
        try { m.subList(0, 99); throw new AssertionError("subList(0,99) IOOBE expected"); }
        catch (IndexOutOfBoundsException e) { passed++; }
        try { m.subList(2, 1); throw new AssertionError("subList(2,1) IAE expected"); }
        catch (IllegalArgumentException e) { passed++; }
        check(m.subList(0, 0).isEmpty(), "empty subList view");
    }

    static void testEmptyAndBounds() {
        SL empty = new SL();
        try { empty.get(0); throw new AssertionError("empty get(0) IOOBE expected"); }
        catch (IndexOutOfBoundsException e) { passed++; }
        try { empty.remove(0); throw new AssertionError("empty remove(0) IOOBE expected"); }
        catch (IndexOutOfBoundsException e) { passed++; }
        check(!empty.iterator().hasNext(), "empty iterator exhausted");
        check(empty.add("first"), "add to empty works");
        check(empty.get(0).equals("first"), "element readable after add");
        SL m = new SL();
        m.fill("a");
        try { m.get(-1); throw new AssertionError("get(-1) IOOBE expected"); }
        catch (IndexOutOfBoundsException e) { passed++; }
        try { m.listIterator(2); throw new AssertionError("listIterator(size+1) IOOBE expected"); }
        catch (IndexOutOfBoundsException e) { passed++; }
        try { m.listIterator(-1); throw new AssertionError("listIterator(-1) IOOBE expected"); }
        catch (IndexOutOfBoundsException e) { passed++; }
    }

    static void testSequencedDefaults() {
        // AbstractSequentialList inherits List's sequenced defaults, routed via listIterator
        SL m = new SL();
        m.fill("a", "b", "c");
        check(m.getFirst().equals("a"), "getFirst routes via iterator");
        check(m.getLast().equals("c"), "getLast routes via iterator");
        m.addFirst("0");
        m.addLast("d");
        check(m.equals(Arrays.asList("0", "a", "b", "c", "d")), "addFirst/addLast route via add(int,E)");
        check(m.removeFirst().equals("0") && m.removeLast().equals("d"), "removeFirst/removeLast");
        check(m.reversed().equals(Arrays.asList("c", "b", "a")), "reversed() view");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAllOperationsRouteThroughListIterator();
        testReadOnlySubclassUOE();
        testEqualsHashCodeListContract();
        testSubListWriteChannels();
        testEmptyAndBounds();
        testSequencedDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
