/*
 * @test
 * @summary java.util.IllegalFormatConversionException conformance: white-box structure + black-box behavior.
 * @run main IllegalFormatConversionExceptionTest
 */

import java.util.Formatter;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatException;

public class IllegalFormatConversionExceptionTest {
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
        Class<?> c = Class.forName("java.util.IllegalFormatConversionException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        c.getDeclaredConstructor(char.class, Class.class);
        check(c.getDeclaredMethod("getConversion").getReturnType() == char.class, "getConversion char");
        check(c.getDeclaredMethod("getArgumentClass").getReturnType() == Class.class,
                "getArgumentClass Class");
    }

    static void testBlackBox() {
        IllegalFormatConversionException e =
                new IllegalFormatConversionException('d', String.class);
        check(e.getConversion() == 'd', "getConversion");
        check(e.getArgumentClass() == String.class, "getArgumentClass");
        check("d != java.lang.String".equals(e.getMessage()), "getMessage");

        final Formatter f = new Formatter();
        expectThrows(() -> f.format("%d", "x"), IllegalFormatConversionException.class,
                "type mismatch");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
