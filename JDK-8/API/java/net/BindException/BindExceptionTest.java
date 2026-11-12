/*
 * @test
 * @summary java.net.BindException conformance: message and hierarchy.
 *          Deepened: exact constructor surface, toString forms,
 *          serialVersionUID, full hierarchy (BE < SE < IOException), catch
 *          matrix, cause chaining, suppressed exceptions, serialization
 *          round-trips, cross-thread propagation, and a real loopback
 *          trigger: binding a ServerSocket to an occupied port throws
 *          BindException "Address already in use (Bind failed)" on JDK-8.
 * @run main BindExceptionTest
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;

public class BindExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testCtors() {
        BindException e = new BindException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e.getMessage().equals(e.getLocalizedMessage()), "getLocalizedMessage mirrors");
        BindException e0 = new BindException();
        check(e0.getMessage() == null, "no-arg message null");
        BindException en = new BindException((String) null);
        check(en.getMessage() == null, "null message stays null");
        check("java.net.BindException: boom".equals(e.toString()), "toString with message");
        check("java.net.BindException".equals(e0.toString()), "toString without message");
        BindException em = new BindException("");
        check(em.getMessage() != null && em.getMessage().isEmpty(), "empty message retained");
        check("java.net.BindException: ".equals(em.toString()), "toString empty message");
        check(new BindException("占用").getMessage().equals("占用"), "non-ascii message");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.BindException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == SocketException.class, "superclass SocketException");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -5945005768251722951L,
                "serialVersionUID exact");
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 2, "exactly 2 public ctors, got " + ctors.length);
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
        try {
            c.getConstructor(Throwable.class);
            check(false, "ctor(Throwable) absent");
        } catch (NoSuchMethodException nsme) {
            passed++;
        }
        check(c.getSuperclass().getSuperclass() == IOException.class, "grandparent IOException");
    }

    static void testHierarchy() {
        BindException e = new BindException("h");
        check(e instanceof SocketException, "is SocketException");
        check(e instanceof IOException, "is IOException");
        check(e instanceof Exception, "is Exception");
        try { throw e; } catch (SocketException x) { passed++; }
        try { throw e; } catch (IOException x) { passed++; }
        // A plain SocketException is not a BindException (narrowing only).
        SocketException se = new SocketException("x");
        check(!BindException.class.isInstance(se), "plain SE is not BE");
        // STE is on the other branch entirely.
        check(!BindException.class.isInstance(new java.net.SocketTimeoutException("t")), "STE is not BE");
        // equals is identity-based.
        check(!new BindException("h").equals(new BindException("h")), "equals identity");
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
        BindException f2 = new BindException("f2");
        f2.initCause(null);
        check(f2.getCause() == null, "initCause(null) legal when fresh");
        // initCause accepts a BindException as cause (chained bind failures).
        BindException inner = new BindException("inner");
        BindException outer = new BindException("outer");
        outer.initCause(inner);
        check(outer.getCause() instanceof BindException, "BE may cause BE");
    }

    static void testSuppressed() {
        BindException e = new BindException("sup");
        check(e.getSuppressed().length == 0, "no suppressed initially");
        SocketException s1 = new SocketException("s1");
        BindException s2 = new BindException("s2");
        e.addSuppressed(s1);
        e.addSuppressed(s2);
        check(e.getSuppressed().length == 2, "two suppressed");
        check(e.getSuppressed()[0] == s1 && e.getSuppressed()[1] == s2, "order preserved");
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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        BindException back = (BindException) new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getClass() == BindException.class, "same class");
        check("ser".equals(back.getMessage()), "message survives");
        check(back.getCause() != null && "c".equals(back.getCause().getMessage()), "cause survives");
        check(back.getSuppressed().length == 1, "suppressed survives");
        check(back instanceof SocketException, "deserialized is SocketException");
        BindException back0 = (BindException) roundTrip(new BindException());
        check(back0.getMessage() == null, "null message survives");
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
        Thread t = new Thread(new Runnable() {
            public void run() {
                try { throw new BindException("from-thread"); }
                catch (BindException be) { threadResult = be; }
            }
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
        // port, deterministically yields BindException on JDK-8 with suffix
        // "(Bind failed)".
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
                check(be instanceof SocketException, "trigger is SocketException");
            }
            // Binding to the same port with the wildcard address also fails.
            try {
                ServerSocket third = new ServerSocket(first.getLocalPort());
                third.close();
                check(false, "wildcard double bind must throw");
            } catch (BindException be2) {
                passed++;
            } catch (IOException other) {
                check(false, "wildcard double bind must be BindException, got " + other);
            }
        } finally {
            first.close();
        }
        // After the holder closes, the port becomes bindable again.
        int port = first.getLocalPort();
        ServerSocket again = new ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"));
        check(again.getLocalPort() == port, "port rebindable after close");
        again.close();
    }

    public static void main(String[] args) throws Exception {
        java.net.BindException e = new java.net.BindException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.net.SocketException, "is SocketException");
        java.net.BindException e0 = new java.net.BindException();
        check(e0.getMessage() == null, "no-arg message null");
        testCtors();
        testWhiteBox();
        testHierarchy();
        testCauseChaining();
        testSuppressed();
        testSerialization();
        testThreadPropagation();
        testRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
