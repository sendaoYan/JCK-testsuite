/*
 * @test
 * @summary java.util.ArrayList conformance. White-box: Class.forName + superclass/
 *          interface/member shape. Black-box: full List contract chain (add/addAll ->
 *          get/set -> remove/removeAll/retainAll -> contains/indexOf/lastIndexOf ->
 *          iterator/listIterator fail-fast -> subList view -> toArray -> sort/spliterator ->
 *          removeIf/replaceAll -> equals/hashCode/clear) plus Java 21+ SequencedCollection
 *          first/last accessors.
 * @run main ArrayListTest
 *
 * 设计案：
 * 1. 契约：ArrayList 是可变、可含 null 的基于数组的 List（RandomAccess）；保证插入顺序、
 *    支持索引随机访问，结构性修改使活动迭代器/subList 视图快速失败（CME）。
 * 2. 用户场景：new ArrayList → add/addAll 建表 → get/set/remove 改表 →
 *    contains/indexOf/lastIndexOf 查询 → iterator/listIterator 遍历 → subList 取区间视图 →
 *    sort 排序、spliterator 切分 → removeIf/replaceAll 批量改写 → equals/hashCode/clear。
 * 3. 组合流程：见 main 各 test*，全部是「构造 → 多步操作 → 断言」的黑盒链路。
 * 4. 不变量：元素顺序 == 插入顺序；equals/hashCode 满足 List 契约；subList 是视图（双向联动）；
 *    clone 浅拷贝（元素对象共享）。
 * 5. 边界/异常：越界 get/set/remove/add(index) 抛 IndexOutOfBoundsException；
 *    结构修改后 iterator/listIterator/subList 访问抛 ConcurrentModificationException；
 *    允许 null 元素；空表 getFirst/getLast/removeFirst/removeLast 抛 NoSuchElementException。
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.UnaryOperator;

public class ArrayListTest {
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
        Class<?> c = Class.forName("java.util.ArrayList");
        check(!c.isInterface(), "ArrayList is a class, not interface");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "ArrayList public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "ArrayList concrete");
        check(c.getSuperclass() == java.util.AbstractList.class, "extends AbstractList");
        check(java.util.List.class.isAssignableFrom(c), "implements List");
        check(java.util.RandomAccess.class.isAssignableFrom(c), "implements RandomAccess");
        check(java.lang.Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        c.getMethod("add", Object.class);
        c.getMethod("get", int.class);
        c.getMethod("set", int.class, Object.class);
        c.getMethod("remove", int.class);
        c.getMethod("sort", java.util.Comparator.class);
        c.getMethod("spliterator");
        check(true, "key public methods present");
    }

    static void testConstruction() {
        ArrayList<String> a = new ArrayList<>();
        check(a.size() == 0 && a.isEmpty(), "default ctor empty");
        ArrayList<String> b = new ArrayList<>(10);
        check(b.size() == 0, "capacity ctor empty");
        ArrayList<String> c = new ArrayList<>(Arrays.asList("a", "b"));
        check(c.size() == 2 && c.get(0).equals("a"), "Collection ctor copies");
    }

    static void testContractChain() {
        ArrayList<String> list = new ArrayList<>();
        check(list.add("a") && list.add("b") && list.add(null), "add returns true");
        check(list.addAll(Arrays.asList("c", "d")), "addAll appends");
        check(list.equals(Arrays.asList("a", "b", null, "c", "d")), "order after add/addAll");

        check(list.get(2) == null && list.get(3).equals("c"), "get by index incl null");
        check(list.set(3, "C").equals("c"), "set returns old value");
        check(list.remove(2) == null, "remove(int) returns removed null");
        check(list.remove("d"), "remove(Object) returns true");
        check(list.equals(Arrays.asList("a", "b", "C")), "list after set/remove");

        check(list.removeAll(Arrays.asList("a", "C")), "removeAll returns true");
        check(list.equals(Arrays.asList("b")), "removeAll removes matching");
        list.addAll(Arrays.asList("a", "C", "x"));
        check(list.retainAll(Arrays.asList("a", "C")), "retainAll returns true");
        check(list.equals(Arrays.asList("a", "C")), "retainAll keeps matching");

        check(list.contains("a") && list.contains("C") && !list.contains(null), "contains");
        check(list.indexOf("a") == 0 && list.lastIndexOf("a") == 0, "indexOf/lastIndexOf");
        check(list.indexOf("z") == -1, "indexOf missing");
    }

    static void testFirstLast() {
        ArrayList<String> list = new ArrayList<>();
        list.addLast("a");
        list.addLast("b");
        list.addFirst("z");
        check(list.equals(Arrays.asList("z", "a", "b")), "addFirst/addLast order");
        check(list.getFirst().equals("z") && list.getLast().equals("b"), "getFirst/getLast");
        check(list.removeFirst().equals("z"), "removeFirst");
        check(list.removeLast().equals("b"), "removeLast");
        check(list.equals(Arrays.asList("a")), "after removeFirst/Last");

        ArrayList<String> empty = new ArrayList<>();
        expect(NoSuchElementException.class, empty::getFirst, "getFirst empty");
        expect(NoSuchElementException.class, empty::removeLast, "removeLast empty");
    }

    static void testSortSpliterator() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("banana", "apple", "cherry", "apricot"));
        list.sort(String::compareTo);
        check(list.equals(Arrays.asList("apple", "apricot", "banana", "cherry")), "sort natural");

        ArrayList<String> byLen = new ArrayList<>(Arrays.asList("bb", "aa", "c"));
        byLen.sort((x, y) -> Integer.compare(x.length(), y.length()));
        check(byLen.equals(Arrays.asList("c", "bb", "aa")), "sort by length stable");

        Spliterator<String> sp = list.spliterator();
        check(sp.estimateSize() == 4, "spliterator estimateSize");
        check(sp.hasCharacteristics(Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED),
              "spliterator characteristics ORDERED|SIZED|SUBSIZED");
        Spliterator<String> left = sp.trySplit();
        check(left != null, "trySplit non-null for size 4");
        check(left.estimateSize() + sp.estimateSize() == 4, "trySplit partitions fully");
        java.util.ArrayList<String> gathered = new java.util.ArrayList<>();
        left.forEachRemaining(gathered::add);
        sp.forEachRemaining(gathered::add);
        check(gathered.equals(list), "spliterator covers all elements in order");
    }

    static void testRemoveIfReplaceAll() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        check(list.removeIf(x -> x % 2 == 0), "removeIf returns true");
        check(list.equals(Arrays.asList(1, 3, 5)), "removeIf removes evens");
        check(!list.removeIf(x -> x > 100), "removeIf returns false when none");

        list.replaceAll(x -> x * 10);
        check(list.equals(Arrays.asList(10, 30, 50)), "replaceAll transforms all");
        list.replaceAll(UnaryOperator.identity());
        check(list.equals(Arrays.asList(10, 30, 50)), "replaceAll identity");
    }

    static void testIteratorFailFast() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = list.iterator();
        check(it.next().equals("a"), "iterator next");
        it.remove();
        check(list.equals(Arrays.asList("b", "c")), "iterator.remove()");

        Iterator<String> it2 = list.iterator();
        it2.next();
        list.add("d");
        expect(ConcurrentModificationException.class, it2::next, "iterator fail-fast on add");

        ListIterator<String> li = list.listIterator(1);
        li.add("X");
        check(list.equals(Arrays.asList("b", "X", "c", "d")), "listIterator add");
        check(li.previous().equals("X"), "listIterator previous returns added");
        li.set("Y");
        check(list.equals(Arrays.asList("b", "Y", "c", "d")), "listIterator set");
        check(li.next().equals("Y"), "listIterator next after set");
        li.remove();
        check(list.equals(Arrays.asList("b", "c", "d")), "listIterator remove");
    }

    static void testSubListView() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> sub = list.subList(1, 4);
        check(sub.equals(Arrays.asList(2, 3, 4)), "subList content");
        sub.set(0, 20);
        check(list.get(1) == 20, "subList set reflected");
        list.set(2, 30);
        check(sub.get(1) == 30, "backing change reflected in subList");
        sub.add(99);
        check(list.equals(Arrays.asList(1, 20, 30, 4, 99, 5)), "subList add reflected");
        sub.clear();
        check(list.equals(Arrays.asList(1, 5)), "subList.clear removes range");

        List<Integer> sub2 = list.subList(0, 2);
        list.add(6);
        expect(ConcurrentModificationException.class, () -> sub2.get(0), "subList fail-fast");
    }

    static void testToArray() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b"));
        check(Arrays.equals(list.toArray(), new Object[]{"a", "b"}), "toArray()");
        check(Arrays.equals(list.toArray(new String[0]), new String[]{"a", "b"}),
              "toArray(T[]) sized");
        String[] padded = list.toArray(new String[5]);
        check(padded.length == 5 && padded[0].equals("a") && padded[2] == null,
              "toArray(T[]) pads trailing null");
        String[] generated = list.toArray(String[]::new);
        check(Arrays.equals(generated, new String[]{"a", "b"}), "toArray(IntFunction)");
    }

    static void testEqualsHashCodeClear() {
        ArrayList<String> a = new ArrayList<>(Arrays.asList("x", "y"));
        ArrayList<String> b = new ArrayList<>(Arrays.asList("x", "y"));
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "equal lists equal hashCode");
        @SuppressWarnings("unchecked")
        ArrayList<String> clone = (ArrayList<String>) a.clone();
        check(a != clone && a.equals(clone), "clone shallow but equal");
        clone.set(0, "X");
        check(a.get(0).equals("x"), "clone independent structure");

        a.clear();
        check(a.isEmpty() && a.size() == 0, "clear empties");
        check(!a.equals(b), "empty != non-empty");
    }

    static void testBlackBox() {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList("a", "b", "c", "d"));

        // ensureCapacity: no observable content change; still usable afterwards
        list.ensureCapacity(100);
        check(list.size() == 4 && list.equals(Arrays.asList("a", "b", "c", "d")),
              "ensureCapacity preserves content");

        // forEach in index order
        java.util.List<String> seen = new java.util.ArrayList<>();
        list.forEach(seen::add);
        check(seen.equals(Arrays.asList("a", "b", "c", "d")), "forEach index order");

        // trimToSize shrinks backing storage; content is unchanged and list stays usable
        list.trimToSize();
        check(list.equals(Arrays.asList("a", "b", "c", "d")), "trimToSize preserves content");
        list.add("e");
        check(list.size() == 5 && list.get(4).equals("e"), "usable after trimToSize");

        // removeRange is protected; expose via a minimal subclass to assert observable behavior
        class ExposedArrayList extends ArrayList<String> {
            ExposedArrayList(java.util.Collection<String> c) { super(c); }
            void removeRangeExposed(int from, int to) { removeRange(from, to); }
        }
        ExposedArrayList ex = new ExposedArrayList(Arrays.asList("a", "b", "c", "d", "e"));
        ex.removeRangeExposed(1, 4);
        check(ex.equals(Arrays.asList("a", "e")), "removeRange removes [from,to) and shifts");

        // ---- boundary/equivalence additions (M2) ----
        ArrayList<String> e0 = new ArrayList<>();
        check(e0.isEmpty() && e0.size() == 0, "empty list size/isEmpty");
        expect(IndexOutOfBoundsException.class, () -> e0.get(0), "get(0) on empty throws IOOBE");
        expect(IndexOutOfBoundsException.class, () -> e0.remove(0), "remove(0) on empty throws IOOBE");

        ArrayList<String> l2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        expect(IndexOutOfBoundsException.class, () -> l2.get(l2.size()), "get(size) throws IOOBE");
        expect(IndexOutOfBoundsException.class, () -> l2.get(-1), "get(-1) throws IOOBE");
        expect(IndexOutOfBoundsException.class, () -> l2.add(l2.size() + 1, "x"), "add at size+1 throws IOOBE");
        l2.add(0, "first");
        check(l2.get(0).equals("first") && l2.size() == 4, "add at index 0");
        l2.add(l2.size(), "last");
        check(l2.get(l2.size() - 1).equals("last"), "add at index==size appends");
        check(l2.subList(0, 0).isEmpty(), "subList(0,0) empty view");
        check(l2.subList(2, 2).isEmpty(), "subList(i,i) empty view");
        check(l2.indexOf(null) == -1, "indexOf(null) when no null present");
        check(l2.lastIndexOf(l2.get(l2.size() - 1)) == l2.size() - 1, "lastIndexOf last element");
        ExposedArrayList ex2 = new ExposedArrayList(Arrays.asList("p", "q", "r"));
        ex2.removeRangeExposed(1, 1);
        check(ex2.equals(Arrays.asList("p", "q", "r")), "removeRange(from==to) no-op");
    }

    static void testConstructorMatrix() {
        expect(IllegalArgumentException.class, () -> new ArrayList<String>(-1), "ArrayList(-1) IAE");
        expect(NullPointerException.class, () -> new ArrayList<String>((java.util.Collection<String>) null),
              "ArrayList(null Collection) NPE");
        ArrayList<String> src = new ArrayList<>(Arrays.asList("a", "b"));
        ArrayList<String> copy = new ArrayList<>(src);
        check(copy.equals(src), "ArrayList(Collection) copies");
        copy.add("c");
        check(src.size() == 2, "ArrayList(Collection) independent");
    }

    static void testNullArgs() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a"));
        expect(NullPointerException.class, () -> list.addAll(null), "addAll(null) NPE");
        expect(NullPointerException.class, () -> list.addAll(0, null), "addAll(index,null) NPE");
        expect(NullPointerException.class, () -> list.containsAll(null), "containsAll(null) NPE");
        expect(NullPointerException.class, () -> list.removeAll(null), "removeAll(null) NPE");
        expect(NullPointerException.class, () -> list.retainAll(null), "retainAll(null) NPE");
    }

    static void testSerialization() throws Exception {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", null));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(list); }
        ArrayList<String> r;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            r = (ArrayList<String>) ois.readObject();
        }
        check(r != list, "round-trip distinct object");
        check(r.equals(list), "round-trip equals original");
        check(r.size() == 3 && r.get(2) == null, "round-trip preserves content incl null");
        r.add("z");
        check(list.size() == 3, "round-trip result independent");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(ArrayList.class);
        check(osc.getSerialVersionUID() == 8683452581122892189L, "ArrayList serialVersionUID stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstruction();
        testContractChain();
        testFirstLast();
        testSortSpliterator();
        testRemoveIfReplaceAll();
        testIteratorFailFast();
        testSubListView();
        testToArray();
        testEqualsHashCodeClear();
        testBlackBox();
        testConstructorMatrix();
        testNullArgs();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
