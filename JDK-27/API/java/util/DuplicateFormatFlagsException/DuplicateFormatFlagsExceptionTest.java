/*
 * @test
 * @summary java.util.DuplicateFormatFlagsException conformance: white-box structure + black-box behavior.
 * @run main DuplicateFormatFlagsExceptionTest
 */

import java.util.DuplicateFormatFlagsException;
import java.util.Formatter;
import java.util.IllegalFormatException;

public class DuplicateFormatFlagsExceptionTest {
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
        Class<?> c = Class.forName("java.util.DuplicateFormatFlagsException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        c.getDeclaredConstructor(String.class);
        check(c.getDeclaredMethod("getFlags").getReturnType() == String.class, "getFlags String");
    }

    static void testBlackBox() {
        DuplicateFormatFlagsException e = new DuplicateFormatFlagsException("+,");
        check("+,".equals(e.getFlags()), "getFlags");
        check("Flags = '+,'".equals(e.getMessage()), "getMessage");

        final Formatter f = new Formatter();
        expectThrows(() -> f.format("%,,d", 1), DuplicateFormatFlagsException.class, "duplicate flag");
        check(true, "instanceof IllegalFormatException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
