/*
 * @test
 * @summary java.lang.annotation.AnnotationFormatError conformance: white-box structure + black-box
 *          behavior, null arguments, and serialization round-trip.
 * @run main AnnotationFormatErrorTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.annotation.AnnotationFormatError;

public class AnnotationFormatErrorTest {
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
        AnnotationFormatError e1 = new AnnotationFormatError("bad annotation");
        check("bad annotation".equals(e1.getMessage()), "(String) message");
        check(e1.getCause() == null, "(String) no cause");
        check(e1 instanceof Error, "Error family");

        RuntimeException cause = new RuntimeException("underlying");
        AnnotationFormatError e2 = new AnnotationFormatError("bad annotation", cause);
        check("bad annotation".equals(e2.getMessage()), "(String,Throwable) message");
        check(e2.getCause() == cause, "(String,Throwable) cause identity");
        check(e2.getCause().getMessage().equals("underlying"), "cause message readable");

        AnnotationFormatError e3 = new AnnotationFormatError(cause);
        check(e3.getCause() == cause, "(Throwable) cause identity");
        check(cause.toString().equals(e3.getMessage()),
              "(Throwable) message defaults to cause.toString(): " + e3.getMessage());

        // null message / null cause are permitted
        AnnotationFormatError eNullMsg = new AnnotationFormatError((String) null);
        check(eNullMsg.getMessage() == null, "null message permitted");
        AnnotationFormatError eNullCause = new AnnotationFormatError("m", null);
        check(eNullCause.getCause() == null, "null cause permitted");
        AnnotationFormatError eThrowNull = new AnnotationFormatError((Throwable) null);
        check(eThrowNull.getCause() == null && eThrowNull.getMessage() == null,
              "null cause defaults message to null");

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

        check(e1.getStackTrace() != null, "stack trace available");
        e1.addSuppressed(new AnnotationFormatError("s"));
        check(e1.getSuppressed().length == 1, "suppression supported");

        check(e1.toString().startsWith("java.lang.annotation.AnnotationFormatError: bad annotation"),
              "toString format");
    }

    // ---- Part 3: serialization ----
    static void testSerialization() throws Exception {
        Class<?> c = AnnotationFormatError.class;
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -4256701562333669892L,
              "serialVersionUID stable");

        AnnotationFormatError back =
                (AnnotationFormatError) roundTrip(
                        new AnnotationFormatError("bad annotation", new RuntimeException("c")));
        check("bad annotation".equals(back.getMessage()), "message survives round-trip");
        check(back.getCause() != null && "c".equals(back.getCause().getMessage()),
              "cause survives round-trip");
        check(back instanceof Error, "still Error after round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
