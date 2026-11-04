/*
 * @test
 * @summary java.net.http.WebSocketHandshakeException conformance: white-box structure + black-box response/cause.
 * @run main WebSocketHandshakeExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.http.HttpResponse;
import java.net.http.WebSocketHandshakeException;

public class WebSocketHandshakeExceptionTest {
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
        Class<?> c = Class.forName("java.net.http.WebSocketHandshakeException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == IOException.class, "superclass is IOException");
        check(c.getConstructor(HttpResponse.class) != null, "HttpResponse constructor present");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable (probed)");
        check(c.getDeclaredConstructors().length == 1, "one constructor (probed)");
        check(c.getDeclaredFields().length == 2
                        && java.util.Arrays.stream(c.getDeclaredFields())
                                .anyMatch(f -> f.getName().equals("response")
                                        && java.lang.reflect.Modifier.isTransient(f.getModifiers())),
                "declares a transient 'response' field plus serialVersionUID (probed)");
        check(c.getMethod("getResponse").getReturnType() == HttpResponse.class,
                "getResponse() returns HttpResponse<?>");
        long svuid = java.io.ObjectStreamClass.lookup(c).getSerialVersionUID();
        check(svuid == 1L, "serialVersionUID 1 (probed)");
        check(c.getPackageName().equals("java.net.http"), "package java.net.http");
    }

    static void testBlackBox() {
        HttpResponse<?> response = (HttpResponse<?>) Proxy.newProxyInstance(
                WebSocketHandshakeExceptionTest.class.getClassLoader(),
                new Class<?>[]{HttpResponse.class},
                (p, m, a) -> m.getName().equals("statusCode") ? 403 : null);

        WebSocketHandshakeException e = new WebSocketHandshakeException(response);
        check(e.getResponse() == response, "getResponse() returns constructor argument");
        check(e instanceof IOException, "is an IOException");
        check(e.getMessage() == null, "no message");
        check(e.getCause() == null, "no cause initially");
        check(e.getStackTrace().length > 0, "stack trace captured");

        Exception cause = new Exception("cause");
        check(e.initCause(cause) == e, "initCause() returns this");
        check(e.getCause() == cause, "getCause() is the supplied cause");
        expect(IllegalStateException.class, () -> e.initCause(new Exception("second")), "double initCause throws");

        // ---- deepened: null response, toString, suppressed (probed) ----
        check(new WebSocketHandshakeException(null) != null, "null response allowed (probed)");
        check(new WebSocketHandshakeException(null).getResponse() == null, "null response round-trips");
        String s = e.toString();
        check("java.net.http.WebSocketHandshakeException".equals(s),
                "toString is the plain class name, no message (probed)");
        e.addSuppressed(new Exception("sup"));
        check(e.getSuppressed().length == 1, "suppressed exceptions supported");
        check("sup".equals(e.getSuppressed()[0].getMessage()), "suppressed message kept");
        WebSocketHandshakeException other = new WebSocketHandshakeException(response);
        check(!e.equals(other), "identity equality");
        check(e.getResponse().statusCode() == 403, "response accessible through the exception");
        // initCause is overridden covariantly to return WebSocketHandshakeException (probed).
        try {
            WebSocketHandshakeException w = new WebSocketHandshakeException(null);
            WebSocketHandshakeException ret = w.initCause(new Exception("c2"));
            check(ret == w, "covariant initCause returns this (probed)");
        } catch (Throwable t) {
            check(false, "covariant initCause: " + t);
        }
    }

    // ---- deepened: serialization drops the transient response (probed) ----
    static void testSerialization() throws Exception {
        HttpResponse<?> response = (HttpResponse<?>) Proxy.newProxyInstance(
                WebSocketHandshakeExceptionTest.class.getClassLoader(),
                new Class<?>[]{HttpResponse.class},
                (p, m, a) -> m.getName().equals("statusCode") ? 401 : null);
        WebSocketHandshakeException original = new WebSocketHandshakeException(response);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }
        Object back;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            back = ois.readObject();
        }
        check(back instanceof WebSocketHandshakeException, "round-trips the class");
        check(((WebSocketHandshakeException) back).getResponse() == null,
                "transient response is null after serialization (probed)");
        check(((WebSocketHandshakeException) back).getMessage() == null,
                "message remains null (probed)");
        check(back instanceof IOException, "still an IOException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
