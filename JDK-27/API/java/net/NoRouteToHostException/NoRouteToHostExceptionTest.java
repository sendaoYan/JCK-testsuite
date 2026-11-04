/*
 * @test
 * @summary java.net.NoRouteToHostException conformance: white-box structure + black-box behavior.
 *          Deepened: exact constructor surface, toString forms,
 *          serialVersionUID, full hierarchy (NRTH < SE < IOException), catch
 *          matrix, cause chaining (including modeling connect retries),
 *          suppressed exceptions, serialization round-trips, cross-thread
 *          propagation, and simulated usage in a connect-retry loop. No real
 *          network trigger is attempted (reaching an unroutable host would
 *          leave the loopback sandbox).
 * @run main NoRouteToHostExceptionTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

public class NoRouteToHostExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.NoRouteToHostException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(SocketException.class.isAssignableFrom(c), "extends SocketException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
        check(c.getSuperclass() == SocketException.class, "superclass exact");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -1897550894873493790L,
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
        NoRouteToHostException e = new NoRouteToHostException("no route");
        check(e.getMessage().equals("no route"), "message");
        check(e instanceof SocketException, "instanceof SocketException");
        try { throw e; } catch (SocketException se) { passed++; }
        NoRouteToHostException e2 = new NoRouteToHostException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    static void testCtorDetails() {
        check("java.net.NoRouteToHostException: no route".equals(
                new NoRouteToHostException("no route").toString()), "toString with message");
        check("java.net.NoRouteToHostException".equals(
                new NoRouteToHostException().toString()), "toString no message");
        NoRouteToHostException en = new NoRouteToHostException((String) null);
        check(en.getMessage() == null, "null message stays null");
        NoRouteToHostException em = new NoRouteToHostException("");
        check(em.getMessage() != null && em.getMessage().isEmpty(), "empty message retained");
        check("java.net.NoRouteToHostException: ".equals(em.toString()), "toString empty message");
        check(new NoRouteToHostException("不可达").getMessage().equals("不可达"), "non-ascii message");
        check(new NoRouteToHostException("x").getLocalizedMessage().equals("x"),
                "getLocalizedMessage mirrors");
        check(!new NoRouteToHostException("h").equals(new NoRouteToHostException("h")), "equals identity");
    }

    static void testHierarchy() {
        NoRouteToHostException e = new NoRouteToHostException("h");
        check(e instanceof java.io.IOException, "is IOException");
        try { throw e; } catch (java.io.IOException x) { passed++; }
        try { throw e; } catch (Exception x) { passed++; }
        SocketException se = new SocketException("x");
        check(!NoRouteToHostException.class.isInstance(se), "plain SE is not NRTH");
        check(!NoRouteToHostException.class.isInstance(new BindException("b")), "BE is not NRTH");
        check(!NoRouteToHostException.class.isInstance(new ConnectException("c")), "CE is not NRTH");
        check(!NoRouteToHostException.class.isInstance(new SocketTimeoutException("t")), "STE is not NRTH");
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
        NoRouteToHostException route = new NoRouteToHostException("no route to host");
        route.initCause(new ConnectException("connection refused"));
        check(route.getCause() instanceof ConnectException, "CE may cause NRTH");
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
        Object back = roundTrip(e);
        NoRouteToHostException b = (NoRouteToHostException) back;
        check(b.getClass() == NoRouteToHostException.class, "same class");
        check("ser".equals(b.getMessage()), "message survives");
        check(b.getCause() != null && "c".equals(b.getCause().getMessage()), "cause survives");
        check(b.getSuppressed().length == 1, "suppressed survives");
        check(b instanceof SocketException, "deserialized is SocketException");
        NoRouteToHostException b0 = (NoRouteToHostException) roundTrip(new NoRouteToHostException());
        check(b0.getMessage() == null, "null message survives");
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
        Thread t = new Thread(() -> {
            try { throw new NoRouteToHostException("from-thread"); }
            catch (NoRouteToHostException ne) { threadResult = ne; }
        });
        t.setDaemon(true);
        t.start();
        t.join(2000);
        check(threadResult != null, "thread captured");
        check("from-thread".equals(threadResult.getMessage()), "message preserved");
        check(threadResult instanceof SocketException, "hierarchy preserved in thread");
    }

    static void testRetrySimulation() {
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
        boolean caught = false;
        try {
            throw last;
        } catch (java.io.IOException ioe) {
            caught = true;
            check(ioe.getMessage().equals("attempt 2"), "loop-level message");
        }
        check(caught, "retry chain caught as IOException");
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
        testRetrySimulation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
