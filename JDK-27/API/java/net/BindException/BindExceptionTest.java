/*
 * @test
 * @summary java.net.BindException conformance: white-box structure + black-box behavior.
 *          Deepened: exact constructor surface, toString forms,
 *          serialVersionUID, full hierarchy (BE < SE < IOException), catch
 *          matrix, cause chaining, suppressed exceptions, serialization
 *          round-trips, cross-thread propagation, and a real loopback
 *          trigger: binding a ServerSocket to an occupied port throws
 *          BindException "Address already in use" on JDK-27 POSIX (Windows
 *          appends the failing call, "Address already in use: bind").
 * @run main BindExceptionTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

public class BindExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.BindException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(SocketException.class.isAssignableFrom(c), "extends SocketException");
        check(java.io.IOException.class.isAssignableFrom(c), "is IOException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
        check(c.getSuperclass() == SocketException.class, "superclass exact");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -5945005768251722951L,
                "serialVersionUID exact");
        check(c.getConstructors().length == 2, "exactly 2 public ctors, got " + c.getConstructors().length);
        try {
            c.getConstructor(Throwable.class);
            check(false, "ctor(Throwable) absent");
        } catch (NoSuchMethodException nsme) {
            passed++;
        }
        check(c.getSuperclass().getSuperclass() == java.io.IOException.class, "grandparent IOException");
    }

    static void testBlackBox() throws Exception {
        BindException e = new BindException("port in use");
        check(e.getMessage().equals("port in use"), "message");
        check(e instanceof SocketException, "instanceof SocketException");
        check(e instanceof java.io.IOException, "instanceof IOException");
        // catchable via supertype
        try { throw e; } catch (SocketException se) { passed++; }
        BindException e2 = new BindException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    static void testCtorDetails() {
        check("java.net.BindException: port in use".equals(
                new BindException("port in use").toString()), "toString with message");
        check("java.net.BindException".equals(new BindException().toString()), "toString no message");
        BindException en = new BindException((String) null);
        check(en.getMessage() == null, "null message stays null");
        BindException em = new BindException("");
        check(em.getMessage() != null && em.getMessage().isEmpty(), "empty message retained");
        check("java.net.BindException: ".equals(em.toString()), "toString empty message");
        check(new BindException("占用").getMessage().equals("占用"), "non-ascii message");
        check(new BindException("x").getLocalizedMessage().equals("x"), "getLocalizedMessage mirrors");
        check(!new BindException("h").equals(new BindException("h")), "equals identity");
    }

    static void testHierarchy() {
        BindException e = new BindException("h");
        try { throw e; } catch (java.io.IOException x) { passed++; }
        try { throw e; } catch (Exception x) { passed++; }
        SocketException se = new SocketException("x");
        check(!BindException.class.isInstance(se), "plain SE is not BE");
        check(!BindException.class.isInstance(new SocketTimeoutException("t")), "STE is not BE");
        // BindException narrows SocketException: catching BE after SE is
        // unreachable-in-practice but the subtyping direction is one-way.
        check(BindException.class.isAssignableFrom(BindException.class), "self assignable");
    }

    static void testCauseChaining() {
        RuntimeException cause = new RuntimeException("c");
        BindException e = new BindException("f");
        check(e.getCause() == null, "fresh cause null");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "cause round trip");
        try { e.initCause(new RuntimeException("y")); check(false, "twice ISE"); }
        catch (IllegalStateException ise) { passed++; }
        try {
            BindException self = new BindException("s");
            self.initCause(self);
            check(false, "self IAE");
        } catch (IllegalArgumentException iae) {
            passed++;
        }
        BindException inner = new BindException("inner");
        BindException outer = new BindException("outer");
        outer.initCause(inner);
        check(outer.getCause() instanceof BindException, "BE may cause BE");
    }

    static void testSuppressed() {
        BindException e = new BindException("sup");
        check(e.getSuppressed().length == 0, "no suppressed initially");
        e.addSuppressed(new SocketException("s1"));
        e.addSuppressed(new BindException("s2"));
        check(e.getSuppressed().length == 2, "two suppressed");
        check(e.getSuppressed()[1] instanceof BindException, "second is BE");
        Throwable[] arr = e.getSuppressed();
        arr[0] = null;
        check(e.getSuppressed()[0] != null, "defensive copy");
        try { e.addSuppressed(e); check(false, "self-suppression IAE"); }
        catch (IllegalArgumentException iae) { passed++; }
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack non-empty");
    }

    static void testSerialization() throws Exception {
        BindException e = new BindException("ser");
        e.initCause(new RuntimeException("c"));
        e.addSuppressed(new BindException("sup"));
        Object back = roundTrip(e);
        BindException b = (BindException) back;
        check(b.getClass() == BindException.class, "same class");
        check("ser".equals(b.getMessage()), "message survives");
        check(b.getCause() != null && "c".equals(b.getCause().getMessage()), "cause survives");
        check(b.getSuppressed().length == 1, "suppressed survives");
        check(b instanceof SocketException, "deserialized is SocketException");
        BindException b0 = (BindException) roundTrip(new BindException());
        check(b0.getMessage() == null, "null message survives");
    }
    static Object roundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    static volatile BindException threadResult;
    static void testThreadPropagation() throws Exception {
        Thread t = new Thread(() -> {
            try { throw new BindException("from-thread"); }
            catch (BindException be) { threadResult = be; }
        });
        t.setDaemon(true);
        t.start();
        t.join(2000);
        check(threadResult != null, "thread captured");
        check("from-thread".equals(threadResult.getMessage()), "message preserved");
        check(threadResult instanceof SocketException, "hierarchy preserved in thread");
    }

    static void testRealTrigger() throws Exception {
        // Occupying a loopback port, then binding a second server to the same
        // port, deterministically yields BindException; on JDK-27 the message
        // is exactly "Address already in use" (the JDK-8 suffix is gone).
        ServerSocket first = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        try {
            try {
                ServerSocket second = new ServerSocket(first.getLocalPort(), 50,
                        InetAddress.getByName("127.0.0.1"));
                second.close();
                check(false, "double bind must throw");
            } catch (BindException be) {
                check(be.getMessage().startsWith("Address already in use"),
                        "trigger message prefix, got: " + be.getMessage());
                // The JDK-27 POSIX text has no suffix; the Windows winsock
                // mapping appends the failing call: "...: bind".
                String expected = File.separatorChar == '\\'
                        ? "Address already in use: bind" : "Address already in use";
                check(expected.equals(be.getMessage()),
                        "exact trigger message, got: " + be.getMessage());
                check(be instanceof SocketException, "trigger is SocketException");
            }
            try {
                ServerSocket third = new ServerSocket(first.getLocalPort());
                third.close();
                check(false, "wildcard double bind must throw");
            } catch (BindException be2) {
                passed++;
            }
        } finally {
            first.close();
        }
        int port = first.getLocalPort();
        ServerSocket again = new ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"));
        check(again.getLocalPort() == port, "port rebindable after close");
        again.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorDetails();
        testHierarchy();
        testCauseChaining();
        testSuppressed();
        testSerialization();
        testThreadPropagation();
        testRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
