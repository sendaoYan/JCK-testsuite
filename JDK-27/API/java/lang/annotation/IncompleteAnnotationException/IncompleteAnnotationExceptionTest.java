/*
 * @test
 * @summary java.lang.annotation.IncompleteAnnotationException conformance: white-box + black-box
 *          behavior, null-argument NullPointerException, and serialization round-trip.
 * @run main IncompleteAnnotationExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
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

    static Object roundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        return ois.readObject();
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
        int pub = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pub++;
        }
        check(pub == 1, "single public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        IncompleteAnnotationException e =
                new IncompleteAnnotationException(Retention.class, "priority");
        check(e.annotationType() == Retention.class, "annotationType identity");
        check("priority".equals(e.elementName()), "elementName exact");

        check(e instanceof RuntimeException, "RuntimeException");
        try {
            throw e;
        } catch (IncompleteAnnotationException caught) {
            check(caught == e, "catchable as itself");
        } catch (RuntimeException other) {
            check(false, "should not reach here");
        }

        check(e.getMessage() != null && e.getMessage().contains("priority"),
              "message mentions element name: " + e.getMessage());
        check(e.getMessage().contains("missing element"), "message canonical wording");

        IncompleteAnnotationException e2 =
                new IncompleteAnnotationException(SuppressWarnings.class, "value");
        check(e2.annotationType() == SuppressWarnings.class, "second annotationType");
        check(e2.elementName().equals("value"), "second elementName");

        // constructor throws NullPointerException when either argument is null
        try {
            new IncompleteAnnotationException(null, "x");
            check(false, "null annotationType should throw");
        } catch (NullPointerException expected) {
            check(true, "NPE on null annotationType");
        }
        try {
            new IncompleteAnnotationException(Retention.class, null);
            check(false, "null elementName should throw");
        } catch (NullPointerException expected) {
            check(true, "NPE on null elementName");
        }

        check(e.getStackTrace() != null, "stack trace");
        e.addSuppressed(e2);
        check(e.getSuppressed().length == 1, "suppression");
        check(e.toString().startsWith("java.lang.annotation.IncompleteAnnotationException"),
              "toString class name");

        Retention r = Retention.class.getAnnotation(Retention.class);
        check(r != null && r.value() == RetentionPolicy.RUNTIME,
              "defaulted members readable without exception (contrast case)");
    }

    // ---- Part 3: serialization ----
    static void testSerialization() throws Exception {
        Class<?> c = IncompleteAnnotationException.class;
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 8445097402741811912L,
              "serialVersionUID stable");

        IncompleteAnnotationException back =
                (IncompleteAnnotationException) roundTrip(
                        new IncompleteAnnotationException(Retention.class, "value"));
        check(back.annotationType() == Retention.class, "annotationType survives round-trip");
        check("value".equals(back.elementName()), "elementName survives round-trip");
        check(back instanceof RuntimeException, "still RuntimeException after round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
