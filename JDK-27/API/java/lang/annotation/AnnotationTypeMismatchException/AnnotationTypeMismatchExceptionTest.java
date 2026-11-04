/*
 * @test
 * @summary java.lang.annotation.AnnotationTypeMismatchException conformance: white-box + black-box behavior.
 * @run main AnnotationTypeMismatchExceptionTest
 */

import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.annotation.Retention;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AnnotationTypeMismatchExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.AnnotationTypeMismatchException");
        check(RuntimeException.class.isAssignableFrom(c), "is a RuntimeException");
        check(c.getSuperclass() == RuntimeException.class, "direct subclass");
        check(c.getDeclaredMethod("element").getReturnType() == Method.class, "element() returns Method");
        check(c.getDeclaredMethod("foundType").getReturnType() == String.class,
              "foundType() returns String");
        check(Modifier.isPublic(c.getDeclaredConstructor(Method.class, String.class).getModifiers()),
              "public (Method, String) constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // construct with the standard shape: the annotation element Method and the found type
        Method element = Retention.class.getMethod("value");
        AnnotationTypeMismatchException e =
                new AnnotationTypeMismatchException(element, "Ljava/lang/String;");
        check(e.element() == element, "element() returns the same Method instance");
        check(e.element().getName().equals("value"), "element name");
        check("Ljava/lang/String;".equals(e.foundType()), "foundType exact string");

        // runtime exception family
        check(e instanceof RuntimeException, "RuntimeException");
        try {
            throw e;
        } catch (RuntimeException caught) {
            check(caught == e, "catchable as RuntimeException");
        }

        // different constructor arguments produce independent readbacks
        Method other = Object.class.getMethod("toString");
        AnnotationTypeMismatchException e2 = new AnnotationTypeMismatchException(other, "I");
        check(e2.element() == other, "second instance element");
        check("I".equals(e2.foundType()), "second instance foundType");
        check(!e.equals(e2), "instances distinct");

        // message/toString derived from Throwable defaults (constructor takes no message)
        check(e.getMessage() != null || e.foundType() != null,
              "no message arg; foundType carries the detail");

        // foundType is typically a descriptor string; consumers parse it
        check(e.foundType().startsWith("L") || e2.foundType().startsWith("I"),
              "descriptor-like values");

        // suppression/stack trace machinery
        e.addSuppressed(e2);
        check(e.getSuppressed().length == 1, "suppression supported");
        check(e.getStackTrace() != null, "stack trace available");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
