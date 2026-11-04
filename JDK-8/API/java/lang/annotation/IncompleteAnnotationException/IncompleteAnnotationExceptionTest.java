/*
 * @test
 * @summary java.lang.annotation.IncompleteAnnotationException conformance: constructor,
 *          annotationType()/elementName() exact read-back, RuntimeException hierarchy,
 *          null-argument NullPointerException, and serialization round-trip.
 * @run main IncompleteAnnotationExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.annotation.Retention;
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

    // ---- Part 1: structural contract ----
    static void testStructural() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.IncompleteAnnotationException");
        check(RuntimeException.class.isAssignableFrom(c), "is a RuntimeException");
        check(c.getSuperclass() == RuntimeException.class, "direct RuntimeException subclass");
        check(c.getDeclaredMethod("annotationType").getReturnType() == Class.class,
              "annotationType() returns Class");
        check(c.getDeclaredMethod("elementName").getReturnType() == String.class,
              "elementName() returns String");
        check(Modifier.isPublic(c.getDeclaredConstructor(Class.class, String.class).getModifiers()),
              "public (Class, String) constructor");
        int pub = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pub++;
        }
        check(pub == 1, "single public constructor");
    }

    // ---- Part 2: behavior ----
    static void testBehavior() {
        IncompleteAnnotationException e =
                new IncompleteAnnotationException(Retention.class, "value");
        check(e.annotationType() == Retention.class, "annotationType identity");
        check("value".equals(e.elementName()), "elementName exact");
        check(e instanceof RuntimeException, "RuntimeException family");

        // auto-generated message mentions both the type name and the missing element
        String msg = e.getMessage();
        check(msg != null && msg.contains("Retention"), "message mentions annotation type: " + msg);
        check(msg.contains("value"), "message mentions element name");
        check(msg.contains("missing element"), "message uses canonical wording");

        try {
            throw e;
        } catch (IncompleteAnnotationException caught) {
            check(caught == e, "catchable as itself");
        } catch (RuntimeException other) {
            check(false, "should not reach here");
        }

        // two instances independent
        IncompleteAnnotationException e2 =
                new IncompleteAnnotationException(SuppressWarnings.class, "priority");
        check(e2.annotationType() == SuppressWarnings.class, "second annotationType");
        check("priority".equals(e2.elementName()), "second elementName");
        check(!e.equals(e2), "instances distinct");

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

        // Throwable machinery
        check(e.getStackTrace() != null, "stack trace");
        e.addSuppressed(e2);
        check(e.getSuppressed().length == 1, "suppression");
        check(e.toString().startsWith("java.lang.annotation.IncompleteAnnotationException"),
              "toString class name");
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
        testStructural();
        testBehavior();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
