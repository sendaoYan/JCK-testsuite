/*
 * @test
 * @summary java.util.FormatFlagsConversionMismatchException conformance: white-box structure + black-box behavior.
 * @run main FormatFlagsConversionMismatchExceptionTest
 */

import java.util.FormatFlagsConversionMismatchException;
import java.util.Formatter;
import java.util.IllegalFormatException;

public class FormatFlagsConversionMismatchExceptionTest {
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
        Class<?> c = Class.forName("java.util.FormatFlagsConversionMismatchException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        c.getDeclaredConstructor(String.class, char.class);
        check(c.getDeclaredMethod("getFlags").getReturnType() == String.class, "getFlags String");
        check(c.getDeclaredMethod("getConversion").getReturnType() == char.class, "getConversion char");
    }

    static void testBlackBox() {
        FormatFlagsConversionMismatchException e =
                new FormatFlagsConversionMismatchException("#", 's');
        check("#".equals(e.getFlags()), "getFlags");
        check(e.getConversion() == 's', "getConversion");
        check("Conversion = s, Flags = #".equals(e.getMessage()), "getMessage");

        final Formatter f = new Formatter();
        expectThrows(() -> f.format("%#s", "x"), FormatFlagsConversionMismatchException.class,
                "flag/conversion mismatch");
        check(true, "instanceof IllegalFormatException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
