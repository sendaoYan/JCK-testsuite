/*
 * @test
 * @summary java.net.SocketTimeoutException conformance: message and hierarchy.
 *          Deepened: STE extends InterruptedIOException and is NOT a
 *          SocketException (independent branches under IOException),
 *          bytesTransferred contract, exact constructor surface, toString
 *          forms, serialVersionUID, cause chaining, suppressed exceptions,
 *          serialization (message + bytesTransferred survive), cross-thread
 *          propagation, and real loopback triggers: accept timeout
 *          ("Accept timed out") and read timeout ("Read timed out").
 * @run main SocketTimeoutExceptionTest
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class SocketTimeoutExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testCtors() {
        SocketTimeoutException e = new SocketTimeoutException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e.getMessage().equals(e.getLocalizedMessage()), "getLocalizedMessage mirrors");
        SocketTimeoutException e0 = new SocketTimeoutException();
        check(e0.getMessage() == null, "no-arg message null");
        SocketTimeoutException en = new SocketTimeoutException((String) null);
        check(en.getMessage() == null, "null message stays null");
        check("java.net.SocketTimeoutException: boom".equals(e.toString()), "toString with message");
        check("java.net.SocketTimeoutException".equals(e0.toString()), "toString without message");
        SocketTimeoutException em = new SocketTimeoutException("");
        check("".equals(em.getMessage()) && em.getMessage() != null, "empty message retained");
        check("java.net.SocketTimeoutException: 回".equals(
                new SocketTimeoutException("回").toString()), "non-ascii toString");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketTimeoutException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == InterruptedIOException.class, "superclass InterruptedIOException");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -8846654841826352300L,
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
    }

    static void testHierarchy() {
        SocketTimeoutException e = new SocketTimeoutException("h");
        check(e instanceof InterruptedIOException, "is InterruptedIOException");
        check(e instanceof IOException, "is IOException");
        check(e instanceof Exception, "is Exception");
        // The crucial split: timeout is NOT a socket error under this taxonomy.
        // (instanceof would not compile: STE can provably never be an SE.)
        check(!SocketException.class.isInstance(e), "NOT a SocketException");
        // Catchable through the InterruptedIOException branch.
        try { throw e; } catch (InterruptedIOException x) { passed++; }
        try { throw e; } catch (IOException x) { passed++; }
        // A SocketException is likewise not a timeout.
        SocketException se = new SocketException("x");
        check(!SocketTimeoutException.class.isInstance(se), "SE is not STE");
        // Class-level relationships.
        Class<?> c = SocketTimeoutException.class;
        check(c.getSuperclass().getSuperclass() == IOException.class, "grandparent IOException");
    }

    static void testBytesTransferred() {
        SocketTimeoutException e = new SocketTimeoutException("t");
        check(e.bytesTransferred == 0, "bytesTransferred 0 default");
        e.bytesTransferred = 42;
        check(e.bytesTransferred == 42, "bytesTransferred settable");
        e.bytesTransferred = -1;
        check(e.bytesTransferred == -1, "bytesTransferred negative allowed");
        // Field is inherited from InterruptedIOException, public and non-final.
        try {
            InterruptedIOException.class.getField("bytesTransferred");
            passed++;
        } catch (NoSuchFieldException nsfe) {
            check(false, "bytesTransferred must be a public field");
        }
    }

    static void testCauseChaining() {
        RuntimeException cause = new RuntimeException("c");
        SocketTimeoutException e = new SocketTimeoutException("f");
        check(e.getCause() == null, "fresh cause null");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "cause round trip");
        try { e.initCause(new RuntimeException("y")); check(false, "twice ISE"); }
        catch (IllegalStateException ise) { passed++; }
        try {
            SocketTimeoutException self = new SocketTimeoutException("s");
            self.initCause(self);
            check(false, "self IAE");
        } catch (IllegalArgumentException iae) {
            passed++;
        }
        SocketTimeoutException f2 = new SocketTimeoutException("f2");
        f2.initCause(null);
        check(f2.getCause() == null, "initCause(null) legal when fresh");
    }

    static void testSuppressed() {
        SocketTimeoutException e = new SocketTimeoutException("sup");
        check(e.getSuppressed().length == 0, "no suppressed initially");
        SocketException s1 = new SocketException("s1");
        e.addSuppressed(s1);
        check(e.getSuppressed().length == 1, "one suppressed");
        check(e.getSuppressed()[0] == s1, "identity preserved");
        Throwable[] arr = e.getSuppressed();
        arr[0] = null;
        check(e.getSuppressed()[0] != null, "defensive copy");
        try { e.addSuppressed(e); check(false, "self-suppression IAE"); }
        catch (IllegalArgumentException iae) { passed++; }
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack non-empty");
    }

    static void testSerialization() throws Exception {
        SocketTimeoutException e = new SocketTimeoutException("ser");
        e.bytesTransferred = 42;
        e.initCause(new RuntimeException("c"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        SocketTimeoutException back = (SocketTimeoutException) new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getClass() == SocketTimeoutException.class, "same class");
        check("ser".equals(back.getMessage()), "message survives");
        check(back.bytesTransferred == 42, "bytesTransferred survives");
        check(back.getCause() != null && "c".equals(back.getCause().getMessage()), "cause survives");
        check(back instanceof InterruptedIOException, "deserialized is InterruptedIOException");
        // Zero-length transfer survives too.
        SocketTimeoutException b2 = (SocketTimeoutException) roundTrip(new SocketTimeoutException("z"));
        check(b2.bytesTransferred == 0, "default bytesTransferred survives");
    }
    static Object roundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    static volatile SocketTimeoutException threadResult;
    static void testThreadPropagation() throws Exception {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try { throw new SocketTimeoutException("from-thread"); }
                catch (SocketTimeoutException se) { threadResult = se; }
            }
        });
        t.setDaemon(true);
        t.start();
        t.join(2000);
        check(threadResult != null, "thread captured");
        check("from-thread".equals(threadResult.getMessage()), "message preserved");
        check(!SocketException.class.isInstance(threadResult), "still not SE in thread");
    }

    static void testAcceptTimeoutTrigger() throws Exception {
        // accept() with a bounded SoTimeout throws STE with an exact message.
        ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        try {
            ss.setSoTimeout(200);
            check(ss.getSoTimeout() == 200, "SoTimeout set");
            try {
                ss.accept();
                check(false, "accept must time out");
            } catch (SocketTimeoutException ste) {
                check("Accept timed out".equals(ste.getMessage()),
                        "accept timeout message, got: " + ste.getMessage());
                check(ste.bytesTransferred == 0, "bytesTransferred 0 on accept timeout");
            }
            // The server stays usable after a timeout: a second accept also throws STE.
            try {
                ss.accept();
                check(false, "second accept must time out");
            } catch (SocketTimeoutException ste2) {
                passed++;
            }
        } finally {
            ss.close();
        }
    }

    static void testReadTimeoutTrigger() throws Exception {
        // A connected pair where the peer stays silent: read() with SoTimeout
        // throws STE "Read timed out" and the socket remains open.
        final ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        Thread acc = new Thread(new Runnable() {
            public void run() {
                try {
                    Socket c = ss.accept();
                    try { Thread.sleep(1500); } catch (InterruptedException ie) {}
                    c.close();
                } catch (IOException e) {
                } finally {
                    try { ss.close(); } catch (IOException e) {}
                }
            }
        });
        acc.setDaemon(true);
        acc.start();
        Socket s = new Socket("127.0.0.1", ss.getLocalPort());
        try {
            s.setSoTimeout(150);
            try {
                s.getInputStream().read();
                check(false, "read must time out");
            } catch (SocketTimeoutException ste) {
                check("Read timed out".equals(ste.getMessage()),
                        "read timeout message, got: " + ste.getMessage());
                check(s.isConnected(), "socket still connected after read timeout");
            }
            // After the timeout the socket can still be read again (another STE).
            try {
                s.getInputStream().read();
                check(false, "second read must time out");
            } catch (SocketTimeoutException ste2) {
                passed++;
            }
        } finally {
            s.close();
            acc.join(2000);
        }
    }

    public static void main(String[] args) throws Exception {
        java.net.SocketTimeoutException e = new java.net.SocketTimeoutException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.io.InterruptedIOException, "is InterruptedIOException");
        check(e.bytesTransferred == 0, "bytesTransferred 0");
        java.net.SocketTimeoutException e0 = new java.net.SocketTimeoutException();
        check(e0.getMessage() == null, "no-arg message null");
        testCtors();
        testWhiteBox();
        testHierarchy();
        testBytesTransferred();
        testCauseChaining();
        testSuppressed();
        testSerialization();
        testThreadPropagation();
        testAcceptTimeoutTrigger();
        testReadTimeoutTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
