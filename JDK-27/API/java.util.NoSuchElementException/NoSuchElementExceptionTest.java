/*
 * @test
 * @summary java.util.NoSuchElementException conformance: white-box structure + black-box behavior.
 * @run main NoSuchElementExceptionTest
 */

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class NoSuchElementExceptionTest {
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
        Class<?> c = Class.forName("java.util.NoSuchElementException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(Throwable.class);
        c.getDeclaredConstructor(String.class, Throwable.class);
        check(true, "four constructors present");
    }

    static void testBlackBox() {
        check(new NoSuchElementException().getMessage() == null, "no-arg message null");
        check("gone".equals(new NoSuchElementException("gone").getMessage()), "String ctor message");
        Throwable cause = new IllegalStateException("c");
        check(cause == new NoSuchElementException(cause).getCause(), "Throwable ctor cause");
        check(cause == new NoSuchElementException("m", cause).getCause(), "String+Throwable cause");

        Iterator<String> it = List.<String>of().iterator();
        expectThrows(it::next, NoSuchElementException.class, "empty iterator next");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
