/*
 * @test
 * @summary java.util.HashSet 一致性：白盒（反射结构）+ 黑盒（去重 + 集合运算 + fail-fast）。
 * @run main HashSetTest
 *
 * 白盒（结构）：Class.forName 加载、继承 AbstractSet、实现 Set/Cloneable/Serializable、
 *   add/remove/contains/size/isEmpty/iterator 成员形态。
 * 黑盒（行为）设计案：
 * 1. 契约：HashSet 是基于 HashMap 的无序 Set，元素唯一（equals/hashCode），允许 null，
 *    不保证迭代顺序；add 重复元素返回 false。
 * 2. 场景：new HashSet() -> add/remove/contains -> 去重（equals 相等的对象视为同一元素）
 *    -> 集合运算 containsAll/removeAll/retainAll -> equals/hashCode -> 迭代（排序后比较）。
 * 3. 组合链路：插入重复与 null -> 校验 size 去重 -> 批量运算 -> 排序迭代 -> 结构修改 fail-fast。
 * 4. 不变量：size 等于唯一元素数；等值 Set 的 hashCode 相等且 equals 顺序无关。
 * 5. 边界：null 允许；add 重复返回 false；迭代器结构修改后 fail-fast 抛 CME。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeSet;

public class HashSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.HashSet");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.util.AbstractSet"), "extends AbstractSet");
        check(Set.class.isAssignableFrom(c), "implements Set");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "add", Object.class), "add(E)");
        check(hasMethod(c, "remove", Object.class), "remove(Object)");
        check(hasMethod(c, "contains", Object.class), "contains(Object)");
        check(hasMethod(c, "size") && hasMethod(c, "isEmpty"), "size/isEmpty");
        check(hasMethod(c, "iterator"), "iterator");
    }

    static void testAddRemoveContains() {
        HashSet<String> set = new HashSet<>();
        check(set.isEmpty() && set.size() == 0, "empty");
        check(set.add("a"), "add new returns true");
        check(set.add("b"), "add another");
        check(set.add(null), "add null");
        check(!set.add("a"), "add duplicate returns false");
        check(set.size() == 3, "size counts unique + null");
        check(set.contains("a") && set.contains(null) && !set.contains("z"), "contains");
        check(set.remove("a"), "remove returns true");
        check(!set.remove("a"), "remove absent returns false");
        check(set.remove(null) && !set.contains(null), "remove null");
        set.clear();
        check(set.isEmpty(), "clear");
    }

    static void testDedupByEqualsHashCode() {
        Set<String> set = new HashSet<>();
        String x1 = new String("x");
        String x2 = new String("x");
        check(set.add(x1), "add first equal object");
        check(!set.add(x2), "add equal object rejected");
        check(set.size() == 1, "equal objects collapse to one element");
        check(set.contains(x2) && set.contains("x"), "contains by equals");
    }

    static void testCollectionOps() {
        HashSet<String> set = new HashSet<>(Arrays.asList("a", "b", "c"));
        check(set.containsAll(Arrays.asList("a", "c")), "containsAll");
        check(!set.containsAll(Arrays.asList("a", "z")), "!containsAll");
        set.removeAll(Arrays.asList("a", "z"));
        check(set.size() == 2 && !set.contains("a"), "removeAll");
        set.addAll(Arrays.asList("d", "e", "d"));
        check(set.size() == 4, "addAll dedups");
        set.retainAll(Arrays.asList("b", "c", "e", "zz"));
        check(set.size() == 3 && set.contains("b") && set.contains("c") && set.contains("e"),
              "retainAll keeps intersection");
        check(set.removeIf(s -> s.equals("b")), "removeIf");
        check(!set.contains("b") && set.size() == 2, "removeIf removed");
    }

    static void testEqualsHashCode() {
        HashSet<String> a = new HashSet<>(Arrays.asList("x", "y"));
        HashSet<String> b = new HashSet<>(Arrays.asList("y", "x"));
        check(a.equals(b) && b.equals(a), "equals order-independent");
        check(a.hashCode() == b.hashCode(), "equal sets equal hashCode");
        b.add("z");
        check(!a.equals(b), "!equals different content");
    }

    static void testIterationAndFailFast() {
        HashSet<String> set = new HashSet<>(Arrays.asList("a", "b", "c"));
        List<String> sorted = new ArrayList<>(set);
        Collections.sort(sorted);
        check(sorted.equals(Arrays.asList("a", "b", "c")), "iteration contains all (sorted)");

        Iterator<String> it = set.iterator();
        set.add("d");
        try {
            it.next();
            throw new AssertionError("expected ConcurrentModificationException");
        } catch (java.util.ConcurrentModificationException expected) {
            passed++;
        }
    }

    static void testBlackBox() {
        HashSet<String> a = new HashSet<>(16, 0.75f);
        a.add("x");
        check(a.contains("x"), "ctor (int,float) works");
        HashSet<String> b = new HashSet<>(8);
        b.add("y");
        check(b.contains("y"), "ctor (int) works");

        HashSet<String> viaFactory = HashSet.newHashSet(8);
        viaFactory.add("z");
        check(viaFactory.contains("z"), "newHashSet(int) factory");

        HashSet<String> orig = new HashSet<>(Arrays.asList("a", "b", "c"));
        @SuppressWarnings("unchecked")
        HashSet<String> cloned = (HashSet<String>) orig.clone();
        check(cloned != orig && cloned.equals(orig), "clone independent and equal");
        cloned.add("d");
        check(orig.size() == 3 && !orig.contains("d"), "clone structural independence");

        // spliterator reports SIZED|DISTINCT and covers all elements
        java.util.Spliterator<String> sp = orig.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        check(sp.hasCharacteristics(java.util.Spliterator.SIZED | java.util.Spliterator.DISTINCT),
              "spliterator characteristics SIZED|DISTINCT");
        List<String> gathered = new ArrayList<>();
        sp.forEachRemaining(gathered::add);
        Collections.sort(gathered);
        check(gathered.equals(Arrays.asList("a", "b", "c")), "spliterator covers all (sorted)");

        // toArray() and toArray(T[]) return the element multiset
        Object[] arr = orig.toArray();
        List<String> sortedArr = new ArrayList<>();
        for (Object o : arr) sortedArr.add((String) o);
        Collections.sort(sortedArr);
        check(sortedArr.equals(Arrays.asList("a", "b", "c")), "toArray() multiset");

        String[] tarr = orig.toArray(new String[0]);
        check(tarr.length == 3, "toArray(T[]) length");
        List<String> sortedTarr = new ArrayList<>(Arrays.asList(tarr));
        Collections.sort(sortedTarr);
        check(sortedTarr.equals(Arrays.asList("a", "b", "c")), "toArray(T[]) multiset");
    }

    // ---- contract deep-dive (constructor matrix / null / write channels /
    //      fail-fast / toArray / cross-impl equals / clone / serialization /
    //      spliterator / state invariants) ------------------------------------

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    @SuppressWarnings("unchecked")
    static <E> HashSet<E> roundTrip(HashSet<E> set) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(set); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (HashSet<E>) ois.readObject();
        }
    }

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new HashSet<>(-1), "HashSet(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> new HashSet<>(-1, 0.75f), "HashSet(-1,lf) IAE");
        checkThrows(IllegalArgumentException.class, () -> new HashSet<>(1, 0f), "HashSet loadFactor 0 IAE");
        checkThrows(IllegalArgumentException.class, () -> new HashSet<>(1, -0.5f), "HashSet negative loadFactor IAE");
        checkThrows(IllegalArgumentException.class, () -> new HashSet<>(1, Float.NaN), "HashSet NaN loadFactor IAE");
        checkThrows(NullPointerException.class, () -> new HashSet<>((Collection<String>) null), "HashSet(null Collection) NPE");

        HashSet<String> zero = new HashSet<>(0);
        zero.add("a");
        check(zero.contains("a"), "HashSet(0) usable");
        HashSet<String> sized = new HashSet<>(16, 0.5f);
        sized.add("x");
        check(sized.contains("x"), "HashSet(int,float) usable");
    }

    static void testNullSemantics() {
        HashSet<String> s = new HashSet<>();
        check(s.add(null), "add null returns true");
        check(!s.add(null), "add null duplicate returns false");
        check(s.contains(null), "contains null");
        check(s.remove(null), "remove null returns true");
        check(!s.remove(null), "remove absent null returns false");
    }

    static void testIteratorWriteChannels() {
        HashSet<Integer> s = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        Iterator<Integer> it = s.iterator();
        Integer first = it.next();
        it.remove();
        check(!s.contains(first), "iterator.remove removes element");
        checkThrows(IllegalStateException.class, it::remove, "remove before next ISE");

        Iterator<Integer> it2 = s.iterator();
        checkThrows(NoSuchElementException.class, () -> { while (it2.hasNext()) it2.next(); it2.next(); },
                    "exhausted iterator next NSEE");

        HashSet<Integer> r = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        r.removeAll(Arrays.asList(2, 4));
        check(r.size() == 3 && !r.contains(2) && !r.contains(4), "removeAll");
        r.retainAll(Arrays.asList(1, 3));
        check(r.size() == 2 && r.contains(1) && r.contains(3), "retainAll");
        check(r.removeIf(x -> x == 3), "removeIf returns true");
        check(!r.contains(3), "removeIf removed");
        r.clear();
        check(r.isEmpty(), "clear");
    }

    static void testFailFast() {
        HashSet<Integer> s = new HashSet<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = s.iterator();
        it.next();
        s.add(99);
        checkThrows(java.util.ConcurrentModificationException.class, it::next, "iterator CME after add");

        Iterator<Integer> it2 = s.iterator();
        it2.next();
        s.remove(99);
        checkThrows(java.util.ConcurrentModificationException.class, it2::next, "iterator CME after remove");

        Iterator<Integer> it3 = s.iterator();
        it3.next();
        s.clear();
        checkThrows(java.util.ConcurrentModificationException.class, it3::next, "iterator CME after clear");
    }

    static void testToArrayContract() {
        HashSet<String> s = new HashSet<>(Arrays.asList("a", "b"));
        Object[] oa = s.toArray();
        check(oa.length == 2, "toArray() length");
        List<String> sortedOa = new ArrayList<>();
        for (Object o : oa) sortedOa.add((String) o);
        Collections.sort(sortedOa);
        check(sortedOa.equals(Arrays.asList("a", "b")), "toArray() multiset");

        String[] exact = s.toArray(new String[0]);
        check(exact.length == 2, "toArray(new T[0]) exact length");

        String[] reusable = new String[]{"X", "X", "X"};
        String[] returned = s.toArray(reusable);
        check(returned == reusable, "toArray(T[]) reuses array when it fits");
        check(reusable[2] == null, "toArray(T[]) pads trailing null");
        List<String> sortedReuse = new ArrayList<>(Arrays.asList(reusable[0], reusable[1]));
        Collections.sort(sortedReuse);
        check(sortedReuse.equals(Arrays.asList("a", "b")), "toArray(T[]) content multiset");
    }

    static void testCrossImplEquals() {
        HashSet<Integer> hs = new HashSet<>(Arrays.asList(1, 2, 3));
        TreeSet<Integer> ts = new TreeSet<>(Arrays.asList(3, 1, 2));
        check(hs.equals(ts) && ts.equals(hs), "HashSet equals TreeSet symmetric");
        check(hs.hashCode() == ts.hashCode(), "equal sets equal hashCode");
        check(!hs.equals(new HashSet<>(Arrays.asList(1, 2))), "!equals different size");
    }

    static void testClone() {
        HashSet<String> s = new HashSet<>(Arrays.asList("a", "b"));
        @SuppressWarnings("unchecked")
        HashSet<String> clone = (HashSet<String>) s.clone();
        check(clone != s, "clone distinct object");
        check(clone.equals(s) && clone.hashCode() == s.hashCode(), "clone equals original");
        clone.add("c");
        check(s.size() == 2 && !s.contains("c"), "clone structural independence");
    }

    static void testSerialization() throws Exception {
        HashSet<String> s = new HashSet<>(Arrays.asList("a", null, "b"));
        HashSet<String> r = roundTrip(s);
        check(r != s, "round-trip distinct object");
        check(r.equals(s), "round-trip content equal");
        check(r.contains(null), "round-trip preserves null element");
        r.add("c");
        check(r.size() == 4 && s.size() == 3, "round-trip result usable and independent");

        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(HashSet.class);
        check(osc.getSerialVersionUID() == -5024744406713321676L, "HashSet serialVersionUID stable");
    }

    static void testSpliterator() {
        HashSet<String> s = new HashSet<>(Arrays.asList("a", "b", "c"));
        Spliterator<String> sp = s.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        check(sp.hasCharacteristics(Spliterator.SIZED | Spliterator.DISTINCT), "spliterator SIZED|DISTINCT");
        List<String> gathered = new ArrayList<>();
        sp.forEachRemaining(gathered::add);
        Collections.sort(gathered);
        check(gathered.equals(Arrays.asList("a", "b", "c")), "spliterator covers all (sorted)");
    }

    static void testStateInvariants() {
        HashSet<Integer> s = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        s.remove(3);
        check(s.size() == 4 && !s.contains(3), "remove updates size");
        check(s.contains(1) && s.contains(2) && s.contains(4) && s.contains(5), "others survive remove");
        check(!s.remove(99), "failed remove leaves state intact");
        check(s.size() == 4, "state intact after failed op");
        s.clear();
        check(s.isEmpty() && s.size() == 0, "clear empties");
        for (int i = 1; i <= 5; i++) check(!s.contains(i), "clear removed " + i);
    }

    // ---- contract deep-dive II: ctor messages / batch return values / hashCode
    //      formula / iteration growth / spliterator exact bits / serialization deep ------

    static String iaeMessage(ThrowingRunnable r) {
        try {
            r.run();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Throwable t) {
            throw new AssertionError("expected IllegalArgumentException but got " + t, t);
        }
        throw new AssertionError("expected IllegalArgumentException");
    }

    static void testCtorExceptionMessages() {
        String m1 = iaeMessage(() -> new HashSet<String>(-1));
        check(m1 != null && m1.startsWith("Illegal initial capacity"),
              "HashSet(-1) message 'Illegal initial capacity': " + m1);
        String m2 = iaeMessage(() -> new HashSet<String>(1, 0f));
        check(m2 != null && m2.startsWith("Illegal load factor"),
              "HashSet(1,0f) message 'Illegal load factor': " + m2);
        HashSet<String> fromSet = new HashSet<>(new TreeSet<>(Arrays.asList("a", "b")));
        check(fromSet.equals(new HashSet<>(Arrays.asList("a", "b"))),
              "HashSet(Collection from SortedSet) copies content");
        HashSet<String> fromSet2 = new HashSet<>(new HashSet<>(Arrays.asList("x")));
        check(fromSet2.contains("x"), "HashSet(Collection from HashSet) copies content");
        // JDK 19+ static factory equivalent to newHashSet(numInitialCapacity)
        HashSet<String> viaFactory2 = HashSet.newHashSet(4);
        viaFactory2.add("f");
        check(viaFactory2.contains("f"), "newHashSet(int) functional");
        check(HashSet.newHashSet(0).isEmpty(), "newHashSet(0) empty");
        checkThrows(IllegalArgumentException.class, () -> HashSet.newHashSet(-1),
                    "newHashSet(-1) IAE");
    }

    static void testBatchOpReturnContracts() {
        HashSet<String> s = new HashSet<>(Arrays.asList("a", "b"));
        check(!s.addAll(new ArrayList<String>()), "addAll(empty) returns false");
        check(!s.removeAll(Arrays.asList("z")), "removeAll(no match) returns false");
        check(!s.retainAll(Arrays.asList("a", "b", "c")), "retainAll(superset) returns false");
        check(!s.removeIf(x -> x.equals("zz")), "removeIf(no match) returns false");
        check(s.containsAll(new ArrayList<String>()), "containsAll(empty) returns true");
        check(!s.addAll(s), "addAll(self) returns false");
        check(s.size() == 2, "no-op batch operations leave size unchanged");
        checkThrows(NullPointerException.class, () -> s.addAll(null), "addAll(null) NPE");
        checkThrows(NullPointerException.class, () -> s.containsAll(null), "containsAll(null) NPE");
        checkThrows(NullPointerException.class, () -> s.removeAll(null), "removeAll(null) NPE");
        checkThrows(NullPointerException.class, () -> s.retainAll(null), "retainAll(null) NPE");
        HashSet<String> empty = new HashSet<>();
        empty.clear();
        check(empty.isEmpty(), "clear() on empty set is a no-op");
    }

    static void testHashCodeFormulaLock() {
        // Set.hashCode == sum of element hashCodes (AbstractSet)
        check(new HashSet<Integer>(Arrays.asList(1, 2, 3)).hashCode() == 6,
              "hashCode {1,2,3} == 1+2+3 == 6");
        check(new HashSet<String>(Arrays.asList("a", "b")).hashCode() == 97 + 98,
              "hashCode {'a','b'} == 97+98 == 195");
        check(new HashSet<String>(Arrays.asList(null, "a")).hashCode() == 0 + 97,
              "hashCode {null,'a'} == 0+97 == 97 (null hashes as 0)");
        check(new HashSet<String>().hashCode() == 0, "empty set hashCode == 0");
        HashSet<Integer> hs = new HashSet<>(Arrays.asList(1, 2, 3));
        check(hs.hashCode() == new TreeSet<>(Arrays.asList(1, 2, 3)).hashCode(),
              "Set hashCode formula identical across implementations");
        check(!new HashSet<>(Arrays.asList("a")).equals(Arrays.asList("a")),
              "Set.equals(List) is false (Set equals only Sets)");
        check(!hs.equals(Arrays.asList(1, 2, 3)), "HashSet.equals(List) false");
    }

    static void testIterationAndGrowth() {
        HashSet<Integer> s = new HashSet<>();
        for (int i = 0; i < 100; i++) s.add(i);
        check(s.size() == 100, "size after 100 adds");
        int containsAll = 0;
        for (int i = 0; i < 100; i++) if (s.contains(i)) containsAll++;
        check(containsAll == 100, "all 100 elements found after rehash-free growth");

        final int[] forEachCount = {0};
        s.forEach(x -> forEachCount[0]++);
        check(forEachCount[0] == 100, "forEach visits every element exactly once");

        check(s.stream().count() == 100, "stream().count() == size");
        java.util.Set<Integer> filtered = s.stream().filter(x -> x < 10)
                .collect(java.util.stream.Collectors.toSet());
        check(filtered.equals(new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))),
              "stream filter content multiset");

        java.util.Set<Integer> viaParallel = s.parallelStream()
                .collect(java.util.stream.Collectors.toSet());
        check(viaParallel.equals(s), "parallelStream collect multiset equals original");

        Iterator<Integer> it = s.iterator();
        Integer taken = it.next();
        List<Integer> rest = new ArrayList<>();
        it.forEachRemaining(rest::add);
        check(rest.size() == 99, "forEachRemaining visits only remaining 99");
        check(!rest.contains(taken), "forEachRemaining skips element already consumed by next()");
        HashSet<Integer> union = new HashSet<>(rest);
        union.add(taken);
        check(union.equals(s), "remaining + consumed covers exactly the original multiset");
    }

    static void testSpliteratorDeep() {
        HashSet<Integer> s = new HashSet<>();
        for (int i = 0; i < 64; i++) s.add(i);
        Spliterator<Integer> sp = s.spliterator();
        check(sp.characteristics() == (Spliterator.SIZED | Spliterator.DISTINCT),
              "HashSet spliterator exact characteristics == SIZED|DISTINCT (0x41)");
        check(sp.getExactSizeIfKnown() == 64, "getExactSizeIfKnown == size");

        Spliterator<Integer> front = sp.trySplit();
        check(front != null, "trySplit returns non-null for 64 elements");
        List<Integer> a = new ArrayList<>(), b = new ArrayList<>();
        front.forEachRemaining(a::add);
        sp.forEachRemaining(b::add);
        check(a.size() + b.size() == 64, "split halves sizes sum to original size");
        a.addAll(b);
        Collections.sort(a);
        List<Integer> expect = new ArrayList<>();
        for (int i = 0; i < 64; i++) expect.add(i);
        check(a.equals(expect), "split halves multiset union == original multiset");

        Spliterator<Integer> single = new HashSet<>(Arrays.asList(7)).spliterator();
        final boolean[] advanced = {false};
        check(single.tryAdvance(x -> advanced[0] = x == 7), "tryAdvance returns true and visits element");
        check(advanced[0], "tryAdvance consumer saw the element");
        check(!single.tryAdvance(x -> { }), "second tryAdvance returns false when exhausted");
        check(single.hasCharacteristics(Spliterator.SIZED | Spliterator.DISTINCT),
              "characteristics unchanged after draining");
    }

    static void testSerializationDeep() throws Exception {
        HashSet<String> empty = roundTrip(new HashSet<String>());
        check(empty.isEmpty(), "empty set round-trip stays empty");
        check(empty.add("a") && empty.contains("a"), "deserialized empty set usable");

        // custom serializable element class: content equality survives round-trip
        HashSet<Box> boxes = new HashSet<>();
        boxes.add(new Box(1));
        boxes.add(new Box(2));
        HashSet<Box> back = roundTrip(boxes);
        check(back.equals(boxes) && back.size() == 2, "custom Serializable elements round-trip");
        check(back.contains(new Box(1)) && back.contains(new Box(2)),
              "deserialized set finds equal-but-distinct element instances");
    }

    static class Box implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        final int v;
        Box(int v) { this.v = v; }
        @Override public boolean equals(Object o) { return o instanceof Box && ((Box) o).v == v; }
        @Override public int hashCode() { return v; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testAddRemoveContains();
        testDedupByEqualsHashCode();
        testCollectionOps();
        testEqualsHashCode();
        testIterationAndFailFast();
        testBlackBox();
        testConstructorMatrix();
        testNullSemantics();
        testIteratorWriteChannels();
        testFailFast();
        testToArrayContract();
        testCrossImplEquals();
        testClone();
        testSerialization();
        testSpliterator();
        testStateInvariants();
        testCtorExceptionMessages();
        testBatchOpReturnContracts();
        testHashCodeFormulaLock();
        testIterationAndGrowth();
        testSpliteratorDeep();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
