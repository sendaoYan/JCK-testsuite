/*
 * @test
 * @summary java.io.IOError conformance (pure black-box): unchecked Error wrapping
 *          a Throwable cause; single constructor, cause retention, message derived
 *          from the cause, catch semantics, propagation as an Error.
 * @run main IOErrorTest
 */

import java.io.IOException;
import java.io.IOError;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

public class IOErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBlackBox() throws Exception {
        // 1. constructor: cause kept, message defaults to cause.toString()
        IOException cause = new IOException("fatal device failure");
        IOError e = new IOError(cause);
        check(e.getCause() == cause, "cause retained");
        check(cause.toString().equals(e.getMessage()), "message = cause.toString()");
        check(e.getMessage().contains("fatal device failure"), "message carries cause detail");

        // 2. wraps any Throwable, not only IOException
        RuntimeException rte = new RuntimeException("rt");
        IOError e2 = new IOError(rte);
        check(e2.getCause() == rte && rte.toString().equals(e2.getMessage()),
              "arbitrary Throwable cause");

        // 3. unchecked: declared nowhere, escapes methods without throws clauses
        Runnable r = () -> { throw new IOError(cause); };
        try {
            r.run();
            check(false, "IOError must propagate unchecked");
        } catch (IOError expected) {
            passed++;
        }

        // 4. it is an Error, not an Exception (never caught by catch (IOException))
        check(Error.class.isAssignableFrom(IOError.class)
                  && !Exception.class.isAssignableFrom(IOError.class),
              "IOError is an Error, not an Exception");
        try {
            throw new IOError(cause);
        } catch (Throwable t) {
            check(t instanceof IOError, "caught as Throwable");
        }

        // 5. stack trace and toString behave like any Error
        IOError t = new IOError(cause);
        check(t.toString().startsWith("java.io.IOError"), "toString names the class");
        check(t.getStackTrace().length > 0, "stack trace captured");
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

    // ---- constructor surface and message/cause derivation (实测双侧一致) ----
    static void testConstructorRules() throws Exception {
        // exactly one public constructor, taking any Throwable
        java.lang.reflect.Constructor<?>[] ks = IOError.class.getConstructors();
        check(ks.length == 1, "single public constructor: " + ks.length);
        check(ks[0].getParameterTypes()[0] == Throwable.class,
              "the ctor parameter type is Throwable");
        check(IOError.class.getSuperclass() == Error.class, "direct child of Error");
        // message derivation is a snapshot: cause.toString() at construction time
        IOException cause = new IOException("c1");
        IOError e = new IOError(cause);
        check("java.io.IOException: c1".equals(e.getMessage()),
              "message = cause.toString(): " + e.getMessage());
        check(e.getLocalizedMessage().equals(e.getMessage()),
              "getLocalizedMessage mirrors the derived message");
        check(e.toString().equals("java.io.IOError: java.io.IOException: c1"),
              "toString embeds the derived message");
        // mutating the cause afterwards does NOT recompose the message
        // (IOError caches the literal, unlike WriteAbortedException's live composition)
        check(e.getMessage().equals("java.io.IOException: c1"),
              "message stays the construction-time literal");
        // null cause is ACCEPTED (unlike UncheckedIOException): message null, cause null
        IOError n = new IOError(null);
        check(n.getMessage() == null, "null cause yields a null message");
        check(n.getCause() == null, "null cause stays null");
        check(n.toString().equals("java.io.IOError"),
              "null-cause toString carries no message segment");
        // any Throwable subclass is a legal cause
        check(new IOError(new RuntimeException("rt")).getMessage()
                  .equals("java.lang.RuntimeException: rt"),
              "RuntimeException cause renders via toString");
        check(new IOError(new AssertionError("ae")).getCause() instanceof Error,
              "another Error can nest inside IOError");
        // unlike UncheckedIOException, an Exception cause is fine too
        check(new IOError(new Exception("plain")).getCause() instanceof Exception,
              "plain Exception cause accepted (Throwable surface)");
    }

    // ---- cause protocol: slot closed at construction, suppression unaffected (实测) ----
    static void testCauseProtocol() {
        IOError e = new IOError(new IOException("c1"));
        try {
            e.initCause(new IOException("c2"));
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            check(expected.getMessage().startsWith("Can't overwrite cause"),
                  "refusal diagnostic: " + expected.getMessage());
        }
        // even a null-cause instance has its slot consumed
        try {
            new IOError(null).initCause(new IOException("late"));
            check(false, "initCause on null-cause instance must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        // suppression works normally alongside
        e.addSuppressed(new Exception("x"));
        e.addSuppressed(new Exception("y"));
        check(e.getSuppressed().length == 2, "suppressed kept in order");
        try {
            e.addSuppressed(e);
            check(false, "self-suppression must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        // stack trace protocol
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        StackTraceElement[] st = {new StackTraceElement("C", "readPassword", "C.java", 3)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1
                  && "readPassword".equals(e.getStackTrace()[0].getMethodName()),
              "setStackTrace/getStackTrace round-trip");
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) yields empty view");
        // dispatch: an Error, never caught by Exception handlers
        check(!Exception.class.isAssignableFrom(IOError.class),
              "IOError escapes every Exception handler");
        try {
            throw new IOError(new IOException("x"));
        } catch (Error err) {
            check(err instanceof IOError, "caught in the Error channel");
        }
    }

    // ---- serialization contract: derived message and cause travel (实测双侧一致) ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(IOError.class).getSerialVersionUID()
                  == 67100927991680413L, "serialVersionUID == 67100927991680413");
        IOError e = new IOError(new IOException("c1"));
        e.addSuppressed(new IllegalStateException("z"));
        IOError r = (IOError) back(serOf(e));
        check("java.io.IOException: c1".equals(r.getMessage()),
              "derived message survives round-trip");
        check(r.getCause() instanceof IOException
                  && "c1".equals(r.getCause().getMessage()),
              "cause data survives round-trip");
        check(r.getSuppressed().length == 1, "suppressed survive round-trip");
        check(r.toString().equals("java.io.IOError: java.io.IOException: c1"),
              "toString after round-trip");
        // null-cause instance round-trips keeping the null message
        IOError n = (IOError) back(serOf(new IOError(null)));
        check(n.getMessage() == null && n.getCause() == null,
              "null-cause instance round-trips");
    }

    // ---- practical wrapping pattern: checked world -> unchecked Error boundary ----
    static void testWrapBoundary() {
        // a method constrained to no throws clause wraps an IOException in IOError
        Runnable ioBoundary = () -> {
            try {
                throw new IOException("device gone");
            } catch (IOException io) {
                throw new IOError(io);
            }
        };
        try {
            ioBoundary.run();
            check(false, "wrapped boundary must throw");
        } catch (IOError err) {
            check(err.getCause() instanceof IOException
                      && "device gone".equals(err.getCause().getMessage()),
                  "the checked cause is retrievable at the boundary");
            check("java.io.IOException: device gone".equals(err.getMessage()),
                  "message still derived from the cause");
        }
        // a caller can translate back: catch IOError, rethrow the checked cause
        try {
            ioBoundary.run();
        } catch (IOError err) {
            try {
                throw (Exception) err.getCause();
            } catch (IOException recovered) {
                check("device gone".equals(recovered.getMessage()),
                      "round-trip back to the checked exception");
            } catch (Exception other) {
                check(false, "recovered cause should be the IOException");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testConstructorRules();
        testCauseProtocol();
        testSerializationContract();
        testWrapBoundary();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
