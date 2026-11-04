/*
 * @test
 * @summary java.io.ObjectStreamException API conformance: full contract coverage.
 *          Abstract serialization-exception root with protected constructors; subclass
 *          instantiation through the protected ctors; catch semantics as
 *          ObjectStreamException/IOException; toString; stack trace capture.
 * @run main ObjectStreamExceptionTest
 */

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.io.WriteAbortedException;

public class ObjectStreamExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Subclass exercises the protected constructors. */
    static class MyStreamException extends ObjectStreamException {
        static final long serialVersionUID = 1L;
        MyStreamException() { super(); }
        MyStreamException(String m) { super(m); }
    }

    // ---- 1. abstract class not directly instantiable ----
    static void testNotInstantiable() throws Exception {
        try {
            ObjectStreamException.class.getDeclaredConstructor(String.class).newInstance("nope");
            check(false, "abstract class must not be instantiable");
        } catch (java.lang.reflect.InvocationTargetException | InstantiationException
                 | IllegalAccessException | NoSuchMethodException expected) {
            passed++;
        }
    }

    // ---- 2. protected constructors behave through a subclass ----
    static void testProtectedConstructors() {
        check(new MyStreamException().getMessage() == null, "no-arg protected ctor");
        check("detail".equals(new MyStreamException("detail").getMessage()),
              "(String) protected ctor");
        // JDK 8 ObjectStreamException only exposes () and (String) protected ctors.
        // The (Throwable)/(String,Throwable) cause ctors arrived in a later release.
    }

    // ---- 3. subclass catchable as ObjectStreamException ----
    static void testCatchSemantics() throws Exception {
        try {
            throw new MyStreamException("from serializer");
        } catch (ObjectStreamException ose) {
            check(ose instanceof MyStreamException && "from serializer".equals(ose.getMessage()),
                  "subclass catchable as ObjectStreamException");
        } catch (IOException io) {
            check(false, "ObjectStreamException should have matched first");
        }
    }

    // ---- 4. concrete JDK subclasses are members of the family ----
    static void testSubclasses() {
        ObjectStreamException[] subs = new ObjectStreamException[] {
            new InvalidClassException("ic"),
            new InvalidObjectException("io"),
            new NotActiveException("na"),
            new NotSerializableException("ns"),
            new StreamCorruptedException("sc"),
            new WriteAbortedException("wa", new Exception("cause")),
        };
        for (ObjectStreamException e : subs) {
            check(e instanceof IOException, e.getClass().getSimpleName() + " extends IOException");
        }
        // OptionalDataException has no public constructor; verify membership by type.
        check(ObjectStreamException.class.isAssignableFrom(OptionalDataException.class)
              && IOException.class.isAssignableFrom(OptionalDataException.class),
              "OptionalDataException extends ObjectStreamException/IOException");
    }

    // ---- 5. toString and stack trace ----
    static void testToStringStackTrace() {
        MyStreamException t = new MyStreamException("x");
        check(t.toString().contains("MyStreamException: x"), "toString");
        check(t.getStackTrace().length > 0, "stack trace captured");
    }

    public static void main(String[] args) throws Exception {
        testNotInstantiable();
        testProtectedConstructors();
        testCatchSemantics();
        testSubclasses();
        testToStringStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
