/*
 * @test
 * @summary java.net.NoRouteToHostException conformance: message and hierarchy.
 *          Deepened: exact constructor surface, toString forms,
 *          serialVersionUID, full hierarchy (NRTH < SE < IOException), catch
 *          matrix, cause chaining (including modeling connect retries),
 *          suppressed exceptions, serialization round-trips, cross-thread
 *          propagation, and simulated usage in a connect-retry loop. No real
 *          network trigger is attempted (reaching an unroutable host would
 *          leave the loopback sandbox).
 * @run main NoRouteToHostExceptionTest
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.net.NoRouteToHostException;
import java.net.SocketException;

public class NoRouteToHostExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testCtors() {
        NoRouteToHostException e = new NoRouteToHostException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e.getMessage().equals(e.getLocalizedMessage()), "getLocalizedMessage mirrors");
        NoRouteToHostException e0 = new NoRouteToHostException();
        check(e0.getMessage() == null, "no-arg message null");
        NoRouteToHostException en = new NoRouteToHostException((String) null);
        check(en.getMessage() == null, "null message stays null");
        check("java.net.NoRouteToHostException: boom".equals(e.toString()), "toString with message");
        check("java.net.NoRouteToHostException".equals(e0.toString()), "toString without message");
        NoRouteToHostException em = new NoRouteToHostException("");
        check(em.getMessage() != null && em.getMessage().isEmpty(), "empty message retained");
        check("java.net.NoRouteToHostException: ".equals(em.toString()), "toString empty message");
        check(new NoRouteToHostException("不可达").getMessage().equals("不可达"), "non-ascii message");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.NoRouteToHostException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == SocketException.class, "superclass SocketException");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -1897550894873493790L,
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
        NoRouteToHostException e = new NoRouteToHostException("h");
        check(e instanceof SocketException, "is SocketException");
        check(e instanceof IOException, "is IOException");
        check(e instanceof Exception, "is Exception");
        try { throw e; } catch (SocketException x) { passed++; }
        try { throw e; } catch (IOException x) { passed++; }
        SocketException se = new SocketException("x");
        check(!NoRouteToHostException.class.isInstance(se), "plain SE is not NRTH");
        check(!NoRouteToHostException.class.isInstance(new java.net.BindException("b")), "BE is not NRTH");
        check(!NoRouteToHostException.class.isInstance(new java.net.ConnectException("c")), "CE is not NRTH");
        check(!NoRouteToHostException.class.isInstance(
                new java.net.SocketTimeoutException("t")), "STE is not NRTH");
        check(!new NoRouteToHostException("h").equals(new NoRouteToHostException("h")), "equals identity");
    }

    static void testCauseChaining() {
        RuntimeException cause = new RuntimeException("c");
        NoRouteToHostException e = new NoRouteToHostException("f");
        check(e.getCause() == null, "fresh cause null");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "cause round trip");
        try { e.initCause(new RuntimeException("y")); check(false, "twice ISE"); }
        catch (IllegalStateException ise) { passed++; }
        try {
            NoRouteToHostException self = new NoRouteToHostException("s");
            self.initCause(self);
            check(false, "self IAE");
        } catch (IllegalArgumentException iae) {
            passed++;
        }
        NoRouteToHostException f2 = new NoRouteToHostException("f2");
        f2.initCause(null);
        check(f2.getCause() == null, "initCause(null) legal when fresh");
        // Routing failures cascade: a nested connect error as cause.
        NoRouteToHostException route = new NoRouteToHostException("no route to host");
        route.initCause(new java.net.ConnectException("connection refused"));
        check(route.getCause() instanceof java.net.ConnectException, "CE may cause NRTH");
    }

    static void testSuppressed() {
        NoRouteToHostException e = new NoRouteToHostException("sup");
        check(e.getSuppressed().length == 0, "no suppressed initially");
        e.addSuppressed(new SocketException("s1"));
        e.addSuppressed(new NoRouteToHostException("s2"));
        check(e.getSuppressed().length == 2, "two suppressed");
        check(e.getSuppressed()[1] instanceof NoRouteToHostException, "suppressed polymorphic");
        Throwable[] arr = e.getSuppressed();
        arr[0] = null;
        check(e.getSuppressed()[0] != null, "defensive copy");
        try { e.addSuppressed(e); check(false, "self-suppression IAE"); }
        catch (IllegalArgumentException iae) { passed++; }
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack non-empty");
    }

    static void testSerialization() throws Exception {
        NoRouteToHostException e = new NoRouteToHostException("ser");
        e.initCause(new RuntimeException("c"));
        e.addSuppressed(new NoRouteToHostException("sup"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        NoRouteToHostException back = (NoRouteToHostException) new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getClass() == NoRouteToHostException.class, "same class");
        check("ser".equals(back.getMessage()), "message survives");
        check(back.getCause() != null && "c".equals(back.getCause().getMessage()), "cause survives");
        check(back.getSuppressed().length == 1, "suppressed survives");
        check(back instanceof SocketException, "deserialized is SocketException");
        NoRouteToHostException back0 = (NoRouteToHostException) roundTrip(new NoRouteToHostException());
        check(back0.getMessage() == null, "null message survives");
    }
    static Object roundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    static volatile NoRouteToHostException threadResult;
    static void testThreadPropagation() throws Exception {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try { throw new NoRouteToHostException("from-thread"); }
                catch (NoRouteToHostException ne) { threadResult = ne; }
            }
        });
        t.setDaemon(true);
        t.start();
        t.join(2000);
        check(threadResult != null, "thread captured");
        check("from-thread".equals(threadResult.getMessage()), "message preserved");
        check(threadResult instanceof SocketException, "hierarchy preserved in thread");
    }

    static void testRetrySimulation() {
        // Simulated connect loop: N attempts each failing with NRTH, the last
        // one carrying the others as suppressed (try-with-resources shape).
        NoRouteToHostException last = null;
        for (int i = 0; i < 3; i++) {
            NoRouteToHostException cur = new NoRouteToHostException("attempt " + i);
            if (last != null) {
                cur.addSuppressed(last);
            }
            last = cur;
        }
        check(last != null, "retry chain built");
        check("attempt 2".equals(last.getMessage()), "last attempt message");
        check(last.getSuppressed().length == 1, "one suppressed retry");
        check("attempt 1".equals(last.getSuppressed()[0].getMessage()), "suppressed message");
        NoRouteToHostException deeper = (NoRouteToHostException) last.getSuppressed()[0];
        check(deeper.getSuppressed().length == 1, "nested suppressed");
        check("attempt 0".equals(deeper.getSuppressed()[0].getMessage()), "nested message");
        // Catchable at any ancestor level from the loop.
        boolean caught = false;
        try {
            throw last;
        } catch (IOException ioe) {
            caught = true;
            check(ioe.getMessage().equals("attempt 2"), "loop-level message");
        }
        check(caught, "retry chain caught as IOException");
    }

    public static void main(String[] args) throws Exception {
        java.net.NoRouteToHostException e = new java.net.NoRouteToHostException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.net.SocketException, "is SocketException");
        java.net.NoRouteToHostException e0 = new java.net.NoRouteToHostException();
        check(e0.getMessage() == null, "no-arg message null");
        testCtors();
        testWhiteBox();
        testHierarchy();
        testCauseChaining();
        testSuppressed();
        testSerialization();
        testThreadPropagation();
        testRetrySimulation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
