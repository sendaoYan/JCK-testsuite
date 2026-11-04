/*
 * @test
 * @summary java.lang.reflect.GenericSignatureFormatError conformance: white-box structure + black-box behavior.
 *          Deepened batch: LinkageError-level census (never caught by catch(RuntimeException)),
 *          exact toString forms, initCause allowed, serialization round-trip, printing,
 *          non-generic accessors stay healthy on the malformed class.
 * @run main GenericSignatureFormatErrorTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.reflect.GenericSignatureFormatError;
import java.util.ArrayList;

public class GenericSignatureFormatErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    static class Loader extends ClassLoader {
        Loader() {
            super(GenericSignatureFormatErrorTest.class.getClassLoader());
        }

        Class<?> define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    static byte[] withBadSignature() throws Exception {
        String res = GenericSignatureFormatErrorTest.class.getName().replace('.', '/')
                + "$Generic.class";
        byte[] bytes = GenericSignatureFormatErrorTest.class.getClassLoader()
                .getResourceAsStream(res).readAllBytes();
        ClassModel model = ClassFile.of().parse(bytes);
        return ClassFile.of().transformClass(model,
                ClassTransform.dropping(ce -> ce instanceof SignatureAttribute)
                        .andThen(ClassTransform.endHandler(cb -> cb.with(badSignature(cb)))));
    }

    static SignatureAttribute badSignature(ClassBuilder cb) {
        ConstantPoolBuilder cp = cb.constantPool();
        Utf8Entry entry = cp.utf8Entry("i_am_not_a_signature");
        return SignatureAttribute.of(entry);
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        GenericSignatureFormatError e = new GenericSignatureFormatError();
        check(e instanceof ClassFormatError, "is ClassFormatError");
        GenericSignatureFormatError e2 = new GenericSignatureFormatError("bad signature");
        check("bad signature".equals(e2.getMessage()), "message");
        check(java.lang.ClassFormatError.class.isAssignableFrom(GenericSignatureFormatError.class),
              "extends ClassFormatError (LinkageError)");
        check(GenericSignatureFormatError.class.getConstructors().length == 2,
              "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. real trigger: a class whose Signature attribute is malformed
        byte[] bad = withBadSignature();
        Class<?> loaded = new Loader().define(GenericSignatureFormatErrorTest.class.getName()
                + "$Generic", bad);
        check(loaded != null && loaded.getName().contains("Generic"), "malformed class loads");
        try {
            loaded.getGenericSuperclass();
            check(false, "getGenericSuperclass must fail on malformed signature");
        } catch (GenericSignatureFormatError e) {
            check(true, "GSFE thrown when signature parsed");
            check(e.getMessage() != null && e.getMessage().contains("Signature"),
                  "message mentions signature parse");
        }

        // 2. other lazy generic accessors fail the same way
        try {
            loaded.getTypeParameters();
            check(false, "getTypeParameters must fail");
        } catch (GenericSignatureFormatError e) {
            check(true, "GSFE on getTypeParameters");
        }
        try {
            loaded.getGenericInterfaces();
            check(false, "getGenericInterfaces must fail");
        } catch (GenericSignatureFormatError e) {
            check(true, "GSFE on getGenericInterfaces");
        }

        // 3. the pristine class still parses fine (control group)
        java.lang.reflect.Type gs = Generic.class.getGenericSuperclass();
        check(gs instanceof java.lang.reflect.ParameterizedType, "control: parses to PT");
        check(((java.lang.reflect.ParameterizedType) gs).getRawType() == ArrayList.class
              && ((java.lang.reflect.ParameterizedType) gs).getActualTypeArguments()[0] == String.class,
              "control: ArrayList<String> superclass");

        // 4. constructor surface
        GenericSignatureFormatError bare = new GenericSignatureFormatError();
        check(bare.getMessage() == null, "bare null message");
        GenericSignatureFormatError with = new GenericSignatureFormatError("detail");
        check("detail".equals(with.getMessage()), "message kept");
        check(with.toString().contains("GenericSignatureFormatError"), "toString names type");
        check(with.getCause() == null, "no cause by default");

        // 5. Error-level semantics: uncatchable-by-RuntimeException contract
        boolean caughtAsError = false;
        try {
            throw new GenericSignatureFormatError("t");
        } catch (java.lang.Error e) {
            caughtAsError = e instanceof GenericSignatureFormatError;
        }
        check(caughtAsError, "catchable as Error");
    }

    // ===================== deepened batch (2026-11-05) =====================

    static void testLinkageCensus() {
        check(LinkageError.class.isAssignableFrom(GenericSignatureFormatError.class),
              "LinkageError assignable");
        check(Error.class.isAssignableFrom(GenericSignatureFormatError.class),
              "Error assignable");
        check(!RuntimeException.class.isAssignableFrom(GenericSignatureFormatError.class),
              "!RuntimeException.isAssignableFrom");
        check(!Exception.class.isAssignableFrom(GenericSignatureFormatError.class),
              "!Exception.isAssignableFrom");
        check(java.io.Serializable.class.isAssignableFrom(GenericSignatureFormatError.class),
              "Serializable via Throwable");
        boolean asLinkage = false;
        try {
            throw new GenericSignatureFormatError("l");
        } catch (LinkageError e) {
            asLinkage = e instanceof GenericSignatureFormatError;
        }
        check(asLinkage, "catchable as LinkageError");
        boolean seenRuntimeCatch = false;
        boolean fellToErrorCatch = false;
        try {
            throw new GenericSignatureFormatError("l2");
        } catch (RuntimeException e) {
            seenRuntimeCatch = true;
        } catch (GenericSignatureFormatError e) {
            fellToErrorCatch = true;
        }
        check(!seenRuntimeCatch, "not caught by catch(RuntimeException)");
        check(fellToErrorCatch, "reaches the sibling Error handler instead");
    }

    static void testConstructorSurfaceExact() {
        check("java.lang.reflect.GenericSignatureFormatError".equals(
                      new GenericSignatureFormatError().toString()),
              "bare toString exact");
        check("java.lang.reflect.GenericSignatureFormatError: tagged".equals(
                      new GenericSignatureFormatError("tagged").toString()),
              "with-message toString exact");
        GenericSignatureFormatError e = new GenericSignatureFormatError("c");
        RuntimeException root = new RuntimeException("late27");
        e.initCause(root);
        check(e.getCause() == root, "initCause allowed (cause was unset)");
        e.addSuppressed(new IllegalArgumentException("gs"));
        check(e.getSuppressed().length == 1, "suppressed attachable");
    }

    static void testSerializationRoundTrip() throws Exception {
        GenericSignatureFormatError orig = new GenericSignatureFormatError("ser-gsfe27");
        orig.initCause(new IllegalStateException("gsfe-cause27"));
        Object back = serRoundTrip(orig);
        check(back instanceof GenericSignatureFormatError, "round-trip exact type");
        GenericSignatureFormatError res = (GenericSignatureFormatError) back;
        check("ser-gsfe27".equals(res.getMessage()), "round-trip keeps message");
        check(res.getCause() instanceof IllegalStateException, "round-trip keeps cause type");
        check("gsfe-cause27".equals(res.getCause().getMessage()), "round-trip keeps cause message");
        check(res instanceof java.io.Serializable, "instanceof Serializable");
    }

    static void testHealthyAccessorsOnMalformedClass() throws Exception {
        Class<?> loaded = new Loader().define(GenericSignatureFormatErrorTest.class.getName()
                + "$Generic", withBadSignature());
        check(loaded.getSuperclass() == java.util.ArrayList.class,
              "non-generic superclass fine on malformed class");
        check(loaded.getDeclaredFields().length == 0, "fields fine on malformed class");
        check(loaded.getDeclaredMethods().length == 0, "methods fine on malformed class");
        check(!loaded.isInterface(), "kind fine on malformed class");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new GenericSignatureFormatError("p27").printStackTrace(
                new java.io.PrintStream(bos, true, "UTF-8"));
        check(bos.toString("UTF-8").contains("GenericSignatureFormatError"),
              "stack printing does not throw");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLinkageCensus();
        testConstructorSurfaceExact();
        testSerializationRoundTrip();
        testHealthyAccessorsOnMalformedClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
