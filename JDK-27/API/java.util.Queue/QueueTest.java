/*
 * @test
 * @summary java.util.Queue conformance: white-box structure + black-box behavior.
 * @run main QueueTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
