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

    // ---- Part 4: exact constructor matrix (javap-verified on both JDKs) ----
    static void testConstructorMatrix() throws Exception {
        Class<?> c = AnnotationFormatError.class;
        // exactly three declared constructors: (String), (String,Throwable), (Throwable);
        // there is NO no-arg constructor on either JDK 8 or JDK 27
        check(c.getDeclaredConstructors().length == 3,
              "exactly three declared constructors: " + c.getDeclaredConstructors().length);
        try {
            c.getDeclaredConstructor();
            check(false, "no-arg constructor should not exist");
        } catch (NoSuchMethodException expected) {
            check(true, "no no-arg constructor (javap-confirmed both sides)");
        }
        Class<?>[] p1 = c.getDeclaredConstructor(String.class).getParameterTypes();
        check(p1.length == 1 && p1[0] == String.class, "(String) exact parameter list");
        Class<?>[] p2 = c.getDeclaredConstructor(String.class, Throwable.class).getParameterTypes();
        check(p2.length == 2 && p2[0] == String.class && p2[1] == Throwable.class,
              "(String, Throwable) exact parameter list");
        Class<?>[] p3 = c.getDeclaredConstructor(Throwable.class).getParameterTypes();
        check(p3.length == 1 && p3[0] == Throwable.class, "(Throwable) exact parameter list");
    }

    // ---- Part 5: Error semantics ----
    static void testErrorSemantics() {
        AnnotationFormatError e = new AnnotationFormatError("x");
        check(e.toString().equals("java.lang.annotation.AnnotationFormatError: x"),
              "toString exact: " + e);
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getSuppressed().length == 0, "no suppressed exceptions by default");
        check(!java.lang.Exception.class.isAssignableFrom(e.getClass()), "an Error, never an Exception");
        check(e instanceof Error, "instanceof Error");

        // deep cause chain: cause of cause preserved by identity
        Error root = new Error("root");
        Exception mid = new Exception("mid", root);
        AnnotationFormatError chained = new AnnotationFormatError("top", mid);
        check(chained.getCause() == mid && mid.getCause() == root,
              "cause chain preserved two levels deep");

        // (Throwable) form with an Error cause keeps the Error flavor
        AnnotationFormatError wrapped = new AnnotationFormatError((Throwable) root);
        check(wrapped.getCause() == root, "(Throwable) wraps Error cause");
        check(root.toString().equals(wrapped.getMessage()),
              "(Throwable) message is cause.toString()");

        // real-trigger note: AnnotationFormatError is thrown by the JVM/reflective
        // machinery when a RuntimeVisibleAnnotations attribute is malformed; producing
        // such a class file requires bytecode crafting, so only construction is asserted
        // here (deterministic path), per the batch brief.
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        testConstructorMatrix();
        testErrorSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
