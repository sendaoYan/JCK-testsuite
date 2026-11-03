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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testFactories();
        testAddRemoveIteration();
        testCollectionOpsAndClone();
        testNullRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
