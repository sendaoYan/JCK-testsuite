/*
 * @test
 * @summary java.net.SocketException conformance: white-box structure + black-box behavior.
 *          Deepened: exact 4-constructor surface (JDK 20+ adds (String,Throwable)
 *          and (Throwable)), toString forms, serialVersionUID, cause-chaining
 *          contract (initCause one-shot, self-causation rejected), suppressed
 *          exceptions, stack capture, serialization preserving
 *          message/cause/suppressed, cross-thread propagation, and real
 *          loopback triggers (closed socket accessors yield "Socket is closed").
 * @run main SocketExceptionTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

public class SocketExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getConstructor() != null, "ctor()");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, Throwable.class) != null, "ctor(String,Throwable)");
        check(c.getConstructor(Throwable.class) != null, "ctor(Throwable)");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == java.io.IOException.class, "superclass IOException");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -5935874303556886934L,
                "serialVersionUID exact");
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 4, "exactly 4 public ctors, got " + ctors.length);
    }

    static void testBlackBox() throws Exception {
        SocketException e = new SocketException("boom");
        check(e.getMessage().equals("boom"), "message");
        check(e instanceof java.io.IOException, "instanceof IOException");

        Throwable cause = new RuntimeException("cause");
        SocketException chained = new SocketException("outer", cause);
        check(chained.getMessage().equals("outer"), "chained message");
        check(chained.getCause() == cause, "chained cause");

        SocketException causeOnly = new SocketException(cause);
        check(causeOnly.getCause() == cause, "cause-only");
        check(causeOnly.getMessage() == null || causeOnly.getMessage().equals(cause.toString()), "cause-only message");

        SocketException empty = new SocketException();
        check(empty.getMessage() == null, "no-arg message null");
    }

    static void testCtorDetails() {
        Throwable cause = new RuntimeException("root");
        SocketException causeOnly = new SocketException(cause);
        // JDK 20+ convention: message mirrors cause.toString().
        check(cause.toString().equals(causeOnly.getMessage()),
                "cause-only message is cause.toString(), got " + causeOnly.getMessage());
        // Chained ctor with null cause is permitted (cause stays unset).
        SocketException n = new SocketException("m", null);
        check("m".equals(n.getMessage()), "chained null-cause message");
        check(n.getCause() == null, "chained null-cause stays null");
        // initCause on a (String,Throwable)-constructed instance is rejected.
        try {
            n.initCause(new RuntimeException("x"));
            check(false, "initCause after ctor cause must fail");
        } catch (IllegalStateException ise) {
            passed++;
        }
        // Chained ctor chains self-reference rejection.
        SocketException self = new SocketException("s");
        try {
            new SocketException("s2", self).initCause(self);
            check(false, "overwriting ctor cause must fail");
        } catch (IllegalStateException ise) {
            passed++;
        }
    }

    static void testMessageForms() {
        SocketException e0 = new SocketException();
        check("java.net.SocketException".equals(e0.toString()), "toString no message");
        check("java.net.SocketException: boom".equals(new SocketException("boom").toString()),
                "toString with message");
        SocketException em = new SocketException("");
        check("java.net.SocketException: ".equals(em.toString()), "toString empty message");
        check(new SocketException("回环").getMessage().equals("回环"), "non-ascii message");
        SocketException en = new SocketException((String) null);
        check(en.getMessage() == null, "null message stays null");
        check("java.net.SocketException".equals(en.toString()), "toString null message");
        check(new SocketException("x").getLocalizedMessage().equals("x"), "getLocalizedMessage mirrors");
    }

    static void testHierarchy() {
        SocketException e = new SocketException("h");
        check(e instanceof Exception, "is Exception");
        check(e instanceof Throwable, "is Throwable");
        try { throw e; } catch (Exception x) { passed++; }
        try { throw e; } catch (java.io.IOException x) { passed++; }
        // equals is identity-based.
        check(!new SocketException("h").equals(new SocketException("h")), "equals identity");
    }

    static void testCauseContract() {
        RuntimeException cause = new RuntimeException("c");
        SocketException e = new SocketException("f");
        check(e.getCause() == null, "fresh cause null");
        check(e.initCause(cause) == e, "initCause returns this");
        try { e.initCause(new RuntimeException("y")); check(false, "twice ISE"); }
        catch (IllegalStateException ise) { passed++; }
        try { e.initCause(null); check(false, "null-after ISE"); }
        catch (IllegalStateException ise) { passed++; }
        SocketException self = new SocketException("s");
        try { self.initCause(self); check(false, "self IAE"); }
        catch (IllegalArgumentException iae) { passed++; }
        SocketException f2 = new SocketException("f2");
        f2.initCause(null);
        check(f2.getCause() == null, "initCause(null) legal when fresh");
    }

    static void testSuppressed() {
        SocketException e = new SocketException("sup");
        check(e.getSuppressed().length == 0, "no suppressed initially");
        SocketTimeoutException s1 = new SocketTimeoutException("s1");
        e.addSuppressed(s1);
        e.addSuppressed(new BindException("s2"));
        check(e.getSuppressed().length == 2, "two suppressed");
        check(e.getSuppressed()[0] == s1, "order preserved");
        Throwable[] arr = e.getSuppressed();
        arr[0] = null;
        check(e.getSuppressed()[0] != null, "defensive copy");
        try { e.addSuppressed(e); check(false, "self-suppression IAE"); }
        catch (IllegalArgumentException iae) { passed++; }
    }

    static void testStack() {
        SocketException e = makeHere();
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack non-empty");
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
        SocketException r = new SocketException("r");
        r.fillInStackTrace();
        check(r.getStackTrace()[0].getClassName().equals("SocketExceptionTest"), "refill points here");
    }
    static SocketException makeHere() { return new SocketException("made"); }

    static void testSerialization() throws Exception {
        SocketException e = new SocketException("ser");
        RuntimeException cause = new RuntimeException("c");
        e.initCause(cause);
        e.addSuppressed(new SocketTimeoutException("sup"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        SocketException back = (SocketException) new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getClass() == SocketException.class, "same class");
        check("ser".equals(back.getMessage()), "message survives");
        check(back.getCause() != null && "c".equals(back.getCause().getMessage()), "cause survives");
        check(back.getSuppressed().length == 1, "suppressed survives");
        check("sup".equals(back.getSuppressed()[0].getMessage()), "suppressed message survives");
        SocketException back0 = (SocketException) roundTrip(new SocketException());
        check(back0.getMessage() == null, "null message survives");
        check(back0 instanceof java.io.IOException, "deserialized is IOException");
    }
    static Object roundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    static volatile SocketException threadResult;
    static void testThreadPropagation() throws Exception {
        Thread t = new Thread(() -> {
            try { throw new SocketException("from-thread"); }
            catch (SocketException se) { threadResult = se; }
        });
        t.setDaemon(true);
        t.start();
        t.join(2000);
        check(threadResult != null, "thread captured");
        check("from-thread".equals(threadResult.getMessage()), "message preserved in thread");
        check(threadResult.getStackTrace().length > 0, "stack from throwing thread");
    }

    static void testRealTriggers() throws Exception {
        Socket s = new Socket();
        s.close();
        try {
            s.getInputStream();
            check(false, "closed getInputStream must throw");
        } catch (SocketException se) {
            check("Socket is closed".equals(se.getMessage()),
                    "closed getInputStream message, got: " + se.getMessage());
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
        testWhiteBox();
        testBlackBox();
        testCtorDetails();
        testMessageForms();
        testHierarchy();
        testCauseContract();
        testSuppressed();
        testStack();
        testSerialization();
        testThreadPropagation();
        testRealTriggers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
