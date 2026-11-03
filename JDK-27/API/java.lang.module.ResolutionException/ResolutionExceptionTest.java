/*
 * @test
 * @summary java.lang.module.ResolutionException conformance: white-box + black-box behavior.
 * @run main ResolutionExceptionTest
 */

import java.lang.module.ResolutionException;

public class ResolutionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ResolutionException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int ctors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) ctors++;
        }
        check(ctors == 4, "four public constructors");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(Throwable.class);
        c.getDeclaredConstructor(String.class, Throwable.class);
        check(c.getDeclaredField("serialVersionUID").getType() == long.class,
              "serialVersionUID declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(new ResolutionException() instanceof RuntimeException, "is a RuntimeException");

        ResolutionException e0 = new ResolutionException();
        check(e0.getMessage() == null, "no-arg message null");
        check(e0.getCause() == null, "no-arg cause null");

        ResolutionException e1 = new ResolutionException("cycle detected");
        check("cycle detected".equals(e1.getMessage()), "String ctor message");
        check(e1.getCause() == null, "String ctor no cause");

        RuntimeException cause = new RuntimeException("cause");
        ResolutionException e2 = new ResolutionException(cause);
        check(e2.getCause() == cause, "Throwable ctor cause identity");
        check(e2.getMessage().equals(cause.toString()), "Throwable ctor message = cause.toString()");

        ResolutionException e3 = new ResolutionException("msg", cause);
        check("msg".equals(e3.getMessage()), "String+Throwable message");
        check(e3.getCause() == cause, "String+Throwable cause identity");

        check(e1.toString().startsWith("java.lang.module.ResolutionException: cycle detected"),
              "toString format");
        e1.addSuppressed(new IllegalStateException("s"));
        check(e1.getSuppressed().length == 1, "suppressed supported");

        // unchecked: can be thrown from methods with no throws clause
        try {
            throw new ResolutionException("read cyclic requires");
        } catch (RuntimeException re) {
            check(re instanceof ResolutionException, "caught as RuntimeException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
