/*
 * @test
 * @summary java.lang.annotation.AnnotationTypeMismatchException conformance: white-box + black-box
 *          behavior, null arguments, and serialization round-trip (JDK 27 element is transient).
 * @run main AnnotationTypeMismatchExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
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
        Class<?> c = Class.forName("java.lang.annotation.AnnotationTypeMismatchException");
        check(RuntimeException.class.isAssignableFrom(c), "is a RuntimeException");
        check(c.getSuperclass() == RuntimeException.class, "direct subclass");
        check(c.getDeclaredMethod("element").getReturnType() == Method.class, "element() returns Method");
        check(c.getDeclaredMethod("foundType").getReturnType() == String.class,
              "foundType() returns String");
        check(Modifier.isPublic(c.getDeclaredConstructor(Method.class, String.class).getModifiers()),
              "public (Method, String) constructor");
        int pub = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pub++;
        }
        check(pub == 1, "single public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Method element = Retention.class.getMethod("value");
        AnnotationTypeMismatchException e =
                new AnnotationTypeMismatchException(element, "Ljava/lang/String;");
        check(e.element() == element, "element() returns the same Method instance");
        check(e.element().getName().equals("value"), "element name");
        check("Ljava/lang/String;".equals(e.foundType()), "foundType exact string");

        check(e instanceof RuntimeException, "RuntimeException");
        try {
            throw e;
        } catch (RuntimeException caught) {
            check(caught == e, "catchable as RuntimeException");
        }

        Method other = Object.class.getMethod("toString");
        AnnotationTypeMismatchException e2 = new AnnotationTypeMismatchException(other, "I");
        check(e2.element() == other, "second instance element");
        check("I".equals(e2.foundType()), "second instance foundType");
        check(!e.equals(e2), "instances distinct");

        check(e.getMessage() != null
              && e.getMessage().contains("Incorrectly typed data found for annotation element"),
              "auto-generated message prefix: " + e.getMessage());

        // null element and null foundType are permitted
        AnnotationTypeMismatchException e3 = new AnnotationTypeMismatchException(null, null);
        check(e3.element() == null, "null element permitted");
        check(e3.foundType() == null, "null foundType permitted");
        check(e3.getMessage() != null, "message generated with null args");

        e.addSuppressed(e2);
        check(e.getSuppressed().length == 1, "suppression supported");
        check(e.getStackTrace() != null, "stack trace available");
    }

    // ---- Part 3: serialization ----
    static void testSerialization() throws Exception {
        Class<?> c = AnnotationTypeMismatchException.class;
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 8125925355765570191L,
              "serialVersionUID stable");

        // JDK 27: element is transient, so the exception serializes; after reading back
        // element() is null (spec: value may be unavailable after serialization) but
        // foundType survives.
        Method element = Object.class.getMethod("toString");
        AnnotationTypeMismatchException back =
                (AnnotationTypeMismatchException) roundTrip(
                        new AnnotationTypeMismatchException(element, "Ljava/lang/String;"));
        check(back.element() == null, "transient element lost after round-trip");
        check("Ljava/lang/String;".equals(back.foundType()), "foundType survives round-trip");
        check(back instanceof RuntimeException, "still RuntimeException after round-trip");

        AnnotationTypeMismatchException back2 =
                (AnnotationTypeMismatchException) roundTrip(
                        new AnnotationTypeMismatchException(null, "I"));
        check(back2.element() == null, "null element stays null");
        check("I".equals(back2.foundType()), "foundType survives null-element round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
