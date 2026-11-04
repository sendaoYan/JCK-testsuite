/*
 * @test
 * @summary java.util.IllegalFormatPrecisionException conformance: white-box structure + black-box behavior.
 * @run main IllegalFormatPrecisionExceptionTest
 */

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.IllegalFormatPrecisionException;

public class IllegalFormatPrecisionExceptionTest {
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
        Class<?> c = Class.forName("java.util.IllegalFormatPrecisionException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        c.getDeclaredConstructor(int.class);
        check(c.getDeclaredMethod("getPrecision").getReturnType() == int.class, "getPrecision int");
    }

    static void testBlackBox() {
        IllegalFormatPrecisionException e = new IllegalFormatPrecisionException(2);
        check(e.getPrecision() == 2, "getPrecision");
        check("2".equals(e.getMessage()), "getMessage");

        final Formatter f = new Formatter();
        expectThrows(() -> f.format("%.2d", 1), IllegalFormatPrecisionException.class,
                "precision on integer");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
