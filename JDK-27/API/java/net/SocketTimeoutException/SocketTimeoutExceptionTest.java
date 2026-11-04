/*
 * @test
 * @summary java.net.SocketTimeoutException conformance: white-box structure + black-box behavior.
 *          Deepened: STE extends InterruptedIOException and is NOT a
 *          SocketException (independent branches under IOException),
 *          bytesTransferred contract, exact constructor surface (still only
 *          () and (String)), toString forms, serialVersionUID, cause chaining,
 *          suppressed exceptions, serialization (message + bytesTransferred
 *          survive), cross-thread propagation, and real loopback triggers:
 *          accept timeout ("Accept timed out") and read timeout
 *          ("Read timed out").
 * @run main SocketTimeoutExceptionTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

public class SocketTimeoutExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketTimeoutException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.InterruptedIOException.class.isAssignableFrom(c), "extends InterruptedIOException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
        check(c.getSuperclass() == java.io.InterruptedIOException.class, "superclass exact");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -8846654841826352300L,
                "serialVersionUID exact");
        check(c.getConstructors().length == 2, "exactly 2 public ctors, got " + c.getConstructors().length);
        try {
            c.getConstructor(Throwable.class);
            check(false, "ctor(Throwable) absent");
        } catch (NoSuchMethodException nsme) {
            passed++;
        }
        // STE is not on the SocketException branch.
        check(!SocketException.class.isAssignableFrom(c), "not under SocketException");
    }

    static void testBlackBox() throws Exception {
        SocketTimeoutException e = new SocketTimeoutException("read timed out");
        check(e.getMessage().equals("read timed out"), "message");
        check(e instanceof java.io.InterruptedIOException, "instanceof InterruptedIOException");
        check(e instanceof java.io.IOException, "instanceof IOException");
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
        SocketTimeoutException e2 = new SocketTimeoutException();
        check(e2.getMessage() == null, "no-arg message null");
        // Compile-time-provable non-relationship expressed via Class.
        check(!SocketException.class.isInstance(e), "NOT a SocketException");
    }

    static void testCtorDetails() {
        check("java.net.SocketTimeoutException: boom".equals(
                new SocketTimeoutException("boom").toString()), "toString with message");
        check("java.net.SocketTimeoutException".equals(
                new SocketTimeoutException().toString()), "toString no message");
        SocketTimeoutException en = new SocketTimeoutException((String) null);
        check(en.getMessage() == null, "null message stays null");
        check("java.net.SocketTimeoutException".equals(en.toString()), "toString null message");
        SocketTimeoutException em = new SocketTimeoutException("");
        check("".equals(em.getMessage()) && em.getMessage() != null, "empty message retained");
        check("java.net.SocketTimeoutException: ".equals(em.toString()), "toString empty message");
        check(new SocketTimeoutException("回").getMessage().equals("回"), "non-ascii message");
        check(new SocketTimeoutException("x").getLocalizedMessage().equals("x"),
                "getLocalizedMessage mirrors");
    }

    static void testBytesTransferred() throws Exception {
        SocketTimeoutException e = new SocketTimeoutException("t");
        check(e.bytesTransferred == 0, "bytesTransferred 0 default");
        e.bytesTransferred = 42;
        check(e.bytesTransferred == 42, "bytesTransferred settable");
        e.bytesTransferred = -1;
        check(e.bytesTransferred == -1, "negative allowed");
        check(InterruptedIOException.class.getField("bytesTransferred") != null,
                "public field on InterruptedIOException");
        int mods = InterruptedIOException.class.getField("bytesTransferred").getModifiers();
        check(Modifier.isPublic(mods) && !Modifier.isFinal(mods), "public non-final");
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
        Object back = roundTrip(e);
        SocketTimeoutException b = (SocketTimeoutException) back;
        check(b.getClass() == SocketTimeoutException.class, "same class");
        check("ser".equals(b.getMessage()), "message survives");
        check(b.bytesTransferred == 42, "bytesTransferred survives");
        check(b.getCause() != null && "c".equals(b.getCause().getMessage()), "cause survives");
        check(b instanceof java.io.InterruptedIOException, "deserialized is InterruptedIOException");
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
        Thread t = new Thread(() -> {
            try { throw new SocketTimeoutException("from-thread"); }
            catch (SocketTimeoutException se) { threadResult = se; }
        });
        t.setDaemon(true);
        t.start();
        t.join(2000);
        check(threadResult != null, "thread captured");
        check("from-thread".equals(threadResult.getMessage()), "message preserved");
        check(!SocketException.class.isInstance(threadResult), "still not SE in thread");
    }

    static void testAcceptTimeoutTrigger() throws Exception {
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
        final ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        Thread acc = new Thread(() -> {
            try {
                Socket c = ss.accept();
                try { Thread.sleep(1500); } catch (InterruptedException ie) {}
                c.close();
            } catch (IOException e) {
            } finally {
                try { ss.close(); } catch (IOException e) {}
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
        testWhiteBox();
        testBlackBox();
        testCtorDetails();
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
