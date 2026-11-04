/*
 * @test
 * @summary java.util.Queue API conformance: offer/add return semantics, peek/element and
 *          poll/remove return-and-remove semantics, empty-queue null vs NoSuchElementException,
 *          and null-argument NPE.
 * @run main QueueTest
 */

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class QueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
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

    static void testOfferAdd() {
        Queue<String> q = new ArrayDeque<>();
        check(q.offer("a"), "offer returns true");
        check(q.add("b"), "add returns true (unbounded)");
        check(q.size() == 2, "size after offer/add");
        expectThrows(NullPointerException.class, () -> q.offer(null), "offer(null) NPE");
        expectThrows(NullPointerException.class, () -> q.add(null), "add(null) NPE");
    }

    static void testPeekElement() {
        Queue<String> q = new ArrayDeque<>(Arrays.asList("a", "b"));
        check("a".equals(q.peek()), "peek head (no remove)");
        check("a".equals(q.element()), "element head (no remove)");
        check(q.size() == 2, "peek/element do not remove");
    }

    static void testPollRemove() {
        Queue<String> q = new ArrayDeque<>(Arrays.asList("a", "b"));
        check("a".equals(q.poll()), "poll removes head");
        check("b".equals(q.remove()), "remove removes head");
        check(q.isEmpty(), "emptied after poll/remove");
    }

    static void testEmptySemantics() {
        Queue<String> q = new ArrayDeque<>();
        check(q.peek() == null, "peek on empty returns null");
        check(q.poll() == null, "poll on empty returns null");
        expectThrows(NoSuchElementException.class, q::element, "element on empty NSEE");
        expectThrows(NoSuchElementException.class, q::remove, "remove on empty NSEE");
    }

    static void testCollectionContract() {
        Queue<String> q = new ArrayDeque<>(Arrays.asList("a", "b", "c"));
        check(q.contains("b") && !q.contains("z"), "contains");
        check(q.remove("b") && q.size() == 2, "remove(Object)");
        Iterator<String> it = q.iterator();
        check("a".equals(it.next()), "iterator yields head first");
    }

    // ---- contract deep-dive: six-method two-state table --------------------

    static void testSixMethodTwoStateTable() {
        // Non-empty state: all six Queue methods on the same 3-element queue.
        Queue<String> q = new ArrayDeque<>(Arrays.asList("a", "b", "c"));
        check(q.add("d"), "non-empty add returns true (unbounded)");
        check(q.size() == 4, "add grows size");
        check(q.offer("e"), "non-empty offer returns true (unbounded)");
        check(q.size() == 5, "offer grows size");
        check("a".equals(q.element()), "element returns head");
        check(q.size() == 5, "element does not remove");
        check("a".equals(q.peek()), "peek returns head");
        check(q.size() == 5, "peek does not remove");
        check("a".equals(q.remove()), "remove returns and removes head");
        check(q.size() == 4 && "b".equals(q.peek()), "remove shifts head");
        check("b".equals(q.poll()), "poll returns and removes head");
        check(q.size() == 3 && "c".equals(q.peek()), "poll shifts head");

        // Empty state: stable repeated nulls, never exceptions, from poll/peek.
        Queue<String> e = new ArrayDeque<>();
        check(e.peek() == null && e.peek() == null && e.peek() == null,
                "repeated peek on empty stable null");
        check(e.poll() == null && e.poll() == null,
                "repeated poll on empty stable null (no exception)");
        check(e.add("x") && "x".equals(e.poll()) && e.poll() == null,
                "add after empty drain works, then null again");

        // Reuse after full drain.
        Queue<String> r = new ArrayDeque<>(Arrays.asList("1", "2"));
        r.poll();
        r.poll();
        check(r.isEmpty() && r.peek() == null, "drained queue is empty");
        check(r.offer("3") && "3".equals(r.peek()), "offer after drain");
    }

    static void testFifoDiscipline() {
        Queue<Integer> q = new ArrayDeque<>();
        int[] in = {3, 1, 4, 1, 5, 9, 2, 6};
        for (int v : in) q.offer(v);
        for (int v : in) check(q.poll() == v, "FIFO extraction order for " + v);
        check(q.isEmpty(), "FIFO drained");

        // Interleaved enqueue/dequeue keeps FIFO order.
        Queue<Integer> iq = new ArrayDeque<>();
        iq.offer(1);
        check(iq.poll() == 1, "interleave: poll before later offers");
        iq.offer(2);
        iq.offer(3);
        check(iq.poll() == 2, "interleave: FIFO survives mixing");
        iq.offer(4);
        check(iq.poll() == 3 && iq.poll() == 4, "interleave: drain remaining in order");
    }

    static void testCrossCarrierNullSemantics() {
        // Carrier 1: ArrayDeque rejects null elements.
        Queue<String> ad = new ArrayDeque<>();
        expectThrows(NullPointerException.class, () -> ad.offer(null), "ArrayDeque offer(null) NPE");
        expectThrows(NullPointerException.class, () -> ad.add(null), "ArrayDeque add(null) NPE");
        check(ad.isEmpty(), "failed null offer leaves queue empty");

        // Carrier 2: LinkedList (also a Queue) tolerates null elements.
        Queue<String> ll = new LinkedList<>(Arrays.asList("p", null, "q"));
        check(ll.size() == 3, "LinkedList queue holds null element");
        check(ll.contains(null), "LinkedList queue contains(null) true");
        check("p".equals(ll.peek()), "peek sees real head before null element");
        check("p".equals(ll.poll()), "head before null polls normally");
        check(ll.poll() == null, "null element is pollable (null return is ambiguous here)");
        check("q".equals(ll.poll()), "element after null survives");
    }

    static void testRemoveObjectSemantics() {
        Queue<String> q = new ArrayDeque<>(Arrays.asList("h", "m", "t"));
        check(q.remove("m") && q.size() == 2, "remove middle by Object");
        check(q.remove("h") && "t".equals(q.peek()), "remove head by Object shifts head");
        check(q.remove("t") && q.isEmpty(), "remove last by Object empties");
        check(!q.remove("zzz"), "remove absent returns false");
        Queue<String> q2 = new ArrayDeque<>(Arrays.asList("a", "b", "a"));
        check(q2.remove("a") && "b".equals(q2.peek()) && q2.size() == 2,
                "remove(Object) removes only first occurrence");
        check(q2.containsAll(Arrays.asList("b", "a")), "containsAll subset");
        check(!q2.containsAll(Arrays.asList("b", "z")), "containsAll non-subset false");
    }

    static void testInterfaceShape() throws Exception {
        Class<?> c = Class.forName("java.util.Queue");
        check(c.isInterface(), "Queue is an interface");
        check(java.util.Collection.class.isAssignableFrom(c), "Queue extends Collection");
        // Queue itself provides no default implementations: all six core methods abstract.
        int defaults = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (m.isDefault()) defaults++;
        check(defaults == 0, "Queue declares no default methods (all six abstract)");
        for (String m : new String[] {"add", "offer"})
            check(java.lang.reflect.Modifier.isAbstract(
                    c.getDeclaredMethod(m, Object.class).getModifiers()), m + " abstract");
        for (String m : new String[] {"remove", "poll", "element", "peek"})
            check(java.lang.reflect.Modifier.isAbstract(
                    c.getDeclaredMethod(m).getModifiers()), m + " abstract");
    }

    public static void main(String[] args) throws Exception {
        testOfferAdd();
        testPeekElement();
        testPollRemove();
        testEmptySemantics();
        testCollectionContract();
        testSixMethodTwoStateTable();
        testFifoDiscipline();
        testCrossCarrierNullSemantics();
        testRemoveObjectSemantics();
        testInterfaceShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
