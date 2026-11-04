/*
 * @test
 * @summary java.net.http.HttpConnectTimeoutException conformance: white-box structure + black-box exception chain.
 * @run main HttpConnectTimeoutExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;

public class HttpConnectTimeoutExceptionTest {
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
        Class<?> c = Class.forName("java.net.http.HttpConnectTimeoutException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == HttpTimeoutException.class, "superclass is HttpTimeoutException");
        check(c.getConstructor(String.class) != null, "String constructor present");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable (probed)");
        check(c.getDeclaredConstructors().length == 1, "one constructor (probed)");
        check(c.getDeclaredFields().length == 1
                        && c.getDeclaredFields()[0].getName().equals("serialVersionUID"),
                "only serialVersionUID declared (probed)");
        long svuid = java.io.ObjectStreamClass.lookup(c).getSerialVersionUID();
        check(svuid == 332L, "serialVersionUID 332 (probed)");
        check(java.io.ObjectStreamClass.lookup(c.getSuperclass()).getSerialVersionUID()
                        == 981344271622632951L, "superclass svuid is HttpTimeoutException's");
        check(c.getPackageName().equals("java.net.http"), "package java.net.http");
    }

    static void testBlackBox() {
        HttpConnectTimeoutException e = new HttpConnectTimeoutException("connect timed out");
        check("connect timed out".equals(e.getMessage()), "message preserved");
        check(e instanceof HttpTimeoutException, "is a HttpTimeoutException");
        check(e instanceof IOException, "is an IOException");
        check(e.getClass().getSuperclass() == HttpTimeoutException.class, "superclass is HttpTimeoutException");
        check(e.getCause() == null, "no cause initially");
        check(e.getStackTrace().length > 0, "stack trace captured");
        check(e.toString().contains("connect timed out"), "toString contains message");

        Exception cause = new IllegalStateException("root");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "getCause is the supplied cause");
        expect(IllegalStateException.class, () -> e.initCause(new Exception("second")), "double initCause throws");

        // ---- deepened: toString / suppressed / polymorphism (probed) ----
        check("java.net.http.HttpConnectTimeoutException: connect timed out".equals(e.toString()),
                "exact toString format (probed)");
        HttpConnectTimeoutException nullMsg = new HttpConnectTimeoutException(null);
        check(nullMsg.getMessage() == null, "null message allowed (probed)");
        check("java.net.http.HttpConnectTimeoutException".equals(nullMsg.toString()),
                "toString with null message (probed)");
        e.addSuppressed(new Exception("s"));
        check(e.getSuppressed().length == 1, "suppressed exceptions supported");
        // Polymorphic handling: a catch of the superclass receives it.
        boolean caught = false;
        try {
            throw e;
        } catch (HttpTimeoutException hte) {
            caught = hte instanceof HttpConnectTimeoutException;
        }
        check(caught, "catchable as HttpTimeoutException, instanceof distinguishes the subclass");
        // The subclass can be thrown where the parent is declared.
        HttpTimeoutException up = new HttpConnectTimeoutException("as-super");
        check(up instanceof HttpConnectTimeoutException, "assignment to the superclass type works");
        check("as-super".equals(up.getMessage()), "message via superclass reference");
        check(HttpConnectTimeoutException.class.isAssignableFrom(HttpTimeoutException.class) == false,
                "the superclass is not a subclass of it");
    }

    // ---- deepened: serialization round-trip (probed) ----
    static void testSerialization() throws Exception {
        HttpConnectTimeoutException original = new HttpConnectTimeoutException("connect timed out");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }
        Object back;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            back = ois.readObject();
        }
        check(back instanceof HttpConnectTimeoutException, "round-trips as HttpConnectTimeoutException");
        check("connect timed out".equals(((HttpConnectTimeoutException) back).getMessage()),
                "message survives serialization (probed)");
        check(back instanceof HttpTimeoutException && back instanceof IOException,
                "type hierarchy preserved after deserialization");
        check(((HttpConnectTimeoutException) back).getCause() == null,
                "no cause before or after serialization");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
