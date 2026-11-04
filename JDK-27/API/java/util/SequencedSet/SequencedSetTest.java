/*
 * @test
 * @summary java.util.SequencedSet conformance: white-box structure + black-box behavior.
 * @run main SequencedSetTest
 */

import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.SequencedSet;
import java.util.Set;

public class SequencedSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.SequencedSet");
        check(c.isInterface(), "is an interface");
        check(Set.class.isAssignableFrom(c), "extends Set");
        check(Modifier.isAbstract(c.getDeclaredMethod("reversed").getModifiers()), "reversed abstract");
    }

    static void testBlackBox() {
        SequencedSet<String> s = new LinkedHashSet<>();
        s.add("b");
        s.add("a");
        s.add("c");
        s.add("a");
        check(s.size() == 3, "no duplicates");
        check(s.equals(Set.of("a", "b", "c")), "set equality");
        check(s.getFirst().equals("b") && s.getLast().equals("c"), "insertion order first/last");
        SequencedSet<String> rev = s.reversed();
        check(rev.getFirst().equals("c") && rev.getLast().equals("b"), "reversed order");
        check(rev instanceof SequencedSet, "reversed is SequencedSet");
    }

    // ===================== 契约深化：addFirst/Last 既有元素重定位/null 容忍/空 NSEE/reversed 活视图/TreeSet 载体 =====================

    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
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

    static java.util.List<String> order(java.util.Set<String> s) {
        return new java.util.ArrayList<>(s);
    }

    static void testRelocation() {
        // addFirst/addLast 对既有元素是「重定位」而非报错/复制
        LinkedHashSet<String> s = new LinkedHashSet<>();
        s.add("a"); s.add("b"); s.add("c");
        check(order(s).equals(java.util.List.of("a", "b", "c")), "insertion order");
        s.addFirst("c");                       // 既有元素移到首
        check(order(s).equals(java.util.List.of("c", "a", "b")), "addFirst(existing) relocates to front");
        check(s.size() == 3, "relocation does not duplicate");
        s.addLast("a");                        // 既有元素移到尾
        check(order(s).equals(java.util.List.of("c", "b", "a")), "addLast(existing) relocates to back");
        s.addFirst("new");                     // 新元素正常插入
        check(order(s).equals(java.util.List.of("new", "c", "b", "a")), "addFirst(new) inserts");
        // Set 语义不变
        check(s.equals(Set.of("new", "c", "b", "a")), "Set equality unaffected");
        check(!s.add("new") && s.size() == 4, "add duplicate still returns false");
    }

    static void testNullTolerance() {
        // LinkedHashSet（HashSet 家族）容忍 null：sequenced 方法同样容忍
        LinkedHashSet<String> s = new LinkedHashSet<>();
        s.addFirst(null);
        check(s.size() == 1 && s.contains(null), "addFirst(null) accepted (HashSet family allows null)");
        check(s.getFirst() == null, "getFirst returns null element");
        s.addLast("a");
        check(s.getLast().equals("a"), "mixed null and value");
        check(s.removeFirst() == null && s.size() == 1, "removeFirst returns the null");
    }

    static void testEmptySemantics() {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        checkThrows(NoSuchElementException.class, s::getFirst, "empty getFirst NSEE");
        checkThrows(NoSuchElementException.class, s::getLast, "empty getLast NSEE");
        checkThrows(NoSuchElementException.class, s::removeFirst, "empty removeFirst NSEE");
        checkThrows(NoSuchElementException.class, s::removeLast, "empty removeLast NSEE");
        check(s.isEmpty(), "still empty after failed removes");
    }

    static void testReversedLiveView() {
        LinkedHashSet<String> s = new LinkedHashSet<>(java.util.List.of("a", "b", "c"));
        SequencedSet<String> rev = s.reversed();
        check(order(rev).equals(java.util.List.of("c", "b", "a")), "reversed order");
        // Set.add 无位置语义：落在底层尾端（即原集合的末尾，不是首端——与 List.reversed().add 相对）
        rev.add("R");
        check(order(s).equals(java.util.List.of("a", "b", "c", "R")),
              "reversed.add lands at original tail (add has no positional insert)");
        check(rev.getFirst().equals("R") && rev.getLast().equals("a"),
              "view endpoints (view first == original last)");
        check(rev.size() == s.size(), "size linkage");
        check(rev.removeFirst().equals("R"), "view removeFirst removes original last");
        check(order(s).equals(java.util.List.of("a", "b", "c")), "original after view removal");
        SequencedSet<String> back = rev.reversed();
        check(order(back).equals(order(s)), "reversed().reversed() restores original order");
        // 视图 addFirst == 原集合 addLast：既有元素重定位到原集合尾端
        rev.addFirst("a");
        check(order(s).get(s.size() - 1).equals("a"), "view addFirst(existing) relocates to original last");
    }

    static void testTreeSetCarrier() {
        // TreeSet 实现 SequencedSet：读/删可用；addFirst/addLast 违反序 → UOE
        java.util.TreeSet<String> ts = new java.util.TreeSet<>(java.util.List.of("b", "a", "c"));
        check(ts instanceof SequencedSet, "TreeSet is a SequencedSet");
        check(ts.getFirst().equals("a") && ts.getLast().equals("c"), "TreeSet getFirst/getLast sorted");
        check(ts.removeFirst().equals("a"), "TreeSet removeFirst removes least");
        check(ts.reversed().getFirst().equals("c"), "TreeSet reversed view");
        checkThrows(UnsupportedOperationException.class, () -> ts.addFirst("z"),
              "TreeSet addFirst UOE (order constraint)");
        checkThrows(UnsupportedOperationException.class, () -> ts.addLast("z"),
              "TreeSet addLast UOE (order constraint)");
        check(ts.size() == 2, "size unchanged after failed adds");
    }

    static void testWhoImplements() {
        // 只有有序实现 SequencedSet：HashSet/Set.of 不是
        check(new LinkedHashSet<String>() instanceof SequencedSet, "LinkedHashSet implements");
        check(new java.util.TreeSet<String>() instanceof SequencedSet, "TreeSet implements");
        check(!(new java.util.HashSet<String>() instanceof SequencedSet),
              "HashSet does NOT implement (unordered)");
        check(!(Set.<String>of() instanceof SequencedSet), "Set.of does not implement");
        check(!(java.util.Collections.unmodifiableSet(new LinkedHashSet<String>()) instanceof SequencedSet),
              "unmodifiableSet wrapper not SequencedSet-typed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRelocation();
        testNullTolerance();
        testEmptySemantics();
        testReversedLiveView();
        testTreeSetCarrier();
        testWhoImplements();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
