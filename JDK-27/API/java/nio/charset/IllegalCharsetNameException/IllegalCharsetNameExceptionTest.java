/*
 * @test
 * @summary java.nio.charset.IllegalCharsetNameException conformance: white-box structure + black-box name accessor.
 * @run main IllegalCharsetNameExceptionTest
 */

import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

public class IllegalCharsetNameExceptionTest {
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
        Class<?> c = Class.forName("java.nio.charset.IllegalCharsetNameException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == IllegalArgumentException.class, "superclass is IllegalArgumentException");
        check(c.getConstructor(String.class) != null, "String constructor present");
    }

    static void testBlackBox() {
        IllegalCharsetNameException e = new IllegalCharsetNameException("bad name!");
        check("bad name!".equals(e.getCharsetName()), "getCharsetName");
        check("bad name!".equals(e.getMessage()), "getMessage equals charset name");
        check(e instanceof IllegalArgumentException, "is IllegalArgumentException");
        check(e.getClass().getSuperclass() == IllegalArgumentException.class, "superclass is IllegalArgumentException");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");

        expect(IllegalCharsetNameException.class, () -> Charset.forName("bad name!"), "forName throws IllegalCharsetNameException");
        IllegalCharsetNameException viaForName = null;
        try {
            Charset.forName("bad name!");
        } catch (IllegalCharsetNameException ex) {
            viaForName = ex;
        }
        check("bad name!".equals(viaForName.getCharsetName()), "forName-thrown name preserved");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
