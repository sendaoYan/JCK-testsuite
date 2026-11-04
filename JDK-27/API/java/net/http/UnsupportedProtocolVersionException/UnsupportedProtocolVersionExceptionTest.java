/*
 * @test
 * @summary java.net.http.UnsupportedProtocolVersionException conformance: white-box structure + black-box chain.
 * @run main UnsupportedProtocolVersionExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.http.UnsupportedProtocolVersionException;

public class UnsupportedProtocolVersionExceptionTest {
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
        Class<?> c = Class.forName("java.net.http.UnsupportedProtocolVersionException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == IOException.class, "superclass is IOException");
        check(c.getConstructor(String.class) != null, "String constructor present");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable (probed)");
        check(c.getDeclaredConstructors().length == 1, "one constructor (probed)");
        check(c.getDeclaredFields().length == 1
                        && c.getDeclaredFields()[0].getName().equals("serialVersionUID"),
                "only serialVersionUID declared (probed)");
        long svuid = java.io.ObjectStreamClass.lookup(c).getSerialVersionUID();
        check(svuid == 981344214212332893L, "serialVersionUID 981344214212332893 (probed)");
        check(c.getPackageName().equals("java.net.http"), "package java.net.http");
        check(!c.isAssignableFrom(java.net.http.HttpTimeoutException.class)
                        && !java.net.http.HttpTimeoutException.class.isAssignableFrom(c),
                "sibling of HttpTimeoutException, not related");
    }

    static void testBlackBox() {
        UnsupportedProtocolVersionException e =
                new UnsupportedProtocolVersionException("unsupported protocol version");
        check("unsupported protocol version".equals(e.getMessage()), "message preserved");
        check(e instanceof IOException, "is an IOException");
        check(e.getCause() == null, "no cause initially");
        check(e.getStackTrace().length > 0, "stack trace captured");

        Exception cause = new Exception("cause");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "getCause is the supplied cause");
        expect(IllegalStateException.class, () -> e.initCause(new Exception("second")), "double initCause throws");

        // ---- deepened: surface details (probed) ----
        check("java.net.http.UnsupportedProtocolVersionException: unsupported protocol version"
                        .equals(e.toString()), "exact toString format (probed)");
        UnsupportedProtocolVersionException nullMsg =
                new UnsupportedProtocolVersionException(null);
        check(nullMsg.getMessage() == null, "null message allowed (probed)");
        check("java.net.http.UnsupportedProtocolVersionException".equals(nullMsg.toString()),
                "toString with null message (probed)");
        check(nullMsg.getLocalizedMessage() == null, "localized message falls back to null");
        e.addSuppressed(new Exception("s0"));
        e.addSuppressed(new Exception("s1"));
        check(e.getSuppressed().length == 2, "multiple suppressed exceptions accumulate");
        check("s1".equals(e.getSuppressed()[1].getMessage()), "suppressed order preserved");
        UnsupportedProtocolVersionException empty =
                new UnsupportedProtocolVersionException("");
        check("".equals(empty.getMessage()), "empty message allowed");
        check(empty.toString().endsWith(": "), "toString with empty message");
        check(new UnsupportedProtocolVersionException("x")
                        .getStackTrace().length > 0, "stack trace always captured");
    }

    // ---- deepened: serialization round-trip (probed) ----
    static void testSerialization() throws Exception {
        UnsupportedProtocolVersionException original =
                new UnsupportedProtocolVersionException("nope");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }
        Object back;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            back = ois.readObject();
        }
        check(back instanceof UnsupportedProtocolVersionException, "round-trips the class");
        check("nope".equals(((UnsupportedProtocolVersionException) back).getMessage()),
                "message survives serialization (probed)");
        check(back instanceof IOException, "still an IOException");
        check(((UnsupportedProtocolVersionException) back).getCause() == null,
                "no cause before or after");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
