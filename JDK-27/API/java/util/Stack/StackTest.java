/*
 * @test
 * @summary java.util.Stack conformance. White-box: Class.forName + superclass/member shape.
 *          Black-box: push/pop LIFO, peek non-destructive, empty, search (1-based from top,
 *          equals-based, duplicates), EmptyStackException for pop/peek on empty, and
 *          inherited Vector order/size/capacity.
 * @run main StackTest
 *
 * 设计案：
 * 1. 契约：Stack 继承 Vector，表示后进先出（LIFO）栈；push 压入、pop 弹出并移除、
 *    peek 只读栈顶、search 返回元素距栈顶的 1-based 距离（找不到 -1）。
 * 2. 用户场景：new Stack → push 压栈 → peek 看顶 → search 定位 → pop 依次弹出 →
 *    空栈后 pop/peek 抛 EmptyStackException。
 * 3. 组合流程：见各 test*，均为「构造 → 多步组合 → 断言」。
 * 4. 不变量：栈顶 == 最后 push 的元素；search 相等元素取离栈顶最近者；
 *    继承 Vector 的迭代顺序为自底向顶。
 * 5. 边界/异常：空栈 pop/peek 抛 EmptyStackException；search 缺失返回 -1；
 *    search 用 equals（非 ==）判等。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Stack;

public class StackTest {
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

    @SuppressWarnings("unchecked")
    static <E> Stack<E> roundTrip(Stack<E> s) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(s); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Stack<E>) ois.readObject();
        }
    }

    // ---- white-box: 结构/反射断言 ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Stack");
        check(!c.isInterface(), "Stack is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "Stack public");
        check(c.getSuperclass() == java.util.Vector.class, "extends Vector");
        c.getMethod("push", Object.class);
        c.getMethod("pop");
        c.getMethod("peek");
        c.getMethod("empty");
        c.getMethod("search", Object.class);
        check(true, "key public methods present");
    }

    static void testPushPop() {
        Stack<String> s = new Stack<>();
        check(s.empty(), "empty initially");
        check(s.push("a").equals("a") && s.push("b").equals("b"), "push returns element");
        check(!s.empty() && s.size() == 2, "!empty and size after push");
        check(s.pop().equals("b") && s.pop().equals("a"), "pop LIFO");
        check(s.empty(), "empty after pops");
        expect(EmptyStackException.class, s::pop, "pop empty throws EmptyStackException");
    }

    static void testPeek() {
        Stack<String> s = new Stack<>();
        s.push("a");
        s.push("b");
        s.push("c");
        check(s.peek().equals("c"), "peek top");
        check(s.size() == 3, "peek non-destructive");
        s.pop();
        check(s.peek().equals("b"), "peek after pop");
        Stack<String> empty = new Stack<>();
        expect(EmptyStackException.class, empty::peek, "peek empty throws EmptyStackException");
    }

    static void testSearch() {
        Stack<String> s = new Stack<>();
        s.push("a");
        s.push("b");
        s.push("c");
        check(s.search("c") == 1, "search top == 1");
        check(s.search("b") == 2, "search middle == 2");
        check(s.search("a") == 3, "search bottom == 3");
        check(s.search("z") == -1, "search missing == -1");

        Stack<String> d = new Stack<>();
        d.push("x");
        d.push("y");
        d.push("x");
        check(d.search("x") == 1, "search duplicate returns topmost (1)");
        check(d.search("y") == 2, "search other element");
    }

    static void testInheritedVector() {
        Stack<Integer> s = new Stack<>();
        s.push(1);
        s.push(2);
        s.push(3);
        Iterator<Integer> it = s.iterator();
        check(it.next() == 1 && it.next() == 2 && it.next() == 3, "iteration bottom-to-top");
        check(s.get(0) == 1 && s.size() == 3, "inherited get/size");
        check(s.capacity() >= 3, "inherited capacity");
    }

    // ---- contract deep-dive (equals-based search / null allowance / clone /
    //      serialization) ----------------------------------------------------

    static final class Key {
        final String v;
        Key(String v) { this.v = v; }
        public boolean equals(Object o) { return o instanceof Key && ((Key) o).v.equals(v); }
        public int hashCode() { return v.hashCode(); }
    }

    static void testSearchSemantics() {
        Stack<Key> s = new Stack<>();
        s.push(new Key("a"));
        s.push(new Key("b"));
        s.push(new Key("k"));
        check(s.search(new Key("k")) == 1, "search uses equals, not identity");
        check(s.search(new Key("b")) == 2, "search middle by equals");
        check(s.search(new Key("zz")) == -1, "search missing by equals");

        Stack<String> n = new Stack<>();
        n.push("a");
        n.push(null);
        n.push("b");
        check(n.search(null) == 2, "search(null) finds null element position");
    }

    static void testNullAllowance() {
        Stack<String> s = new Stack<>();
        check(s.push(null) == null, "push(null) allowed returns null");
        check(s.pop() == null, "pop returns pushed null");
    }

    static void testClone() {
        Stack<Integer> s = new Stack<>();
        s.push(1);
        s.push(2);
        @SuppressWarnings("unchecked")
        Stack<Integer> clone = (Stack<Integer>) s.clone();
        check(clone != s, "clone distinct object");
        check(clone.equals(s) && clone.size() == 2, "clone content equal");
        clone.push(3);
        check(clone.size() == 3 && s.size() == 2, "clone structural independence");
    }

    static void testSerialization() throws Exception {
        Stack<Integer> s = new Stack<>();
        s.push(1);
        s.push(2);
        s.push(3);
        Stack<Integer> r = roundTrip(s);
        check(r != s && r.equals(s), "round-trip distinct and equal");
        check(r.pop() == 3 && r.pop() == 2 && r.pop() == 1, "round-trip preserves LIFO");
        check(r.empty(), "round-trip drained");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(Stack.class);
        check(osc.getSerialVersionUID() == 1224463164541339165L, "Stack serialVersionUID stable");
    }

    
    static void testBlackBox() throws Exception {
        testPushPop();
        testPeek();
        testSearch();
        testInheritedVector();
    }

    // ---- deepened II: push 引用同一性 / equals-hashCode 公式 / Vector legacy /
    //      subList+迭代器 CME / SequencedCollection 继承（栈顶=last）-------------------

    static void testPushIdentityAndLifo() {
        Object o1 = new Object(), o2 = new Object();
        Stack<Object> s = new Stack<>();
        check(s.push(o1) == o1 && s.push(o2) == o2, "push returns identical reference");
        Stack<String> st = new Stack<>();
        st.push("a"); st.push("b"); st.push("c");
        StringBuilder sb = new StringBuilder();
        while (!st.empty()) sb.append(st.pop());
        check(sb.toString().equals("cba"), "drain order is exact LIFO (c,b,a)");
        expect(EmptyStackException.class, st::peek, "peek empty EmptyStackException");
        expect(EmptyStackException.class, new Stack<String>()::pop, "pop empty EmptyStackException");
        check(st.push("r").equals("r") && st.pop().equals("r"), "refill after drain");
    }

    static void testEqualsHashCodeListContract() {
        check(new Stack<String>().hashCode() == 1, "empty stack hashCode 1");
        Stack<String> s1 = new Stack<>();
        s1.push("a");
        check(s1.hashCode() == 128, "stack [a] hashCode 128");
        s1.push("b");
        check(s1.hashCode() == 4066, "stack [a,b] hashCode 4066");
        java.util.List<String> al = new java.util.ArrayList<>(java.util.Arrays.asList("a", "b"));
        check(s1.equals(al) && al.equals(s1), "Stack equals ArrayList bottom-to-top");
        check(s1.hashCode() == al.hashCode(), "Stack/List same hashCode");
    }

    static void testInheritedVectorLegacyOps() {
        Stack<String> s = new Stack<>();
        s.addElement("a");
        s.addElement("b");
        check(s.peek().equals("b") && s.size() == 2, "addElement behaves as push");
        s.insertElementAt("bottom", 0);
        check(s.get(0).equals("bottom") && s.peek().equals("b"), "insertElementAt grows bottom, top unchanged");
        check(s.firstElement().equals("bottom") && s.lastElement().equals("b"),
              "firstElement=bottom, lastElement=top");
        s.setElementAt("B", s.size() - 1);
        check(s.peek().equals("B"), "setElementAt at top changes peek");
        s.add(0, "floor");
        check(s.pop().equals("B"), "pop still takes last element after bottom insert");
        StringBuilder sb = new StringBuilder();
        java.util.Enumeration<String> e = s.elements();
        while (e.hasMoreElements()) sb.append(e.nextElement());
        check(sb.toString().equals("floorbottoma"), "elements() enumerates bottom-to-top ([floor,bottom,a])");
    }

    static void testSubListAndIterator() {
        Stack<Integer> s = new Stack<>();
        s.push(1); s.push(2); s.push(3); s.push(4);
        java.util.List<Integer> sub = s.subList(1, 3);
        check(sub.equals(java.util.Arrays.asList(2, 3)), "subList is bottom-to-top segment");
        sub.set(0, 20);
        check(s.get(1) == 20, "subList set reflects into stack");
        sub.remove(0);
        check(s.size() == 3 && s.peek() == 4, "subList remove shrinks stack, top intact");
        java.util.Iterator<Integer> it = s.iterator();
        it.next();
        s.push(9);
        expect(java.util.ConcurrentModificationException.class, it::next, "Stack iterator CME after push");
        Stack<Integer> s2 = new Stack<>();
        s2.push(1); s2.push(2);
        java.util.Iterator<Integer> it2 = s2.iterator();
        it2.next();
        it2.remove();
        check(s2.size() == 1 && s2.peek() == 2, "iterator.remove removes bottom element");
    }

    static void testSequencedDefaultsInherited() {
        // Stack is a Vector: SequencedCollection first == stack bottom, last == stack top
        Stack<String> s = new Stack<>();
        s.push("a"); s.push("b"); s.push("c");
        check(s.getFirst().equals("a"), "getFirst is stack bottom");
        check(s.getLast().equals("c"), "getLast is stack top");
        s.addLast("d");
        check(s.peek().equals("d"), "addLast equivalent to push");
        check(s.removeLast().equals("d"), "removeLast equivalent to pop");
        check(s.reversed().equals(java.util.Arrays.asList("c", "b", "a")),
              "reversed() is top-to-bottom List");
        expect(java.util.NoSuchElementException.class, new Stack<String>()::getLast,
               "empty getLast NSEE (Sequenced default)");
        check(s.size() == 3 && s.peek().equals("c"), "state intact after sequenced ops");
    }

public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSearchSemantics();
        testNullAllowance();
        testClone();
        testSerialization();
        testPushIdentityAndLifo();
        testEqualsHashCodeListContract();
        testInheritedVectorLegacyOps();
        testSubListAndIterator();
        testSequencedDefaultsInherited();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
