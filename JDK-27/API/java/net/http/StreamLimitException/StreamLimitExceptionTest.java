/*
 * @test
 * @summary java.net.http.StreamLimitException conformance: white-box structure + black-box version/chain behavior.
 * @run main StreamLimitExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.http.HttpClient;
import java.net.http.StreamLimitException;

public class StreamLimitExceptionTest {
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
        Class<?> c = Class.forName("java.net.http.StreamLimitException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == IOException.class, "superclass is IOException");
        check(c.getConstructor(HttpClient.Version.class, String.class) != null, "Version,String constructor present");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable (probed)");
        check(c.getDeclaredConstructors().length == 1, "one constructor (probed)");
        check(c.getDeclaredFields().length == 2
                        && java.util.Arrays.stream(c.getDeclaredFields())
                                .anyMatch(f -> f.getName().equals("version")
                                        && !java.lang.reflect.Modifier.isTransient(f.getModifiers())),
                "declares a non-transient 'version' field plus serialVersionUID (probed)");
        check(c.getMethod("version").getReturnType() == HttpClient.Version.class,
                "version() returns HttpClient.Version");
        long svuid = java.io.ObjectStreamClass.lookup(c).getSerialVersionUID();
        check(svuid == 2614981180406031159L, "serialVersionUID 2614981180406031159 (probed)");
        check(c.getPackageName().equals("java.net.http"), "package java.net.http");
    }

    static void testBlackBox() {
        StreamLimitException e = new StreamLimitException(HttpClient.Version.HTTP_2, "stream limit reached");
        check("stream limit reached".equals(e.getMessage()), "message preserved");
        check(e.version() == HttpClient.Version.HTTP_2, "version() accessor");
        check(e instanceof IOException, "is an IOException");
        check(e.getCause() == null, "no cause initially");
        check(e.getStackTrace().length > 0, "stack trace captured");

        StreamLimitException h1 = new StreamLimitException(HttpClient.Version.HTTP_1_1, "limit");
        check(h1.version() == HttpClient.Version.HTTP_1_1, "version() accessor HTTP_1_1");

        Exception cause = new Exception("cause");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "getCause is the supplied cause");
        expect(IllegalStateException.class, () -> e.initCause(new Exception("second")), "double initCause throws");

        // ---- deepened: negatives and surface (probed) ----
        expect(NullPointerException.class,
                () -> new StreamLimitException(null, "m"), "null version NPE (probed)");
        StreamLimitException nullMsg = new StreamLimitException(HttpClient.Version.HTTP_3, null);
        check(nullMsg.getMessage() == null, "null message allowed (probed)");
        check(nullMsg.version() == HttpClient.Version.HTTP_3, "version with null message");
        check("java.net.http.StreamLimitException".equals(nullMsg.toString()),
                "toString with null message (probed)");
        check("java.net.http.StreamLimitException: limited".equals(
                        new StreamLimitException(HttpClient.Version.HTTP_3, "limited").toString()),
                "exact toString format (probed)");
        e.addSuppressed(new Exception("s"));
        check(e.getSuppressed().length == 1, "suppressed exceptions supported");
        // All three Version constants are accepted by the constructor.
        for (HttpClient.Version v : HttpClient.Version.values()) {
            check(new StreamLimitException(v, "m").version() == v, "version round-trip " + v);
        }
        check(new StreamLimitException(HttpClient.Version.HTTP_3, "m").version()
                        == HttpClient.Version.HTTP_3, "HTTP_3 version supported (JDK-27)");
    }

    // ---- deepened: serialization keeps the version field (probed) ----
    static void testSerialization() throws Exception {
        StreamLimitException original = new StreamLimitException(HttpClient.Version.HTTP_3, "limited");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }
        Object back;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            back = ois.readObject();
        }
        check(back instanceof StreamLimitException, "round-trips the class");
        check(((StreamLimitException) back).version() == HttpClient.Version.HTTP_3,
                "version survives serialization (non-transient, probed)");
        check("limited".equals(((StreamLimitException) back).getMessage()),
                "message survives serialization (probed)");
        check(back instanceof IOException, "still an IOException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
