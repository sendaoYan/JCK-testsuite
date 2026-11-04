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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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

    @SuppressWarnings("unchecked")
    static <E> Vector<E> roundTrip(Vector<E> v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(v); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Vector<E>) ois.readObject();
        }
    }

    // ---- contract deep-dive (constructor matrix / null allowance /
    //      serialization / cross-impl equals) -------------------------------

    static void testConstructorMatrix() {
        expect(IllegalArgumentException.class, () -> new Vector<String>(-1), "Vector(-1) IAE");
        expect(IllegalArgumentException.class, () -> new Vector<String>(-1, 0), "Vector(-1,0) IAE");
        expect(NullPointerException.class, () -> new Vector<String>((Collection<String>) null),
              "Vector(null Collection) NPE");
        Vector<String> zero = new Vector<>(0);
        zero.add("k");
        check(zero.size() == 1 && zero.get(0).equals("k"), "Vector(0) usable");
        Vector<String> copy = new Vector<>(Arrays.asList("a", "b"));
        check(copy.equals(Arrays.asList("a", "b")), "Vector(Collection) copies");
    }

    static void testNullAllowance() {
        Vector<String> v = new Vector<>();
        v.add(null);
        v.addElement(null);
        check(v.size() == 2 && v.get(0) == null && v.get(1) == null, "add/addElement accept null");
        check(v.contains(null) && v.indexOf(null) == 0 && v.lastIndexOf(null) == 1, "null query");
        v.insertElementAt(null, 0);
        check(v.size() == 3 && v.get(0) == null, "insertElementAt null");
        check(v.set(0, "x") == null, "set null returns old null");
    }

    static void testSerialization() throws Exception {
        Vector<Integer> v = new Vector<>(Arrays.asList(1, 2, 3));
        Vector<Integer> r = roundTrip(v);
        check(r != v && r.equals(v), "round-trip distinct and equal");
        r.add(4);
        check(r.size() == 4 && v.size() == 3, "round-trip result independent");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(Vector.class);
        check(osc.getSerialVersionUID() == -2767605614048989439L, "Vector serialVersionUID stable");
    }

    static void testCrossImplEquals() {
        Vector<Integer> v = new Vector<>(Arrays.asList(1, 2, 3));
        ArrayList<Integer> al = new ArrayList<>(Arrays.asList(1, 2, 3));
        check(v.equals(al) && al.equals(v), "Vector/ArrayList equals");
        check(v.hashCode() == al.hashCode(), "hashCode equal across implementations");
        al.add(4);
        check(!v.equals(al), "!equals different content");
    }

    static void testIteratorFailFast() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b"));
        Iterator<String> it = v.iterator();
        check(it.next().equals("a"), "iterator next");
        v.add("c");
        expect(ConcurrentModificationException.class, it::next, "iterator fail-fast");
    }

    static void testBlackBox() {
        Vector<Integer> sized = new Vector<>(5);
        sized.add(1);
        check(sized.get(0) == 1, "ctor (int) works");

        // containsAll
        Vector<Integer> v = new Vector<>(Arrays.asList(1, 2, 3, 4));
        check(v.containsAll(Arrays.asList(2, 4)), "containsAll true");
        check(!v.containsAll(Arrays.asList(2, 9)), "containsAll false");

        // forEach in index order
        java.util.List<Integer> seen = new java.util.ArrayList<>();
        v.forEach(seen::add);
        check(seen.equals(Arrays.asList(1, 2, 3, 4)), "forEach index order");

        // listIterator() and listIterator(index) traversal
        java.util.ListIterator<Integer> li = v.listIterator();
        check(li.next() == 1 && li.next() == 2, "listIterator() forward");
        check(li.previous() == 2, "listIterator() previous");
        java.util.ListIterator<Integer> li2 = v.listIterator(2);
        check(li2.next() == 3, "listIterator(index) starts at index");

        // sort / replaceAll
        Vector<Integer> toSort = new Vector<>(Arrays.asList(3, 1, 2));
        toSort.sort(java.util.Comparator.naturalOrder());
        check(toSort.equals(Arrays.asList(1, 2, 3)), "sort natural");
        toSort.replaceAll(x -> x * 10);
        check(toSort.equals(Arrays.asList(10, 20, 30)), "replaceAll transforms");

        // removeIf / removeAllElements / clear
        Vector<Integer> r = new Vector<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        check(r.removeIf(x -> x % 2 == 0), "removeIf returns true");
        check(r.equals(Arrays.asList(1, 3, 5)), "removeIf removed evens");
        r.removeAllElements();
        check(r.isEmpty() && r.size() == 0, "removeAllElements empties");
        r.add(7);
        check(r.size() == 1, "reusable after removeAllElements");
        r.clear();
        check(r.isEmpty(), "clear empties");

        // removeRange is protected; expose via a minimal subclass
        class ExposedVector extends Vector<Integer> {
            ExposedVector(java.util.Collection<Integer> c) { super(c); }
            void removeRangeExposed(int from, int to) { removeRange(from, to); }
        }
        ExposedVector ev = new ExposedVector(Arrays.asList(1, 2, 3, 4, 5));
        ev.removeRangeExposed(1, 3);
        check(ev.equals(Arrays.asList(1, 4, 5)), "removeRange removes [from,to)");

        // clone independent + equal
        Vector<Integer> orig = new Vector<>(Arrays.asList(1, 2, 3));
        @SuppressWarnings("unchecked")
        Vector<Integer> cloned = (Vector<Integer>) orig.clone();
        check(cloned != orig && cloned.equals(orig), "clone independent and equal");
        cloned.add(4);
        check(orig.size() == 3, "clone structural independence");

        // spliterator reports ORDERED|SIZED|SUBSIZED and iterates in order
        java.util.Spliterator<Integer> sp = v.spliterator();
        check(sp.estimateSize() == 4, "spliterator estimateSize");
        check(sp.hasCharacteristics(java.util.Spliterator.ORDERED | java.util.Spliterator.SIZED
                | java.util.Spliterator.SUBSIZED), "spliterator characteristics");
        java.util.List<Integer> gathered = new java.util.ArrayList<>();
        sp.forEachRemaining(gathered::add);
        check(gathered.equals(Arrays.asList(1, 2, 3, 4)), "spliterator order");
    }

    // ---- deepened II: hashCode 公式 / Enumeration 契约与 Iterator 对照 /
    //      legacy 越界矩阵 / indexOf(from) / 负 increment / SequencedCollection 继承 --

    static void testHashCodeFormula() {
        check(new Vector<String>().hashCode() == 1, "hashCode of empty vector is 1");
        check(new Vector<>(Arrays.asList("a")).hashCode() == 128, "hashCode [a] = 128");
        check(new Vector<>(Arrays.asList("a", "b")).hashCode() == 4066, "hashCode [a,b] = 4066");
        check(new Vector<>(Arrays.asList("a", "b", null)).hashCode() == 126046, "hashCode [a,b,null] = 126046");
        check(new Vector<String>().capacity() == 10, "default capacity exactly 10");
    }

    static void testEnumerationContract() {
        expect(NoSuchElementException.class, new Vector<String>().elements()::nextElement,
               "empty enumeration next NSEE");
        // Vector's legacy Enumeration is NOT fail-fast; its Iterator IS
        Vector<String> v = new Vector<>(Arrays.asList("a", "b", "c"));
        Enumeration<String> e = v.elements();
        check(e.nextElement().equals("a"), "first element");
        v.add("d");
        check(e.hasMoreElements(), "enumeration still usable after add (not fail-fast)");
        Vector<String> v2 = new Vector<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = v2.iterator();
        it.next();
        v2.add("d");
        expect(ConcurrentModificationException.class, it::next, "iterator CME for same change");
        // Enumeration.asIterator() (JDK 9+) adapts enumeration to Iterator
        Vector<Integer> vi = new Vector<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> adapted = vi.elements().asIterator();
        StringBuilder sb = new StringBuilder();
        adapted.forEachRemaining(x -> sb.append(x));
        check(sb.toString().equals("123"), "Enumeration.asIterator order");
    }

    static void testElementAtBounds() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b"));
        expect(ArrayIndexOutOfBoundsException.class, () -> v.elementAt(2), "elementAt(size) AIOOBE");
        expect(ArrayIndexOutOfBoundsException.class, () -> v.elementAt(-1), "elementAt(-1) AIOOBE");
        expect(ArrayIndexOutOfBoundsException.class, () -> v.insertElementAt("z", 3),
               "insertElementAt(size+1) AIOOBE");
        expect(ArrayIndexOutOfBoundsException.class, () -> v.removeElementAt(2),
               "removeElementAt(size) AIOOBE");
        expect(ArrayIndexOutOfBoundsException.class, () -> v.setElementAt("z", 2),
               "setElementAt(size) AIOOBE");
        check(v.equals(Arrays.asList("a", "b")), "failed legacy ops leave vector intact");
        v.insertElementAt("z", 2);
        check(v.equals(Arrays.asList("a", "b", "z")), "insertElementAt(size) appends");
        v.setElementAt("Z", 2);
        check(v.get(2).equals("Z"), "setElementAt replaces in place");
        expect(NoSuchElementException.class, new Vector<String>()::firstElement, "firstElement empty NSEE");
        expect(NoSuchElementException.class, new Vector<String>()::lastElement, "lastElement empty NSEE");
    }

    static void testIndexOfFromIndex() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b", "a", "b", "a"));
        check(v.indexOf("a", 1) == 2, "indexOf from index skips earlier");
        check(v.indexOf("a", 4) == 4, "indexOf from last position");
        check(v.indexOf("a", 5) == -1, "indexOf from size returns -1");
        check(v.indexOf("a", 100) == -1, "indexOf fromIndex > size yields -1");
        check(v.lastIndexOf("a", 3) == 2, "lastIndexOf bounded above by 3");
        check(v.lastIndexOf("a", 1) == 0, "lastIndexOf scans backward from bound");
        check(v.lastIndexOf("a") == 4, "lastIndexOf unbounded");
        Vector<String> vn = new Vector<>(Arrays.asList("x", null, "y"));
        check(vn.indexOf(null, 1) == 1, "indexOf(null, from)");
        check(vn.lastIndexOf(null, 2) == 1, "lastIndexOf(null, bound)");
    }

    static void testSubListContract() {
        Vector<Integer> v = new Vector<>(Arrays.asList(1, 2, 3, 4));
        List<Integer> sub = v.subList(1, 3);
        check(sub.hashCode() == Arrays.asList(2, 3).hashCode(), "subList hashCode own content");
        check(sub.equals(Arrays.asList(2, 3)) && Arrays.asList(2, 3).equals(sub), "subList equals segment");
        sub.remove(0);
        check(v.equals(Arrays.asList(1, 3, 4)), "subList remove shrinks parent");
        check(v.subList(0, 0).isEmpty(), "empty subList view");
        check(v.subList(v.size(), v.size()).isEmpty(), "tail empty subList (size,size)");
        expect(IllegalArgumentException.class, () -> v.subList(2, 1), "subList(from>to) IAE");
        expect(IndexOutOfBoundsException.class, () -> v.subList(-1, 2), "subList(-1,2) IOOBE");
        expect(IndexOutOfBoundsException.class, () -> v.subList(0, 5), "subList(0,size+1) IOOBE");
    }

    static void testNegativeIncrementAndLegacy() {
        Vector<String> v = new Vector<>(2, -1);
        check(v.capacity() == 2, "Vector(2,-1) initial capacity 2");
        v.add("a"); v.add("b"); v.add("c");
        check(v.size() == 3 && v.capacity() >= 3, "grows despite negative increment");
        check(!v.removeElement("zz"), "removeElement absent returns false");
        check(v.removeElement("a"), "removeElement present returns true");
        check(v.equals(Arrays.asList("b", "c")), "removeElement removed first match");
    }

    static void testSequencedDefaultsInherited() {
        // Vector does not override List's SequencedCollection defaults; inherited behavior
        Vector<String> v = new Vector<>(Arrays.asList("a", "b", "c"));
        check(v.getFirst().equals("a") && v.getLast().equals("c"), "getFirst/getLast inherited");
        v.addFirst("0");
        v.addLast("d");
        check(v.equals(Arrays.asList("0", "a", "b", "c", "d")), "addFirst/addLast inherited");
        check(v.removeFirst().equals("0") && v.removeLast().equals("d"), "removeFirst/removeLast inherited");
        check(v.reversed().equals(Arrays.asList("c", "b", "a")), "reversed() inherited view");
        // reversed() is live
        List<String> rev = v.reversed();
        rev.set(0, "C");
        check(v.get(2).equals("C"), "reversed() write passes through");
        expect(NoSuchElementException.class, new Vector<String>()::getFirst, "empty getFirst NSEE");
        expect(NoSuchElementException.class, new Vector<String>()::removeLast, "empty removeLast NSEE");
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
        testBlackBox();
        testConstructorMatrix();
        testNullAllowance();
        testSerialization();
        testCrossImplEquals();
        testHashCodeFormula();
        testEnumerationContract();
        testElementAtBounds();
        testIndexOfFromIndex();
        testSubListContract();
        testNegativeIncrementAndLegacy();
        testSequencedDefaultsInherited();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
