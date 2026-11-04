/*
 * @test
 * @summary java.util.Iterator API conformance: hasNext/next/remove, next() after exhaustion
 *          throwing NoSuchElementException, and the Java 8 default forEachRemaining.
 * @run main IteratorTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class IteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNextRemove() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = l.iterator();
        check(it.hasNext() && it.next().equals("a"), "next 1");
        it.remove();
        check(l.equals(Arrays.asList("b", "c")), "remove removes last-returned");
        check(it.next().equals("b") && it.hasNext(), "next 2");
        check(it.next().equals("c") && !it.hasNext(), "next 3 / exhausted");
        try {
            it.next();
            throw new AssertionError("next after exhaustion: expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            passed++;
        }
    }

    static void testForEachRemaining() {
        final List<String> seen = new ArrayList<>();
        new ArrayList<>(Arrays.asList("x", "y")).iterator().forEachRemaining(seen::add);
        check(seen.equals(Arrays.asList("x", "y")), "forEachRemaining");
    }

    static void testRemoveWithoutNext() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b"));
        Iterator<String> it = l.iterator();
        try {
            it.remove();
            throw new AssertionError("remove before next: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
        it.next();
        it.remove();
        try {
            it.remove();
            throw new AssertionError("double remove: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(l.equals(Arrays.asList("b")), "single remove applied");
    }

    // ---------- contract deepening (appended; nothing above altered) ----------

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

    /** Bare iterator implementing only hasNext/next: exercises Iterator defaults. */
    static class Bare implements Iterator<String> {
        final String[] data;
        int i;
        Bare(String... data) { this.data = data; }
        public boolean hasNext() { return i < data.length; }
        public String next() {
            if (!hasNext()) throw new NoSuchElementException();
            return data[i++];
        }
    }

    static void testIteratorDefaults() {
        Bare b = new Bare("a", "b");
        expectThrows(UnsupportedOperationException.class, b::remove,
                "default remove UOE before any next");
        b.next();
        expectThrows(UnsupportedOperationException.class, b::remove,
                "default remove UOE even after next");
        Bare c = new Bare("a", "b");
        expectThrows(NullPointerException.class, () -> c.forEachRemaining(null),
                "default forEachRemaining(null) NPE while elements remain");
        final List<String> seen = new ArrayList<>();
        c.forEachRemaining(seen::add);
        check(seen.equals(Arrays.asList("a", "b")),
              "default forEachRemaining consumes via hasNext/next in order");
        check(!c.hasNext(), "iterator exhausted after forEachRemaining");
        final List<String> none = new ArrayList<>();
        new Bare().forEachRemaining(none::add);
        check(none.isEmpty(), "forEachRemaining on exhausted iterator is a no-op");
        expectThrows(NoSuchElementException.class, new Bare()::next, "empty iterator next NSEE");
    }

    static void testHasNextStability() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b"));
        Iterator<String> it = l.iterator();
        check(it.hasNext() && it.hasNext() && it.hasNext(),
              "repeated hasNext is idempotent (no state advance)");
        it.next();
        check(it.hasNext() && it.hasNext(), "hasNext true mid-iteration");
        it.next();
        check(!it.hasNext() && !it.hasNext(), "hasNext consistently false at end");
        expectThrows(NoSuchElementException.class, it::next, "next at exhaustion NSEE");
    }

    static void testRemoveLifecycle() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = l.iterator();
        it.next();
        it.next();
        it.remove();
        check(l.equals(Arrays.asList("a", "c")), "remove mid-iteration drops last returned");
        it.next();
        it.remove();
        check(l.equals(Arrays.asList("a")), "remove after final next is legal");
        expectThrows(IllegalStateException.class, it::remove, "second consecutive remove ISE");
        expectThrows(NoSuchElementException.class, it::next, "next after final remove NSEE");
        Iterator<String> it2 = l.iterator();
        it2.next();
        it2.remove();
        check(l.isEmpty(), "sole element removed via iterator");
        expectThrows(NoSuchElementException.class, it2::next, "no more elements after sole removal");
    }

    static void testConsumerExceptionRelay() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = l.iterator();
        it.next();
        final RuntimeException boom = new IllegalStateException("stop");
        try {
            it.forEachRemaining(s -> { throw boom; });
            throw new AssertionError("consumer exception must propagate");
        } catch (IllegalStateException e) {
            check(e == boom, "ArrayList iterator relays original consumer exception");
        }
        final RuntimeException boom2 = new NumberFormatException("x");
        try {
            new Bare("p", "q").forEachRemaining(s -> { throw boom2; });
            throw new AssertionError("probe consumer exception must propagate");
        } catch (NumberFormatException e) {
            check(e == boom2, "default forEachRemaining relays consumer exception");
        }
    }

    static void testUnsupportedRemoveFlavors() {
        // Arrays.asList is a fixed-size list: its iterator does not support remove
        List<String> fixed = Arrays.asList("a", "b");
        Iterator<String> it = fixed.iterator();
        it.next();
        expectThrows(UnsupportedOperationException.class, it::remove,
                "Arrays.asList iterator remove UOE");
        expectThrows(NoSuchElementException.class, new ArrayList<String>().iterator()::next,
                "empty collection iterator next NSEE");
    }

    public static void main(String[] args) {
        testNextRemove();
        testForEachRemaining();
        testRemoveWithoutNext();
        testIteratorDefaults();
        testHasNextStability();
        testRemoveLifecycle();
        testConsumerExceptionRelay();
        testUnsupportedRemoveFlavors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
