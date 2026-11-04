/*
 * @test
 * @summary java.lang.reflect.GenericSignatureFormatError conformance: constructors/hierarchy.
 *          Deepened batch: real trigger by defining a class whose Signature attribute is
 *          grammatically invalid (handcrafted v52 class file with junk signature) and
 *          asserting the lazy generic accessors throw, control group parses cleanly,
 *          LinkageError-level hierarchy census (never caught by catch(RuntimeException)),
 *          both constructors, toString/initCause/suppressed mechanics, serialization
 *          round-trip, and printing.
 * @run main GenericSignatureFormatErrorTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.GenericSignatureFormatError;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class GenericSignatureFormatErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    /** Same as checkThrows but additionally asserts the message contains a fragment. */
    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
                               String fragment, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                String m = t.getMessage();
                check(m != null && m.contains(fragment),
                        msg + ": message should contain \"" + fragment + "\" but was " + m);
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static Object serRoundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }

    /** Carries a well-formed class Signature: <T:>Ljava/util/ArrayList<T;>; */
    static class Generic extends ArrayList<String> {
    }

    static final class SigLoader extends ClassLoader {
        SigLoader(ClassLoader parent) {
            super(parent);
        }

        Class<?> define(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }

    /**
     * Emits a minimal v52 class {@code public class <name> extends Object} whose single
     * class attribute is {@code Signature = "i_am_not_a_signature"}.
     */
    static byte[] badSignatureBytes(String name) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE);
        d.writeShort(0);                                  // minor_version
        d.writeShort(52);                                 // major_version
        d.writeShort(7);                                  // constant_pool_count
        d.writeByte(1); d.writeUTF(name);                 // #1 Utf8 name
        d.writeByte(7); d.writeShort(1);                  // #2 Class
        d.writeByte(1); d.writeUTF("java/lang/Object");   // #3
        d.writeByte(7); d.writeShort(3);                  // #4 Class
        d.writeByte(1); d.writeUTF("i_am_not_a_signature"); // #5 junk signature
        d.writeByte(1); d.writeUTF("Signature");          // #6
        d.writeShort(0x0021);                             // ACC_PUBLIC|ACC_SUPER
        d.writeShort(2);                                  // this_class
        d.writeShort(4);                                  // super_class
        d.writeShort(0);                                  // interfaces_count
        d.writeShort(0);                                  // fields_count
        d.writeShort(0);                                  // methods_count
        d.writeShort(1);                                  // class attributes_count
        d.writeShort(6);                                  // attribute_name_index
        d.writeInt(2);                                    // attribute_length
        d.writeShort(5);                                  // signature_index
        d.flush();
        return o.toByteArray();
    }

    static Class<?> defineBad() throws IOException {
        SigLoader loader = new SigLoader(GenericSignatureFormatErrorTest.class.getClassLoader());
        return loader.define("BadSigProbe", badSignatureBytes("BadSigProbe"));
    }

    static void testGenericSignatureFormatError() {
        GenericSignatureFormatError e = new GenericSignatureFormatError();
        check(e instanceof ClassFormatError, "is ClassFormatError");
        GenericSignatureFormatError e2 = new GenericSignatureFormatError("bad signature");
        check("bad signature".equals(e2.getMessage()), "message");
    }

    // ===================== deepened batch (2026-11-05) =====================

    static void testHierarchyCensus() {
        check(GenericSignatureFormatError.class.getSuperclass() == ClassFormatError.class,
                "direct ClassFormatError subclass");
        check(ClassFormatError.class.isAssignableFrom(GenericSignatureFormatError.class),
                "ClassFormatError assignable");
        check(LinkageError.class.isAssignableFrom(GenericSignatureFormatError.class),
                "LinkageError assignable");
        check(Error.class.isAssignableFrom(GenericSignatureFormatError.class),
                "Error assignable");
        // static contract: never catchable by catch(RuntimeException)
        check(!RuntimeException.class.isAssignableFrom(GenericSignatureFormatError.class),
                "!RuntimeException.isAssignableFrom");
        check(!Exception.class.isAssignableFrom(GenericSignatureFormatError.class),
                "!Exception.isAssignableFrom");
        check(java.io.Serializable.class.isAssignableFrom(GenericSignatureFormatError.class),
                "Serializable via Throwable");
        check(GenericSignatureFormatError.class.getConstructors().length == 2,
                "two public constructors");
    }

    static void testRealTrigger() throws Exception {
        Class<?> bad = defineBad();
        check(bad != null && "BadSigProbe".equals(bad.getName()), "malformed class loads fine");
        // the error is lazy: it fires when generic metadata is first parsed
        checkThrowsMsg(GenericSignatureFormatError.class,
                () -> bad.getGenericSuperclass(),
                "Signature Parse error", "getGenericSuperclass fails with parse error");
        checkThrows(GenericSignatureFormatError.class,
                () -> bad.getTypeParameters(),
                "getTypeParameters fails");
        checkThrows(GenericSignatureFormatError.class,
                () -> bad.getGenericInterfaces(),
                "getGenericInterfaces fails");
        // non-generic accessors stay healthy on the very same class
        check(bad.getSuperclass() == Object.class, "non-generic superclass fine");
        check(bad.getDeclaredFields().length == 0, "fields fine");
        check(bad.getDeclaredMethods().length == 0, "methods fine");
        check(!bad.isInterface(), "kind fine");

        // control group: a well-formed signature parses to a ParameterizedType
        Type gs = Generic.class.getGenericSuperclass();
        check(gs instanceof ParameterizedType, "control: parses to ParameterizedType");
        ParameterizedType pt = (ParameterizedType) gs;
        check(pt.getRawType() == ArrayList.class, "control: raw type ArrayList");
        check(pt.getActualTypeArguments()[0] == String.class, "control: type argument String");
        check(Generic.class.getTypeParameters().length == 0,
                "control: no own type parameters (argument fixed to String)");
    }

    static void testErrorSemantics() {
        boolean asError = false;
        try {
            throw new GenericSignatureFormatError("t");
        } catch (Error e) {
            asError = e instanceof GenericSignatureFormatError;
        }
        check(asError, "catchable as Error");
        boolean asLinkage = false;
        try {
            throw new GenericSignatureFormatError("t2");
        } catch (LinkageError e) {
            asLinkage = e instanceof GenericSignatureFormatError;
        }
        check(asLinkage, "catchable as LinkageError");
        boolean seenRuntimeCatch = false;
        boolean fellToErrorCatch = false;
        try {
            throw new GenericSignatureFormatError("t3");
        } catch (RuntimeException e) {
            seenRuntimeCatch = true;
        } catch (GenericSignatureFormatError e) {
            fellToErrorCatch = true;
        }
        check(!seenRuntimeCatch, "not caught by catch(RuntimeException)");
        check(fellToErrorCatch, "reaches the sibling Error handler instead");
    }

    static void testConstructorState() {
        GenericSignatureFormatError bare = new GenericSignatureFormatError();
        check(bare.getMessage() == null, "bare message null");
        check("java.lang.reflect.GenericSignatureFormatError".equals(bare.toString()),
                "bare toString exact");
        GenericSignatureFormatError with = new GenericSignatureFormatError("detail");
        check("detail".equals(with.getMessage()), "message kept");
        check("java.lang.reflect.GenericSignatureFormatError: detail".equals(with.toString()),
                "with-message toString exact");
        check(with.getCause() == null, "no cause by default");
        RuntimeException root = new RuntimeException("late");
        with.initCause(root);
        check(with.getCause() == root, "initCause allowed (cause was unset)");
        with.addSuppressed(new IllegalArgumentException("sup"));
        check(with.getSuppressed().length == 1, "suppressed attachable");
        check(with.fillInStackTrace() == with, "fillInStackTrace returns this");
    }

    static void testSerialization() throws Exception {
        GenericSignatureFormatError orig = new GenericSignatureFormatError("ser-gsfe");
        orig.initCause(new IllegalStateException("gsfe-cause"));
        Object back = serRoundTrip(orig);
        check(back instanceof GenericSignatureFormatError, "round-trip exact type");
        GenericSignatureFormatError res = (GenericSignatureFormatError) back;
        check("ser-gsfe".equals(res.getMessage()), "round-trip keeps message");
        check(res.getCause() instanceof IllegalStateException, "round-trip keeps cause type");
        check("gsfe-cause".equals(res.getCause().getMessage()), "round-trip keeps cause message");
        check(res instanceof java.io.Serializable, "instanceof Serializable");
    }

    static void testPrinting() throws Exception {
        GenericSignatureFormatError e = new GenericSignatureFormatError("print-gsfe");
        e.initCause(new RuntimeException("gsfe-root"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos, true, "UTF-8"));
        String rendered = bos.toString("UTF-8");
        check(rendered.contains("GenericSignatureFormatError"), "stack names type");
        check(rendered.contains("print-gsfe"), "stack renders message");
        check(rendered.contains("gsfe-root"), "stack renders cause");
    }

    public static void main(String[] args) throws Exception {
        testGenericSignatureFormatError();
        testHierarchyCensus();
        testRealTrigger();
        testErrorSemantics();
        testConstructorState();
        testSerialization();
        testPrinting();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
