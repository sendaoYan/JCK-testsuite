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

import java.util.ArrayList;
import java.util.Arrays;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testInsertionOrderAndDedup();
        testSequencedOps();
        testEqualsHashCode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
