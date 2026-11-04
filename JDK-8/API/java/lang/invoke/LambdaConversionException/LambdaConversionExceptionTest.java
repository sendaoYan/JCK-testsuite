/*
 * @test
 * @summary java.lang.invoke.LambdaConversionException conformance: hierarchy and ctors.
 *          Deepened: full five-constructor matrix (suppression/writable-stack-
 *          trace flags observable via addSuppressed/getStackTrace, cause-only
 *          ctor message == cause.toString()), Serializable contract with
 *          serialization round-trip, initCause rules ("Can't overwrite cause"
 *          ISE), and a real LambdaMetafactory conversion failure with message
 *          fragment ("is not an interface").
 * @run main LambdaConversionExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class LambdaConversionExceptionTest {
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

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
            String frag, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (t.getMessage() != null && t.getMessage().contains(frag)) {
                    passed++; return;
                }
                throw new AssertionError(msg + ": expected message fragment \"" + frag
                        + "\" but got \"" + t.getMessage() + "\"");
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    public interface F {
        int len(String s);
    }

    static int body(String s) { return s.length(); }

    static void testLambdaConversionException() {
        check(Exception.class.isAssignableFrom(LambdaConversionException.class),
              "extends Exception");
        check(new LambdaConversionException().getMessage() == null, "()");
        check("m".equals(new LambdaConversionException("m").getMessage()), "(String)");
        Throwable cause = new Throwable();
        check(new LambdaConversionException("m", cause).getCause() == cause, "(String,Throwable)");
        check(new LambdaConversionException(cause).getCause() == cause, "(Throwable)");
        check(new LambdaConversionException("m", cause, true, false).getCause() == cause,
              "(String,Throwable,boolean,boolean)");
    }

    // Full constructor matrix with observable suppression / stack-trace flags.
    static void testConstructorChainDeep() throws Exception {
        Class<?> c = LambdaConversionException.class;
        check(c.getSuperclass() == Exception.class, "direct Exception subclass");
        check(Serializable.class.isAssignableFrom(c), "Serializable via Throwable");
        int publicCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) publicCtors++;
        }
        check(publicCtors == 5, "five public constructors");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(String.class, Throwable.class);
        c.getDeclaredConstructor(Throwable.class);
        c.getDeclaredConstructor(String.class, Throwable.class, boolean.class, boolean.class);

        LambdaConversionException empty = new LambdaConversionException();
        check(empty.getMessage() == null, "no-arg message null");
        check(empty.getCause() == null, "no-arg cause null");

        Throwable cause = new IllegalArgumentException("inner");
        LambdaConversionException fromCause = new LambdaConversionException(cause);
        check(fromCause.getCause() == cause, "cause-only ctor cause identity");
        check(fromCause.getMessage().equals(cause.toString()),
              "cause-only ctor message is cause.toString()");

        LambdaConversionException def = new LambdaConversionException("d");
        check(def.getStackTrace().length > 0, "stack trace captured by default");
        def.addSuppressed(new RuntimeException("s"));
        check(def.getSuppressed().length == 1, "suppression enabled by default");

        LambdaConversionException quiet = new LambdaConversionException("q", cause, false, false);
        check(quiet.getStackTrace().length == 0, "writableStackTrace=false -> no frames");
        quiet.addSuppressed(new RuntimeException("s1"));
        quiet.addSuppressed(new RuntimeException("s2"));
        check(quiet.getSuppressed().length == 0, "suppression disabled -> dropped");
        check("q".equals(quiet.getMessage()) && quiet.getCause() == cause,
              "full ctor keeps message and cause");
    }

    // Serialization round-trip preserves the message and class.
    static void testSerializationRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new LambdaConversionException("lce-msg"));
        oos.close();
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof LambdaConversionException, "round-trip same class");
        check("lce-msg".equals(((LambdaConversionException) back).getMessage()),
              "message preserved by serialization");
        check(back instanceof Serializable, "round-trip object is Serializable");
    }

    // initCause: once allowed, the second attempt is the standard ISE; a
    // constructor-provided cause can never be replaced.
    static void testInitCauseRules() {
        LambdaConversionException e = new LambdaConversionException("m");
        RuntimeException first = new RuntimeException("c1");
        check(e.initCause(first) == e, "initCause returns this");
        check(e.getCause() == first, "cause installed");
        checkThrowsMsg(IllegalStateException.class,
              () -> e.initCause(new RuntimeException("c2")),
              "Can't overwrite cause", "second initCause ISE message");
        LambdaConversionException withCause =
              new LambdaConversionException("m", new RuntimeException("c"));
        checkThrowsMsg(IllegalStateException.class,
              () -> withCause.initCause(new RuntimeException("c2")),
              "Can't overwrite cause", "ctor-provided cause cannot be replaced");
    }

    // Real trigger: LambdaMetafactory rejects a non-interface functional type.
    static void testRealTrigger() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle impl = lookup.findStatic(LambdaConversionExceptionTest.class, "body",
                MethodType.methodType(int.class, String.class));
        checkThrowsMsg(LambdaConversionException.class,
              () -> LambdaMetafactory.metafactory(lookup, "len",
                      MethodType.methodType(String.class),
                      MethodType.methodType(int.class, String.class),
                      impl, MethodType.methodType(int.class, String.class)),
              "is not an interface", "metafactory non-interface functional type LCE");
        // a well-formed request still succeeds (control)
        java.lang.invoke.CallSite cs = LambdaMetafactory.metafactory(lookup, "len",
                MethodType.methodType(F.class),
                MethodType.methodType(int.class, String.class),
                impl, MethodType.methodType(int.class, String.class));
        F f = (F) cs.dynamicInvoker().invokeExact();
        check(f.len("abc") == 3, "well-formed metafactory link works");
    }

    // Throwable default surface: suppressed initially empty, toString prefix.
    static void testThrowableDefaults() {
        LambdaConversionException e = new LambdaConversionException("t");
        check(e.toString().startsWith("java.lang.invoke.LambdaConversionException: t"),
              "toString prefix with message");
        check(new LambdaConversionException().toString()
                    .equals("java.lang.invoke.LambdaConversionException"),
              "no-message toString has no colon");
        check(e.getSuppressed().length == 0, "no suppressed initially");
        try {
            LambdaConversionException.class.getDeclaredField("serialVersionUID");
            check(true, "serialVersionUID declared");
        } catch (NoSuchFieldException nsf) {
            check(false, "serialVersionUID should be declared: " + nsf);
        }
        // writableStackTrace=true variant keeps frames
        LambdaConversionException traced = new LambdaConversionException("m", null, true, true);
        check(traced.getStackTrace().length > 0, "full flags keep stack trace");
        check(traced.getCause() == null, "null cause accepted by full ctor");
    }

    public static void main(String[] args) throws Throwable {
        testLambdaConversionException();
        testConstructorChainDeep();
        testSerializationRoundTrip();
        testInitCauseRules();
        testRealTrigger();
        testThrowableDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
