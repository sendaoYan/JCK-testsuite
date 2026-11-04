/*
 * @test
 * @summary java.util.IllegalFormatFlagsException conformance: white-box structure + black-box behavior.
 * @run main IllegalFormatFlagsExceptionTest
 */

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.IllegalFormatFlagsException;

public class IllegalFormatFlagsExceptionTest {
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
        Class<?> c = Class.forName("java.util.IllegalFormatFlagsException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        c.getDeclaredConstructor(String.class);
        check(c.getDeclaredMethod("getFlags").getReturnType() == String.class, "getFlags String");
    }

    static void testBlackBox() {
        IllegalFormatFlagsException e = new IllegalFormatFlagsException("+ ");
        check("+ ".equals(e.getFlags()), "getFlags");
        check("Flags = '+ '".equals(e.getMessage()), "getMessage");

        final Formatter f = new Formatter();
        expectThrows(() -> f.format("%+ d", 1), IllegalFormatFlagsException.class, "illegal flags");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
