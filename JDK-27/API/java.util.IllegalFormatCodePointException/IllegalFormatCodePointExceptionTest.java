/*
 * @test
 * @summary java.util.IllegalFormatCodePointException conformance: white-box structure + black-box behavior.
 * @run main IllegalFormatCodePointExceptionTest
 */

import java.util.Formatter;
import java.util.IllegalFormatCodePointException;
import java.util.IllegalFormatException;

public class IllegalFormatCodePointExceptionTest {
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
        Class<?> c = Class.forName("java.util.IllegalFormatCodePointException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        c.getDeclaredConstructor(int.class);
        check(c.getDeclaredMethod("getCodePoint").getReturnType() == int.class, "getCodePoint int");
    }

    static void testBlackBox() {
        IllegalFormatCodePointException e = new IllegalFormatCodePointException(0x110000);
        check(e.getCodePoint() == 0x110000, "getCodePoint");
        check("Code point = 0x110000".equals(e.getMessage()), "getMessage");

        final Formatter f = new Formatter();
        expectThrows(() -> f.format("%c", 0x110000), IllegalFormatCodePointException.class,
                "invalid code point");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
