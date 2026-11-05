/*
 * @test
 * @summary java.lang.annotation.AnnotationTypeMismatchException conformance: constructors,
 *          element()/foundType() exact read-back, RuntimeException hierarchy, null arguments,
 *          and serialization (JDK 8 keeps element non-transient).
 * @run main AnnotationTypeMismatchExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.annotation.AnnotationTypeMismatchException;
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

    // ---- Part 1: structural contract ----
    static void testStructural() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.AnnotationTypeMismatchException");
        check(RuntimeException.class.isAssignableFrom(c), "is a RuntimeException");
        check(c.getSuperclass() == RuntimeException.class, "direct RuntimeException subclass");
        check(c.getDeclaredMethod("element").getReturnType() == Method.class, "element() returns Method");
        check(c.getDeclaredMethod("foundType").getReturnType() == String.class, "foundType() returns String");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        int pub = 0;
        for (java.lang.reflect.Constructor<?> ct : ctors) {
            if (Modifier.isPublic(ct.getModifiers())) pub++;
        }
        check(pub == 1, "single public constructor");
        check(Modifier.isPublic(c.getDeclaredConstructor(Method.class, String.class).getModifiers()),
              "public (Method, String) constructor");
    }

    // ---- Part 2: behavior ----
    static void testBehavior() throws Exception {
        Method element = Object.class.getMethod("toString");
        AnnotationTypeMismatchException e = new AnnotationTypeMismatchException(element, "I");
        check(e.element() == element, "element() returns same Method instance");
        check("I".equals(e.foundType()), "foundType() exact");
        check(e instanceof RuntimeException, "RuntimeException family");
        check(e.getMessage() != null
              && e.getMessage().contains("Incorrectly typed data found for annotation element"),
              "auto-generated message prefix: " + e.getMessage());
        check(e.getMessage().contains("I"), "message mentions foundType");

        try {
            throw e;
        } catch (RuntimeException caught) {
            check(caught == e, "catchable as RuntimeException");
        }

        // two instances are independent
        Method other = Object.class.getMethod("hashCode");
        AnnotationTypeMismatchException e2 = new AnnotationTypeMismatchException(other, "J");
        check(e2.element() == other, "second element");
        check("J".equals(e2.foundType()), "second foundType");
        check(!e.equals(e2), "instances distinct");

        // null element and null foundType are permitted by the constructor
        AnnotationTypeMismatchException e3 = new AnnotationTypeMismatchException(null, null);
        check(e3.element() == null, "null element permitted");
        check(e3.foundType() == null, "null foundType permitted");
        check(e3.getMessage() != null, "message still generated with null arguments");

        // suppression / stack trace machinery inherited from Throwable
        e.addSuppressed(e2);
        check(e.getSuppressed().length == 1, "suppression supported");
        check(e.getStackTrace() != null, "stack trace available");
    }

    // ---- Part 3: serialization ----
    static void testSerialization() throws Exception {
        Class<?> c = AnnotationTypeMismatchException.class;
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 8125925355765570191L,
              "serialVersionUID stable");

        // JDK 8: element is not transient, and Method is not Serializable, so writing
        // an exception that carries a real Method throws NotSerializableException.
        Method element = Object.class.getMethod("toString");
        try {
            roundTrip(new AnnotationTypeMismatchException(element, "I"));
            check(false, "JDK 8 should throw NotSerializableException for non-transient Method");
        } catch (NotSerializableException expected) {
            check(true, "non-Serializable Method field detected");
        }

        // with a null element the exception serializes and foundType survives
        AnnotationTypeMismatchException back =
                (AnnotationTypeMismatchException) roundTrip(new AnnotationTypeMismatchException(null, "I"));
        check(back.element() == null, "null element survives round-trip");
        check("I".equals(back.foundType()), "foundType survives round-trip");
        check(back instanceof RuntimeException, "still RuntimeException after round-trip");
    }

    // ---- Part 4: exact constructor signature (javap-verified on both JDKs) ----
    static void testConstructorMatrix() throws Exception {
        Class<?> c = AnnotationTypeMismatchException.class;
        // JDK 8 and JDK 27 both declare exactly one constructor: public (Method, String).
        // No (Method, Class) legacy overload survives in either build.
        check(c.getDeclaredConstructors().length == 1,
              "exactly one declared constructor: " + c.getDeclaredConstructors().length);
        Class<?>[] pt = c.getDeclaredConstructor(Method.class, String.class).getParameterTypes();
        check(pt.length == 2 && pt[0] == Method.class && pt[1] == String.class,
              "(Method, String) exact parameter list");
    }

    // ---- Part 5: exact message composition ----
    static void testMessageExact() throws Exception {
        Method element = java.lang.annotation.Retention.class.getMethod("value");
        String found = "Ljava/lang/Integer;";
        AnnotationTypeMismatchException e = new AnnotationTypeMismatchException(element, found);
        String expected = "Incorrectly typed data found for annotation element "
                + element + " (Found data of type " + found + ")";
        check(e.getMessage().equals(expected), "exact message: " + e.getMessage());
        check(e.getMessage().contains(element.toString()), "message embeds Method.toString()");

        // the same composition holds even with null arguments
        AnnotationTypeMismatchException n = new AnnotationTypeMismatchException(null, null);
        check(n.getMessage().equals(
                "Incorrectly typed data found for annotation element null (Found data of type null)"),
              "null-argument message exact: " + n.getMessage());
    }

    // ---- Part 6: element()/foundType() reflective views ----
    static void testElementView() throws Exception {
        Method element = Object.class.getMethod("hashCode");
        AnnotationTypeMismatchException e =
                new AnnotationTypeMismatchException(element, "found with spaces");
        check(e.element().getName().equals("hashCode"), "element().getName");
        check(e.element().getDeclaringClass() == Object.class, "element().getDeclaringClass");
        check("found with spaces".equals(e.foundType()), "foundType with spaces round-trips");
        check(java.lang.Exception.class.isAssignableFrom(e.getClass()),
              "is an Exception (hence RuntimeException)");
        check(!e.toString().isEmpty() && e.toString().startsWith(
                "java.lang.annotation.AnnotationTypeMismatchException"), "toString prefix");
        check(e.toString().contains("hashCode"), "toString carries the message");

        // any Method reference is accepted, including a static method
        Method staticM = System.class.getMethod("getProperty", String.class);
        AnnotationTypeMismatchException e2 = new AnnotationTypeMismatchException(staticM, "X");
        check(e2.element() == staticM, "static Method element identity");
    }

    public static void main(String[] args) throws Exception {
        testStructural();
        testBehavior();
        testSerialization();
        testConstructorMatrix();
        testMessageExact();
        testElementView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
