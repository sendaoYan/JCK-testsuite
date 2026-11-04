/*
 * @test
 * @summary java.util.AbstractQueue conformance: white-box structure + black-box behavior.
 * @run main AbstractQueueTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

public class AbstractQueueTest {
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

    /** Minimal concrete subclass implementing only offer/poll/peek/iterator/size. */
    static class Q extends AbstractQueue<String> {
        private final LinkedList<String> list = new LinkedList<>();
        @Override public boolean offer(String e) { return list.offer(e); }
        @Override public String poll() { return list.poll(); }
        @Override public String peek() { return list.peek(); }
        @Override public Iterator<String> iterator() { return list.iterator(); }
        @Override public int size() { return list.size(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.AbstractQueue");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.util.AbstractCollection.class.isAssignableFrom(c), "extends AbstractCollection");
        check(Queue.class.isAssignableFrom(c), "implements Queue");
        check(!Modifier.isAbstract(c.getDeclaredMethod("add", Object.class).getModifiers()), "add concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("remove").getModifiers()), "remove concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("element").getModifiers()), "element concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("clear").getModifiers()), "clear concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("addAll", java.util.Collection.class).getModifiers()),
                "addAll concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. add == offer, element == peek (non-throwing), remove == poll
        Q q = new Q();
        check(q.add("a") && q.add("b"), "add appends");
        check("a".equals(q.element()), "element returns head without removing");
        check("a".equals(q.remove()), "remove returns and removes head");
        check("b".equals(q.poll()), "poll returns and removes head");
        check(q.size() == 0, "emptied");

        // 2. element()/remove() throw NoSuchElementException on empty; peek/poll return null
        expectThrows(q::element, NoSuchElementException.class, "element on empty");
        expectThrows(q::remove, NoSuchElementException.class, "remove on empty");
        check(q.peek() == null && q.poll() == null, "peek/poll null on empty");

        // 3. clear empties
        q.add("x");
        q.add("y");
        q.clear();
        check(q.isEmpty() && q.size() == 0, "clear empties");

        // 4. addAll adds each element (returns true when queue changed)
        Q q2 = new Q();
        List<String> src = new ArrayList<>(Arrays.asList("m", "n"));
        check(q2.addAll(src), "addAll returns true");
        check(q2.size() == 2 && "m".equals(q2.peek()), "addAll adds in order");

        // 5. iterator() + AbstractCollection.toString integration
        check("[m, n]".equals(q2.toString()), "toString via iterator");
    }

    static void testAddWhenFullAndAddAllEdges() {
        // bounded queue: offer returns false when full -> add throws IllegalStateException
        class Bounded extends AbstractQueue<String> {
            final LinkedList<String> list = new LinkedList<>();
            public boolean offer(String e) {
                if (list.size() >= 1) return false;
                return list.offer(e);
            }
            public String poll() { return list.poll(); }
            public String peek() { return list.peek(); }
            public Iterator<String> iterator() { return list.iterator(); }
            public int size() { return list.size(); }
        }
        Bounded b = new Bounded();
        check(b.add("a"), "add first succeeds");
        expectThrows(() -> b.add("b"), IllegalStateException.class, "add when full throws ISE");

        Q q = new Q();
        expectThrows(() -> q.addAll(null), NullPointerException.class, "addAll(null) NPE");
        expectThrows(() -> q.addAll(q), IllegalArgumentException.class, "addAll(self) IAE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAddWhenFullAndAddAllEdges();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
