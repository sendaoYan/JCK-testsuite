/*
 * @test
 * @summary java.lang.invoke.LambdaConversionException conformance: white-box + black-box behavior.
 * @run main LambdaConversionExceptionTest
 */

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class LambdaConversionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public interface F {
        int len(String s);
    }

    static int body(String s) { return s.length(); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.LambdaConversionException");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int ctors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) ctors++;
        }
        check(ctors == 5, "five public constructors");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(String.class, Throwable.class);
        c.getDeclaredConstructor(Throwable.class);
        c.getDeclaredConstructor(String.class, Throwable.class, boolean.class, boolean.class);
        check(c.getDeclaredField("serialVersionUID").getType() == long.class,
              "serialVersionUID declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        check(new LambdaConversionException() instanceof Exception, "is an Exception");

        // message-only
        LambdaConversionException e1 = new LambdaConversionException("unsupported");
        check("unsupported".equals(e1.getMessage()), "message ctor");
        check(e1.getCause() == null, "no cause");
        check(e1.getStackTrace().length > 0, "stack trace captured by default");
        e1.addSuppressed(new RuntimeException("s"));
        check(e1.getSuppressed().length == 1, "suppression enabled by default");

        // cause-only
        Throwable cause = new IllegalArgumentException("inner");
        LambdaConversionException e2 = new LambdaConversionException(cause);
        check(e2.getCause() == cause, "cause identity");
        check(e2.getMessage().equals(cause.toString()), "message = cause.toString()");

        // message + cause
        LambdaConversionException e3 = new LambdaConversionException("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "msg+cause ctor");

        // full control ctor: no suppression, no stack trace
        LambdaConversionException e4 = new LambdaConversionException("m", cause, false, false);
        check(e4.getStackTrace().length == 0, "stack trace disabled: not captured");
        e4.addSuppressed(new RuntimeException("s"));
        check(e4.getSuppressed().length == 0, "suppression disabled: suppressed dropped");
        e4.addSuppressed(new RuntimeException());
        check(e4.getSuppressed().length == 0, "suppressed dropped when disabled");

        // real trigger: LambdaMetafactory rejects a non-matching SAM name
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        java.lang.invoke.MethodHandle impl = lookup.findStatic(
                LambdaConversionExceptionTest.class, "body",
                MethodType.methodType(int.class, String.class));
        try {
            LambdaMetafactory.metafactory(lookup, "len",
                    MethodType.methodType(String.class),
                    MethodType.methodType(int.class, String.class),
                    impl, MethodType.methodType(int.class, String.class));
            check(false, "conversion failure must throw");
        } catch (LambdaConversionException e) {
            check(e.getMessage() != null, "factory failure has a message");
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
