/*
 * @test
 * @summary java.lang.invoke.StringConcatException conformance: white-box + black-box behavior.
 * @run main StringConcatExceptionTest
 */

import java.lang.invoke.StringConcatException;
import java.lang.invoke.StringConcatFactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class StringConcatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.StringConcatException");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int ctors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) ctors++;
        }
        check(ctors == 2, "two public constructors");
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(String.class, Throwable.class);
        check(c.getDeclaredField("serialVersionUID").getType() == long.class,
              "serialVersionUID declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        check(new StringConcatException("m") instanceof Exception, "is an Exception");
        check(new StringConcatException("m") instanceof java.io.Serializable,
              "Serializable by inheritance");

        // message-only constructor
        StringConcatException e1 = new StringConcatException("bad recipe");
        check("bad recipe".equals(e1.getMessage()), "message kept");
        check(e1.getCause() == null, "no cause");

        // message + cause constructor
        Throwable cause = new IllegalStateException("inner");
        StringConcatException e2 = new StringConcatException("msg", cause);
        check("msg".equals(e2.getMessage()), "message with cause");
        check(e2.getCause() == cause, "cause identity");
        check(e2.toString().startsWith("java.lang.invoke.StringConcatException: msg"),
              "toString format");

        // suppressed and stack trace behave as a normal exception
        e1.addSuppressed(new RuntimeException("s"));
        check(e1.getSuppressed().length == 1, "suppressed supported");
        check(e1.getStackTrace().length > 0, "stack trace captured");

        // thrown by the factory for a recipe whose slots do not match the arity
        try {
            StringConcatFactory.makeConcatWithConstants(MethodHandles.lookup(), "concat",
                    MethodType.methodType(String.class, int.class, int.class), "\u0001");
            check(false, "linkage failure must throw");
        } catch (StringConcatException e) {
            check(e.getMessage() != null && !e.getMessage().isEmpty(),
                  "factory failure carries a message");
            check(e.getCause() == null || e.getCause() instanceof Throwable,
                  "cause is either absent or a real throwable");
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
