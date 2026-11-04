/*
 * @test
 * @summary java.util.LinkedHashSet 一致性：白盒（反射结构）+ 黑盒（插入序 + SequencedSet）。
 * @run main LinkedHashSetTest
 *
 * 白盒（结构）：Class.forName 加载、继承 HashSet、实现 SequencedSet/Cloneable/Serializable、
 *   addFirst/addLast/getFirst/getLast/removeFirst/removeLast/reversed 成员形态。
 * 黑盒（行为）设计案：
 * 1. 契约：LinkedHashSet 是保留插入序的 Set（元素唯一），允许 null，实现 SequencedSet，
 *    提供确定的首尾访问顺序。
 * 2. 场景：new LinkedHashSet() -> add 乱序但保持插入序 -> 重复 add 拒绝 -> addFirst/addLast
 *    -> getFirst/getLast/removeFirst/removeLast -> reversed 倒序视图。
 * 3. 组合链路：插入 a,b,c -> 重复插入 a 不变 -> addFirst z / addLast w -> 校验首尾 -> 移除首尾
 *    -> reversed 视图倒序 -> 重新 add 验证位置。
 * 4. 不变量：迭代顺序 == 插入顺序；等值 Set equals/hashCode 顺序无关；重复元素不改变顺序。
 * 5. 边界：null 允许并保持插入位置；add 重复返回 false。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.Set;

public class LinkedHashSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

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
    static <E> LinkedHashSet<E> roundTrip(LinkedHashSet<E> s) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(s); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (LinkedHashSet<E>) ois.readObject();
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.LinkedHashSet");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.util.HashSet"), "extends HashSet");
        check(SequencedSet.class.isAssignableFrom(c), "implements SequencedSet");
        check(Set.class.isAssignableFrom(c), "implements Set");
        check(hasMethod(c, "addFirst", Object.class), "addFirst");
        check(hasMethod(c, "addLast", Object.class), "addLast");
        check(hasMethod(c, "getFirst") && hasMethod(c, "getLast"), "getFirst/getLast");
        check(hasMethod(c, "removeFirst") && hasMethod(c, "removeLast"), "removeFirst/removeLast");
        check(hasMethod(c, "reversed"), "reversed");
    }

    static void testInsertionOrderAndDedup() {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        check(s.add("a") && s.add("b") && s.add("c"), "add");
        check(!s.add("a"), "duplicate rejected");
        s.add(null);
        check(new ArrayList<>(s).equals(Arrays.asList("a", "b", "c", null)),
              "insertion order incl. null");
        check(s.size() == 4, "size");
        check(s.contains("a") && s.contains(null), "contains");
        check(s.remove("b") && !s.contains("b"), "remove");
        check(new ArrayList<>(s).equals(Arrays.asList("a", "c", null)), "order after remove");
    }

    static void testSequencedOps() {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        s.add("a");
        s.add("b");
        s.add("c");
        s.addFirst("z");
        s.addLast("w");
        check(new ArrayList<>(s).equals(Arrays.asList("z", "a", "b", "c", "w")),
              "addFirst/addLast position");
        check(s.getFirst().equals("z") && s.getLast().equals("w"), "getFirst/getLast");
        check(s.removeFirst().equals("z"), "removeFirst");
        check(s.removeLast().equals("w"), "removeLast");
        check(new ArrayList<>(s).equals(Arrays.asList("a", "b", "c")), "order after remove first/last");
        s.addFirst("c");
        check(new ArrayList<>(s).equals(Arrays.asList("c", "a", "b")), "addFirst repositions existing");
        check(new ArrayList<>(s.reversed()).equals(Arrays.asList("b", "a", "c")), "reversed view");
    }

    static void testEqualsHashCode() {
        LinkedHashSet<String> a = new LinkedHashSet<>(Arrays.asList("x", "y"));
        LinkedHashSet<String> b = new LinkedHashSet<>(Arrays.asList("y", "x"));
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode order-independent");
        b.add("z");
        check(!a.equals(b), "!equals different content");
    }

    // ---- contract deep-dive (constructor matrix / clone / serialization /
    //      cross-impl equals) ------------------------------------------------

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new LinkedHashSet<String>(-1),
                    "LinkedHashSet(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> new LinkedHashSet<String>(1, 0.0f),
                    "LinkedHashSet(1, 0.0f) IAE");
        checkThrows(IllegalArgumentException.class, () -> new LinkedHashSet<String>(1, Float.NaN),
                    "LinkedHashSet(1, NaN) IAE");
        checkThrows(NullPointerException.class, () -> new LinkedHashSet<String>((Collection<String>) null),
                    "LinkedHashSet(null Collection) NPE");
        LinkedHashSet<String> copy = new LinkedHashSet<>(Arrays.asList("c", "a", "b"));
        check(new ArrayList<>(copy).equals(Arrays.asList("c", "a", "b")),
              "LinkedHashSet(Collection) preserves order");
    }

    static void testClone() {
        LinkedHashSet<String> s = new LinkedHashSet<>(Arrays.asList("a", "b"));
        @SuppressWarnings("unchecked")
        LinkedHashSet<String> clone = (LinkedHashSet<String>) s.clone();
        check(clone != s && clone.equals(s), "clone distinct and equal");
        clone.add("c");
        check(clone.size() == 3 && s.size() == 2, "clone structural independence");
    }

    static void testSerialization() throws Exception {
        LinkedHashSet<String> s = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
        LinkedHashSet<String> r = roundTrip(s);
        check(r != s && r.equals(s), "round-trip distinct and equal");
        check(new ArrayList<>(r).equals(Arrays.asList("a", "b", "c")), "round-trip preserves order");
        r.add("d");
        check(r.size() == 4 && s.size() == 3, "round-trip result independent");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(LinkedHashSet.class);
        check(osc.getSerialVersionUID() == -2851667679971038690L, "LinkedHashSet serialVersionUID stable");
    }

    static void testCrossImplEquals() {
        LinkedHashSet<String> lhs = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
        HashSet<String> hs = new HashSet<>(Arrays.asList("c", "a", "b"));
        check(lhs.equals(hs) && hs.equals(lhs), "LinkedHashSet/HashSet equals order-independent");
        check(lhs.hashCode() == hs.hashCode(), "hashCode equal across implementations");
        hs.add("d");
        check(!lhs.equals(hs), "!equals different content");
    }

    static void testBlackBox() {
        LinkedHashSet<String> a = new LinkedHashSet<>(16, 0.75f);
        a.add("x");
        check(a.contains("x"), "ctor (int,float) works");
        LinkedHashSet<String> b = new LinkedHashSet<>(8);
        b.add("y");
        check(b.contains("y"), "ctor (int) works");

        LinkedHashSet<String> viaFactory = LinkedHashSet.newLinkedHashSet(8);
        viaFactory.add("z");
        check(viaFactory.contains("z"), "newLinkedHashSet(int) factory");

        LinkedHashSet<String> s = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
        java.util.Spliterator<String> sp = s.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        check(sp.hasCharacteristics(java.util.Spliterator.ORDERED | java.util.Spliterator.DISTINCT),
              "spliterator characteristics ORDERED|DISTINCT");
        java.util.List<String> gathered = new java.util.ArrayList<>();
        sp.forEachRemaining(gathered::add);
        check(gathered.equals(Arrays.asList("a", "b", "c")), "spliterator preserves insertion order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testInsertionOrderAndDedup();
        testSequencedOps();
        testEqualsHashCode();
        testBlackBox();
        testConstructorMatrix();
        testClone();
        testSerialization();
        testCrossImplEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
