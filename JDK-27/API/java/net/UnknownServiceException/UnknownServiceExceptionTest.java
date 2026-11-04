/*
 * @test
 * @summary java.net.UnknownServiceException conformance: white-box structure + black-box
 *          behavior. Deep contract coverage: constructor matrix ((), (String), null
 *          message), IOException hierarchy, exact serialVersionUID, serialization
 *          round-trip, cause/suppressed state, and catch-as-IOException interop.
 * @run main UnknownServiceExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class UnknownServiceExceptionTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.UnknownServiceException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getSuperclass() == java.io.IOException.class, "direct superclass IOException");
        check(c.getConstructor() != null, "ctor()");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        // no (String, Throwable) constructor in this legacy exception
        try {
            c.getConstructor(String.class, Throwable.class);
            check(false, "no (String,Throwable) ctor expected");
        } catch (NoSuchMethodException e) {
            check(true, "no (String,Throwable) ctor");
        }
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
    }

    static void testBlackBox() throws Exception {
        UnknownServiceException e = new UnknownServiceException("no such service");
        check(e.getMessage().equals("no such service"), "message");
        check(e instanceof java.io.IOException, "instanceof IOException");
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
        UnknownServiceException e2 = new UnknownServiceException();
        check(e2.getMessage() == null, "no-arg message null");
        UnknownServiceException eNull = new UnknownServiceException(null);
        check(eNull.getMessage() == null, "explicit null message");
        // cause starts null and can be initialized once
        check(e.getCause() == null, "cause default null");
        Throwable cause = new RuntimeException("root");
        e.initCause(cause);
        check(e.getCause() == cause, "initCause round-trip");
        try {
            e.initCause(new RuntimeException("second"));
            check(false, "second initCause should throw");
        } catch (IllegalStateException ise) {
            check(true, "initCause once only");
        }
        check(e.getSuppressed().length == 0, "no suppressed initially");
        // toString contains class and message
        check(e.toString().contains("UnknownServiceException"), "toString class name");
        check(e.toString().contains("no such service"), "toString message");
    }

    static void testSerialization() throws Exception {
        // exact serialVersionUID (identical on JDK-8 and JDK-27, probed)
        long uid = ObjectStreamClass.lookup(UnknownServiceException.class).getSerialVersionUID();
        check(uid == -4169033248853639508L, "serialVersionUID exact, got " + uid);
        UnknownServiceException e = roundTrip(new UnknownServiceException("serial-msg"));
        check(e instanceof UnknownServiceException, "round-trip class");
        check("serial-msg".equals(e.getMessage()), "round-trip message");
        check(e instanceof java.io.IOException, "round-trip IOException");
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
        UnknownServiceException e2 = roundTrip(new UnknownServiceException());
        check(e2.getMessage() == null, "round-trip null message");
    }

    static void testThrowableState() throws Exception {
        UnknownServiceException e = new UnknownServiceException("state");
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
        UnknownServiceException e2 = new UnknownServiceException("with-cause");
        e2.initCause(cause);
        UnknownServiceException rt = roundTrip(e2);
        check("with-cause".equals(rt.getMessage()), "round-trip message with cause set");
        check(rt.getCause() instanceof IllegalArgumentException, "cause preserved through serialization");
        check("why".equals(rt.getCause().getMessage()), "cause message preserved");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableState();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
