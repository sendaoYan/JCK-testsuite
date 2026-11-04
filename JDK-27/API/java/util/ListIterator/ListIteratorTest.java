/*
 * @test
 * @summary java.util.ListIterator conformance: white-box structure + black-box behavior.
 * @run main ListIteratorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ListIteratorTest {
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
        Class<?> c = Class.forName("java.util.ListIterator");
        check(c.isInterface(), "is an interface");
        check(java.util.Iterator.class.isAssignableFrom(c), "extends Iterator");
        for (String m : new String[] {"hasPrevious", "previous", "nextIndex", "previousIndex"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("set", Object.class).getModifiers()),
                "set abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("add", Object.class).getModifiers()),
                "add abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("next").getModifiers()), "next abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("hasNext").getModifiers()), "hasNext abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. index bookkeeping
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        check(!it.hasPrevious() && it.hasNext(), "initial position");
        check(it.nextIndex() == 0 && it.previousIndex() == -1, "indexes at start");
        check(it.next().equals("a"), "next");
        check(it.nextIndex() == 1, "nextIndex after next");
        check(it.previous().equals("a"), "previous");
        check(it.previousIndex() == -1, "previousIndex after previous");

        // 2. bidirectional traversal to the end
        ListIterator<String> it2 = l.listIterator();
        check(it2.next().equals("a") && it2.next().equals("b") && it2.next().equals("c"),
                "forward to end");
        check(it2.nextIndex() == 3, "nextIndex at end");
        expectThrows(it2::next, NoSuchElementException.class, "next past end");
        check(it2.hasPrevious() && it2.previous().equals("c"), "previous from end");

        // 3. set replaces last-returned
        List<String> l3 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it3 = l3.listIterator();
        it3.next();
        it3.next();
        it3.set("B");
        check(l3.get(1).equals("B"), "set replaces last-returned");

        // 4. add inserts before the cursor
        it3.add("x");
        check(l3.equals(Arrays.asList("a", "B", "x", "c")), "add inserts");
        check(it3.previous().equals("x"), "previous returns inserted element");

        // 5. remove removes last-returned
        it3.remove();
        check(l3.equals(Arrays.asList("a", "B", "c")), "remove removes last-returned");

        // 6. listIterator(index) starts at a position
        ListIterator<String> mid = l.listIterator(1);
        check(mid.nextIndex() == 1, "listIterator(index) starts at index");
        check(mid.next().equals("b"), "listIterator(index) next");

        // 7. IllegalStateException when there is no last-returned element
        ListIterator<String> it4 = l.listIterator();
        expectThrows(() -> it4.set("z"), IllegalStateException.class, "set before next ISE");
        expectThrows(it4::remove, IllegalStateException.class, "remove before next ISE");
        it4.next();
        it4.add("x");
        expectThrows(() -> it4.set("z"), IllegalStateException.class, "set after add ISE");
        expectThrows(it4::remove, IllegalStateException.class, "remove after add ISE");
        it4.previous();
        it4.remove();
        expectThrows(it4::remove, IllegalStateException.class, "double remove ISE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
