/*
 * @test
 * @summary javax.imageio.IIOException conformance: white-box structure + black-box behavior.
 * @run main IIOExceptionTest
 */

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class IIOExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { void run() throws Exception; }
    static void expectExact(Class<? extends Throwable> expected, Act a, String msg) {
        try {
            a.run();
            check(false, msg + ": expected " + expected.getSimpleName() + " but nothing thrown");
        } catch (Throwable t) {
            check(t.getClass() == expected,
                  msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.IIOException");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass().getName().equals("java.io.IOException"), "extends IOException");
        check(c.getConstructors().length == 2, "two public constructors");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable via IOException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // single-arg constructor carries the message, no cause
        javax.imageio.IIOException e = new javax.imageio.IIOException("boom");
        check(e instanceof java.io.IOException, "is IOException");
        check("boom".equals(e.getMessage()), "message round-trip");
        check(e.getCause() == null, "no cause for single-arg");

        // null message is preserved
        javax.imageio.IIOException eNull = new javax.imageio.IIOException(null);
        check(eNull.getMessage() == null, "null message preserved");
        check(eNull.getCause() == null, "null message has no cause");

        // two-arg constructor sets message and cause
        java.io.IOException cause = new java.io.IOException("root");
        javax.imageio.IIOException e2 = new javax.imageio.IIOException("wrap", cause);
        check("wrap".equals(e2.getMessage()), "two-arg message");
        check(e2.getCause() == cause, "two-arg cause identity");
        check(e2 instanceof java.io.Serializable, "serializable");

        // two-arg with null message still records the cause
        javax.imageio.IIOException e3 = new javax.imageio.IIOException(null, cause);
        check(e3.getMessage() == null, "two-arg null message");
        check(e3.getCause() == cause, "two-arg null message cause");
    }

    // ---- Part 3 (deep): serialization contract (probed SUID stable across both JDKs) ----
    static void testSerialization() throws Exception {
        Class<?> c = Class.forName("javax.imageio.IIOException");
        long suid = ObjectStreamClass.lookup(c).getSerialVersionUID();
        check(suid == -3216210718638985251L, "serialVersionUID is stable (" + suid + ")");
        // JDK 9+ declares the field explicitly (probed on this JDK)
        try {
            Field f = c.getDeclaredField("serialVersionUID");
            check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                  && f.getType() == long.class, "declared serialVersionUID is static final long");
        } catch (NoSuchFieldException e) {
            check(false, "declared serialVersionUID should exist on this JDK");
        }
        // round-trip keeps message and a copy of the cause
        RuntimeException cause = new RuntimeException("root");
        javax.imageio.IIOException e = new javax.imageio.IIOException("msg1", cause);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray()));
        javax.imageio.IIOException back = (javax.imageio.IIOException) ois.readObject();
        ois.close();
        check(back != null && back.getClass() == c, "deserialized class identity");
        check("msg1".equals(back.getMessage()), "message survives serialization");
        check(back.getCause() != null, "cause survives serialization (as a copy)");
        check(back.getCause() != cause, "cause is deserialized copy, not same reference");
        check("root".equals(back.getCause().getMessage()), "copied cause keeps its message");
        check(back instanceof java.io.IOException, "deserialized is still an IOException");
        // single-arg serializes with null cause
        javax.imageio.IIOException simple = new javax.imageio.IIOException("solo");
        bos = new java.io.ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        oos.writeObject(simple);
        oos.close();
        ois = new ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        javax.imageio.IIOException back2 = (javax.imageio.IIOException) ois.readObject();
        ois.close();
        check("solo".equals(back2.getMessage()), "single-arg message survives");
        check(back2.getCause() == null, "single-arg cause stays null after serialization");
    }

    // ---- Part 3: Throwable behaviors inherited but observable ----
    static void testThrowableContract() {
        java.io.IOException cause = new java.io.IOException("c");
        // initCause is allowed once after the single-arg constructor
        javax.imageio.IIOException one = new javax.imageio.IIOException("one");
        check(one.getCause() == null, "single-arg ctor leaves cause null (initCause allowed)");
        Throwable got = one.initCause(cause);
        check(got == one, "initCause returns this");
        check(one.getCause() == cause, "initCause records the cause");
        expectExact(IllegalStateException.class, () -> one.initCause(new java.io.IOException("x")),
                    "initCause twice");
        // the two-arg constructor sets the cause, so initCause is forbidden
        javax.imageio.IIOException two = new javax.imageio.IIOException("two", cause);
        expectExact(IllegalStateException.class, () -> two.initCause(cause),
                    "initCause after two-arg ctor");
        // addSuppressed / getSuppressed
        javax.imageio.IIOException sup = new javax.imageio.IIOException("sup");
        check(sup.getSuppressed().length == 0, "no suppressed initially");
        java.io.IOException s1 = new java.io.IOException("s1");
        java.io.IOException s2 = new java.io.IOException("s2");
        sup.addSuppressed(s1);
        sup.addSuppressed(s2);
        check(sup.getSuppressed().length == 2, "two suppressed recorded");
        check(sup.getSuppressed()[0] == s1 && sup.getSuppressed()[1] == s2,
              "suppressed order preserved");
        // toString / getStackTrace / getLocalizedMessage
        javax.imageio.IIOException ts = new javax.imageio.IIOException("boom");
        check(ts.toString().equals("javax.imageio.IIOException: boom"), "toString format");
        check("boom".equals(ts.getLocalizedMessage()), "getLocalizedMessage default");
        check(ts.getStackTrace().length > 0, "stack trace captured");
        StackTraceElement top = ts.getStackTrace()[0];
        check(top.getClassName().endsWith("IIOExceptionTest"), "top frame is this test class");
        // polymorphic catch: IIOException is caught as IOException
        boolean caughtAsIo = false;
        try { throw new javax.imageio.IIOException("poly"); }
        catch (java.io.IOException ioEx) { caughtAsIo = true; check(ioEx.getMessage().equals("poly"), "poly message"); }
        check(caughtAsIo, "IIOException is catchable as IOException");
        // nested cause chain navigation
        java.io.FileNotFoundException fnfe = new java.io.FileNotFoundException("file gone");
        javax.imageio.IIOException wrapper = new javax.imageio.IIOException("wrapped", fnfe);
        check(wrapper.getCause() == fnfe, "cause identity");
        check(wrapper.getCause().getCause() == null, "chain terminates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        testThrowableContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
