/*
 * @test
 * @summary java.util.UnknownFormatConversionException conformance: white-box structure + black-box behavior.
 * @run main UnknownFormatConversionExceptionTest
 */

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.UnknownFormatConversionException;

public class UnknownFormatConversionExceptionTest {
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
        Class<?> c = Class.forName("java.util.UnknownFormatConversionException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        c.getDeclaredConstructor(String.class);
        check(c.getDeclaredMethod("getConversion").getReturnType() == String.class,
                "getConversion String");
    }

    static void testBlackBox() {
        UnknownFormatConversionException e = new UnknownFormatConversionException("y");
        check("y".equals(e.getConversion()), "getConversion");
        check("Conversion = 'y'".equals(e.getMessage()), "getMessage");

        final Formatter f = new Formatter();
        expectThrows(() -> f.format("%y", 1), UnknownFormatConversionException.class,
                "unknown conversion");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
