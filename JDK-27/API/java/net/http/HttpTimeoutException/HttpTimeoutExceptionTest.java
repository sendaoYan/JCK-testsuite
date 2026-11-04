/*
 * @test
 * @summary java.net.http.HttpTimeoutException conformance: white-box structure + black-box exception chain.
 * @run main HttpTimeoutExceptionTest
 */

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.http.HttpTimeoutException;

public class HttpTimeoutExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.http.HttpTimeoutException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == IOException.class, "superclass is IOException");
        check(c.getConstructor(String.class) != null, "String constructor present");
        check(Serializable.class.isAssignableFrom(c), "Serializable (probed)");
        check(c.getDeclaredConstructors().length == 1, "only one constructor (probed)");
        Constructor<?> con = c.getDeclaredConstructor(String.class);
        check(Modifier.isPublic(con.getModifiers()), "constructor public");
        check(c.getDeclaredFields().length == 1
                        && c.getDeclaredFields()[0].getName().equals("serialVersionUID"),
                "only serialVersionUID declared (probed)");
        long svuid = java.io.ObjectStreamClass.lookup(c).getSerialVersionUID();
        check(svuid == 981344271622632951L, "serialVersionUID 981344271622632951 (probed)");
        check(c.getPackageName().equals("java.net.http"), "package java.net.http");
        check(!c.isSynthetic() && !c.isEnum() && !c.isRecord() && !c.isInterface(), "plain class");
    }

    static void testBlackBox() {
        HttpTimeoutException e = new HttpTimeoutException("request timed out");
        check("request timed out".equals(e.getMessage()), "message preserved");
        check(e instanceof IOException, "is an IOException");
        check(e.getClass().getSuperclass() == IOException.class, "superclass is IOException");
        check(e.getCause() == null, "no cause initially");
        check(e.getStackTrace().length > 0, "stack trace captured");

        Exception cause = new Exception("cause");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "getCause is the supplied cause");
        expect(IllegalStateException.class, () -> e.initCause(new Exception("second")), "double initCause throws");

        // ---- deepened: toString / fillInStackTrace / suppressed ----
        check("java.net.http.HttpTimeoutException: request timed out".equals(e.toString()),
                "toString format (probed)");
        HttpTimeoutException nullMsg = new HttpTimeoutException(null);
        check(nullMsg.getMessage() == null, "null message allowed (probed)");
        check("java.net.http.HttpTimeoutException".equals(nullMsg.toString()),
                "toString with null message (probed)");
        check(nullMsg.getLocalizedMessage() == null, "localized message falls back to null");
        e.addSuppressed(new Exception("s1"));
        check(e.getSuppressed().length == 1, "suppressed exceptions supported");
        check("s1".equals(e.getSuppressed()[0].getMessage()), "suppressed message kept");
        HttpTimeoutException noStack = new HttpTimeoutException("x");
        noStack.setStackTrace(new StackTraceElement[0]);
        check(noStack.getStackTrace().length == 0, "setStackTrace accepted");
        check(HttpTimeoutException.class
                        .isAssignableFrom(java.net.http.HttpConnectTimeoutException.class),
                "HttpConnectTimeoutException is a subclass (JDK-27 fact)");
    }

    // ---- deepened: serialization round-trip (probed) ----
    static void testSerialization() throws Exception {
        HttpTimeoutException original = new HttpTimeoutException("timed out");
        Exception root = new Exception("root");
        original.initCause(root);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }
        Object back;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            back = ois.readObject();
        }
        check(back instanceof HttpTimeoutException, "round-trips as HttpTimeoutException");
        check("timed out".equals(((HttpTimeoutException) back).getMessage()),
                "message survives serialization (probed)");
        check(back instanceof IOException, "still an IOException after deserialization");
        check(((HttpTimeoutException) back).getCause() != null, "cause survives serialization");
        check("root".equals(((HttpTimeoutException) back).getCause().getMessage()),
                "cause message preserved");
        check(back.equals(original) == false, "exceptions use identity equality");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
