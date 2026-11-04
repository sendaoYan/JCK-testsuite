/*
 * @test
 * @summary java.util.EnumSet 一致性：白盒（反射结构）+ 黑盒（工厂 + 声明序迭代 + 集合运算）。
 * @run main EnumSetTest
 *
 * 白盒（结构）：Class.forName 加载、抽象类（isAbstract）、继承 AbstractSet、实现 Cloneable/
 *   Serializable、静态工厂 allOf/noneOf/of/range/copyOf/complementOf 成员形态。
 * 黑盒（行为）设计案：
 * 1. 契约：EnumSet 是枚举集合，内部以位向量实现，迭代序恒等于枚举声明序（ordinal）；
 *    元素不能为 null（NPE）；提供 allOf/noneOf/of/range/copyOf/complementOf 工厂。
 * 2. 场景：noneOf -> add 乱序 -> 校验声明序迭代 -> of/range/complementOf/copyOf 工厂 ->
 *    addAll/removeAll/retainAll -> clone。
 * 3. 组合链路：工厂构造 + 增删 -> 声明序迭代 -> 集合运算 -> 补集还原 -> clone 独立性。
 * 4. 不变量：迭代序恒等于枚举声明序；complementOf 两次还原原集；equals 与顺序无关。
 * 5. 边界/异常：add(null) 抛 NPE；contains/remove(null) 返回 false；of 重复元素静默去重；
 *    range(from,to) from>to 抛 IAE；copyOf(空 Collection) 抛 IAE。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;

public class EnumSetTest {
    enum Color { RED, GREEN, BLUE, YELLOW }

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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.EnumSet");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "not interface/enum/annotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.util.AbstractSet"), "extends AbstractSet");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(hasMethod(c, "allOf", Class.class), "allOf(Class)");
        check(hasMethod(c, "noneOf", Class.class), "noneOf(Class)");
        check(hasMethod(c, "range", Enum.class, Enum.class), "range(E,E)");
        check(hasMethod(c, "of", Enum.class, Enum[].class), "of(E,E...)");
        check(hasMethod(c, "complementOf", EnumSet.class), "complementOf(EnumSet)");
        check(hasMethod(c, "copyOf", Collection.class), "copyOf(Collection)");
        check(hasMethod(c, "add", Object.class) && hasMethod(c, "remove", Object.class)
              && hasMethod(c, "contains", Object.class), "add/remove/contains");
        check(hasMethod(c, "clone"), "clone");
    }

    static void testFactories() {
        EnumSet<Color> all = EnumSet.allOf(Color.class);
        check(all.size() == 4 && all.contains(Color.RED) && all.contains(Color.YELLOW), "allOf");
        check(new ArrayList<>(all).equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)),
              "allOf iteration in declaration order");

        EnumSet<Color> none = EnumSet.noneOf(Color.class);
        check(none.isEmpty(), "noneOf empty");

        EnumSet<Color> of = EnumSet.of(Color.RED, Color.BLUE);
        check(of.size() == 2 && of.contains(Color.RED) && of.contains(Color.BLUE), "of");
        check(new ArrayList<>(of).equals(Arrays.asList(Color.RED, Color.BLUE)),
              "of iteration in declaration order");

        EnumSet<Color> range = EnumSet.range(Color.GREEN, Color.BLUE);
        check(range.size() == 2 && range.contains(Color.GREEN) && range.contains(Color.BLUE),
              "range inclusive");
        check(new ArrayList<>(range).equals(Arrays.asList(Color.GREEN, Color.BLUE)),
              "range iteration order");

        EnumSet<Color> comp = EnumSet.complementOf(EnumSet.of(Color.RED));
        check(comp.size() == 3 && !comp.contains(Color.RED), "complementOf");
        check(EnumSet.complementOf(comp).equals(EnumSet.of(Color.RED)),
              "complementOf twice restores");

        EnumSet<Color> copied = EnumSet.copyOf(of);
        check(copied.equals(of) && copied != of, "copyOf(EnumSet)");

        Collection<Color> list = Arrays.asList(Color.YELLOW, Color.RED);
        EnumSet<Color> fromColl = EnumSet.copyOf(list);
        check(fromColl.equals(EnumSet.of(Color.RED, Color.YELLOW)), "copyOf(Collection)");

        // of 传重复元素：静默去重（不抛异常）
        EnumSet<Color> dup = EnumSet.of(Color.RED, Color.GREEN, Color.RED);
        check(dup.size() == 2 && dup.contains(Color.RED) && dup.contains(Color.GREEN),
              "of varargs silently dedups duplicates");
        // range 反向 -> IAE
        expectThrows(() -> EnumSet.range(Color.BLUE, Color.RED), IllegalArgumentException.class,
                     "range from>to throws IAE");
        // copyOf 空集合 -> IAE
        expectThrows(() -> EnumSet.copyOf(new ArrayList<Color>()), IllegalArgumentException.class,
                     "copyOf(empty) throws IAE");
    }

    static void testAddRemoveIteration() {
        EnumSet<Color> set = EnumSet.noneOf(Color.class);
        check(set.add(Color.BLUE) && set.add(Color.RED), "add returns true");
        check(!set.add(Color.RED), "add duplicate returns false");
        check(set.contains(Color.RED) && !set.contains(Color.GREEN), "contains");
        check(set.remove(Color.RED) && !set.contains(Color.RED), "remove");

        set.clear();
        set.add(Color.YELLOW);
        set.add(Color.RED);
        set.add(Color.GREEN);
        check(new ArrayList<>(set).equals(Arrays.asList(Color.RED, Color.GREEN, Color.YELLOW)),
              "iteration in declaration order regardless of insert order");
        check(set.size() == 3, "size");
    }

    static void testCollectionOpsAndClone() {
        EnumSet<Color> a = EnumSet.of(Color.RED, Color.GREEN);
        EnumSet<Color> b = EnumSet.of(Color.GREEN, Color.BLUE);
        check(a.containsAll(EnumSet.of(Color.RED)), "containsAll");
        a.addAll(b);
        check(a.equals(EnumSet.of(Color.RED, Color.GREEN, Color.BLUE)), "addAll union");
        a.retainAll(b);
        check(a.equals(EnumSet.of(Color.GREEN, Color.BLUE)), "retainAll intersection");
        a.removeAll(b);
        check(a.isEmpty(), "removeAll");

        EnumSet<Color> orig = EnumSet.of(Color.RED, Color.YELLOW);
        EnumSet<Color> cloned = orig.clone();
        check(cloned.equals(orig) && cloned != orig, "clone copies");
        cloned.add(Color.GREEN);
        check(!orig.contains(Color.GREEN), "clone independent");
    }

    static void testNullRejection() {
        EnumSet<Color> set = EnumSet.noneOf(Color.class);
        expectThrows(() -> set.add(null), NullPointerException.class, "add null");
        // contains/remove(null) 不抛异常，而是优雅返回 false（javadoc 约定）
        check(!set.contains(null), "contains null returns false");
        check(!set.remove(null), "remove null returns false");
    }

    @SuppressWarnings("unchecked")
    static <E extends Enum<E>> EnumSet<E> roundTrip(EnumSet<E> s) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(s); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (EnumSet<E>) ois.readObject();
        }
    }

    // ---- contract deep-dive (factory edge cases / serialization /
    //      cross-impl equals / state invariants) -----------------------------

    static void testFactoryEdgeCases() {
        check(EnumSet.range(Color.RED, Color.RED).equals(EnumSet.of(Color.RED)),
              "range(from==to) single element");
        expectThrows(() -> EnumSet.range(null, Color.RED), NullPointerException.class, "range(null from) NPE");
        expectThrows(() -> EnumSet.range(Color.RED, null), NullPointerException.class, "range(null to) NPE");
        check(EnumSet.complementOf(EnumSet.noneOf(Color.class)).equals(EnumSet.allOf(Color.class)),
              "complementOf(noneOf) == allOf");
        check(EnumSet.complementOf(EnumSet.allOf(Color.class)).isEmpty(),
              "complementOf(allOf) == noneOf");
        expectThrows(() -> EnumSet.copyOf((Collection<Color>) null), NullPointerException.class,
                     "copyOf(null Collection) NPE");
        expectThrows(() -> EnumSet.allOf(null), NullPointerException.class, "allOf(null) NPE");
        expectThrows(() -> EnumSet.noneOf(null), NullPointerException.class, "noneOf(null) NPE");
    }

    static void testSerialization() throws Exception {
        EnumSet<Color> s = EnumSet.of(Color.RED, Color.GREEN, Color.BLUE);
        EnumSet<Color> r = roundTrip(s);
        check(r != s && r.equals(s), "round-trip distinct and equal");
        r.add(Color.YELLOW);
        check(r.size() == 4 && s.size() == 3, "round-trip result independent");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(EnumSet.class);
        check(osc.getSerialVersionUID() == 1009687484059888093L, "EnumSet serialVersionUID stable");
    }

    static void testCrossImplEquals() {
        EnumSet<Color> es = EnumSet.of(Color.RED, Color.GREEN, Color.BLUE);
        HashSet<Color> hs = new HashSet<>(Arrays.asList(Color.BLUE, Color.GREEN, Color.RED));
        check(es.equals(hs) && hs.equals(es), "EnumSet/HashSet equals order-independent");
        check(es.hashCode() == hs.hashCode(), "hashCode equal across implementations");
        hs.add(Color.YELLOW);
        check(!es.equals(hs), "!equals different content");
    }

    static void testStateInvariants() {
        EnumSet<Color> s = EnumSet.of(Color.RED, Color.GREEN, Color.BLUE);
        expectThrows(() -> s.add(null), NullPointerException.class, "failed add leaves state");
        check(s.size() == 3 && s.contains(Color.RED) && s.contains(Color.GREEN) && s.contains(Color.BLUE),
              "state intact after failed add");
        s.clear();
        check(s.isEmpty() && s.size() == 0, "clear empties");
        s.add(Color.YELLOW);
        check(s.size() == 1 && s.contains(Color.YELLOW), "reusable after clear");
    }

    static void testBlackBox() {
        EnumSet<Color> set = EnumSet.of(Color.RED, Color.BLUE, Color.GREEN);
        java.util.Spliterator<Color> sp = set.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        check(sp.hasCharacteristics(java.util.Spliterator.SIZED | java.util.Spliterator.SUBSIZED),
              "spliterator reports SIZED|SUBSIZED");
        java.util.List<Color> gathered = new java.util.ArrayList<>();
        sp.forEachRemaining(gathered::add);
        check(gathered.equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE)),
              "spliterator iterates in declaration order");
    }

    // ---- contract deep-dive II: of() arities+null / copyOf typing / addAll typing /
    //      empty enum / iterator channels / cross-type equals / varargs aliasing /
    //      batch returns / spliterator exact bits / huge enum / serialization deep ----

    enum Size { S, M }
    enum Empty {}
    enum Big {
        B00,B01,B02,B03,B04,B05,B06,B07,B08,B09,B10,B11,B12,B13,B14,B15,B16,B17,B18,B19,
        B20,B21,B22,B23,B24,B25,B26,B27,B28,B29,B30,B31,B32,B33,B34,B35,B36,B37,B38,B39,
        B40,B41,B42,B43,B44,B45,B46,B47,B48,B49,B50,B51,B52,B53,B54,B55,B56,B57,B58,B59,
        B60,B61,B62,B63,B64,B65,B66,B67,B68,B69
    }

    static void testOfAritiesAndNull() {
        check(EnumSet.of(Color.RED).size() == 1, "of(E) single");
        check(EnumSet.of(Color.RED, Color.GREEN).size() == 2, "of(E,E)");
        check(EnumSet.of(Color.RED, Color.GREEN, Color.BLUE).size() == 3, "of(E,E,E)");
        check(EnumSet.of(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW).size() == 4, "of(E,E,E,E)");
        check(EnumSet.of(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED).size() == 4,
              "of(E,E,E,E,E) five-arg overload dedups");
        expectThrows(() -> EnumSet.of(null), NullPointerException.class, "of(null) NPE");
        expectThrows(() -> EnumSet.of(Color.RED, (Color[]) null),
                     NullPointerException.class, "of(e, null rest array) NPE");
        expectThrows(() -> EnumSet.of(Color.RED, new Color[]{Color.BLUE, null}),
                     NullPointerException.class, "of(e, rest containing null) NPE");
    }

    static void testCopyOfTyping() {
        @SuppressWarnings("unchecked")
        Collection<Color> bad = (Collection<Color>) (Collection<?>) Arrays.asList("not-a-color");
        expectThrows(() -> EnumSet.copyOf(bad), ClassCastException.class,
                     "copyOf(Collection of wrong type) CCE");
        expectThrows(() -> EnumSet.copyOf(Arrays.asList(Color.RED, null)),
                     NullPointerException.class, "copyOf(Collection containing null) NPE");
        EnumSet<Color> fromHashSet = EnumSet.copyOf(new HashSet<>(Arrays.asList(Color.BLUE, Color.RED)));
        check(fromHashSet.equals(EnumSet.of(Color.RED, Color.BLUE)),
              "copyOf(HashSet) infers enum type from elements");
        check(new ArrayList<>(fromHashSet).equals(Arrays.asList(Color.RED, Color.BLUE)),
              "copyOf result iterates in declaration order");
        EnumSet<Color> copy = EnumSet.copyOf(EnumSet.range(Color.GREEN, Color.YELLOW));
        check(copy.add(Color.RED) && copy.equals(EnumSet.allOf(Color.class)),
              "copyOf(EnumSet) copy is independent");
    }

    static void testAddAllTyping() {
        EnumSet<Color> s = EnumSet.noneOf(Color.class);
        check(s.addAll(Arrays.asList(Color.RED, Color.BLUE)), "addAll(plain List) true");
        check(s.equals(EnumSet.of(Color.RED, Color.BLUE)), "addAll(plain List) adds elements");
        @SuppressWarnings("unchecked")
        Collection<Color> bad = (Collection<Color>) (Collection<?>) Arrays.asList("x");
        expectThrows(() -> EnumSet.noneOf(Color.class).addAll(bad), ClassCastException.class,
                     "addAll(wrong element type) CCE");
        expectThrows(() -> EnumSet.noneOf(Color.class).addAll(Arrays.asList(Color.RED, null)),
                     NullPointerException.class, "addAll(collection containing null) NPE");
    }

    static void testEmptyEnumUniverse() {
        EnumSet<Empty> none = EnumSet.noneOf(Empty.class);
        check(none.isEmpty() && none.size() == 0, "noneOf(empty enum) empty");
        check(!none.iterator().hasNext(), "empty-universe iterator hasNext false");
        check(EnumSet.allOf(Empty.class).isEmpty(), "allOf(empty enum) empty");
        check(none.clone().isEmpty(), "clone of empty-universe set empty");
        expectThrows(() -> EnumSet.copyOf(new ArrayList<Empty>()),
                     IllegalArgumentException.class, "copyOf(empty) IAE");
    }

    static void testIteratorChannels() {
        EnumSet<Color> s = EnumSet.of(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        java.util.Iterator<Color> it = s.iterator();
        check(it.next() == Color.RED, "iterator starts at lowest ordinal");
        it.remove();
        check(!s.contains(Color.RED) && s.size() == 3, "iterator.remove removes");
        check(it.next() == Color.GREEN, "iteration continues in ordinal order after remove");
        expectThrows(() -> s.iterator().remove(), IllegalStateException.class,
                     "fresh iterator remove before next ISE");
        java.util.List<Color> rest = new ArrayList<>();
        it.forEachRemaining(rest::add);
        check(rest.equals(Arrays.asList(Color.BLUE, Color.YELLOW)),
              "forEachRemaining visits remaining in ordinal order");

        // EnumSet iterators are NOT fail-fast: they snapshot the membership mask at
        // creation, so a structural add during iteration neither throws CME nor is
        // observed by the ongoing iterator (verified identical on JDK-8 and JDK-27).
        EnumSet<Color> mod = EnumSet.of(Color.RED, Color.GREEN);
        java.util.Iterator<Color> it2 = mod.iterator();
        it2.next();
        mod.add(Color.BLUE);
        java.util.List<Color> remaining = new ArrayList<>();
        it2.forEachRemaining(remaining::add);
        check(remaining.equals(Arrays.asList(Color.GREEN)),
              "iterator unaffected by concurrent add (no CME, new element not visible)");
        java.util.List<Color> fresh = new ArrayList<>();
        for (Color c : mod) fresh.add(c);
        check(fresh.equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE)),
              "fresh iterator sees the element added during previous iteration");
    }

    static void testCrossTypeEquals() {
        check(!EnumSet.of(Color.RED).equals(EnumSet.of(Size.S)),
              "EnumSets of different enum types are not equal (same size)");
        check(EnumSet.of(Color.RED, Color.GREEN).equals(EnumSet.of(Color.GREEN, Color.RED)),
              "equals independent of iteration/insert order");
    }

    static void testVarargsNotAliased() {
        Color[] rest = {Color.GREEN, Color.BLUE};
        EnumSet<Color> s = EnumSet.of(Color.RED, rest);
        rest[0] = Color.YELLOW;
        check(s.equals(EnumSet.of(Color.RED, Color.GREEN, Color.BLUE)),
              "of() does not alias the caller's varargs array");
    }

    static void testBatchReturnValues() {
        EnumSet<Color> s = EnumSet.of(Color.RED, Color.GREEN);
        check(!s.addAll(Arrays.asList(Color.RED)), "addAll(all duplicates) false");
        check(!s.removeAll(Arrays.asList(Color.BLUE)), "removeAll(no match) false");
        check(!s.retainAll(EnumSet.allOf(Color.class)), "retainAll(superset) false");
        check(!s.removeIf(c -> c == Color.BLUE), "removeIf(no match) false");
        check(s.containsAll(new ArrayList<Color>()), "containsAll(empty) true");
        check(!s.containsAll(EnumSet.allOf(Color.class)), "containsAll(bigger) false");
        expectThrows(() -> s.containsAll(null), NullPointerException.class, "containsAll(null) NPE");
        expectThrows(() -> s.addAll(null), NullPointerException.class, "addAll(null) NPE");
        expectThrows(() -> s.removeAll(null), NullPointerException.class, "removeAll(null) NPE");
        expectThrows(() -> s.retainAll(null), NullPointerException.class, "retainAll(null) NPE");
    }

    static void testSpliteratorExact() {
        EnumSet<Color> s = EnumSet.of(Color.RED, Color.GREEN, Color.BLUE);
        java.util.Spliterator<Color> sp = s.spliterator();
        // JDK 9+ EnumSet spliterator reports ORDERED|SORTED|NONNULL in addition to
        // SIZED|SUBSIZED|DISTINCT (0x4155). The JDK-8 variant locks the narrower
        // 0x4041 mask instead -- this is a documented JDK-8 vs JDK-27 difference.
        check(sp.characteristics() == (java.util.Spliterator.ORDERED
                | java.util.Spliterator.DISTINCT | java.util.Spliterator.SORTED
                | java.util.Spliterator.SIZED | java.util.Spliterator.NONNULL
                | java.util.Spliterator.SUBSIZED),
              "JDK-27 EnumSet spliterator exact characteristics ORDERED|DISTINCT|SORTED|SIZED|NONNULL|SUBSIZED (0x4155)");
        check(sp.getExactSizeIfKnown() == 3, "getExactSizeIfKnown == size");
        check(sp.hasCharacteristics(java.util.Spliterator.SORTED), "SORTED reported");
        check(sp.getComparator() == null,
              "SORTED with null comparator means natural (declaration) order");
        java.util.List<Color> gathered = new ArrayList<>();
        sp.forEachRemaining(gathered::add);
        check(gathered.equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE)),
              "spliterator visits elements in declaration order");
    }

    static void testHugeEnumUniverse() {
        // > 64 constants exercises the non-Regular EnumSet implementation
        EnumSet<Big> range = EnumSet.range(Big.B05, Big.B66);
        check(range.size() == 62, "huge-universe range size inclusive both ends");
        check(range.contains(Big.B05) && range.contains(Big.B66), "range endpoints present");
        check(!range.contains(Big.B04) && !range.contains(Big.B67), "range excludes neighbors");
        java.util.List<Big> seq = new ArrayList<>();
        java.util.Iterator<Big> it = range.iterator();
        while (it.hasNext()) seq.add(it.next());
        check(seq.size() == 62 && seq.get(0) == Big.B05 && seq.get(61) == Big.B66,
              "huge-universe iteration follows declaration order endpoints");
        it = range.iterator();
        it.next();
        it.remove();
        check(!range.contains(Big.B05) && range.size() == 61, "huge-universe iterator.remove");
        EnumSet<Big> comp = EnumSet.complementOf(range);
        check(comp.size() == 9, "huge-universe complementOf size");
        check(comp.contains(Big.B00) && comp.contains(Big.B69), "complement contains outside elements");
        check(EnumSet.complementOf(comp).equals(range), "double complement restores");
    }

    static void testSerializationDeep() throws Exception {
        EnumSet<Color> s = EnumSet.of(Color.YELLOW, Color.RED);
        EnumSet<Color> r = roundTrip(s);
        check(new ArrayList<>(r).equals(Arrays.asList(Color.RED, Color.YELLOW)),
              "round-trip iterates in declaration order regardless of fill order");
        r.add(Color.GREEN);
        check(r.equals(EnumSet.of(Color.RED, Color.GREEN, Color.YELLOW)),
              "round-trip result still ordinal-ordered after add");
        EnumSet<Color> empty = roundTrip(EnumSet.noneOf(Color.class));
        check(empty.isEmpty() && empty.add(Color.BLUE), "round-trip of empty set usable");
        EnumSet<Big> big = EnumSet.range(Big.B00, Big.B69);
        EnumSet<Big> bigBack = roundTrip(big);
        check(bigBack.equals(big) && bigBack.size() == 70, "huge-universe set round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testFactories();
        testAddRemoveIteration();
        testCollectionOpsAndClone();
        testNullRejection();
        testBlackBox();
        testFactoryEdgeCases();
        testSerialization();
        testCrossImplEquals();
        testStateInvariants();
        testOfAritiesAndNull();
        testCopyOfTyping();
        testAddAllTyping();
        testEmptyEnumUniverse();
        testIteratorChannels();
        testCrossTypeEquals();
        testVarargsNotAliased();
        testBatchReturnValues();
        testSpliteratorExact();
        testHugeEnumUniverse();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
