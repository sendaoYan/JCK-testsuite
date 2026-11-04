/*
 * @test
 * @summary java.net.SocketException conformance: message and hierarchy.
 *          Deepened: exact constructor surface ((), (String) only on JDK-8),
 *          toString/getLocalizedMessage forms, serialVersionUID, cause
 *          chaining contract (initCause one-shot, self-causation rejected),
 *          suppressed exceptions, fillInStackTrace/stack trace capture,
 *          serialization round-trips preserving message/cause/suppressed,
 *          cross-thread propagation, and real loopback triggers (closed
 *          socket accessors yield "Socket is closed").
 * @run main SocketExceptionTest
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class SocketExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testCtors() {
        SocketException e = new SocketException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e.getMessage().equals(e.getLocalizedMessage()), "getLocalizedMessage mirrors");
        SocketException e0 = new SocketException();
        check(e0.getMessage() == null, "no-arg message null");
        SocketException en = new SocketException((String) null);
        check(en.getMessage() == null, "null message stays null");
        // toString: "java.net.SocketException: msg" / bare class name when no message.
        check("java.net.SocketException: boom".equals(e.toString()), "toString with message");
        check("java.net.SocketException".equals(e0.toString()), "toString without message");
        check("java.net.SocketException".equals(en.toString()), "toString null message");
        // Non-ASCII message survives.
        SocketException u = new SocketException("回环");
        check("回环".equals(u.getMessage()), "non-ascii message");
        // Empty message is retained (distinct from null).
        SocketException em = new SocketException("");
        check(em.getMessage() != null && em.getMessage().isEmpty(), "empty message retained");
        check("java.net.SocketException: ".equals(em.toString()), "toString empty message");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == IOException.class, "superclass IOException");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -5935874303556886934L,
                "serialVersionUID exact");
        // JDK-8 surface: exactly the two legacy constructors.
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 2, "exactly 2 public ctors, got " + ctors.length);
        check(c.getConstructor(String.class) != null, "ctor(String) present");
        check(c.getConstructor() != null, "ctor() present");
        try {
            c.getConstructor(Throwable.class);
            check(false, "ctor(Throwable) must be absent on JDK-8");
        } catch (NoSuchMethodException nsme) {
            passed++;
        }
    }

    static void testHierarchy() {
        SocketException e = new SocketException("h");
        check(e instanceof IOException, "is IOException");
        check(e instanceof Exception, "is Exception");
        check(e instanceof Throwable, "is Throwable");
        // Catchable through each ancestor level.
        try { throw e; } catch (IOException x) { passed++; }
        try { throw e; } catch (Exception x) { passed++; }
        // SocketTimeoutException is NOT a SocketException (independent branches).
        SocketException other = new SocketException("o");
        check(!other.equals(new SocketException("h")), "equals is identity-based");
    }

    static void testCauseChaining() {
        RuntimeException cause = new RuntimeException("root");
        SocketException e = new SocketException("outer");
        check(e.getCause() == null, "fresh cause null");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "cause round trip");
        check(e.getCause().getMessage().equals("root"), "cause message");
        try {
            e.initCause(new RuntimeException("second"));
            check(false, "initCause twice must fail");
        } catch (IllegalStateException ise) {
            passed++;
        }
        try {
            e.initCause(null);
            check(false, "initCause(null) after set must fail");
        } catch (IllegalStateException ise) {
            passed++;
        }
        try {
            SocketException self = new SocketException("s");
            self.initCause(self);
            check(false, "self-causation must fail");
        } catch (IllegalArgumentException iae) {
            passed++;
        }
        // initCause(null) on a fresh instance is legal and clears the cause.
        SocketException f = new SocketException("f");
        f.initCause(null);
        check(f.getCause() == null, "initCause(null) legal when fresh");
        // The (String) ctor never presets a cause.
        check(new SocketException("x").getCause() == null, "ctor does not set cause");
    }

    static void testSuppressed() {
        SocketException e = new SocketException("sup");
        check(e.getSuppressed().length == 0, "no suppressed initially");
        SocketTimeoutException s1 = new SocketTimeoutException("s1");
        BindLike s2 = new BindLike("s2");
        e.addSuppressed(s1);
        e.addSuppressed(s2);
        check(e.getSuppressed().length == 2, "two suppressed");
        check(e.getSuppressed()[0] == s1, "suppressed order preserved");
        check(e.getSuppressed()[1] == s2, "suppressed order preserved 2");
        // Returned array is a copy.
        Throwable[] arr = e.getSuppressed();
        arr[0] = null;
        check(e.getSuppressed()[0] != null, "suppressed array is defensive copy");
        try {
            e.addSuppressed(e);
            check(false, "self-suppression must fail");
        } catch (IllegalArgumentException iae) {
            passed++;
        }
    }
    static class BindLike extends java.net.BindException {
        BindLike(String m) { super(m); }
    }

    static void testStack() {
        SocketException e = new SocketExceptionTest().makeHere();
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace() != null, "stack trace non-null");
        check(e.getStackTrace().length > 0, "stack trace non-empty");
        // Inlining may fold makeHere into testStack, so assert on the class.
        check(e.getStackTrace()[0].getClassName().equals("SocketExceptionTest"),
                "stack top is this class, got " + e.getStackTrace()[0]);
        boolean found = false;
        for (StackTraceElement f : e.getStackTrace()) {
            if (f.getMethodName().equals("makeHere") || f.getMethodName().equals("testStack")) {
                found = true; break;
            }
        }
        check(found, "stack captures creation site");
        // Re-fill resets to the current frame.
        SocketException r = new SocketException("r");
        r.fillInStackTrace();
        check(r.getStackTrace().length > 0, "refill still non-empty");
        check(r.getStackTrace()[0].getClassName().equals("SocketExceptionTest"), "refill points here");
    }
    SocketException makeHere() { return new SocketException("made"); }

    static void testSerialization() throws Exception {
        SocketException e = new SocketException("ser");
        RuntimeException cause = new RuntimeException("c");
        e.initCause(cause);
        e.addSuppressed(new SocketTimeoutException("sup"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        SocketException back = (SocketException) ois.readObject();
        check(back != null, "deserialized non-null");
        check(back.getClass() == SocketException.class, "same class");
        check("ser".equals(back.getMessage()), "message survives");
        check(back.getCause() != null && "c".equals(back.getCause().getMessage()), "cause survives");
        check(back.getSuppressed().length == 1, "suppressed survives");
        check("sup".equals(back.getSuppressed()[0].getMessage()), "suppressed message survives");
        // No-arg instance round trip.
        SocketOutputStreamRoundTrip();
    }
    static void SocketOutputStreamRoundTrip() throws Exception {
        SocketException e0 = new SocketException();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e0);
        oos.close();
        SocketException back = (SocketException) new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getMessage() == null, "null message survives");
        check(back instanceof IOException, "deserialized is IOException");
    }

    static SocketException threadResult;
    static void testThreadPropagation() throws Exception {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    throw new SocketException("from-thread");
                } catch (SocketException se) {
                    threadResult = se;
                }
            }
        });
        t.setDaemon(true);
        t.start();
        t.join(2000);
        check(threadResult != null, "thread captured exception");
        check(threadResult instanceof SocketException, "exact type in thread");
        check("from-thread".equals(threadResult.getMessage()), "message preserved in thread");
        check(threadResult.getStackTrace().length > 0, "stack from throwing thread");
    }

    static void testRealTriggers() throws Exception {
        // Closed-socket accessor failures map to SocketException with an exact message.
        Socket s = new Socket();
        s.close();
        try {
            s.getInputStream();
            check(false, "closed getInputStream must throw");
        } catch (SocketException se) {
            check("Socket is closed".equals(se.getMessage()),
                    "closed getInputStream message, got: " + se.getMessage());
            check(se instanceof IOException, "trigger is IOException");
        }
        try {
            s.getSoTimeout();
            check(false, "closed getSoTimeout must throw");
        } catch (SocketException se) {
            check("Socket is closed".equals(se.getMessage()), "closed getSoTimeout message");
        }
        try {
            s.getOutputStream();
            check(false, "closed getOutputStream must throw");
        } catch (SocketException se) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        java.net.SocketException e = new java.net.SocketException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.io.IOException, "is IOException");
        java.net.SocketException e0 = new java.net.SocketException();
        check(e0.getMessage() == null, "no-arg message null");
        testCtors();
        testWhiteBox();
        testHierarchy();
        testCauseChaining();
        testSuppressed();
        testStack();
        testSerialization();
        testThreadPropagation();
        testRealTriggers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
