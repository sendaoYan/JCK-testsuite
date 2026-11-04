/*
 * @test
 * @summary java.net.ConnectException conformance: message and hierarchy.
 *          Deepened: exact constructor surface, toString forms,
 *          serialVersionUID, full hierarchy (CE < SE < IOException), catch
 *          matrix, cause chaining, suppressed exceptions, serialization
 *          round-trips, cross-thread propagation, and a real loopback
 *          trigger: connecting to a freshly closed loopback port throws
 *          ConnectException "Connection refused (Connection refused)" on JDK-8.
 * @run main ConnectExceptionTest
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ConnectExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testCtors() {
        ConnectException e = new ConnectException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e.getMessage().equals(e.getLocalizedMessage()), "getLocalizedMessage mirrors");
        ConnectException e0 = new ConnectException();
        check(e0.getMessage() == null, "no-arg message null");
        ConnectException en = new ConnectException((String) null);
        check(en.getMessage() == null, "null message stays null");
        check("java.net.ConnectException: boom".equals(e.toString()), "toString with message");
        check("java.net.ConnectException".equals(e0.toString()), "toString without message");
        ConnectException em = new ConnectException("");
        check(em.getMessage() != null && em.getMessage().isEmpty(), "empty message retained");
        check("java.net.ConnectException: ".equals(em.toString()), "toString empty message");
        check(new ConnectException("拒绝").getMessage().equals("拒绝"), "non-ascii message");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ConnectException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == SocketException.class, "superclass SocketException");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 3831404271622369215L,
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
        ConnectException e = new ConnectException("h");
        check(e instanceof SocketException, "is SocketException");
        check(e instanceof IOException, "is IOException");
        check(e instanceof Exception, "is Exception");
        try { throw e; } catch (SocketException x) { passed++; }
        try { throw e; } catch (IOException x) { passed++; }
        SocketException se = new SocketException("x");
        check(!ConnectException.class.isInstance(se), "plain SE is not CE");
        check(!ConnectException.class.isInstance(new java.net.SocketTimeoutException("t")), "STE is not CE");
        check(!ConnectException.class.isInstance(new java.net.BindException("b")), "BE is not CE");
        check(!new ConnectException("h").equals(new ConnectException("h")), "equals identity");
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
        ConnectException f2 = new ConnectException("f2");
        f2.initCause(null);
        check(f2.getCause() == null, "initCause(null) legal when fresh");
        // Chaining a ConnectException as cause models retry cascades.
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
        check(e.getSuppressed()[0] instanceof SocketException, "suppressed polymorphic");
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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        ConnectException back = (ConnectException) new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getClass() == ConnectException.class, "same class");
        check("ser".equals(back.getMessage()), "message survives");
        check(back.getCause() != null && "c".equals(back.getCause().getMessage()), "cause survives");
        check(back.getSuppressed().length == 1, "suppressed survives");
        check(back instanceof SocketException, "deserialized is SocketException");
        ConnectException back0 = (ConnectException) roundTrip(new ConnectException());
        check(back0.getMessage() == null, "null message survives");
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
        Thread t = new Thread(new Runnable() {
            public void run() {
                try { throw new ConnectException("from-thread"); }
                catch (ConnectException ce) { threadResult = ce; }
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
        // Reserve then release a loopback port; connecting to it is refused.
        ServerSocket holder = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        int deadPort = holder.getLocalPort();
        holder.close();
        Socket s = new Socket();
        try {
            try {
                s.connect(new InetSocketAddress("127.0.0.1", deadPort), 1000);
                check(false, "refused connect must throw");
            } catch (ConnectException ce) {
                check(ce.getMessage().startsWith("Connection refused"),
                        "trigger message prefix, got: " + ce.getMessage());
                check("Connection refused (Connection refused)".equals(ce.getMessage()),
                        "JDK-8 exact trigger message, got: " + ce.getMessage());
                check(ce instanceof SocketException, "trigger is SocketException");
            }
            // JDK-8 keeps the socket open-ish after a refused connect
            // (JDK-27 reports isClosed=true here).
            check(!s.isClosed(), "JDK-8 socket not closed after refused connect");
            check(!s.isConnected(), "not connected after refusal");
        } finally {
            s.close();
        }
        // A fresh attempt with a second socket is refused again.
        Socket s2 = new Socket();
        try {
            try {
                s2.connect(new InetSocketAddress("127.0.0.1", deadPort), 1000);
                check(false, "second refused connect must throw");
            } catch (ConnectException ce2) {
                passed++;
            }
        } finally {
            s2.close();
        }
    }

    public static void main(String[] args) throws Exception {
        java.net.ConnectException e = new java.net.ConnectException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.net.SocketException, "is SocketException");
        java.net.ConnectException e0 = new java.net.ConnectException();
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
