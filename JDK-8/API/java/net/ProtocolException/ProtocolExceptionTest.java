/*
 * @test
 * @summary java.net.ProtocolException conformance: white-box structure + black-box
 *          behavior. Deep contract coverage: constructor matrix ((), (String), null),
 *          IOException hierarchy, exact serialVersionUID, serialization round-trip,
 *          cause state, and the HttpURLConnection.setRequestMethod integration that
 *          throws ProtocolException for invalid methods.
 * @run main ProtocolExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class ProtocolExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    // ------------------------------------------------------------------
    // original checks (kept from previous version)
    // ------------------------------------------------------------------
    static void testOriginal() {
        java.net.ProtocolException e = new java.net.ProtocolException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.io.IOException, "is IOException");
        java.net.ProtocolException e0 = new java.net.ProtocolException();
        check(e0.getMessage() == null, "no-arg message null");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ProtocolException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getSuperclass() == java.io.IOException.class, "direct superclass IOException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
        try {
            c.getConstructor(String.class, Throwable.class);
            check(false, "no (String,Throwable) ctor expected");
        } catch (NoSuchMethodException e) {
            check(true, "no (String,Throwable) ctor");
        }
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
    }

    static void testBlackBox() throws Exception {
        ProtocolException e = new ProtocolException("bad method");
        check(e.getMessage().equals("bad method"), "message");
        check(e instanceof java.io.IOException, "instanceof IOException");
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
        ProtocolException e2 = new ProtocolException();
        check(e2.getMessage() == null, "no-arg message null");
        ProtocolException eNull = new ProtocolException(null);
        check(eNull.getMessage() == null, "explicit null message");
        check(e.getCause() == null, "cause default null");
        Throwable cause = new IOException("underlying");
        e.initCause(cause);
        check(e.getCause() == cause, "initCause round-trip");
        try {
            e.initCause(new IOException("second"));
            check(false, "second initCause should throw");
        } catch (IllegalStateException ise) {
            check(true, "initCause once only");
        }
        check(e.getSuppressed().length == 0, "no suppressed initially");
        check(e.toString().contains("ProtocolException"), "toString class name");
        check(e.toString().contains("bad method"), "toString message");
    }

    static void testSerialization() throws Exception {
        // exact serialVersionUID (identical on JDK-8 and JDK-27, probed)
        long uid = ObjectStreamClass.lookup(ProtocolException.class).getSerialVersionUID();
        check(uid == -6098449442062388080L, "serialVersionUID exact, got " + uid);
        ProtocolException e = roundTrip(new ProtocolException("serial-msg"));
        check(e instanceof ProtocolException, "round-trip class");
        check("serial-msg".equals(e.getMessage()), "round-trip message");
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
        ProtocolException e2 = roundTrip(new ProtocolException());
        check(e2.getMessage() == null, "round-trip null message");
    }

    /** HttpURLConnection throws ProtocolException for invalid request methods (probed). */
    static void testSetMethodIntegration() throws Exception {
        HttpURLConnection h = new HttpURLConnection(new URL("http://example.com/")) {
            public void disconnect() { }
            public boolean usingProxy() { return false; }
            public void connect() { }
        };
        try {
            h.setRequestMethod("get");
            check(false, "invalid method should throw ProtocolException");
        } catch (ProtocolException pe) {
            check("Invalid HTTP method: get".equals(pe.getMessage()),
                    "integration message, got " + pe.getMessage());
        }
    }

    static void testThrowableState() throws Exception {
        ProtocolException e = new ProtocolException("state");
        check(e.getStackTrace() != null && e.getStackTrace().length > 0, "stack trace captured");
        e.addSuppressed(new IllegalStateException("sup1"));
        e.addSuppressed(new IllegalStateException("sup2"));
        check(e.getSuppressed().length == 2, "suppressed count");
        check("sup1".equals(e.getSuppressed()[0].getMessage()), "suppressed order 0");
        check("sup2".equals(e.getSuppressed()[1].getMessage()), "suppressed order 1");
        // fillInStackTrace returns this and refreshes the trace
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        // catch interop at the Exception level
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
        Throwable cause = new IllegalArgumentException("why");
        ProtocolException e2 = new ProtocolException("with-cause");
        e2.initCause(cause);
        ProtocolException rt = roundTrip(e2);
        check("with-cause".equals(rt.getMessage()), "round-trip message with cause set");
        check(rt.getCause() instanceof IllegalArgumentException, "cause preserved through serialization");
        check("why".equals(rt.getCause().getMessage()), "cause message preserved");
    }

    public static void main(String[] args) throws Exception {
        testOriginal();
        testWhiteBox();
        testBlackBox();
        testThrowableState();
        testSerialization();
        testSetMethodIntegration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
