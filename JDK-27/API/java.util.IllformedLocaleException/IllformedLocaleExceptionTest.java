/*
 * @test
 * @summary java.util.IllformedLocaleException conformance: white-box structure + black-box behavior.
 * @run main IllformedLocaleExceptionTest
 */

import java.util.IllformedLocaleException;
import java.util.Locale;

public class IllformedLocaleExceptionTest {
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
        Class<?> c = Class.forName("java.util.IllformedLocaleException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(String.class, int.class);
        check(c.getDeclaredMethod("getErrorIndex").getReturnType() == int.class,
                "getErrorIndex int");
    }

    static void testBlackBox() {
        check(new IllformedLocaleException().getErrorIndex() == -1, "default errorIndex -1");
        check(new IllformedLocaleException("m").getErrorIndex() == -1, "String ctor errorIndex -1");
        check(new IllformedLocaleException("m", 3).getErrorIndex() == 3, "explicit errorIndex");
        check("m [at index 3]".equals(new IllformedLocaleException("m", 3).getMessage()),
                "message with index");

        expectThrows(() -> new Locale.Builder().setLanguage("abcdefghi"),
                IllformedLocaleException.class, "ill-formed language");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
