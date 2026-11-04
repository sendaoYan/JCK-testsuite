/*
 * @test
 * @summary java.net.PortUnreachableException conformance: message and hierarchy.
 *          Deepened: exact constructor surface, toString forms,
 *          serialVersionUID, full hierarchy (PUE < SE < IOException), catch
 *          matrix, cause chaining, suppressed exceptions, serialization
 *          round-trips, cross-thread propagation, and a real loopback
 *          trigger: a connected DatagramSocket that sends to a freshly
 *          closed loopback UDP port receives the ICMP error as a
 *          PortUnreachableException ("ICMP Port Unreachable" on JDK-8).
 * @run main PortUnreachableExceptionTest
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.net.BindException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class PortUnreachableExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testCtors() {
        PortUnreachableException e = new PortUnreachableException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e.getMessage().equals(e.getLocalizedMessage()), "getLocalizedMessage mirrors");
        PortUnreachableException e0 = new PortUnreachableException();
        check(e0.getMessage() == null, "no-arg message null");
        PortUnreachableException en = new PortUnreachableException((String) null);
        check(en.getMessage() == null, "null message stays null");
        check("java.net.PortUnreachableException: boom".equals(e.toString()), "toString with message");
        check("java.net.PortUnreachableException".equals(e0.toString()), "toString without message");
        PortUnreachableException em = new PortUnreachableException("");
        check(em.getMessage() != null && em.getMessage().isEmpty(), "empty message retained");
        check("java.net.PortUnreachableException: ".equals(em.toString()), "toString empty message");
        check(new PortUnreachableException("端口不可达").getMessage().equals("端口不可达"), "non-ascii message");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.PortUnreachableException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == SocketException.class, "superclass SocketException");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 8462541992376507323L,
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
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
    }

    static void testHierarchy() {
        PortUnreachableException e = new PortUnreachableException("h");
        check(e instanceof SocketException, "is SocketException");
        check(e instanceof IOException, "is IOException");
        check(e instanceof Exception, "is Exception");
        try { throw e; } catch (SocketException x) { passed++; }
        try { throw e; } catch (IOException x) { passed++; }
        SocketException se = new SocketException("x");
        check(!PortUnreachableException.class.isInstance(se), "plain SE is not PUE");
        check(!PortUnreachableException.class.isInstance(new BindException("b")), "BE is not PUE");
        check(!PortUnreachableException.class.isInstance(new ConnectException("c")), "CE is not PUE");
        check(!PortUnreachableException.class.isInstance(new SocketTimeoutException("t")), "STE is not PUE");
        check(!new PortUnreachableException("h").equals(new PortUnreachableException("h")), "equals identity");
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
        PortUnreachableException f2 = new PortUnreachableException("f2");
        f2.initCause(null);
        check(f2.getCause() == null, "initCause(null) legal when fresh");
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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        PortUnreachableException back = (PortUnreachableException) new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getClass() == PortUnreachableException.class, "same class");
        check("ser".equals(back.getMessage()), "message survives");
        check(back.getCause() != null && "c".equals(back.getCause().getMessage()), "cause survives");
        check(back.getSuppressed().length == 1, "suppressed survives");
        check(back instanceof SocketException, "deserialized is SocketException");
        PortUnreachableException back0 = (PortUnreachableException) roundTrip(new PortUnreachableException());
        check(back0.getMessage() == null, "null message survives");
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
        Thread t = new Thread(new Runnable() {
            public void run() {
                try { throw new PortUnreachableException("from-thread"); }
                catch (PortUnreachableException pe) { threadResult = pe; }
            }
        });
        t.setDaemon(true);
        t.start();
        t.join(2000);
        check(threadResult != null, "thread captured");
        check("from-thread".equals(threadResult.getMessage()), "message preserved");
        check(threadResult instanceof SocketException, "hierarchy preserved in thread");
    }

    static void testUdpTrigger() throws Exception {
        // Reserve a loopback UDP port, release it, then send to it from a
        // connected DatagramSocket: the ICMP Port Unreachable error comes
        // back on the next receive (Linux delivers it for connected sockets).
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
                check("ICMP Port Unreachable".equals(pue.getMessage()),
                        "JDK-8 trigger message, got: " + pue.getMessage());
            } catch (SocketTimeoutException ste) {
                // Hosts that filter loopback ICMP would time out instead; on
                // this platform ICMP is delivered, so flag the divergence.
                check(false, "expected ICMP delivery, got timeout: " + ste);
            }
        } finally {
            u.close();
        }
    }

    public static void main(String[] args) throws Exception {
        java.net.PortUnreachableException e = new java.net.PortUnreachableException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.net.SocketException, "is SocketException");
        java.net.PortUnreachableException e0 = new java.net.PortUnreachableException();
        check(e0.getMessage() == null, "no-arg message null");
        testCtors();
        testWhiteBox();
        testHierarchy();
        testCauseChaining();
        testSuppressed();
        testSerialization();
        testThreadPropagation();
        testUdpTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
