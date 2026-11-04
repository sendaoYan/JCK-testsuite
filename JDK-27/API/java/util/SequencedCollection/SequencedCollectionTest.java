/*
 * @test
 * @summary java.util.SequencedCollection conformance: white-box structure + black-box behavior.
 * @run main SequencedCollectionTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SequencedCollection;

public class SequencedCollectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.SequencedCollection");
        check(c.isInterface(), "is an interface");
        check(java.util.Collection.class.isAssignableFrom(c), "extends Collection");
        check(Modifier.isAbstract(c.getDeclaredMethod("reversed").getModifiers()), "reversed abstract");
        for (String m : new String[] {"addFirst", "addLast"})
            check(!Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()),
                    m + " default");
        for (String m : new String[] {"getFirst", "getLast", "removeFirst", "removeLast"})
            check(!Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " default");
    }

    static void testBlackBox() {
        SequencedCollection<String> c = new ArrayList<>();
        c.addFirst("a");
        c.addLast("c");
        c.addFirst("z");
        check(c.equals(List.of("z", "a", "c")), "addFirst/addLast order");
        check("z".equals(c.getFirst()) && "c".equals(c.getLast()), "getFirst/getLast");
        check("z".equals(c.removeFirst()) && "c".equals(c.removeLast()), "removeFirst/removeLast");
        check(c.equals(List.of("a")), "remaining after removes");
        check(c.reversed().getFirst().equals("a"), "reversed view");
        check(c.reversed() instanceof SequencedCollection, "reversed is SequencedCollection");
    }

    // ===================== 契约深化：reversed() 活视图/空容器 NSEE/null 容忍/不可变 UOE/Deque 载体 =====================

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

    static void testReversedLiveView() {
        // reversed() 是活视图：写通道双向传导
        List<String> l = new ArrayList<>(List.of("a", "b", "c"));
        List<String> rev = l.reversed();
        check(rev.equals(List.of("c", "b", "a")), "reversed content");
        rev.add("Z");                      // 加在 reversed 的尾端 == 原列表的首端
        check(l.equals(List.of("Z", "a", "b", "c")), "reversed.add reflects to original front");
        check(rev.size() == l.size(), "size linkage");
        rev.remove("b");                   // 按值删透传
        check(l.equals(List.of("Z", "a", "c")), "reversed.remove reflects to original");
        check(rev.reversed().equals(l), "reversed().reversed() restores original order");
        // 原列表修改在视图中可见
        l.addLast("tail");
        check(rev.getFirst().equals("tail"), "original modification visible in view");
        // 视图 removeFirst 删的是原列表的 last
        check("tail".equals(rev.removeFirst()), "view removeFirst removes original last");
        check(l.getLast().equals("c"), "original last after view removal");
        // iterator 逆序
        java.util.Iterator<String> it = rev.iterator();
        check("c".equals(it.next()), "reversed iterator starts at original last");
    }

    static void testEmptySemantics() {
        // 空容器：get/remove First/Last → NoSuchElementException
        List<String> empty = new ArrayList<>();
        checkThrows(NoSuchElementException.class, empty::getFirst, "empty getFirst NSEE");
        checkThrows(NoSuchElementException.class, empty::getLast, "empty getLast NSEE");
        checkThrows(NoSuchElementException.class, empty::removeFirst, "empty removeFirst NSEE");
        checkThrows(NoSuchElementException.class, empty::removeLast, "empty removeLast NSEE");
        // 非空后各方法成功
        List<String> one = new ArrayList<>();
        one.add("x");
        check("x".equals(one.getFirst()) && "x".equals(one.getLast())
              && "x".equals(one.removeFirst()) && one.isEmpty(), "single element lifecycle");
    }

    static void testNullElements() {
        // ArrayList 容忍 null 元素：sequenced 方法对 null 不抛
        List<String> l = new ArrayList<>();
        l.addFirst(null);
        l.addLast("a");
        l.addLast(null);
        check(l.getFirst() == null && "a".equals(l.get(1)) && l.getLast() == null,
              "null first/last tolerated in List");
        check(l.size() == 3, "nulls counted");
        check(l.removeFirst() == null, "removeFirst returns null element");
    }

    static void testImmutableCarriers() {
        // 不可变 List.of：写方法 UOE；空表 getFirst 先报 NSEE（读路径无不可变性检查）
        List<String> imm = List.of("a");
        checkThrows(UnsupportedOperationException.class, () -> imm.addFirst("z"),
              "List.of addFirst UOE");
        checkThrows(UnsupportedOperationException.class, () -> imm.addLast("z"),
              "List.of addLast UOE");
        checkThrows(UnsupportedOperationException.class, () -> imm.removeFirst(),
              "List.of removeFirst UOE");
        check("a".equals(imm.getFirst()) && "a".equals(imm.getLast()),
              "List.of getFirst/getLast readable");
        checkThrows(NoSuchElementException.class, () -> List.<String>of().getFirst(),
              "empty List.of getFirst NSEE");
        checkThrows(UnsupportedOperationException.class, () -> List.<String>of().removeFirst(),
              "empty List.of removeFirst UOE (immutability precedes emptiness)");
        // unmodifiableList 包装同样拒绝
        List<String> um = java.util.Collections.unmodifiableList(new ArrayList<>(List.of("a")));
        checkThrows(UnsupportedOperationException.class, () -> um.addFirst("z"),
              "unmodifiableList addFirst UOE");
        check("a".equals(um.getFirst()), "unmodifiableList getFirst readable");
    }

    static void testDequeCarrier() {
        // Deque 也实现 SequencedCollection：头尾方法与 Deque 方法等价
        java.util.ArrayDeque<String> d = new java.util.ArrayDeque<>();
        d.addFirst("1");
        d.addLast("2");
        SequencedCollection<String> sc = d;
        check("1".equals(sc.getFirst()) && "2".equals(sc.getLast()),
              "ArrayDeque sequenced getFirst/getLast");
        check("1".equals(sc.removeFirst()) && "2".equals(sc.removeLast()),
              "ArrayDeque sequenced removeFirst/removeLast");
        check(d.isEmpty(), "drained via sequenced methods");
        checkThrows(NoSuchElementException.class, () -> new java.util.ArrayDeque<String>().getFirst(),
              "empty ArrayDeque getFirst NSEE");
        // LinkedList 载体：getFirst==element、addFirst==push 语义
        java.util.LinkedList<String> ll = new java.util.LinkedList<>(List.of("a", "b"));
        check(ll.getFirst().equals(ll.element()) && ll.getFirst().equals(ll.peek()),
              "LinkedList getFirst == element == peek");
        check(ll.reversed().getFirst().equals(ll.getLast()), "LinkedList reversed view");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testReversedLiveView();
        testEmptySemantics();
        testNullElements();
        testImmutableCarriers();
        testDequeCarrier();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
