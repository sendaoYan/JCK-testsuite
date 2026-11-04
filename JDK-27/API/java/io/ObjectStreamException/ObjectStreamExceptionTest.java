/*
 * @test
 * @summary java.io.ObjectStreamException conformance: white-box structure (abstract
 *          serialization-exception root with protected constructors) + black-box
 *          behavior (subclass instantiation through the protected ctors, the concrete
 *          JDK subclasses, catch semantics).
 * @run main ObjectStreamExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
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

    // ================= 深化追加（契约级） =================

    static byte[] serOf(Object o) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(b)) {
            oos.writeObject(o);
        }
        return b.toByteArray();
    }

    static Object back(byte[] b) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return ois.readObject();
        }
    }

    // ---- reflective structure: abstract, exactly the protected ctors (JDK-8: 2) ----
    static void testReflectiveStructure() throws Exception {
        java.lang.reflect.Constructor<?>[] ks = ObjectStreamException.class.getDeclaredConstructors();
        check(ks.length == 4, "JDK-27 declares four constructors (cause variants added): " + ks.length);
        for (java.lang.reflect.Constructor<?> k : ks) {
            check(java.lang.reflect.Modifier.isProtected(k.getModifiers()),
                  "constructor " + k + " protected");
        }
        check(java.lang.reflect.Modifier.isAbstract(ObjectStreamException.class.getModifiers()),
              "class itself is abstract (cannot be instantiated)");
        check(ObjectStreamException.class.getSuperclass() == IOException.class,
              "direct child of IOException");
        check(ObjectStreamException.class.getConstructors().length == 0,
              "no public constructors at all");
    }

    // ---- Throwable protocol matrix exercised through the subclass ----
    static void testThrowableProtocol() {
        MyStreamException e = new MyStreamException("m");
        check("m".equals(e.getLocalizedMessage()), "getLocalizedMessage defaults to getMessage");
        check(e.toString().contains("MyStreamException: m"), "toString = simple name + message");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured");
        StackTraceElement[] st = {new StackTraceElement("S", "readObject", "S.java", 12)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1
                  && "readObject".equals(e.getStackTrace()[0].getMethodName()),
              "setStackTrace/getStackTrace round-trip");
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) yields empty view");
        try {
            e.setStackTrace(null);
            check(false, "setStackTrace(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        Exception cause = new IllegalStateException("c");
        check(new MyStreamException("w").initCause(cause).getCause() == cause,
              "initCause injects a cause through the inherited surface");
        try {
            new MyStreamException("w").initCause(cause).initCause(cause);
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new MyStreamException().initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        MyStreamException sup = new MyStreamException("s");
        sup.addSuppressed(new Exception("x"));
        sup.addSuppressed(new Exception("y"));
        check(sup.getSuppressed().length == 2, "suppressed kept in order");
        try {
            sup.addSuppressed(sup);
            check(false, "self-suppression must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        try {
            sup.addSuppressed(null);
            check(false, "addSuppressed(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    // ---- serialization: the abstract root and a subclass round-trip ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(ObjectStreamException.class).getSerialVersionUID()
                  == 7260898174833392607L, "root serialVersionUID == 7260898174833392607");
        MyStreamException r = (MyStreamException) back(serOf(new MyStreamException("kept")));
        check("kept".equals(r.getMessage()), "subclass message survives round-trip");
        check(r instanceof ObjectStreamException, "round-tripped instance still in the family");
        MyStreamException chained = new MyStreamException("w");
        chained.initCause(new RuntimeException("r"));
        check("r".equals(((MyStreamException) back(serOf(chained))).getCause().getMessage()),
              "cause survives round-trip");
        // each concrete JDK subclass is serializable with a stable identity
        long[] uids = {
            ObjectStreamClass.lookup(InvalidClassException.class).getSerialVersionUID(),
            ObjectStreamClass.lookup(InvalidObjectException.class).getSerialVersionUID(),
            ObjectStreamClass.lookup(NotActiveException.class).getSerialVersionUID(),
            ObjectStreamClass.lookup(NotSerializableException.class).getSerialVersionUID(),
            ObjectStreamClass.lookup(OptionalDataException.class).getSerialVersionUID(),
            ObjectStreamClass.lookup(StreamCorruptedException.class).getSerialVersionUID(),
            ObjectStreamClass.lookup(WriteAbortedException.class).getSerialVersionUID()};
        for (long u : uids) {
            check(u != 0L, "subclass uid recorded: " + u);
        }
        check(ObjectStreamClass.lookup(InvalidClassException.class).getSerialVersionUID()
                  == -4333316296251054416L, "ICE uid pinned");
        check(ObjectStreamClass.lookup(StreamCorruptedException.class).getSerialVersionUID()
                  == 8983558202217591746L, "SCE uid pinned");
    }

    // ---- concrete-family dispatch matrix: every member catchable through all three ancestors ----
    static void testFamilyDispatch() {
        ObjectStreamException[] members = {
            new InvalidClassException("ic"),
            new InvalidObjectException("io"),
            new NotActiveException("na"),
            new NotSerializableException("ns"),
            new StreamCorruptedException("sc"),
            new WriteAbortedException("wa", new Exception("c"))};
        for (ObjectStreamException m : members) {
            String n = m.getClass().getSimpleName();
            check(m instanceof ObjectStreamException, n + " instanceof ObjectStreamException");
            check(m instanceof IOException, n + " instanceof IOException");
            check(m instanceof Exception, n + " instanceof Exception");
            check(java.io.Serializable.class.isInstance(m), n + " is Serializable");
        }
        // OptionalDataException cannot be constructed (no public ctor) but is a member
        check(ObjectStreamException.class.isAssignableFrom(OptionalDataException.class),
              "OptionalDataException belongs to the family");
        check(IOException.class.isAssignableFrom(OptionalDataException.class),
              "OptionalDataException is an IOException");
        // a subclass thrown where ObjectStreamException is caught still matches IOException
        try {
            throw new MyStreamException("dispatch");
        } catch (IOException io) {
            check(io instanceof ObjectStreamException,
                  "IOException catch sees the ObjectStreamException family");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testReflectiveStructure();
        testThrowableProtocol();
        testSerializationContract();
        testFamilyDispatch();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

