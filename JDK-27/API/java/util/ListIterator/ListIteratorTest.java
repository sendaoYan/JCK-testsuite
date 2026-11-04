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


    // ---- deepened II: 交替遍历 / add 可见性矩阵 / set-remove 状态机 /
    //      forEachRemaining / 越界 / CME -------------------------------------------

    static void testAlternatingTraversal() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        check(it.next().equals("a") && it.previous().equals("a"), "next then previous returns same");
        check(it.next().equals("a") && it.next().equals("b"), "forward again");
        check(it.previous().equals("b") && it.next().equals("b"), "back-and-forth at middle");
        check(it.next().equals("c") && !it.hasNext(), "reach end");
        check(it.previous().equals("c") && it.previous().equals("b") && it.previous().equals("a"),
              "walk fully back");
        check(!it.hasPrevious(), "back at start");
    }

    static void testAddVisibilityMatrix() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "c"));
        ListIterator<String> it = l.listIterator(1);
        it.add("B");
        check(l.equals(Arrays.asList("a", "B", "c")), "add inserted at cursor");
        check(it.nextIndex() == 2 && it.previousIndex() == 1, "cursor shifted right by add");
        check(it.next().equals("c"), "next skips the inserted element");
        check(it.previous().equals("c") && it.previous().equals("B"),
              "previous walks back over inserted element");
        it.set("Z");
        check(l.equals(Arrays.asList("a", "Z", "c")), "set after previous over inserted");
        ListIterator<String> head = l.listIterator();
        head.add("0");
        check(l.get(0).equals("0"), "add at head");
        ListIterator<String> tail = l.listIterator(l.size());
        tail.add("9");
        check(l.get(l.size() - 1).equals("9"), "add at tail");
    }

    static void testSetRemoveStateMatrix() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        it.next();
        it.set("A");
        check(l.get(0).equals("A"), "set visible immediately");
        it.remove();
        check(l.equals(Arrays.asList("b", "c")), "remove deletes last-returned");
        check(it.nextIndex() == 0, "cursor moved back after remove");
        it.next();
        it.previous();
        it.remove();
        check(l.equals(Arrays.asList("c")), "remove after previous");
        it.next();
        it.set(null);
        check(l.get(0) == null, "set to null");
        it.set("back");
        check(l.get(0).equals("back"), "set back from null");
    }

    static void testForEachRemaining() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        ListIterator<String> it = l.listIterator(1);
        StringBuilder sb = new StringBuilder();
        it.forEachRemaining(sb::append);
        check(sb.toString().equals("bcd"), "forEachRemaining from cursor 1");
        check(!it.hasNext(), "exhausted after forEachRemaining");
        check(it.previous().equals("d"), "previous still works after forEachRemaining");
    }

    static void testIndexConstructorBounds() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b"));
        check(l.listIterator(0).nextIndex() == 0, "listIterator(0)");
        check(l.listIterator(l.size()).previousIndex() == l.size() - 1, "listIterator(size) at tail");
        expectThrows(() -> l.listIterator(-1), IndexOutOfBoundsException.class, "listIterator(-1) IOOBE");
        expectThrows(() -> l.listIterator(3), IndexOutOfBoundsException.class, "listIterator(size+1) IOOBE");
    }

    static void testConcurrentModification() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        it.next();
        l.add("d");
        expectThrows(it::next, java.util.ConcurrentModificationException.class,
                "listIterator next after structural mod CME");
        List<String> l2 = new ArrayList<>(Arrays.asList("a", "b"));
        ListIterator<String> it2 = l2.listIterator();
        it2.next();
        l2.remove(0);
        expectThrows(it2::previous, java.util.ConcurrentModificationException.class,
                "listIterator previous after structural mod CME");
        List<String> l3 = new ArrayList<>(Arrays.asList("a", "b"));
        ListIterator<String> it3 = l3.listIterator();
        it3.next();
        it3.add("X");
        expectThrows(it3::remove, IllegalStateException.class, "remove right after add ISE");
        it3.previous();
        it3.remove();
        check(l3.equals(Arrays.asList("a", "b")), "own-modification path leaves consistent state");
    }

    static void testOnLinkedListToo() {
        // same contract on a different implementation
        List<String> l = new java.util.LinkedList<>(Arrays.asList("x", "y", "z"));
        ListIterator<String> it = l.listIterator(1);
        check(it.next().equals("y"), "LinkedList LI next");
        it.set("Y");
        check(l.get(1).equals("Y"), "LinkedList LI set");
        it.add("M");
        check(l.equals(Arrays.asList("x", "Y", "M", "z")), "LinkedList LI add");
        expectThrows(() -> { new java.util.LinkedList<String>().listIterator().previous(); },
                NoSuchElementException.class, "empty LinkedList previous NSEE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAlternatingTraversal();
        testAddVisibilityMatrix();
        testSetRemoveStateMatrix();
        testForEachRemaining();
        testIndexConstructorBounds();
        testConcurrentModification();
        testOnLinkedListToo();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
