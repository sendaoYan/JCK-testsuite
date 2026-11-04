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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
