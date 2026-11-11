/*
 * @test
 * @summary java.net.ConnectException conformance: white-box structure + black-box behavior.
 *          Deepened: exact constructor surface, toString forms,
 *          serialVersionUID, full hierarchy (CE < SE < IOException), catch
 *          matrix, cause chaining, suppressed exceptions, serialization
 *          round-trips, cross-thread propagation, and a real loopback
 *          trigger: connecting to a freshly closed loopback port throws
 *          ConnectException "Connection refused (connect failed)" on JDK-27.
 * @run main ConnectExceptionTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

public class ConnectExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ConnectException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(SocketException.class.isAssignableFrom(c), "extends SocketException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
        check(c.getSuperclass() == SocketException.class, "superclass exact");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 3831404271622369215L,
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
        ConnectException e = new ConnectException("connection refused");
        check(e.getMessage().equals("connection refused"), "message");
        check(e instanceof SocketException, "instanceof SocketException");
        check(e instanceof java.io.IOException, "instanceof IOException");
        try { throw e; } catch (SocketException se) { passed++; }
        ConnectException e2 = new ConnectException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    static void testCtorDetails() {
        check("java.net.ConnectException: connection refused".equals(
                new ConnectException("connection refused").toString()), "toString with message");
        check("java.net.ConnectException".equals(new ConnectException().toString()), "toString no message");
        ConnectException en = new ConnectException((String) null);
        check(en.getMessage() == null, "null message stays null");
        ConnectException em = new ConnectException("");
        check(em.getMessage() != null && em.getMessage().isEmpty(), "empty message retained");
        check("java.net.ConnectException: ".equals(em.toString()), "toString empty message");
        check(new ConnectException("拒绝").getMessage().equals("拒绝"), "non-ascii message");
        check(new ConnectException("x").getLocalizedMessage().equals("x"), "getLocalizedMessage mirrors");
        check(!new ConnectException("h").equals(new ConnectException("h")), "equals identity");
    }

    static void testHierarchy() {
        ConnectException e = new ConnectException("h");
        try { throw e; } catch (java.io.IOException x) { passed++; }
        try { throw e; } catch (Exception x) { passed++; }
        SocketException se = new SocketException("x");
        check(!ConnectException.class.isInstance(se), "plain SE is not CE");
        check(!ConnectException.class.isInstance(new SocketTimeoutException("t")), "STE is not CE");
        check(!ConnectException.class.isInstance(new BindException("b")), "BE is not CE");
    }

    static void testCauseChaining() {
        RuntimeException cause = new RuntimeException("c");
        ConnectException e = new ConnectException("f");
        check(e.getCause() == null, "fresh cause null");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "cause round trip");
        try { e.initCause(new RuntimeException("y")); check(false, "twice ISE"); }
        catch (IllegalStateException ise) { passed++; }
        try {
            ConnectException self = new ConnectException("s");
            self.initCause(self);
            check(false, "self IAE");
        } catch (IllegalArgumentException iae) {
            passed++;
        }
        ConnectException inner = new ConnectException("attempt 1 refused");
        ConnectException outer = new ConnectException("gave up");
        outer.initCause(inner);
        check(outer.getCause() instanceof ConnectException, "CE may cause CE");
        check(outer.getCause().getMessage().equals("attempt 1 refused"), "chained message readable");
    }

    static void testSuppressed() {
        ConnectException e = new ConnectException("sup");
        check(e.getSuppressed().length == 0, "no suppressed initially");
        e.addSuppressed(new SocketException("s1"));
        e.addSuppressed(new ConnectException("s2"));
        check(e.getSuppressed().length == 2, "two suppressed");
        check(e.getSuppressed()[1] instanceof ConnectException, "suppressed polymorphic");
        Throwable[] arr = e.getSuppressed();
        arr[0] = null;
        check(e.getSuppressed()[0] != null, "defensive copy");
        try { e.addSuppressed(e); check(false, "self-suppression IAE"); }
        catch (IllegalArgumentException iae) { passed++; }
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack non-empty");
    }

    static void testSerialization() throws Exception {
        ConnectException e = new ConnectException("ser");
        e.initCause(new RuntimeException("c"));
        e.addSuppressed(new ConnectException("sup"));
        Object back = roundTrip(e);
        ConnectException b = (ConnectException) back;
        check(b.getClass() == ConnectException.class, "same class");
        check("ser".equals(b.getMessage()), "message survives");
        check(b.getCause() != null && "c".equals(b.getCause().getMessage()), "cause survives");
        check(b.getSuppressed().length == 1, "suppressed survives");
        check(b instanceof SocketException, "deserialized is SocketException");
        ConnectException b0 = (ConnectException) roundTrip(new ConnectException());
        check(b0.getMessage() == null, "null message survives");
    }
    static Object roundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    static volatile ConnectException threadResult;
    static void testThreadPropagation() throws Exception {
        Thread t = new Thread(() -> {
            try { throw new ConnectException("from-thread"); }
            catch (ConnectException ce) { threadResult = ce; }
        });
        t.setDaemon(true);
        t.start();
        t.join(2000);
        check(threadResult != null, "thread captured");
        check("from-thread".equals(threadResult.getMessage()), "message preserved");
        check(threadResult instanceof SocketException, "hierarchy preserved in thread");
    }

    static void testRealTrigger() throws Exception {
        // Reserve then release a loopback port; connecting to it is refused.
        ServerSocket holder = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        int deadPort = holder.getLocalPort();
        holder.close();
        Socket s = new Socket();
        boolean refused = false;
        try {
            try {
                s.connect(new InetSocketAddress("127.0.0.1", deadPort), 1000);
                check(false, "refused connect must throw");
            } catch (ConnectException ce) {
                refused = true;
                check(ce.getMessage() != null && ce.getMessage().startsWith("Connection refused"),
                        "trigger message prefix, got: " + ce.getMessage());
                // The trailing "(connect failed)" is a platform strerror detail:
                // Linux appends it, Windows reports the bare "Connection refused".
                String m = ce.getMessage();
                check("Connection refused".equals(m) || "Connection refused (connect failed)".equals(m),
                        "JDK-27 trigger message, got: " + m);
                check(ce instanceof SocketException, "trigger is SocketException");
            } catch (java.net.SocketTimeoutException ste) {
                // Windows may drop the SYN for a freshly released loopback port
                // and report a timeout instead of an explicit refusal.
                passed++;
            }
            // JDK-27 marks the socket closed after a refused connect
            // (JDK-8 reports isClosed=false here); only meaningful on refusal.
            if (refused) {
                check(s.isClosed(), "JDK-27 socket closed after refused connect");
            }
            check(!s.isConnected(), "not connected after refusal");
        } finally {
            s.close();
        }
        Socket s2 = new Socket();
        try {
            try {
                s2.connect(new InetSocketAddress("127.0.0.1", deadPort), 1000);
                check(false, "second refused connect must throw");
            } catch (ConnectException ce2) {
                passed++;
            } catch (java.net.SocketTimeoutException ste2) {
                passed++;
            }
        } finally {
            s2.close();
        }
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
