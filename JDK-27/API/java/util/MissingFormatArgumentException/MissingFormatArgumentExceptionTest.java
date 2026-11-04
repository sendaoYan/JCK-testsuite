/*
 * @test
 * @summary java.util.MissingFormatArgumentException conformance: white-box structure + black-box behavior.
 * @run main MissingFormatArgumentExceptionTest
 */

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.MissingFormatArgumentException;

public class MissingFormatArgumentExceptionTest {
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
        Class<?> c = Class.forName("java.util.MissingFormatArgumentException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        c.getDeclaredConstructor(String.class);
        check(c.getDeclaredMethod("getFormatSpecifier").getReturnType() == String.class,
                "getFormatSpecifier String");
    }

    static void testBlackBox() {
        MissingFormatArgumentException e = new MissingFormatArgumentException("%s");
        check("%s".equals(e.getFormatSpecifier()), "getFormatSpecifier");
        check("Format specifier '%s'".equals(e.getMessage()), "getMessage");

        final Formatter f = new Formatter();
        expectThrows(() -> f.format("%d %s", 1), MissingFormatArgumentException.class,
                "missing argument");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
