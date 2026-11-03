/*
 * @test
 * @summary java.util.FormatterClosedException conformance: white-box structure + black-box behavior.
 * @run main FormatterClosedExceptionTest
 */

import java.util.Formatter;
import java.util.FormatterClosedException;

public class FormatterClosedExceptionTest {
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
        Class<?> c = Class.forName("java.util.FormatterClosedException");
        check(c.getSuperclass() == IllegalStateException.class, "extends IllegalStateException");
        c.getDeclaredConstructor();
        check(c.getDeclaredConstructors().length == 1, "single constructor");
    }

    static void testBlackBox() {
        check(new FormatterClosedException().getMessage() == null, "no-arg message null");
        final Formatter f = new Formatter();
        f.close();
        expectThrows(() -> f.format("%d", 1), FormatterClosedException.class, "format after close");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
