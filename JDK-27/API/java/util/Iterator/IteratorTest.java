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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
