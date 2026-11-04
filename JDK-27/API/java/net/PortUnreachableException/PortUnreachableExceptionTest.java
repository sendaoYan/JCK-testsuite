/*
 * @test
 * @summary java.net.PortUnreachableException conformance: white-box structure + black-box behavior.
 *          Deepened: exact constructor surface, toString forms,
 *          serialVersionUID, full hierarchy (PUE < SE < IOException), catch
 *          matrix, cause chaining, suppressed exceptions, serialization
 *          round-trips, cross-thread propagation, and a real loopback
 *          trigger: a connected DatagramSocket that sends to a freshly
 *          closed loopback UDP port receives the ICMP error as a
 *          PortUnreachableException (message null on JDK-27, unlike the
 *          JDK-8 "ICMP Port Unreachable").
 * @run main PortUnreachableExceptionTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

public class PortUnreachableExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.PortUnreachableException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(SocketException.class.isAssignableFrom(c), "extends SocketException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
        check(c.getSuperclass() == SocketException.class, "superclass exact");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 8462541992376507323L,
                "serialVersionUID exact");
        check(c.getConstructors().length == 2, "exactly 2 public ctors, got " + c.getConstructors().length);
        try {
            c.getConstructor(Throwable.class);
            check(false, "ctor(Throwable) absent");
        } catch (NoSuchMethodException nsme) {
            passed++;
        }
        check(c.getSuperclass().getSuperclass() == java.io.IOException.class, "grandparent IOException");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
    }

    static void testBlackBox() throws Exception {
        PortUnreachableException e = new PortUnreachableException("icmp unreachable");
        check(e.getMessage().equals("icmp unreachable"), "message");
        check(e instanceof SocketException, "instanceof SocketException");
        try { throw e; } catch (SocketException se) { passed++; }
        PortUnreachableException e2 = new PortUnreachableException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    static void testCtorDetails() {
        check("java.net.PortUnreachableException: icmp unreachable".equals(
                new PortUnreachableException("icmp unreachable").toString()), "toString with message");
        check("java.net.PortUnreachableException".equals(
                new PortUnreachableException().toString()), "toString no message");
        PortUnreachableException en = new PortUnreachableException((String) null);
        check(en.getMessage() == null, "null message stays null");
        PortUnreachableException em = new PortUnreachableException("");
        check(em.getMessage() != null && em.getMessage().isEmpty(), "empty message retained");
        check("java.net.PortUnreachableException: ".equals(em.toString()), "toString empty message");
        check(new PortUnreachableException("端口不可达").getMessage().equals("端口不可达"), "non-ascii message");
        check(new PortUnreachableException("x").getLocalizedMessage().equals("x"),
                "getLocalizedMessage mirrors");
        check(!new PortUnreachableException("h").equals(new PortUnreachableException("h")), "equals identity");
    }

    static void testHierarchy() {
        PortUnreachableException e = new PortUnreachableException("h");
        check(e instanceof java.io.IOException, "is IOException");
        try { throw e; } catch (java.io.IOException x) { passed++; }
        try { throw e; } catch (Exception x) { passed++; }
        SocketException se = new SocketException("x");
        check(!PortUnreachableException.class.isInstance(se), "plain SE is not PUE");
        check(!PortUnreachableException.class.isInstance(new BindException("b")), "BE is not PUE");
        check(!PortUnreachableException.class.isInstance(new ConnectException("c")), "CE is not PUE");
        check(!PortUnreachableException.class.isInstance(new SocketTimeoutException("t")), "STE is not PUE");
    }

    static void testCauseChaining() {
        RuntimeException cause = new RuntimeException("c");
        PortUnreachableException e = new PortUnreachableException("f");
        check(e.getCause() == null, "fresh cause null");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "cause round trip");
        try { e.initCause(new RuntimeException("y")); check(false, "twice ISE"); }
        catch (IllegalStateException ise) { passed++; }
        try {
            PortUnreachableException self = new PortUnreachableException("s");
            self.initCause(self);
            check(false, "self IAE");
        } catch (IllegalArgumentException iae) {
            passed++;
        }
        PortUnreachableException outer = new PortUnreachableException("outer");
        outer.initCause(new PortUnreachableException("inner"));
        check(outer.getCause() instanceof PortUnreachableException, "PUE may cause PUE");
    }

    static void testSuppressed() {
        PortUnreachableException e = new PortUnreachableException("sup");
        check(e.getSuppressed().length == 0, "no suppressed initially");
        e.addSuppressed(new SocketException("s1"));
        e.addSuppressed(new PortUnreachableException("s2"));
        check(e.getSuppressed().length == 2, "two suppressed");
        check(e.getSuppressed()[1] instanceof PortUnreachableException, "suppressed polymorphic");
        Throwable[] arr = e.getSuppressed();
        arr[0] = null;
        check(e.getSuppressed()[0] != null, "defensive copy");
        try { e.addSuppressed(e); check(false, "self-suppression IAE"); }
        catch (IllegalArgumentException iae) { passed++; }
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack non-empty");
    }

    static void testSerialization() throws Exception {
        PortUnreachableException e = new PortUnreachableException("ser");
        e.initCause(new RuntimeException("c"));
        e.addSuppressed(new PortUnreachableException("sup"));
        Object back = roundTrip(e);
        PortUnreachableException b = (PortUnreachableException) back;
        check(b.getClass() == PortUnreachableException.class, "same class");
        check("ser".equals(b.getMessage()), "message survives");
        check(b.getCause() != null && "c".equals(b.getCause().getMessage()), "cause survives");
        check(b.getSuppressed().length == 1, "suppressed survives");
        check(b instanceof SocketException, "deserialized is SocketException");
        PortUnreachableException b0 = (PortUnreachableException) roundTrip(new PortUnreachableException());
        check(b0.getMessage() == null, "null message survives");
    }
    static Object roundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    static volatile PortUnreachableException threadResult;
    static void testThreadPropagation() throws Exception {
        Thread t = new Thread(() -> {
            try { throw new PortUnreachableException("from-thread"); }
            catch (PortUnreachableException pe) { threadResult = pe; }
        });
        t.setDaemon(true);
        t.start();
        t.join(2000);
        check(threadResult != null, "thread captured");
        check("from-thread".equals(threadResult.getMessage()), "message preserved");
        check(threadResult instanceof SocketException, "hierarchy preserved in thread");
    }

    static void testUdpTrigger() throws Exception {
        DatagramSocket dead = new DatagramSocket(new InetSocketAddress("127.0.0.1", 0));
        int deadPort = dead.getLocalPort();
        dead.close();
        DatagramSocket u = new DatagramSocket();
        try {
            u.connect(new InetSocketAddress("127.0.0.1", deadPort));
            check(u.isConnected(), "datagram socket connected");
            u.send(new DatagramPacket(new byte[1], 1, InetAddress.getByName("127.0.0.1"), deadPort));
            u.setSoTimeout(500);
            try {
                u.receive(new DatagramPacket(new byte[1], 1));
                check(false, "receive must fail after ICMP");
            } catch (PortUnreachableException pue) {
                check(pue instanceof SocketException, "trigger is SocketException");
                check(pue.getMessage() == null,
                        "JDK-27 trigger message is null, got: " + pue.getMessage());
            } catch (SocketTimeoutException ste) {
                check(false, "expected ICMP delivery, got timeout: " + ste);
            }
        } finally {
            u.close();
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
        testUdpTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
