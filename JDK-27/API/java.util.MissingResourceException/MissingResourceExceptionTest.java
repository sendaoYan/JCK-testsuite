/*
 * @test
 * @summary java.util.MissingResourceException conformance: white-box structure + black-box behavior.
 * @run main MissingResourceExceptionTest
 */

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MissingResourceExceptionTest {
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
        Class<?> c = Class.forName("java.util.MissingResourceException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        c.getDeclaredConstructor(String.class, String.class, String.class);
        check(c.getDeclaredMethod("getClassName").getReturnType() == String.class,
                "getClassName String");
        check(c.getDeclaredMethod("getKey").getReturnType() == String.class, "getKey String");
    }

    static void testBlackBox() {
        MissingResourceException e = new MissingResourceException("msg", "com.foo.Bundle", "k1");
        check("com.foo.Bundle".equals(e.getClassName()), "getClassName");
        check("k1".equals(e.getKey()), "getKey");
        check("msg".equals(e.getMessage()), "getMessage");

        expectThrows(() -> ResourceBundle.getBundle("no.such.b9x", Locale.ROOT),
                MissingResourceException.class, "missing bundle");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
