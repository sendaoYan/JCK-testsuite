/*
 * @test
 * @summary java.util.Iterator conformance: white-box structure + black-box behavior.
 * @run main IteratorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class IteratorTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Iterator");
        check(c.isInterface(), "is an interface");
        check(Modifier.isAbstract(c.getDeclaredMethod("hasNext").getModifiers()), "hasNext abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("next").getModifiers()), "next abstract");
        Method remove = c.getDeclaredMethod("remove");
        check(!Modifier.isAbstract(remove.getModifiers()), "remove default");
        Method fer = c.getDeclaredMethod("forEachRemaining", Consumer.class);
        check(!Modifier.isAbstract(fer.getModifiers()), "forEachRemaining default");
        check(fer.getReturnType() == void.class, "forEachRemaining void");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. hasNext/next
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = l.iterator();
        check(it.hasNext() && it.next().equals("a"), "next 1");
        check(it.next().equals("b") && it.next().equals("c"), "next 2/3");
        check(!it.hasNext(), "exhausted");

        // 2. next after exhaustion throws NoSuchElementException
        expectThrows(it::next, NoSuchElementException.class, "next after exhaustion");

        // 3. remove removes last-returned element
        List<String> l2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it2 = l2.iterator();
        it2.next();
        it2.remove();
        check(l2.equals(Arrays.asList("b", "c")), "remove removes last-returned");

        // 4. forEachRemaining consumes the remainder
        List<String> l3 = new ArrayList<>(Arrays.asList("x", "y", "z"));
        Iterator<String> it3 = l3.iterator();
        it3.next();
        final List<String> seen = new ArrayList<>();
        it3.forEachRemaining(seen::add);
        check(seen.equals(Arrays.asList("y", "z")), "forEachRemaining consumes the rest");

        // 5. default remove throws UnsupportedOperationException when unsupported
        Iterator<String> ro = List.of("q").iterator();
        ro.next();
        expectThrows(ro::remove, UnsupportedOperationException.class, "immutable iterator remove");

        // 6. empty iterator
        Iterator<String> empty = List.<String>of().iterator();
        check(!empty.hasNext(), "empty iterator hasNext false");
        expectThrows(empty::next, NoSuchElementException.class, "empty next throws");

        // 7. remove without next / double remove throw IllegalStateException
        Iterator<String> it4 = new ArrayList<>(Arrays.asList("a", "b")).iterator();
        expectThrows(it4::remove, IllegalStateException.class, "remove before next ISE");
        it4.next();
        it4.remove();
        expectThrows(it4::remove, IllegalStateException.class, "double remove ISE");
    }

    // ---------- contract deepening (appended; nothing above altered) ----------

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
        expectThrows(b::remove, UnsupportedOperationException.class,
                "default remove UOE before any next");
        b.next();
        expectThrows(b::remove, UnsupportedOperationException.class,
                "default remove UOE even after next");
        Bare c = new Bare("a", "b");
        expectThrows(() -> c.forEachRemaining(null), NullPointerException.class,
                "default forEachRemaining(null) NPE while elements remain");
        final List<String> seen = new ArrayList<>();
        c.forEachRemaining(seen::add);
        check(seen.equals(List.of("a", "b")),
              "default forEachRemaining consumes via hasNext/next in order");
        check(!c.hasNext(), "iterator exhausted after forEachRemaining");
        final List<String> none = new ArrayList<>();
        new Bare().forEachRemaining(none::add);
        check(none.isEmpty(), "forEachRemaining on exhausted iterator is a no-op");
        expectThrows(new Bare()::next, NoSuchElementException.class, "empty iterator next NSEE");
    }

    static void testHasNextStability() {
        List<String> l = new ArrayList<>(List.of("a", "b"));
        Iterator<String> it = l.iterator();
        check(it.hasNext() && it.hasNext() && it.hasNext(),
              "repeated hasNext is idempotent (no state advance)");
        it.next();
        check(it.hasNext() && it.hasNext(), "hasNext true mid-iteration");
        it.next();
        check(!it.hasNext() && !it.hasNext(), "hasNext consistently false at end");
        expectThrows(it::next, NoSuchElementException.class, "next at exhaustion NSEE");
    }

    static void testRemoveLifecycle() {
        List<String> l = new ArrayList<>(List.of("a", "b", "c"));
        Iterator<String> it = l.iterator();
        it.next();
        it.next();
        it.remove();
        check(l.equals(List.of("a", "c")), "remove mid-iteration drops last returned");
        it.next();
        it.remove();
        check(l.equals(List.of("a")), "remove after final next is legal");
        expectThrows(it::remove, IllegalStateException.class, "second consecutive remove ISE");
        expectThrows(it::next, NoSuchElementException.class, "next after final remove NSEE");
        Iterator<String> it2 = l.iterator();
        it2.next();
        it2.remove();
        check(l.isEmpty(), "sole element removed via iterator");
        expectThrows(it2::next, NoSuchElementException.class, "no more elements after sole removal");
    }

    static void testConsumerExceptionRelay() {
        List<String> l = new ArrayList<>(List.of("a", "b", "c"));
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
        expectThrows(it::remove, UnsupportedOperationException.class,
                "Arrays.asList iterator remove UOE");
        expectThrows(new ArrayList<String>().iterator()::next, NoSuchElementException.class,
                "empty collection iterator next NSEE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testIteratorDefaults();
        testHasNextStability();
        testRemoveLifecycle();
        testConsumerExceptionRelay();
        testUnsupportedRemoveFlavors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
