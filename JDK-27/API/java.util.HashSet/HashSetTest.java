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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testAddRemoveContains();
        testDedupByEqualsHashCode();
        testCollectionOps();
        testEqualsHashCode();
        testIterationAndFailFast();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
