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

    // ---- Part 4: exact message composition (identical on JDK 8 and JDK 27) ----
    static void testMessageExact() {
        IncompleteAnnotationException e =
                new IncompleteAnnotationException(Retention.class, "value");
        check(e.getMessage().equals("java.lang.annotation.Retention missing element value"),
              "exact message: " + e.getMessage());
        check(e.toString().equals(
                "java.lang.annotation.IncompleteAnnotationException: "
              + "java.lang.annotation.Retention missing element value"),
              "toString exact: " + e.toString());
    }

    // ---- Part 5: constructor matrix and extra views ----
    static void testConstructorMatrixAndViews() throws Exception {
        // exactly one declared constructor: public (Class<? extends Annotation>, String)
        Class<?> c = IncompleteAnnotationException.class;
        check(c.getDeclaredConstructors().length == 1,
              "exactly one declared constructor: " + c.getDeclaredConstructors().length);
        Class<?>[] pt = c.getDeclaredConstructor(Class.class, String.class).getParameterTypes();
        check(pt.length == 2 && pt[0] == Class.class && pt[1] == String.class,
              "(Class, String) exact parameter list");

        // elementName may be any string, including one with punctuation/whitespace
        IncompleteAnnotationException e = new IncompleteAnnotationException(
                Retention.class, "weird name #42");
        check("weird name #42".equals(e.elementName()), "elementName odd string round-trips");
        check(e.getMessage().endsWith("weird name #42"), "message embeds odd elementName");

        // message uses getName(): for a nested annotation type the binary form appears
        IncompleteAnnotationException n = new IncompleteAnnotationException(
                LocalMarker.class, "x");
        check(n.getMessage().equals(LocalMarker.class.getName() + " missing element x"),
              "nested type uses getName(): " + n.getMessage());

        // the constructor accepts any annotation Class, including meta-annotations
        IncompleteAnnotationException t = new IncompleteAnnotationException(
                java.lang.annotation.Target.class, "value");
        check(t.annotationType() == java.lang.annotation.Target.class, "Target annotationType");

        // it is an Exception (RuntimeException subclass), catchable as such
        check(java.lang.Exception.class.isAssignableFrom(c), "Exception family");

        // real-trigger note: this exception is thrown by the reflective annotation
        // machinery when an annotation instance lacks a member that its (evolved)
        // annotation type now requires -- producing that state deterministically needs
        // two compiled versions of one annotation type, so only construction paths are
        // asserted here (deterministic path per the batch brief).
    }

    // nested annotation fixture: proves getName()/binary-name form in the message
    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @interface LocalMarker {
    }

    public static void main(String[] args) throws Exception {
        testStructural();
        testBehavior();
        testSerialization();
        testMessageExact();
        testConstructorMatrixAndViews();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
