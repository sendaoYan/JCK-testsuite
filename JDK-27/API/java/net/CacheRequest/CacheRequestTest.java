/*
 * @test
 * @summary java.net.CacheRequest conformance: white-box structure + black-box behavior.
 * @run main CacheRequestTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class CacheRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.CacheRequest");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object directly");
        check(Modifier.isPublic(c.getConstructor().getModifiers()), "public no-arg ctor");
        check(c.getMethod("getBody") != null, "getBody");
        check(c.getMethod("abort") != null, "abort");
        // both members are abstract; the class declares nothing else
        check(Modifier.isAbstract(c.getMethod("getBody").getModifiers()), "getBody abstract");
        check(Modifier.isAbstract(c.getMethod("abort").getModifiers()), "abort abstract");
        check(c.getMethod("getBody").getReturnType() == java.io.OutputStream.class, "getBody -> OutputStream");
        check(c.getMethod("abort").getReturnType() == void.class, "abort -> void");
        check(c.getMethod("getBody").getExceptionTypes().length == 1
            && c.getMethod("getBody").getExceptionTypes()[0] == java.io.IOException.class,
            "getBody declares IOException");
        check(c.getMethod("abort").getExceptionTypes().length == 0, "abort declares no checked exceptions");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (Method m : c.getMethods())
            if (m.getDeclaringClass() != Object.class) names.add(m.getName());
        check(names.size() == 2, "exactly 2 declared public methods");
        try {
            c.getDeclaredConstructor().newInstance();
            check(false, "newInstance should fail");
        } catch (InstantiationException e) {
            check(true, "InstantiationException");
        } catch (InvocationTargetException e) {
            check(true, "wrapped InstantiationException");
        }
    }

    static void testBlackBox() throws Exception {
        final ByteArrayOutputStream body = new ByteArrayOutputStream();
        final boolean[] aborted = { false };
        final int[] abortCount = { 0 };
        CacheRequest req = new CacheRequest() {
            public OutputStream getBody() { return body; }
            public void abort() { aborted[0] = true; abortCount[0]++; }
        };
        OutputStream os = req.getBody();
        os.write("cached".getBytes("UTF-8"));
        check(new String(body.toByteArray(), "UTF-8").equals("cached"), "body written");
        check(!aborted[0], "not aborted yet");
        req.abort();
        check(aborted[0], "aborted");
        // abort has no state machine in the base class: subclass decides idempotency
        req.abort();
        check(abortCount[0] == 2, "abort called twice reaches subclass twice");
        // getBody may return a fresh stream each call (subclass-defined)
        OutputStream os2 = req.getBody();
        check(os2 == body, "getBody stable reference here");
        os2.write('x');
        check(body.size() == 7, "second stream still backed by same buffer");
    }

    // ===== deepened contract tests (batch D) =====

    static void testStreamSemantics() throws Exception {
        // a subclass exposing an independent stream per getBody call
        final byte[] sink = new byte[1];
        final int[] calls = { 0 };
        CacheRequest fresh = new CacheRequest() {
            public OutputStream getBody() {
                calls[0]++;
                return new ByteArrayOutputStream();
            }
            public void abort() { }
        };
        OutputStream a = fresh.getBody();
        OutputStream b = fresh.getBody();
        check(a != b, "getBody may return independent streams");
        check(calls[0] == 2, "getBody invoked per call");
        a.write(1);
        b.write(2);
        check(sink[0] == 0, "streams write to their own destinations");
        // abort does not interact with getBody in the base contract
        fresh.abort();
        OutputStream c = fresh.getBody();
        check(c != null, "getBody usable after abort (subclass allows)");
        // throwing subclass
        CacheRequest failing = new CacheRequest() {
            public OutputStream getBody() throws IOException { throw new IOException("no-stream"); }
            public void abort() { }
        };
        try { failing.getBody(); check(false, "getBody IOException should propagate"); }
        catch (IOException e) { check(e.getMessage().equals("no-stream"), "getBody IOException propagated"); }
    }

    static void testObjectContracts() throws Exception {
        final ByteArrayOutputStream body = new ByteArrayOutputStream();
        CacheRequest r1 = new CacheRequest() {
            public OutputStream getBody() { return body; }
            public void abort() { }
        };
        CacheRequest r2 = new CacheRequest() {
            public OutputStream getBody() { return body; }
            public void abort() { }
        };
        check(r1.equals(r1), "identity equals inherited from Object");
        check(!r1.equals(r2), "two instances are not equal (Object identity)");
        check(r1.hashCode() == r1.hashCode(), "hashCode stable");
        check(r1.toString() != null && !r1.toString().isEmpty(), "toString non-empty");
        // anonymous subclasses can capture state and expose it via getBody ordering
        final boolean[] abortBeforeBody = { false };
        CacheRequest stateful = new CacheRequest() {
            boolean aborted;
            public OutputStream getBody() {
                abortBeforeBody[0] = aborted;
                return new ByteArrayOutputStream();
            }
            public void abort() { aborted = true; }
        };
        stateful.getBody();
        check(!abortBeforeBody[0], "body requested before abort sees not-aborted");
        stateful.abort();
        stateful.getBody();
        check(abortBeforeBody[0], "body requested after abort sees aborted");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testStreamSemantics();
        testObjectContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
