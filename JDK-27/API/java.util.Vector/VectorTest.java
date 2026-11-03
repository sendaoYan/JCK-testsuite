/*
 * @test
 * @summary java.util.Vector conformance. White-box: Class.forName + superclass/interface/
 *          member shape. Black-box: capacity growth (initialCapacity + capacityIncrement),
 *          add/get/set/remove + indexOf/lastIndexOf(fromIndex)/contains, removeAll/retainAll,
 *          toArray/copyInto, legacy elementAt/firstElement/lastElement/addElement/
 *          insertElementAt/removeElementAt/removeElement/elements, setSize, equals/hashCode,
 *          subList view, and iterator fail-fast.
 * @run main VectorTest
 *
 * 设计案：
 * 1. 契约：Vector 是线程安全（方法 synchronized）、可动态扩容、可含 null 的 List；
 *    默认容量 10，扩容按 capacityIncrement（>0 时）否则翻倍。
 * 2. 用户场景：new Vector(int,int) → add/addElement 建表 → get/elementAt 查询 →
 *    indexOf(fromIndex)/lastIndexOf 定位 → remove/removeElement 删除 → copyInto 拷出 →
 *    elements() 枚举 → setSize 调整大小 → subList 取视图。
 * 3. 组合流程：见各 test*，均为「构造 → 多步组合 → 断言」。
 * 4. 不变量：元素顺序 == 插入顺序；equals/hashCode 满足 List 契约；capacity() >= size()。
 * 5. 边界/异常：elementAt/get/set/remove(int) 越界抛 ArrayIndexOutOfBoundsException；
 *    结构修改后 iterator/subList 抛 ConcurrentModificationException；
 *    copyInto 目标数组过小抛 ArrayIndexOutOfBoundsException；setSize 变大填 null、变小截断。
 */

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class VectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                passed++;
            } else {
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getName(), t);
            }
        }
    }

    // ---- white-box: 结构/反射断言 ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Vector");
        check(!c.isInterface(), "Vector is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "Vector public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Vector concrete");
        check(c.getSuperclass() == java.util.AbstractList.class, "extends AbstractList");
        check(java.util.List.class.isAssignableFrom(c), "implements List");
        check(java.util.RandomAccess.class.isAssignableFrom(c), "implements RandomAccess");
        check(java.lang.Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        c.getMethod("elementAt", int.class);
        c.getMethod("addElement", Object.class);
        c.getMethod("capacity");
        c.getMethod("elements");
        check(true, "key public methods present");
    }

    static void testCapacity() {
        Vector<String> v = new Vector<>();
        check(v.isEmpty() && v.size() == 0, "default ctor empty");
        check(v.capacity() == 10, "default capacity 10");

        Vector<String> g = new Vector<>(3, 5);
        check(g.capacity() == 3, "ctor (3,5) initial capacity 3");
        g.add("a");
        g.add("b");
        g.add("c");
        check(g.capacity() == 3, "capacity unchanged while size <= 3");
        g.add("d");
        check(g.capacity() == 8, "grow by capacityIncrement 5 => 8");

        g.ensureCapacity(50);
        check(g.capacity() >= 50, "ensureCapacity grows");
        g.trimToSize();
        check(g.capacity() == g.size(), "trimToSize shrinks to size");
    }

    static void testContractChain() {
        Vector<String> v = new Vector<>();
        v.add("a");
        v.add("b");
        v.add("a");
        v.add(null);
        check(v.equals(Arrays.asList("a", "b", "a", null)), "order incl null");
        check(v.get(1).equals("b") && v.get(3) == null, "get");
        check(v.set(1, "B").equals("b"), "set returns old");
        check(v.indexOf("a") == 0 && v.lastIndexOf("a") == 2, "indexOf/lastIndexOf");
        check(v.indexOf("a", 1) == 2, "indexOf(fromIndex)");
        check(v.lastIndexOf("a", 1) == 0, "lastIndexOf(fromIndex)");
        check(v.contains("B") && v.contains(null) && !v.contains("z"), "contains");

        v.removeAll(Arrays.asList("a"));
        check(v.equals(Arrays.asList("B", null)), "removeAll removes matching");
        v.addAll(Arrays.asList("x", "B"));
        check(v.retainAll(Arrays.asList("B")), "retainAll returns true");
        check(v.equals(Arrays.asList("B", "B")), "retainAll keeps matching");

        check(v.remove("B"), "remove(Object) returns true");
        check(v.remove(0).equals("B"), "remove(int) returns removed");
        check(v.isEmpty(), "empty after removes");
    }

    static void testLegacy() {
        Vector<String> v = new Vector<>();
        v.addElement("a");
        v.addElement("b");
        v.addElement("c");
        check(v.elementAt(1).equals("b"), "elementAt");
        check(v.firstElement().equals("a") && v.lastElement().equals("c"),
              "firstElement/lastElement");
        v.insertElementAt("x", 1);
        check(v.equals(Arrays.asList("a", "x", "b", "c")), "insertElementAt");
        v.setElementAt("X", 1);
        check(v.get(1).equals("X"), "setElementAt");
        v.removeElementAt(1);
        check(v.equals(Arrays.asList("a", "b", "c")), "removeElementAt");
        check(v.removeElement("b"), "removeElement returns true");
        check(!v.removeElement("z"), "removeElement absent false");

        expect(ArrayIndexOutOfBoundsException.class, () -> v.elementAt(99), "elementAt OOB");
        expect(ArrayIndexOutOfBoundsException.class, () -> v.elementAt(-1), "elementAt negative");
    }

    static void testCopyToArray() {
        Vector<Integer> v = new Vector<>(Arrays.asList(1, 2, 3));
        Integer[] arr = new Integer[5];
        v.copyInto(arr);
        check(arr[0] == 1 && arr[1] == 2 && arr[2] == 3 && arr[3] == null, "copyInto");
        check(Arrays.equals(v.toArray(), new Object[]{1, 2, 3}), "toArray()");
        check(Arrays.equals(v.toArray(new Integer[0]), new Integer[]{1, 2, 3}),
              "toArray(T[])");
        Integer[] small = new Integer[2];
        expect(ArrayIndexOutOfBoundsException.class, () -> v.copyInto(small),
              "copyInto too small");
    }

    static void testEnumeration() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b", "c"));
        Enumeration<String> e = v.elements();
        StringBuilder sb = new StringBuilder();
        while (e.hasMoreElements()) sb.append(e.nextElement());
        check(sb.toString().equals("abc"), "elements() enumeration order");
    }

    static void testSetSize() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b"));
        v.setSize(4);
        check(v.size() == 4 && v.get(2) == null && v.get(3) == null, "setSize pads null");
        v.setSize(1);
        check(v.size() == 1 && v.get(0).equals("a"), "setSize truncates");
    }

    static void testEqualsHashCodeSubList() {
        Vector<Integer> a = new Vector<>(Arrays.asList(1, 2, 3));
        Vector<Integer> b = new Vector<>(Arrays.asList(1, 2, 3));
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode");

        List<Integer> sub = a.subList(1, 3);
        sub.set(0, 20);
        check(a.get(1) == 20, "subList set reflected");
        a.add(4);
        expect(ConcurrentModificationException.class, () -> sub.get(0), "subList fail-fast");
    }

    static void testIteratorFailFast() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b"));
        Iterator<String> it = v.iterator();
        check(it.next().equals("a"), "iterator next");
        v.add("c");
        expect(ConcurrentModificationException.class, it::next, "iterator fail-fast");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCapacity();
        testContractChain();
        testLegacy();
        testCopyToArray();
        testEnumeration();
        testSetSize();
        testEqualsHashCodeSubList();
        testIteratorFailFast();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
