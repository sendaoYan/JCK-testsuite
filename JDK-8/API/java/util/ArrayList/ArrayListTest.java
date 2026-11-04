/*
 * @test
 * @summary java.util.ArrayList API conformance: construction/size/isEmpty, add (tail
 *          and indexed, incl. null), get/set/remove, contains/indexOf/lastIndexOf,
 *          addAll/removeAll/retainAll/containsAll, toArray variants, iterator/listIterator
 *          order and fail-fast (ConcurrentModificationException), subList view semantics,
 *          clone (shallow), equals/hashCode, clear, and capacity methods.
 * @run main ArrayListTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ArrayListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    static void expectIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
    }

    @SuppressWarnings("unchecked")
    static <T> ArrayList<T> roundTrip(ArrayList<T> list) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(list); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (ArrayList<T>) ois.readObject();
        }
    }

    static void testConstruction() {
        ArrayList<String> a = new ArrayList<>();
        check(a.size() == 0 && a.isEmpty(), "default ctor empty");
        ArrayList<String> b = new ArrayList<>(10);
        check(b.size() == 0, "capacity ctor empty");
        ArrayList<String> c = new ArrayList<>(Arrays.asList("a", "b"));
        check(c.size() == 2 && c.get(0).equals("a"), "Collection ctor copies");
    }

    static void testAddGetSetRemove() {
        ArrayList<String> list = new ArrayList<>();
        check(list.add("a") && list.add("b") && list.add(null), "add returns true");
        check(list.size() == 3, "size after add");
        check(list.get(0).equals("a") && list.get(1).equals("b") && list.get(2) == null,
              "get preserves order incl null");
        check(list.set(1, "B").equals("b"), "set returns old value");
        check(list.get(1).equals("B"), "set updates element");
        check(list.remove(0).equals("a"), "remove(int) returns removed");
        check(list.size() == 2 && list.get(0).equals("B"), "remove shifts left");
        check(list.remove("B"), "remove(Object) returns true");
        check(!list.remove("absent"), "remove(absent) returns false");
        check(list.remove(null), "remove(null)");
        check(list.size() == 0, "empty after removes");

        list.add(0, "x");
        check(list.get(0).equals("x"), "add(0, x) inserts front");
        list.add(1, "y");
        check(list.equals(Arrays.asList("x", "y")), "add(index) inserts");
        expectIOOBE(() -> list.get(5), "get out of range");
        expectIOOBE(() -> list.set(-1, "z"), "set negative index");
        expectIOOBE(() -> list.remove(5), "remove out of range");
    }

    static void testContainsIndex() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", null, "b", null));
        check(list.contains("a") && list.contains("b") && list.contains(null),
              "contains incl null");
        check(!list.contains("z"), "!contains");
        check(list.indexOf("a") == 0 && list.indexOf(null) == 1, "indexOf");
        check(list.lastIndexOf(null) == 3, "lastIndexOf(null)");
        check(list.indexOf("z") == -1 && list.lastIndexOf("z") == -1, "indexOf missing");
    }

    static void testBulk() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
        check(list.addAll(Arrays.asList(4, 5)), "addAll returns true");
        check(list.equals(Arrays.asList(1, 2, 3, 4, 5)), "addAll appends");
        check(list.addAll(0, Arrays.asList(0)), "addAll at index");
        check(list.equals(Arrays.asList(0, 1, 2, 3, 4, 5)), "addAll(index) inserts");

        check(list.containsAll(Arrays.asList(0, 5)), "containsAll");
        check(!list.containsAll(Arrays.asList(0, 99)), "!containsAll");
        check(list.removeAll(Arrays.asList(0, 5)), "removeAll returns true");
        check(list.equals(Arrays.asList(1, 2, 3, 4)), "removeAll removes matching");

        list.retainAll(Arrays.asList(1, 3));
        check(list.equals(Arrays.asList(1, 3)), "retainAll keeps matching");
    }

    static void testToArray() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b"));
        Object[] oa = list.toArray();
        check(oa.length == 2 && oa[0].equals("a") && oa[1].equals("b"), "toArray()");
        String[] sa = list.toArray(new String[0]);
        check(Arrays.equals(sa, new String[]{"a", "b"}), "toArray(T[]) sized");
        String[] padded = list.toArray(new String[5]);
        check(padded.length == 5 && padded[0].equals("a") && padded[2] == null,
              "toArray(T[]) pads trailing null");
    }

    static void testIterator() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = list.iterator();
        check(it.hasNext() && it.next().equals("a"), "iterator next");
        check(it.next().equals("b"), "iterator order");
        it.remove();
        check(list.equals(Arrays.asList("a", "c")), "iterator.remove()");
        check(it.next().equals("c") && !it.hasNext(), "iterator exhausts");

        // fail-fast: structural modification invalidates an active iterator.
        Iterator<String> it2 = list.iterator();
        it2.next();
        list.add("d");
        try {
            it2.next();
            throw new AssertionError("expected ConcurrentModificationException");
        } catch (ConcurrentModificationException expected) {
            passed++;
        }
    }

    static void testListIterator() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = list.listIterator(1);
        check(it.hasPrevious() && it.previous().equals("a"), "listIterator previous");
        check(it.nextIndex() == 0, "nextIndex after previous");
        check(it.next().equals("a") && it.next().equals("b"), "listIterator forward");
        it.add("X");
        check(list.equals(Arrays.asList("a", "b", "X", "c")), "listIterator add");
        check(it.previous().equals("X") && it.previous().equals("b"), "listIterator back");
        it.set("B");
        check(list.equals(Arrays.asList("a", "B", "X", "c")), "listIterator set");
    }

    static void testSubList() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> sub = list.subList(1, 4);
        check(sub.equals(Arrays.asList(2, 3, 4)), "subList view content");
        sub.set(0, 20);
        check(list.get(1) == 20, "subList set reflected in backing list");
        list.set(2, 30);
        check(sub.get(1) == 30, "backing list change reflected in subList");
        sub.clear();
        check(list.equals(Arrays.asList(1, 5)), "subList.clear removes range");
    }

    static void testCloneEqualsHashCode() {
        ArrayList<String> a = new ArrayList<>(Arrays.asList("x", "y"));
        @SuppressWarnings("unchecked")
        ArrayList<String> b = (ArrayList<String>) a.clone();
        check(a != b, "clone distinct object");
        check(a.equals(b), "clone equals original");
        check(a.hashCode() == b.hashCode(), "equal lists equal hashCode");

        ArrayList<String> c = new ArrayList<>(Arrays.asList("x", "y"));
        check(a.equals(c) && c.equals(a), "equals symmetric for same elements");
        ArrayList<String> d = new ArrayList<>(Arrays.asList("x"));
        check(!a.equals(d), "!equals different size");
    }

    static void testClearCapacity() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        list.clear();
        check(list.isEmpty() && list.size() == 0, "clear empties");
        list.ensureCapacity(100);
        list.add("x");
        list.trimToSize();
        check(list.size() == 1 && list.get(0).equals("x"), "ensureCapacity/trimToSize usable");
    }

    // ---- deepened contract coverage ----------------------------------------

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new ArrayList<String>(-1),
                    "ArrayList(-1) IAE");
        checkThrows(NullPointerException.class, () -> new ArrayList<String>((java.util.Collection<String>) null),
                    "ArrayList(null Collection) NPE");
        ArrayList<String> src = new ArrayList<>(Arrays.asList("a", "b"));
        ArrayList<String> copy = new ArrayList<>(src);
        check(copy.equals(src), "ArrayList(Collection) copies");
        copy.add("c");
        check(src.size() == 2, "ArrayList(Collection) independent");
    }

    static void testFailFastChannels() {
        // structural modification via any channel invalidates active iterators
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = list.iterator();
        it.next();
        list.remove(0);
        checkThrows(ConcurrentModificationException.class, it::next, "CME after remove(int)");

        ArrayList<String> l2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> lit = l2.listIterator();
        lit.next();
        l2.clear();
        checkThrows(ConcurrentModificationException.class, lit::next, "CME after clear");

        ArrayList<String> l3 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it3 = l3.iterator();
        it3.next();
        l3.add("d");
        checkThrows(ConcurrentModificationException.class, it3::remove, "CME on iterator.remove after add");

        // structural modification through subList also fails fast on backing iterators
        ArrayList<String> l4 = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        Iterator<String> it4 = l4.iterator();
        it4.next();
        l4.subList(0, 2).clear();
        checkThrows(ConcurrentModificationException.class, it4::next, "CME after subList structural change");
    }

    static void testListIteratorEdges() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = list.listIterator(3);
        check(it.hasNext() == false && it.hasPrevious(), "listIterator(size) at end");
        check(it.previous().equals("c"), "previous from end");
        check(it.nextIndex() == 2 && it.previousIndex() == 1, "indices after previous");

        expectIOOBE(() -> list.listIterator(-1), "listIterator(-1) IOBE");
        expectIOOBE(() -> list.listIterator(4), "listIterator(size+1) IOBE");

        ListIterator<String> mid = list.listIterator(1);
        check(mid.nextIndex() == 1 && mid.previousIndex() == 0, "indices at middle");
        check(mid.next().equals("b"), "next at middle returns b");
        mid.remove();
        check(list.equals(Arrays.asList("a", "c")), "listIterator.remove at middle");
    }

    static void testSubListBounds() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
        expectIOOBE(() -> list.subList(-1, 2), "subList(-1,2) IOBE");
        expectIOOBE(() -> list.subList(0, 4), "subList(0,4) IOBE");
        checkThrows(IllegalArgumentException.class, () -> list.subList(2, 1), "subList(2,1) IAE");

        // nested subList still reflects backing list
        List<Integer> sub = list.subList(0, 3).subList(1, 3);
        check(sub.equals(Arrays.asList(2, 3)), "nested subList");
        sub.set(0, 20);
        check(list.get(1) == 20, "nested subList set reflects in backing");
    }

    static void testNullArgs() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a"));
        checkThrows(NullPointerException.class, () -> list.addAll(null), "addAll(null) NPE");
        checkThrows(NullPointerException.class, () -> list.addAll(0, null), "addAll(index,null) NPE");
        checkThrows(NullPointerException.class, () -> list.containsAll(null), "containsAll(null) NPE");
        checkThrows(NullPointerException.class, () -> list.removeAll(null), "removeAll(null) NPE");
        checkThrows(NullPointerException.class, () -> list.retainAll(null), "retainAll(null) NPE");
    }

    static void testListDefaultMethods() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(3, 1, 2));
        list.sort(Integer::compareTo);
        check(list.equals(Arrays.asList(1, 2, 3)), "List.sort");

        ArrayList<Integer> l2 = new ArrayList<>(Arrays.asList(1, 2, 3));
        l2.replaceAll(x -> x * 10);
        check(l2.equals(Arrays.asList(10, 20, 30)), "List.replaceAll");

        ArrayList<Integer> l3 = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        l3.removeIf(x -> x % 2 == 0);
        check(l3.equals(Arrays.asList(1, 3)), "Collection.removeIf");

        final int[] sum = {0};
        l3.forEach(x -> sum[0] += x);
        check(sum[0] == 4, "Iterable.forEach");
    }

    static void testSerialization() throws Exception {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", null));
        ArrayList<String> r = roundTrip(list);
        check(r != list, "round-trip distinct object");
        check(r.equals(list), "round-trip equals original");
        check(r.size() == 3 && r.get(2) == null, "round-trip preserves content incl null");
        r.add("z");
        check(list.size() == 3, "round-trip result independent");

        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(ArrayList.class);
        check(osc.getSerialVersionUID() == 8683452581122892189L, "ArrayList serialVersionUID stable");
    }

    // ---- deepened contract coverage II: hashCode formula, cross-impl equals,
    //      toArray identity, shallow clone, iterator edges, subList liveness ---

    static void testHashCodeFormula() {
        // List.hashCode: result=1; result = 31*result + (e==null?0:e.hashCode())
        check(new ArrayList<String>().hashCode() == 1, "hashCode of empty list is 1");
        // ["a"]: 31*1 + 97 = 128
        check(new ArrayList<>(Arrays.asList("a")).hashCode() == 128, "hashCode [a] = 31*1+97 = 128");
        // ["a","b"]: 31*128 + 98 = 4066
        check(new ArrayList<>(Arrays.asList("a", "b")).hashCode() == 4066, "hashCode [a,b] = 31*128+98 = 4066");
        // ["a","b",null]: 31*4066 + 0 = 126046
        check(new ArrayList<>(Arrays.asList("a", "b", null)).hashCode() == 126046,
              "hashCode [a,b,null] = 31*4066+0 = 126046 (null contributes 0)");
        // [null]: 31*1 + 0 = 31
        check(new ArrayList<>(Arrays.asList((String) null)).hashCode() == 31, "hashCode [null] = 31");
    }

    static void testCrossImplEquals() {
        // Same content in every List implementation must be equal, symmetrically.
        List<String> al = new ArrayList<>(Arrays.asList("a", "b", null));
        java.util.LinkedList<String> ll = new java.util.LinkedList<>(Arrays.asList("a", "b", null));
        java.util.Vector<String> vec = new java.util.Vector<>(Arrays.asList("a", "b", null));
        java.util.Stack<String> st = new java.util.Stack<>();
        st.addAll(Arrays.asList("a", "b", null));
        List<String> asList = Arrays.asList("a", "b", null);
        check(al.equals(ll) && ll.equals(al), "ArrayList equals LinkedList");
        check(al.equals(vec) && vec.equals(al), "ArrayList equals Vector");
        check(al.equals(st) && st.equals(al), "ArrayList equals Stack (Stack is a List)");
        check(al.equals(asList) && asList.equals(al), "ArrayList equals Arrays.asList");
        check(ll.equals(vec) && vec.equals(st) && st.equals(asList), "all List impls mutually equal");
        int h = al.hashCode();
        check(ll.hashCode() == h && vec.hashCode() == h && st.hashCode() == h && asList.hashCode() == h,
              "equal lists (all impls incl null element) share hashCode");
        // unequal cases
        check(!al.equals(Arrays.asList("a", "b")), "size difference not equal");
        check(!al.equals(Arrays.asList("a", "b", "c")), "null vs non-null element not equal");
        check(!Arrays.asList(1, 2, 3).equals(Arrays.asList(3, 2, 1)), "order significant");
        check(!new ArrayList<String>().equals("not a list"), "equals non-List false");
    }

    static void testToArrayContract() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b"));
        String[] big = new String[10];
        String[] ret = list.toArray(big);
        check(ret == big, "toArray(T[]) returns the given array when it fits (identity)");
        check(big[0].equals("a") && big[1].equals("b"), "toArray(T[]) fills from index 0");
        check(big[2] == null, "toArray(T[]) null-terminates just past last element");
        check(big[9] == null, "toArray(T[]) rest of array untouched beyond terminator");
        String[] exact = list.toArray(new String[0]);
        check(exact.length == 2, "toArray(T[0]) exact length");
        String[] same = list.toArray(new String[2]);
        check(same.length == 2 && same[0].equals("a"), "toArray(T[size]) exact fit no null terminator needed");
        Object[] oa = list.toArray();
        check(oa instanceof Object[] && !(oa instanceof String[]), "toArray() is Object[]");
        check(new ArrayList<>().toArray().length == 0, "toArray() of empty is length 0");
        // empty-source with roomy array: per contract a[0] is set to null
        String[] roomy = new String[3];
        String[] r2 = new ArrayList<String>().toArray(roomy);
        check(r2 == roomy && roomy[0] == null, "toArray(roomy) on empty list nulls index 0");
    }

    static void testCloneShallow() {
        // clone: new object, equal content, structurally independent, elements shared.
        StringBuilder shared = new StringBuilder("mut");
        ArrayList<StringBuilder> src = new ArrayList<>();
        src.add(shared);
        src.add(null);
        @SuppressWarnings("unchecked")
        ArrayList<StringBuilder> clone = (ArrayList<StringBuilder>) src.clone();
        check(clone != src && clone.getClass() == src.getClass(), "clone same class, distinct object");
        clone.get(0).append("ated");
        check(src.get(0).toString().equals("mutated"), "clone is shallow: mutation of shared element visible");
        clone.add(new StringBuilder("extra"));
        check(src.size() == 2, "clone structurally independent");
        check(clone.get(1) == null, "clone keeps null element");
        clone.remove(0);
        check(src.size() == 2 && src.get(0) == shared, "removal in clone does not touch source");
    }

    static void testIteratorEdges() {
        // remove() before next() -> IllegalStateException
        checkThrows(IllegalStateException.class,
                    () -> new ArrayList<>(Arrays.asList("a")).iterator().remove(),
                    "iterator.remove before next ISE");
        // remove() twice without next -> ISE
        ArrayList<String> l = new ArrayList<>(Arrays.asList("a", "b"));
        Iterator<String> it = l.iterator();
        it.next();
        it.remove();
        checkThrows(IllegalStateException.class, it::remove, "second remove without next ISE");
        // iterator.remove is the only modification allowed during iteration
        ArrayList<Integer> nums = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        Iterator<Integer> ni = nums.iterator();
        int sum = 0;
        while (ni.hasNext()) {
            int v = ni.next();
            if (v % 2 == 0) ni.remove();
            else sum += v;
        }
        check(sum == 4 && nums.equals(Arrays.asList(1, 3)), "iterate-and-remove evens leaves odds");
        // forEachRemaining visits the remainder, then hasNext is false
        ArrayList<String> fr = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> fi = fr.iterator();
        fi.next();
        StringBuilder sb = new StringBuilder();
        fi.forEachRemaining(sb::append);
        check(sb.toString().equals("bc"), "forEachRemaining visits remaining elements only");
        check(!fi.hasNext(), "forEachRemaining exhausts iterator");
        // removeIf return values
        ArrayList<String> rf = new ArrayList<>(Arrays.asList("a", "b"));
        check(!rf.removeIf(x -> false), "removeIf nothing matched returns false");
        check(rf.removeIf(x -> true) && rf.isEmpty(), "removeIf all matched returns true and empties");
        // forEach over nulls tolerated
        ArrayList<String> withNull = new ArrayList<>(Arrays.asList(null, "x"));
        final int[] count = {0};
        withNull.forEach(x -> count[0]++);
        check(count[0] == 2, "forEach counts null elements");
        // empty iterator next -> NoSuchElementException
        checkThrows(java.util.NoSuchElementException.class,
                    new ArrayList<String>().iterator()::next, "empty iterator next NSEE");
    }

    static void testSubListLiveness() {
        ArrayList<Integer> parent = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> sub = parent.subList(1, 4);
        // structural modification of subList reflects in parent
        sub.add(99);
        check(parent.equals(Arrays.asList(1, 2, 3, 4, 99, 5)), "subList.add grows parent in place");
        sub.remove(Integer.valueOf(99));
        check(parent.size() == 5, "subList.remove(Object) shrinks parent");
        // parent's iterator sees subList's changes
        sub.set(0, 20);
        check(parent.get(1) == 20, "subList.set reflected at parent index");
        // subList iterator remove passes through
        java.util.Iterator<Integer> sit = sub.iterator();
        sit.next();
        sit.remove();
        check(parent.equals(Arrays.asList(1, 3, 4, 5)), "subList.iterator.remove removes from parent");
        // subList equals/hashCode computed on its own contents
        List<Integer> seg = new ArrayList<>(Arrays.asList(2, 3, 4));
        List<Integer> segView = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)).subList(1, 4);
        check(segView.equals(seg) && seg.equals(segView), "subList equals plain List of same content");
        check(segView.hashCode() == seg.hashCode(), "subList hashCode equals plain List hashCode");
        // structural change of parent invalidates subList operations (CME)
        ArrayList<Integer> p2 = new ArrayList<>(Arrays.asList(1, 2, 3));
        List<Integer> s2 = p2.subList(0, 2);
        p2.add(4);
        checkThrows(ConcurrentModificationException.class, () -> s2.get(0), "subList.get after parent mod CME");
        checkThrows(ConcurrentModificationException.class, () -> s2.size(), "subList.size after parent mod CME");
        checkThrows(ConcurrentModificationException.class, () -> s2.iterator(), "subList.iterator after parent mod CME");
        checkThrows(ConcurrentModificationException.class, () -> s2.add(9), "subList.add after parent mod CME");
        // parent modification via subList does NOT invalidate the subList itself
        List<Integer> s3 = new ArrayList<>(Arrays.asList(1, 2, 3)).subList(0, 3);
        s3.add(4);
        check(s3.size() == 4, "subList stays usable after its own structural change");
    }

    static void testIndexedBounds() {
        ArrayList<String> l = new ArrayList<>(Arrays.asList("a", "b"));
        expectIOOBE(() -> l.add(-1, "x"), "add(-1,e) IOOBE");
        expectIOOBE(() -> l.add(3, "x"), "add(size+1,e) IOOBE");
        expectIOOBE(() -> l.addAll(3, Arrays.asList("c")), "addAll(size+1,c) IOOBE");
        expectIOOBE(() -> l.addAll(-1, Arrays.asList("c")), "addAll(-1,c) IOOBE");
        check(l.equals(Arrays.asList("a", "b")), "failed indexed ops leave list intact");
        l.add(2, "c");
        check(l.equals(Arrays.asList("a", "b", "c")), "add(size,e) appends");
        l.addAll(0, Arrays.asList("z"));
        check(l.equals(Arrays.asList("z", "a", "b", "c")), "addAll(0,c) prepends");
    }

    static void testEqualsSubListFormula() {
        // equals between a subList and a whole list of identical content
        ArrayList<Integer> whole = new ArrayList<>(Arrays.asList(7, 8));
        List<Integer> view = new ArrayList<>(Arrays.asList(6, 7, 8, 9)).subList(1, 3);
        check(view.equals(whole) && whole.equals(view), "subList equals whole list with same elements");
        check(!view.equals(Arrays.asList(6, 7)), "subList not equal to a different segment");
        // nested subList equals its outer subList segment
        List<Integer> outer = new ArrayList<>(Arrays.asList(0, 1, 2, 3)).subList(1, 4); // [1,2,3]
        List<Integer> inner = outer.subList(1, 3); // [2,3]
        check(inner.equals(Arrays.asList(2, 3)), "nested subList content");
        inner.set(0, 20);
        check(outer.get(1) == 20, "nested subList set reflects outer subList");
    }

    public static void main(String[] args) throws Exception {
        testConstruction();
        testAddGetSetRemove();
        testContainsIndex();
        testBulk();
        testToArray();
        testIterator();
        testListIterator();
        testSubList();
        testCloneEqualsHashCode();
        testClearCapacity();
        testConstructorMatrix();
        testFailFastChannels();
        testListIteratorEdges();
        testSubListBounds();
        testNullArgs();
        testListDefaultMethods();
        testSerialization();
        testHashCodeFormula();
        testCrossImplEquals();
        testToArrayContract();
        testCloneShallow();
        testIteratorEdges();
        testSubListLiveness();
        testIndexedBounds();
        testEqualsSubListFormula();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
