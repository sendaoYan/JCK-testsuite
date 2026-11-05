/*
 * @test
 * @summary java.lang.Iterable conformance: white-box structure + black-box behavior
 *          (iterator() traversal, default forEach, default spliterator).
 * @run main IterableTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class IterableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Iterable");
        check(c.isInterface(), "Iterable is an interface");
        check(c.getMethod("iterator").getReturnType() == Iterator.class,
              "iterator() returns Iterator");
        check(c.getMethod("forEach", java.util.function.Consumer.class).isDefault(),
              "forEach(Consumer) is a default method");
        check(c.getMethod("spliterator").isDefault(),
              "spliterator() is a default method");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testIterator() {
        Iterable<String> it = Arrays.asList("a", "b", "c");
        Iterator<String> i = it.iterator();
        check(i.hasNext() && i.next().equals("a"), "next 1");
        check(i.hasNext() && i.next().equals("b"), "next 2");
        check(i.hasNext() && i.next().equals("c"), "next 3");
        check(!i.hasNext(), "exhausted");
        try {
            i.next();
            throw new AssertionError("next() after exhaustion: expected NoSuchElementException");
        } catch (java.util.NoSuchElementException expected) {
            passed++;
        }
    }

    static void testForEach() {
        Iterable<Integer> it = Arrays.asList(1, 2, 3);
        final List<Integer> seen = new ArrayList<>();
        it.forEach(seen::add);
        check(seen.equals(Arrays.asList(1, 2, 3)), "forEach visits in order");
    }

    static void testSpliterator() {
        Iterable<Integer> it = Arrays.asList(1, 2, 3);
        java.util.Spliterator<Integer> sp = it.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize == 3");
        final int[] count = {0};
        sp.forEachRemaining(x -> count[0]++);
        check(count[0] == 3, "spliterator visits all elements");
    }

    
    // ==================== 契约深化（2026-11）====================

    static void testWhiteBoxCounts() throws Exception {
        Class<?> c = Class.forName("java.lang.Iterable");
        check(c.getTypeParameters().length == 1 && "T".equals(c.getTypeParameters()[0].getName()),
              "Iterable<T>: one type parameter named T");
        int total = 0, abstractCount = 0;
        for (java.lang.reflect.Method m : c.getMethods()) {
            total++;
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) abstractCount++;
        }
        check(total == 3, "Iterable exposes exactly 3 public methods, got " + total);
        check(abstractCount == 1, "exactly 1 abstract method (iterator), got " + abstractCount);
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("iterator").getModifiers()),
              "iterator() is the sole abstract method");
        check(c.getMethod("forEach", java.util.function.Consumer.class).getReturnType() == void.class,
              "forEach returns void");
    }

    /**
     * 自造可迭代类型：固定元素数组；不覆写 forEach/spliterator，
     * 因此默认实现（基于 iterator()）被真实走到。
     */
    static class Box implements Iterable<String> {
        final String[] items;
        Box(String... items) { this.items = items.clone(); }
        public Iterator<String> iterator() {
            return new Iterator<String>() {
                int cursor = 0;
                public boolean hasNext() { return cursor < items.length; }
                public String next() { return items[cursor++]; }
                public void remove() { throw new UnsupportedOperationException("immutable Box"); }
            };
        }
    }

    /** iterator() 恒返回 null 的负例：增强 for 必然 NPE。 */
    static class NullIterator implements Iterable<Object> {
        public Iterator<Object> iterator() { return null; }
    }

    /** 支持 remove 的可变Iterable（自造，非集合框架）。 */
    static class MutableList implements Iterable<Integer> {
        final List<Integer> data = new ArrayList<>(Arrays.asList(10, 20, 30));
        public Iterator<Integer> iterator() {
            return new Iterator<Integer>() {
                int cursor = 0;
                public boolean hasNext() { return cursor < data.size(); }
                public Integer next() { return data.get(cursor++); }
                public void remove() {
                    if (cursor == 0) throw new IllegalStateException("remove() before next()");
                    data.remove(--cursor);
                }
            };
        }
    }

    /** 增强 for 与手工 iterator() 完全等价（同一 Iterable 上逐一相等）。 */
    static void testForEachEquivalence() {
        Box box = new Box("x", "y", "z");
        List<String> forEachSeen = new ArrayList<>();
        for (String s : box) forEachSeen.add(s);
        List<String> manualSeen = new ArrayList<>();
        for (Iterator<String> i = box.iterator(); i.hasNext(); ) manualSeen.add(i.next());
        check(forEachSeen.size() == 3, "for-each visits all 3 elements");
        check(forEachSeen.equals(manualSeen), "for-each and manual iterator() identical");
        check(forEachSeen.equals(Arrays.asList("x", "y", "z")), "for-each order x,y,z");
        Object array = new int[] {1, 2};
        check(!(array instanceof Iterable), "arrays are for-each-able but NOT Iterable instances");
        check(!Iterable.class.isAssignableFrom(String.class), "String is NOT an Iterable");
    }

    /** 默认 forEach(Consumer)：恰好 n 次、按迭代顺序、异常透传同一实例且中断遍历。 */
    static void testForEachDefaultSemantics() {
        Box box = new Box("a", "b", "c");
        final int[] n = {0};
        final StringBuilder order = new StringBuilder();
        box.forEach(new java.util.function.Consumer<String>() {
            public void accept(String t) { n[0]++; order.append(t); }
        });
        check(n[0] == 3, "default forEach invokes consumer exactly 3 times, got " + n[0]);
        check(order.toString().equals("abc"), "default forEach invokes in iteration order");
        final List<String> visited = new ArrayList<>();
        final RuntimeException boom = new RuntimeException("stop");
        try {
            box.forEach(new java.util.function.Consumer<String>() {
                public void accept(String t) {
                    visited.add(t);
                    if (t.equals("b")) throw boom;
                }
            });
            throw new AssertionError("forEach: expected consumer exception to propagate");
        } catch (RuntimeException e) {
            check(e == boom, "forEach propagates the SAME consumer exception instance");
        }
        check(visited.equals(Arrays.asList("a", "b")),
              "forEach stops iterating after the throwing element");
        check(visited.size() == 2, "exactly 2 elements visited before the exception");
    }

    /** forEach(null) → NullPointerException（默认实现 requireNonNull 路径，无消息）。 */
    static void testForEachNullAction() {
        Box box = new Box("a");
        try {
            box.forEach(null);
            throw new AssertionError("forEach(null): expected NullPointerException");
        } catch (NullPointerException e) {
            passed++;
            check(e.getMessage() == null,
                  "forEach(null) NPE from default impl carries no message");
        }
    }

    /** iterator() 返回 null 时增强 for 抛 NPE。 */
    static void testNullIteratorForEnhancedFor() {
        NullIterator bad = new NullIterator();
        try {
            for (Object o : bad) {
                throw new AssertionError("unreachable body");
            }
            throw new AssertionError("for-each over null iterator: expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    /** 不可变 Iterable：next 后 remove → UnsupportedOperationException，且迭代器仍可用。 */
    static void testImmutableIteratorRemove() {
        Box box = new Box("p", "q");
        Iterator<String> i = box.iterator();
        check("p".equals(i.next()), "first element p");
        try {
            i.remove();
            throw new AssertionError("remove() on immutable iterator: expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            passed++;
            check("immutable Box".equals(e.getMessage()), "remove() propagates its exact message");
        }
        check("q".equals(i.next()), "iterator still usable after failed remove");
        check(!i.hasNext(), "iterator exhausted after 2 elements");
    }

    /** 支持 remove 的迭代器：先 next 才能 remove；remove 删除“最近返回”的元素。 */
    static void testMutableIteratorRemove() {
        MutableList ml = new MutableList();
        Iterator<Integer> i = ml.iterator();
        try {
            i.remove();
            throw new AssertionError("remove before next: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(i.next() == 10 && i.next() == 20, "read 10 then 20");
        i.remove();
        check(ml.data.equals(Arrays.asList(10, 30)), "remove dropped last-returned element 20");
        check(i.next() == 30, "next continues at 30 after remove");
        check(!i.hasNext(), "exhausted after remove");
    }

    /** 每次 iterator() 返回独立游标：两个迭代器互不干扰。 */
    static void testIndependentIteratorCursors() {
        Box box = new Box("1", "2", "3");
        Iterator<String> a = box.iterator();
        Iterator<String> b = box.iterator();
        check(a != b, "iterator() returns a fresh Iterator per call");
        check("1".equals(a.next()), "cursor a at 1");
        check("1".equals(b.next()), "cursor b independent, also at 1");
        check("2".equals(a.next()), "cursor a at 2");
        check("2".equals(b.next()), "cursor b at 2");
        check(a.hasNext() && b.hasNext(), "both still have one element");
        check("3".equals(a.next()) && "3".equals(b.next()), "cursors never interfere (both see full sequence)");
        check(!a.hasNext() && !b.hasNext(), "both exhausted");
    }

    /** 空Iterable：for-each 零次、iterator 立即耗尽、forEach 零调用。 */
    static void testEmptyIterable() {
        Box empty = new Box();
        int loops = 0;
        for (String s : empty) loops++;
        check(loops == 0, "for-each over empty Iterable runs body 0 times");
        check(!empty.iterator().hasNext(), "empty iterator hasNext()==false immediately");
        final int[] calls = {0};
        empty.forEach(new java.util.function.Consumer<String>() {
            public void accept(String t) { calls[0]++; }
        });
        check(calls[0] == 0, "forEach over empty Iterable invokes consumer 0 times");
        java.util.Spliterator<String> sp = empty.spliterator();
        check(!sp.tryAdvance(new java.util.function.Consumer<String>() {
            public void accept(String t) { }
        }), "tryAdvance on empty Iterable returns false");
    }

    /** 顺序 for-each 两次：每次都拿到全新迭代器（Iterable 可重复迭代）。 */
    static void testIterableIsRepeatable() {
        Box box = new Box("u", "v");
        List<String> first = new ArrayList<>(), second = new ArrayList<>();
        for (String s : box) first.add(s);
        for (String s : box) second.add(s);
        check(first.equals(Arrays.asList("u", "v")), "first pass complete");
        check(second.equals(first), "second pass identical (fresh iterator each time)");
    }

    /** 自造类型上的 spliterator：默认实现按迭代顺序消费；未知尺寸报告 Long.MAX_VALUE。 */
    static void testSpliteratorOnCustom() {
        Box box = new Box("m", "n");
        java.util.Spliterator<String> sp = box.spliterator();
        check(sp != null, "spliterator() non-null on custom Iterable");
        check(sp.estimateSize() == Long.MAX_VALUE,
              "default spliterator (unknown size) estimates Long.MAX_VALUE");
        check(sp.getExactSizeIfKnown() == -1, "getExactSizeIfKnown == -1 when size unknown");
        final List<String> seen = new ArrayList<>();
        check(sp.tryAdvance(new java.util.function.Consumer<String>() {
            public void accept(String s) { seen.add(s); }
        }), "first tryAdvance succeeds");
        check(sp.tryAdvance(new java.util.function.Consumer<String>() {
            public void accept(String s) { seen.add(s); }
        }), "second tryAdvance succeeds");
        check(seen.equals(Arrays.asList("m", "n")), "tryAdvance consumed m,n in order");
        check(!sp.tryAdvance(new java.util.function.Consumer<String>() {
            public void accept(String s) { seen.add(s); }
        }), "tryAdvance false when exhausted");
        check(seen.size() == 2, "no element visited after exhaustion");
        java.util.Spliterator<String> sp2 = box.spliterator();
        final int[] remaining = {0};
        sp2.forEachRemaining(new java.util.function.Consumer<String>() {
            public void accept(String s) { remaining[0]++; }
        });
        check(remaining[0] == 2, "forEachRemaining visits all 2 elements");
    }

    /** 协变使用：Iterable<String> 可当 Iterable<? extends CharSequence> 消费。 */
    static void testCovariantConsumption() {
        Iterable<String> box = new Box("aa", "bb");
        Iterable<? extends CharSequence> wider = box;
        final StringBuilder joined = new StringBuilder();
        wider.forEach(new java.util.function.Consumer<CharSequence>() {
            public void accept(CharSequence cs) { joined.append(cs); }
        });
        check(joined.toString().equals("aabb"), "covariant forEach consumes CharSequence view");
        int totalLen = 0;
        for (CharSequence cs : wider) totalLen += cs.length();
        check(totalLen == 4, "covariant for-each sees all elements");
    }

    /** 补充契约点：迭代器耗尽后 remove 的行为面 + for-each 只调用一次 iterator()。 */
    static void testIteratorContractExtras() {
        Box box = new Box("k1", "k2");
        // for-each 每次循环恰好调一次 iterator()：两次循环 → 两次新迭代器
        int totalLoops = 0;
        for (String s : box) totalLoops++;
        for (String s : box) totalLoops++;
        check(totalLoops == 4, "two for-each passes visit 4 elements total");
        // 耗尽后的 hasNext 稳定为 false
        Iterator<String> i = box.iterator();
        while (i.hasNext()) i.next();
        check(!i.hasNext(), "hasNext stays false after exhaustion");
        check(!i.hasNext(), "repeated hasNext still false (no side effects)");
        // 不可变迭代器耗尽后 remove 仍拒绝
        try {
            i.remove();
            throw new AssertionError("exhausted remove: expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            passed++;
        }
        // forEach 与 for-each 观察同一顺序（默认 forEach 内部走 for-each）
        final StringBuilder viaForEach = new StringBuilder();
        box.forEach(new java.util.function.Consumer<String>() {
            public void accept(String s) { viaForEach.append(s); }
        });
        StringBuilder viaFor = new StringBuilder();
        for (String s : box) viaFor.append(s);
        check(viaForEach.toString().equals(viaFor.toString()),
              "default forEach and for-each observe identical order");
    }

    static void testBlackBox() throws Exception {
        testIterator();
        testForEach();
        testSpliterator();
        testForEachEquivalence();
        testForEachDefaultSemantics();
        testForEachNullAction();
        testNullIteratorForEnhancedFor();
        testImmutableIteratorRemove();
        testMutableIteratorRemove();
        testIndependentIteratorCursors();
        testEmptyIterable();
        testIterableIsRepeatable();
        testSpliteratorOnCustom();
        testCovariantConsumption();
        testIteratorContractExtras();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWhiteBoxCounts();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
