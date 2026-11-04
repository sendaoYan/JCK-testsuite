/*
 * @test
 * @summary java.util.ConcurrentModificationException conformance: white-box structure + black-box behavior.
 * @run main ConcurrentModificationExceptionTest
 */

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public class ConcurrentModificationExceptionTest {
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
        Class<?> c = Class.forName("java.util.ConcurrentModificationException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(Throwable.class);
        c.getDeclaredConstructor(String.class, Throwable.class);
        check(true, "four constructors present");
    }

    static void testBlackBox() {
        check(new ConcurrentModificationException().getMessage() == null, "no-arg message null");
        check("boom".equals(new ConcurrentModificationException("boom").getMessage()),
                "String ctor message");
        Throwable cause = new IllegalStateException("c");
        check(cause == new ConcurrentModificationException(cause).getCause(), "Throwable ctor cause");
        check(cause == new ConcurrentModificationException("m", cause).getCause(),
                "String+Throwable ctor cause");

        List<String> l = new ArrayList<>();
        l.add("a");
        l.add("b");
        final Iterator<String> it = l.iterator();
        it.next();
        l.add("c");
        expectThrows(it::next, ConcurrentModificationException.class, "fail-fast iteration");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
