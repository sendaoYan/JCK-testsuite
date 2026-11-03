/*
 * @test
 * @summary java.lang.annotation.AnnotationFormatError conformance: white-box structure + black-box behavior.
 * @run main AnnotationFormatErrorTest
 */

import java.lang.annotation.AnnotationFormatError;

public class AnnotationFormatErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.AnnotationFormatError");
        check(Error.class.isAssignableFrom(c), "is an Error");
        check(c.getSuperclass() == Error.class, "direct subclass of Error");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 3, "three public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // (String)
        AnnotationFormatError e1 = new AnnotationFormatError("bad annotation");
        check("bad annotation".equals(e1.getMessage()), "(String) message");
        check(e1.getCause() == null, "(String) no cause");
        check(e1 instanceof Error, "Error family");

        // (String, Throwable)
        RuntimeException cause = new RuntimeException("underlying");
        AnnotationFormatError e2 = new AnnotationFormatError("bad annotation", cause);
        check("bad annotation".equals(e2.getMessage()), "(String,Throwable) message");
        check(e2.getCause() == cause, "(String,Throwable) cause identity");
        check(e2.getCause().getMessage().equals("underlying"), "cause message readable");

        // (Throwable)
        AnnotationFormatError e3 = new AnnotationFormatError(cause);
        check(e3.getCause() == cause, "(Throwable) cause identity");
        check(cause.toString().equals(e3.getMessage()),
              "(Throwable) message defaults to cause.toString(): " + e3.getMessage());

        // catchability through the Error hierarchy
        try {
            throw e2;
        } catch (Error caught) {
            check(caught == e2, "catchable as Error");
        }
        try {
            throw e3;
        } catch (Throwable caught) {
            check(caught instanceof AnnotationFormatError, "catchable as Throwable");
        }

        // stack trace and suppression machinery inherited from Throwable
        check(e1.getStackTrace() != null, "stack trace available");
        e1.addSuppressed(new AnnotationFormatError("s"));
        check(e1.getSuppressed().length == 1, "suppression supported");

        // toString includes class and message
        check(e1.toString().startsWith("java.lang.annotation.AnnotationFormatError: bad annotation"),
              "toString format");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
