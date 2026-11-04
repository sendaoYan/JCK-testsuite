/*
 * @test
 * @summary java.lang.annotation.IncompleteAnnotationException conformance: white-box + black-box behavior.
 * @run main IncompleteAnnotationExceptionTest
 */

import java.lang.annotation.IncompleteAnnotationException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;

public class IncompleteAnnotationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.IncompleteAnnotationException");
        check(RuntimeException.class.isAssignableFrom(c), "is a RuntimeException");
        check(c.getSuperclass() == RuntimeException.class, "direct subclass");
        check(c.getDeclaredMethod("annotationType").getReturnType() == Class.class,
              "annotationType() returns Class");
        check(c.getDeclaredMethod("elementName").getReturnType() == String.class,
              "elementName() returns String");
        check(Modifier.isPublic(
                  c.getDeclaredConstructor(Class.class, String.class).getModifiers()),
              "public (Class, String) constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // construct with a real annotation type and a member name that went missing
        IncompleteAnnotationException e =
                new IncompleteAnnotationException(Retention.class, "priority");
        check(e.annotationType() == Retention.class, "annotationType identity");
        check("priority".equals(e.elementName()), "elementName exact");

        // runtime family / catchability
        check(e instanceof RuntimeException, "RuntimeException");
        try {
            throw e;
        } catch (IncompleteAnnotationException caught) {
            check(caught == e, "catchable as itself");
        } catch (RuntimeException other) {
            check(false, "should not reach here");
        }

        // message is auto-generated from the two constructor args
        check(e.getMessage() != null && e.getMessage().contains("priority"),
              "message mentions element name: " + e.getMessage());

        // a second instance is independent
        IncompleteAnnotationException e2 =
                new IncompleteAnnotationException(SuppressWarnings.class, "value");
        check(e2.annotationType() == SuppressWarnings.class, "second annotationType");
        check(e2.elementName().equals("value"), "second elementName");

        // Throwable machinery
        check(e.getStackTrace() != null, "stack trace");
        e.addSuppressed(e2);
        check(e.getSuppressed().length == 1, "suppression");
        check(e.toString().startsWith("java.lang.annotation.IncompleteAnnotationException"),
              "toString class name");

        // a defaulted element never triggers this path: reading Retention.value() works
        Retention r = Retention.class.getAnnotation(Retention.class);
        check(r != null && r.value() == RetentionPolicy.RUNTIME,
              "defaulted members readable without exception (contrast case)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
