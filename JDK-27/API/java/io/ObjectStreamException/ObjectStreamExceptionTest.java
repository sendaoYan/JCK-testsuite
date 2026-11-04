/*
 * @test
 * @summary java.io.ObjectStreamException conformance: white-box structure (abstract
 *          serialization-exception root with protected constructors) + black-box
 *          behavior (subclass instantiation through the protected ctors, the concrete
 *          JDK subclasses, catch semantics).
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

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
        MyStreamException(Throwable c) { super(c); }
        MyStreamException(String m, Throwable c) { super(m, c); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectStreamException");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.io.IOException"), "extends IOException");
        for (Constructor<?> k : c.getDeclaredConstructors())
            check(Modifier.isProtected(k.getModifiers()),
                  "constructor is protected: " + k);
        for (Class<?> sub : new Class<?>[]{InvalidClassException.class,
                InvalidObjectException.class, NotActiveException.class,
                NotSerializableException.class, OptionalDataException.class,
                StreamCorruptedException.class, WriteAbortedException.class})
            check(c.isAssignableFrom(sub), sub.getSimpleName() + " extends ObjectStreamException");
        check(!RuntimeException.class.isAssignableFrom(c), "checked exception family");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. cannot be instantiated directly
        try {
            java.lang.reflect.Constructor<ObjectStreamException> k =
                ObjectStreamException.class.getDeclaredConstructor(String.class);
            k.newInstance("nope");
            check(false, "abstract class must not be instantiable");
        } catch (java.lang.reflect.InvocationTargetException | InstantiationException
                 | IllegalAccessException | NoSuchMethodException expected) {
            passed++;
        }

        // 2. protected constructors behave through a subclass
        check(new MyStreamException().getMessage() == null, "no-arg protected ctor");
        check("detail".equals(new MyStreamException("detail").getMessage()),
              "(String) protected ctor");
        Exception cause = new IllegalStateException("root");
        check(new MyStreamException(cause).getCause() == cause
              && cause.toString().equals(new MyStreamException(cause).getMessage()),
              "(Throwable) ctor: cause and cause.toString() message");
        MyStreamException both = new MyStreamException("m", cause);
        check("m".equals(both.getMessage()) && both.getCause() == cause,
              "(String, Throwable) ctor");

        // 3. a subclass exception flows through code catching ObjectStreamException
        try {
            throw new MyStreamException("from serializer");
        } catch (ObjectStreamException ose) {
            check(ose instanceof MyStreamException && "from serializer".equals(ose.getMessage()),
                  "subclass catchable as ObjectStreamException");
        } catch (IOException io) {
            check(false, "ObjectStreamException should have matched first");
        }

        // 4. stack frames and toString behave like any IOException subclass
        MyStreamException t = new MyStreamException("x");
        check(t.toString().contains("MyStreamException: x"), "toString");
        check(t.getStackTrace().length > 0, "stack trace captured");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
