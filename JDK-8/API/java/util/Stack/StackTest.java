/*
 * @test
 * @summary java.util.Stack API conformance: full contract coverage.
 *          push/pop LIFO, peek non-destructive, empty, search (1-based from top, equals-based,
 *          duplicate -> topmost, missing -> -1, null element), EmptyStackException for pop/peek
 *          on empty, inherited Vector order (bottom-to-top), null element allowance, clone
 *          shallow, serialization round-trip + serialVersionUID, and state invariants.
 * @run main StackTest
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
    static <E> Stack<E> roundTrip(Stack<E> s) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(s); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Stack<E>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testPushPop() {
        Stack<String> s = new Stack<>();
        check(s.empty(), "empty initially");
        check(s.push("a").equals("a") && s.push("b").equals("b"), "push returns element");
        check(!s.empty(), "!empty after push");
        check(s.pop().equals("b"), "pop LIFO");
        check(s.pop().equals("a"), "pop LIFO");
        check(s.empty(), "empty after pops");
        checkThrows(EmptyStackException.class, s::pop, "pop empty throws EmptyStackException");
    }

    static void testPeekSearch() {
        Stack<String> s = new Stack<>();
        s.push("a");
        s.push("b");
        s.push("c");
        check(s.peek().equals("c"), "peek top without removing");
        check(s.size() == 3, "peek non-destructive");
        check(s.search("c") == 1, "search top == 1");
        check(s.search("b") == 2, "search middle == 2");
        check(s.search("a") == 3, "search bottom == 3");
        check(s.search("z") == -1, "search missing == -1");
        checkThrows(EmptyStackException.class, new Stack<String>()::peek, "peek empty throws EmptyStackException");
    }

    // ---- equals-based search + duplicate + null ----------------------------

    static final class Key {
        final String v;
        Key(String v) { this.v = v; }
        public boolean equals(Object o) { return o instanceof Key && ((Key) o).v.equals(v); }
        public int hashCode() { return v.hashCode(); }
    }

    static void testSearchSemantics() {
        Stack<Key> s = new Stack<>();
        Key top = new Key("k");
        s.push(new Key("a"));
        s.push(new Key("b"));
        s.push(top);
        // search uses equals (not identity): a distinct equal Key matches.
        check(s.search(new Key("k")) == 1, "search uses equals, not identity");
        check(s.search(new Key("b")) == 2, "search middle by equals");
        check(s.search(new Key("zz")) == -1, "search missing by equals");

        Stack<String> d = new Stack<>();
        d.push("x");
        d.push("y");
        d.push("x");
        check(d.search("x") == 1, "search duplicate returns topmost (1)");
        check(d.search("y") == 2, "search other element");

        // null element: search(null) returns the 1-based position of the topmost null.
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

    // ---- inherited Vector order + clone + serialization + invariants -------

    static void testInheritedVector() {
        Stack<Integer> s = new Stack<>();
        s.push(1);
        s.push(2);
        s.push(3);
        Iterator<Integer> it = s.iterator();
        check(it.next() == 1 && it.next() == 2 && it.next() == 3, "iteration bottom-to-top");
        check(s.get(0) == 1 && s.size() == 3, "inherited get/size");
        check(s.capacity() >= 3, "inherited capacity");
        s.remove(0);
        check(s.get(0) == 2 && s.size() == 2, "inherited remove(int) shifts bottom");
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

    static void testStateInvariants() {
        Stack<String> s = new Stack<>();
        s.push("a");
        s.push("b");
        check(s.peek().equals("b") && s.size() == 2, "peek leaves state intact");
        s.clear();
        check(s.empty() && s.size() == 0, "clear empties stack");
        check(s.search("a") == -1, "search after clear == -1");
        s.push("z");
        check(!s.empty() && s.peek().equals("z"), "reusable after clear");
    }

    public static void main(String[] args) throws Exception {
        testPushPop();
        testPeekSearch();
        testSearchSemantics();
        testNullAllowance();
        testInheritedVector();
        testClone();
        testSerialization();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
