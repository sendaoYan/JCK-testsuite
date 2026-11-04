/*
 * @test
 * @summary java.util.Spliterator conformance: white-box structure plus black-box behavior of
 *          estimateSize/getExactSizeIfKnown, characteristics/hasCharacteristics, tryAdvance,
 *          forEachRemaining, trySplit, and getComparator on a non-SORTED spliterator.
 * @run main SpliteratorTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;

public class SpliteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Spliterator");
        check(c.isInterface(), "Spliterator is an interface");
        c.getMethod("tryAdvance", java.util.function.Consumer.class);
        c.getMethod("trySplit");
        c.getMethod("estimateSize");
        c.getMethod("characteristics");
        c.getMethod("hasCharacteristics", int.class);
        c.getMethod("getExactSizeIfKnown");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Spliterator<String> sp = new ArrayList<>(Arrays.asList("a", "b", "c")).spliterator();
        check(sp.estimateSize() == 3, "estimateSize");
        check(sp.getExactSizeIfKnown() == 3, "getExactSizeIfKnown");
        check(sp.hasCharacteristics(Spliterator.ORDERED), "hasCharacteristics ORDERED");
        check(sp.hasCharacteristics(Spliterator.SIZED), "hasCharacteristics SIZED");
        check(!sp.hasCharacteristics(Spliterator.SORTED), "!hasCharacteristics SORTED");

        List<String> seen = new ArrayList<>();
        while (sp.tryAdvance(seen::add)) {
            // consume
        }
        check(seen.equals(Arrays.asList("a", "b", "c")), "tryAdvance iteration");

        Spliterator<String> sp2 = new ArrayList<>(Arrays.asList("a", "b", "c")).spliterator();
        Spliterator<String> split = sp2.trySplit();
        long total = (split == null ? 0 : split.estimateSize()) + sp2.estimateSize();
        check(total == 3, "trySplit preserves total size");
        assertThrows(IllegalStateException.class, sp2::getComparator,
                "getComparator on non-SORTED throws IllegalStateException");

        List<String> remaining = new ArrayList<>();
        Spliterator<String> sp3 = new ArrayList<>(Arrays.asList("x", "y")).spliterator();
        sp3.forEachRemaining(remaining::add);
        check(remaining.equals(Arrays.asList("x", "y")), "forEachRemaining");

        Spliterator<String> exhausted = new ArrayList<>(Arrays.asList("q")).spliterator();
        exhausted.tryAdvance(x -> { });
        check(!exhausted.tryAdvance(x -> {
            throw new AssertionError("consumer must not run when exhausted");
        }), "tryAdvance exhausted false");
        check(new ArrayList<String>().spliterator().estimateSize() == 0, "empty estimateSize 0");
    }

    // ===================== 契约深化：常量精确值/实现特性位/trySplit 前后半/默认方法分派 =====================

    static void testConstantValues() {
        // javap -constants 双侧一致锁定（JDK-8 与 JDK-27 相同值）
        check(Spliterator.ORDERED == 16, "ORDERED == 0x10");
        check(Spliterator.DISTINCT == 1, "DISTINCT == 0x1");
        check(Spliterator.SORTED == 4, "SORTED == 0x4");
        check(Spliterator.SIZED == 64, "SIZED == 0x40");
        check(Spliterator.NONNULL == 256, "NONNULL == 0x100");
        check(Spliterator.IMMUTABLE == 1024, "IMMUTABLE == 0x400");
        check(Spliterator.CONCURRENT == 4096, "CONCURRENT == 0x1000");
        check(Spliterator.SUBSIZED == 16384, "SUBSIZED == 0x4000");
        check((Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED) == 0x4050,
              "ORDERED|SIZED|SUBSIZED == 16464");
        check((Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED
              | Spliterator.SIZED) == 0x55, "ORDERED|DISTINCT|SORTED|SIZED == 85 (TreeSet)");
        check(Spliterator.ORDERED > Spliterator.SORTED, "bit values are distinct powers of two");
    }

    static void testImplCharacteristics() {
        // ArrayList: ORDERED|SIZED|SUBSIZED == 16464（实测）
        Spliterator<Integer> al = new ArrayList<>(Arrays.asList(1, 2, 3)).spliterator();
        check(al.characteristics() == (Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED),
              "ArrayList spliterator characteristics == 16464");
        check(al.hasCharacteristics(Spliterator.ORDERED)
              && al.hasCharacteristics(Spliterator.SIZED)
              && al.hasCharacteristics(Spliterator.SUBSIZED), "ArrayList ORDERED/SIZED/SUBSIZED");
        check(!al.hasCharacteristics(Spliterator.DISTINCT)
              && !al.hasCharacteristics(Spliterator.SORTED), "ArrayList not DISTINCT/SORTED");
        // HashSet: DISTINCT|SIZED == 65（无序 → 不含 ORDERED）
        Spliterator<Integer> hs = new java.util.HashSet<>(Arrays.asList(1, 2, 3)).spliterator();
        check(hs.characteristics() == (Spliterator.DISTINCT | Spliterator.SIZED),
              "HashSet spliterator characteristics == 65");
        check(!hs.hasCharacteristics(Spliterator.ORDERED), "HashSet not ORDERED");
        // LinkedHashSet: ORDERED|DISTINCT|SIZED|SUBSIZED == 16465
        Spliterator<Integer> lhs = new java.util.LinkedHashSet<>(Arrays.asList(1, 2, 3)).spliterator();
        check(lhs.characteristics() == (Spliterator.ORDERED | Spliterator.DISTINCT
              | Spliterator.SIZED | Spliterator.SUBSIZED),
              "LinkedHashSet spliterator characteristics == 16465");
        // TreeSet: ORDERED|DISTINCT|SORTED|SIZED == 85（有序天然含 ORDERED）
        Spliterator<Integer> ts = new java.util.TreeSet<>(Arrays.asList(1, 2, 3)).spliterator();
        check(ts.characteristics() == (Spliterator.ORDERED | Spliterator.DISTINCT
              | Spliterator.SORTED | Spliterator.SIZED),
              "TreeSet spliterator characteristics == 85");
        check(ts.hasCharacteristics(Spliterator.ORDERED) && ts.hasCharacteristics(Spliterator.SORTED),
              "TreeSet ORDERED and SORTED");
    }

    static void testSortedGetComparator() {
        // SORTED 特性下 getComparator：给定比较器返回同一实例；自然序返回 null
        java.util.Comparator<String> cmp = java.util.Comparator.reverseOrder();
        java.util.TreeSet<String> ts = new java.util.TreeSet<>(cmp);
        ts.add("a");
        Spliterator<String> sp = ts.spliterator();
        check(sp.hasCharacteristics(Spliterator.SORTED), "TreeSet(cmp) SORTED");
        check(sp.getComparator() == cmp, "getComparator returns same instance for explicit cmp");
        java.util.TreeSet<String> natural = new java.util.TreeSet<>(Arrays.asList("a"));
        check(natural.spliterator().getComparator() == null,
              "getComparator null means natural ordering");
    }

    static void testTrySplitHalves() {
        // 契约：trySplit 返回的 spliterator 覆盖原序的前半，原实例保留后半
        Spliterator<String> sp = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e")).spliterator();
        long before = sp.estimateSize();
        Spliterator<String> front = sp.trySplit();
        check(front != null, "trySplit non-null for size 5");
        List<String> frontSeen = new ArrayList<>();
        List<String> backSeen = new ArrayList<>();
        front.forEachRemaining(frontSeen::add);
        sp.forEachRemaining(backSeen::add);
        check(frontSeen.equals(Arrays.asList("a", "b")) && backSeen.equals(Arrays.asList("c", "d", "e")),
              "trySplit front-half/back-half split (mid = size >>> 1)");
        // 8 元素对半：4/4
        Spliterator<String> sp8 = new ArrayList<>(Arrays.asList(
              "1", "2", "3", "4", "5", "6", "7", "8")).spliterator();
        Spliterator<String> f8 = sp8.trySplit();
        List<String> fa = new ArrayList<>(), ba = new ArrayList<>();
        f8.forEachRemaining(fa::add);
        sp8.forEachRemaining(ba::add);
        check(fa.equals(Arrays.asList("1", "2", "3", "4")) && ba.equals(Arrays.asList("5", "6", "7", "8")),
              "trySplit even-size exact halves");
        check(frontSeen.size() + backSeen.size() == before, "sizes add up to original estimate");
        List<String> all = new ArrayList<>(frontSeen);
        all.addAll(backSeen);
        check(all.equals(Arrays.asList("a", "b", "c", "d", "e")), "union preserves encounter order");
    }

    static void testDefaultMethodDelegation() {
        // 最小实现探针：只实现抽象方法，验证接口默认方法的分派路径
        final int[] advances = {0};
        Spliterator<String> probe = new Spliterator<String>() {
            public boolean tryAdvance(java.util.function.Consumer<? super String> action) {
                if (advances[0] < 2) { advances[0]++; action.accept("e" + advances[0]); return true; }
                return false;
            }
            public Spliterator<String> trySplit() { return null; }
            public long estimateSize() { return 2; }
            public int characteristics() { return 0; }
        };
        List<String> viaDefault = new ArrayList<>();
        probe.forEachRemaining(viaDefault::add);   // 默认实现逐个调 tryAdvance
        check(viaDefault.equals(Arrays.asList("e1", "e2")), "default forEachRemaining loops tryAdvance");
        check(advances[0] == 2, "default forEachRemaining invoked tryAdvance twice");
        check(probe.getExactSizeIfKnown() == -1,
              "getExactSizeIfKnown is -1 when SIZED not reported (default impl)");
        check(probe.hasCharacteristics(0), "hasCharacteristics(0) trivially true");
        check(probe.trySplit() == null, "custom trySplit returns null");
        assertThrows(IllegalStateException.class, probe::getComparator,
              "default getComparator throws ISE when not SORTED");
        // 耗尽后的 forEachRemaining 是 no-op
        final int[] calls = {0};
        probe.forEachRemaining(x -> calls[0]++);
        check(calls[0] == 0, "forEachRemaining after exhaustion is a no-op");
    }

    static void testUnsizedEstimates() {
        Spliterator<String> sp = java.util.Spliterators.spliteratorUnknownSize(
                Arrays.asList("p", "q").iterator(), Spliterator.ORDERED);
        check(sp.estimateSize() == Long.MAX_VALUE, "unknown-size estimateSize == Long.MAX_VALUE");
        check(sp.getExactSizeIfKnown() == -1, "unknown-size getExactSizeIfKnown == -1");
        check(sp.hasCharacteristics(Spliterator.ORDERED) && !sp.hasCharacteristics(Spliterator.SIZED),
              "unknown-size keeps given ORDERED but not SIZED");
        List<String> seen = new ArrayList<>();
        sp.forEachRemaining(seen::add);
        check(seen.equals(Arrays.asList("p", "q")), "unknown-size content drained");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantValues();
        testImplCharacteristics();
        testSortedGetComparator();
        testTrySplitHalves();
        testDefaultMethodDelegation();
        testUnsizedEstimates();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
