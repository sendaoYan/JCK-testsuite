/*
 * @test
 * @summary java.io.UncheckedIOException conformance: white-box structure + black-box
 *          behavior (both constructors require an IOException cause; getCause typing;
 *          real trigger: BufferedReader.lines() wrapping an IOException raised mid-stream;
 *          the IOException cause is retrievable for recovery).
 * @run main UncheckedIOExceptionTest
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class UncheckedIOExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Emits one line, then the underlying device fails. */
    static class FailingReader extends Reader {
        int calls;
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (calls++ == 0) {
                cbuf[off] = 'a'; cbuf[off + 1] = '\n';
                return 2;
            }
            throw new IOException("device gone");
        }
        public void close() {}
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.UncheckedIOException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        int two = 0, one = 0;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 2 && p[0] == String.class && p[1] == java.io.IOException.class) two++;
            if (p.length == 1 && p[0] == java.io.IOException.class) one++;
        }
        check(two == 1 && one == 1, "two public constructors");
        boolean typedCause = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (m.getName().equals("getCause")
                && m.getReturnType() == java.io.IOException.class)
                typedCause = true;
        check(typedCause, "getCause declared to return IOException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. (IOException) constructor: message defaults to cause.toString()
        IOException cause = new IOException("io failure");
        UncheckedIOException u = new UncheckedIOException(cause);
        check(u.getCause() == cause && cause.toString().equals(u.getMessage()),
              "cause kept, message = cause.toString()");

        // 2. (String, IOException) constructor
        UncheckedIOException u2 = new UncheckedIOException("wrapper message", cause);
        check("wrapper message".equals(u2.getMessage()) && u2.getCause() == cause,
              "message + cause kept");

        // 3. null cause is refused
        try {
            new UncheckedIOException((IOException) null);
            check(false, "null cause must throw");
        } catch (NullPointerException expected) {
            passed++;
        }

        // 4. unchecked: no throws clause needed
        Runnable r = () -> {
            throw new UncheckedIOException(new IOException("x"));
        };
        try {
            r.run();
        } catch (UncheckedIOException expected) {
            passed++;
        }

        // 5. real trigger: BufferedReader.lines() wraps mid-stream IOExceptions
        try {
            new BufferedReader(new FailingReader()).lines().count();
            check(false, "lines() must wrap the mid-stream IOException");
        } catch (UncheckedIOException wrapped) {
            check(wrapped.getCause() instanceof IOException
                      && "device gone".equals(wrapped.getCause().getMessage()),
                  "cause carries the original IOException");
        }

        // 6. recovery pattern: catch UncheckedIOException, unwrap, rethrow checked
        try {
            new BufferedReader(new FailingReader()).lines().count();
        } catch (UncheckedIOException wrapped) {
            IOException io = wrapped.getCause();
            check(io != null, "caller can unwrap the checked cause");
            throw io;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        // black-box part rethrows an IOException by design: assert it escapes main's
        // first run through a direct call
        try {
            testBlackBox();
            check(false, "scenario 6 must rethrow the unwrapped IOException");
        } catch (IOException io) {
            check("device gone".equals(io.getMessage()),
                  "unwrapped checked cause propagates for recovery");
        }
        testConstructorRules();
        testCauseProtocol();
        testSerializationContract();
        testLinesTriggerDetail();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= 深化追加（契约级） =================

    static byte[] serOf(Object o) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(b)) {
            oos.writeObject(o);
        }
        return b.toByteArray();
    }

    static Object back(byte[] b) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return ois.readObject();
        }
    }

    // ---- constructor surface and message/cause rules (实测双侧一致) ----
    static void testConstructorRules() throws Exception {
        IOException cause = new IOException("io failure");
        // (IOException): message defaults EXACTLY to cause.toString()
        UncheckedIOException u = new UncheckedIOException(cause);
        check("java.io.IOException: io failure".equals(u.getMessage()),
              "single-arg message = cause.toString(): " + u.getMessage());
        check(u.getLocalizedMessage().equals(u.getMessage()),
              "getLocalizedMessage mirrors the derived message");
        check(u.toString().equals("java.io.UncheckedIOException: java.io.IOException: io failure"),
              "toString embeds the derived message");
        // (String, IOException): a null String is accepted and yields a null message
        UncheckedIOException u2 = new UncheckedIOException(null, cause);
        check(u2.getMessage() == null, "explicit null message stays null");
        check(u2.getCause() == cause, "null message still keeps the cause");
        check(u2.toString().equals("java.io.UncheckedIOException"),
              "no-arg-style toString for null message");
        // both-arg null: NPE for the null CAUSE (checked first, message-less)
        try {
            new UncheckedIOException(null, null);
            check(false, "null cause in both-arg ctor must throw");
        } catch (NullPointerException expected) {
            check(expected.getMessage() == null, "cause NPE carries no message");
        }
        // the cause must be an IOException specifically, not any Throwable (signature-level)
        java.lang.reflect.Constructor<?>[] ks = UncheckedIOException.class.getConstructors();
        check(ks.length == 2, "exactly two public constructors: " + ks.length);
        for (java.lang.reflect.Constructor<?> k : ks) {
            check(k.getParameterTypes()[k.getParameterCount() - 1] == IOException.class,
                  "ctor " + k + " ends in an IOException parameter");
        }
        check(UncheckedIOException.class.getConstructor(IOException.class) != null,
              "(IOException) ctor exists");
        check(UncheckedIOException.class.getConstructor(String.class, IOException.class) != null,
              "(String, IOException) ctor exists");
        // any IOException SUBCLASS is a valid cause
        UncheckedIOException u4 = new UncheckedIOException(new java.io.FileNotFoundException("fnf"));
        check("java.io.FileNotFoundException: fnf".equals(u4.getMessage()),
              "subclass cause renders through toString");
    }

    // ---- cause protocol: closed at construction (实测双侧一致) ----
    static void testCauseProtocol() {
        IOException cause = new IOException("c");
        UncheckedIOException u = new UncheckedIOException("m", cause);
        // covariant override: getCause() is typed IOException without a cast
        IOException typed = u.getCause();
        check(typed == cause, "covariant getCause returns the same instance");
        // the cause slot is closed: any second initCause, even null, is refused
        try {
            u.initCause(new IOException("second"));
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            check(expected.getMessage().startsWith("Can't overwrite cause"),
                  "refusal diagnostic: " + expected.getMessage());
        }
        try {
            u.initCause(null);
            check(false, "initCause(null) after construction must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        // suppression works normally alongside
        u.addSuppressed(new Exception("x"));
        u.addSuppressed(new Exception("y"));
        check(u.getSuppressed().length == 2, "suppressed kept in order");
        try {
            u.addSuppressed(u);
            check(false, "self-suppression must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        // stack trace protocol
        check(u.fillInStackTrace() == u, "fillInStackTrace returns this");
        StackTraceElement[] st = {new StackTraceElement("B", "lines", "B.java", 5)};
        u.setStackTrace(st);
        check(u.getStackTrace().length == 1
                  && "lines".equals(u.getStackTrace()[0].getMethodName()),
              "setStackTrace/getStackTrace round-trip");
        check(u.getStackTrace().length > 0, "stack visible");
        check(UncheckedIOException.class.getSuperclass() == RuntimeException.class,
              "direct child of RuntimeException (unchecked by construction)");
    }

    // ---- serialization: message/suppressed preserved, cause travels as a copy ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(UncheckedIOException.class).getSerialVersionUID()
                  == -8134305061645241065L, "serialVersionUID == -8134305061645241065");
        IOException cause = new IOException("io failure");
        UncheckedIOException u = new UncheckedIOException("m", cause);
        u.addSuppressed(new IllegalStateException("z"));
        UncheckedIOException r = (UncheckedIOException) back(serOf(u));
        check("m".equals(r.getMessage()), "message survives round-trip");
        check(r.getCause() instanceof IOException
                  && "io failure".equals(r.getCause().getMessage()),
              "cause data survives round-trip");
        check(r.getCause() != cause, "cause travels as a deep copy, not the identity");
        check(r.getSuppressed().length == 1, "suppressed survive round-trip");
        check(r.toString().equals("java.io.UncheckedIOException: m"),
              "toString after round-trip");
        // derived-message instance round-trips keeping the derived literal
        UncheckedIOException d = (UncheckedIOException)
            back(serOf(new UncheckedIOException(new IOException("x"))));
        check("java.io.IOException: x".equals(d.getMessage()),
              "derived message is snapshotted as a literal");
    }

    // ---- lines() trigger detail: the wrapping happens at the failing read (实测) ----
    static void testLinesTriggerDetail() {
        try {
            new BufferedReader(new FailingReader()).lines().limit(1).count();
            passed++;                                  // the one good line is delivered lazily
        } catch (UncheckedIOException wrapped) {
            check(false, "limit(1) before the failure must not throw");
        }
        // full consumption reaches the failing read and wraps the IOException
        try {
            new BufferedReader(new FailingReader()).lines().count();
            check(false, "full consumption must hit the failure");
        } catch (UncheckedIOException wrapped) {
            check("device gone".equals(wrapped.getCause().getMessage()),
                  "wrapping preserves the cause message");
            check(wrapped.getMessage() != null,
                  "triggered wrapper carries a message: " + wrapped.getMessage());
        }
        // forEach sees the exception only after the good element was consumed
        java.util.List<String> seen = new java.util.ArrayList<>();
        try {
            new BufferedReader(new FailingReader()).lines().forEach(seen::add);
            check(false, "forEach must reach the failure");
        } catch (UncheckedIOException wrapped) {
            check(seen.size() == 1 && "a".equals(seen.get(0)),
                  "elements before the failure were delivered");
        }
    }
}
