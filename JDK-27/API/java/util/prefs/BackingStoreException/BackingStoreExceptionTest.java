/*
 * @test
 * @summary java.util.prefs.BackingStoreException conformance: white-box structure + black-box behavior.
 * @run main BackingStoreExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.prefs.BackingStoreException;

public class BackingStoreExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.prefs.BackingStoreException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "class not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Exception.class, "extends Exception");

        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 2, "two public constructors");
        c.getConstructor(String.class);
        c.getConstructor(Throwable.class);
        check(true, "(String) and (Throwable) ctors present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // (String) constructor
        BackingStoreException e1 = new BackingStoreException("boom");
        check(e1 instanceof Exception, "is Exception");
        check("boom".equals(e1.getMessage()), "getMessage");
        check(e1.getCause() == null, "no cause initially");

        // (Throwable) constructor: message mirrors cause.toString()
        RuntimeException cause = new RuntimeException("root");
        BackingStoreException e2 = new BackingStoreException(cause);
        check(e2.getCause() == cause, "cause wired");
        check("java.lang.RuntimeException: root".equals(e2.getMessage()), "message == cause toString");

        // throw / catch round-trip
        try {
            throw new BackingStoreException("thrown");
        } catch (BackingStoreException ex) {
            check("thrown".equals(ex.getMessage()), "caught message");
        }

        // a caught cause can be unwrapped through the exception
        try {
            throw new BackingStoreException(new IllegalStateException("inner"));
        } catch (BackingStoreException ex) {
            check(ex.getCause() instanceof IllegalStateException, "unwrapped cause type");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
