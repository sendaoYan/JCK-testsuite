/*
 * @test
 * @summary java.util.MissingFormatWidthException conformance: white-box structure + black-box behavior.
 * @run main MissingFormatWidthExceptionTest
 */

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.MissingFormatWidthException;

public class MissingFormatWidthExceptionTest {
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
        Class<?> c = Class.forName("java.util.MissingFormatWidthException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        c.getDeclaredConstructor(String.class);
        check(c.getDeclaredMethod("getFormatSpecifier").getReturnType() == String.class,
                "getFormatSpecifier String");
    }

    static void testBlackBox() {
        MissingFormatWidthException e = new MissingFormatWidthException("%-d");
        check("%-d".equals(e.getFormatSpecifier()), "getFormatSpecifier");
        check("%-d".equals(e.getMessage()), "getMessage");

        final Formatter f = new Formatter();
        expectThrows(() -> f.format("%-d", 1), MissingFormatWidthException.class,
                "missing width");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
