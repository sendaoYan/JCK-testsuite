/*
 * @test
 * @summary java.nio.charset.UnsupportedCharsetException conformance: white-box structure + black-box name accessor.
 * @run main UnsupportedCharsetExceptionTest
 */

import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class UnsupportedCharsetExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.UnsupportedCharsetException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == IllegalArgumentException.class, "superclass is IllegalArgumentException");
        check(c.getConstructor(String.class) != null, "String constructor present");
    }

    static void testBlackBox() {
        UnsupportedCharsetException e = new UnsupportedCharsetException("no-such-charset");
        check("no-such-charset".equals(e.getCharsetName()), "getCharsetName");
        check("no-such-charset".equals(e.getMessage()), "getMessage equals charset name");
        check(e instanceof IllegalArgumentException, "is IllegalArgumentException");
        check(e.getClass().getSuperclass() == IllegalArgumentException.class, "superclass is IllegalArgumentException");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");

        expect(UnsupportedCharsetException.class, () -> Charset.forName("definitely-not-a-real-charset"),
                "forName throws UnsupportedCharsetException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
