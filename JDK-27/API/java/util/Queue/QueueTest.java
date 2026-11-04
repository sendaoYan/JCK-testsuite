/*
 * @test
 * @summary java.util.Queue conformance: white-box structure + black-box behavior.
 * @run main QueueTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

public class QueueTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Queue");
        check(c.isInterface(), "is an interface");
        check(java.util.Collection.class.isAssignableFrom(c), "extends Collection");
        for (String m : new String[] {"add", "offer"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()),
                    m + " abstract");
        for (String m : new String[] {"remove", "poll", "element", "peek"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
    }

    static void testBlackBox() {
        Queue<String> q = new ArrayDeque<>();
        check(q.offer("a") && q.add("b"), "offer/add");
        check("a".equals(q.peek()) && "a".equals(q.element()), "peek/element no remove");
        check("a".equals(q.poll()), "poll removes head");
        check("b".equals(q.remove()), "remove removes head");
        check(q.peek() == null && q.poll() == null, "empty peek/poll null");
        expectThrows(q::element, NoSuchElementException.class, "empty element");
        expectThrows(q::remove, NoSuchElementException.class, "empty remove");
        expectThrows(() -> q.offer(null), NullPointerException.class, "offer(null) NPE");
        expectThrows(() -> q.add(null), NullPointerException.class, "add(null) NPE");
    }

    // ---- contract deep-dive: six-method two-state table --------------------

    static void testSixMethodTwoStateTable() {
        Queue<String> q = new ArrayDeque<>(List.of("a", "b", "c"));
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

        Queue<String> e = new ArrayDeque<>();
        check(e.peek() == null && e.peek() == null && e.peek() == null,
                "repeated peek on empty stable null");
        check(e.poll() == null && e.poll() == null,
                "repeated poll on empty stable null (no exception)");
        check(e.add("x") && "x".equals(e.poll()) && e.poll() == null,
                "add after empty drain works, then null again");

        Queue<String> r = new ArrayDeque<>(List.of("1", "2"));
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
        expectThrows(() -> ad.offer(null), NullPointerException.class, "ArrayDeque offer(null) NPE");
        expectThrows(() -> ad.add(null), NullPointerException.class, "ArrayDeque add(null) NPE");
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
        Queue<String> q = new ArrayDeque<>(List.of("h", "m", "t"));
        check(q.remove("m") && q.size() == 2, "remove middle by Object");
        check(q.remove("h") && "t".equals(q.peek()), "remove head by Object shifts head");
        check(q.remove("t") && q.isEmpty(), "remove last by Object empties");
        check(!q.remove("zzz"), "remove absent returns false");
        Queue<String> q2 = new ArrayDeque<>(List.of("a", "b", "a"));
        check(q2.remove("a") && "b".equals(q2.peek()) && q2.size() == 2,
                "remove(Object) removes only first occurrence");
        check(q2.containsAll(List.of("b", "a")), "containsAll subset");
        check(!q2.containsAll(List.of("b", "z")), "containsAll non-subset false");
    }

    static void testInterfaceShape() throws Exception {
        Class<?> c = Class.forName("java.util.Queue");
        // Queue provides no default implementations: all six core methods abstract.
        int defaults = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (m.isDefault()) defaults++;
        check(defaults == 0, "Queue declares no default methods (all six abstract)");
        // Queue did NOT join the SequencedCollection hierarchy in JDK 21+.
        Class<?> sc = Class.forName("java.util.SequencedCollection");
        check(!sc.isAssignableFrom(c), "Queue is NOT a SequencedCollection");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSixMethodTwoStateTable();
        testFifoDiscipline();
        testCrossCarrierNullSemantics();
        testRemoveObjectSemantics();
        testInterfaceShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
