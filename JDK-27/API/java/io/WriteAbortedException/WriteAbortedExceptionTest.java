/*
 * @test
 * @summary java.io.WriteAbortedException conformance: white-box structure + black-box
 *          behavior (the public detail field, composed getMessage, getCause == detail;
 *          real triggers: a custom writeObject throwing mid-write, and a
 *          non-Serializable field poisoning the stream — both surface on the read side).
 * @run main WriteAbortedExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.Serial;
import java.io.WriteAbortedException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class WriteAbortedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class ExplodingWrite implements Serializable {
        @Serial
        private static final long serialVersionUID = 750L;
        private void writeObject(ObjectOutputStream out) throws IOException {
            throw new IOException("disk died mid-write");
        }
    }

    static class Wrapper implements Serializable {
        @Serial
        private static final long serialVersionUID = 751L;
        Object payload;                     // non-Serializable value poisons the stream
        Wrapper(Object payload) { this.payload = payload; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.WriteAbortedException");
        check(c.getSuperclass().getName().equals("java.io.ObjectStreamException"),
              "extends ObjectStreamException");
        check(c.getField("detail").getType() == Exception.class,
              "public Exception detail field");
        check(c.getMethod("getMessage").getDeclaringClass() == c, "getMessage overridden");
        check(c.getMethod("getCause").getDeclaringClass() == c, "getCause overridden");
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) ctors++;
        check(ctors == 1 && c.getConstructor(String.class, Exception.class) != null,
              "single (String, Exception) constructor");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. constructor, detail field, composed message, cause
        Exception root = new IOException("root cause");
        WriteAbortedException wae = new WriteAbortedException("write aborted", root);
        check("write aborted; java.io.IOException: root cause".equals(wae.getMessage()),
              "getMessage = detail message + \"; \" + detail: " + wae.getMessage());
        check(wae.detail == root, "detail field holds the write-side exception");
        check(wae.getCause() == root, "getCause returns detail");
        check(wae.detail instanceof Exception, "detail is an Exception");

        // 2. real trigger: custom writeObject throwing aborts the write; the partially
        //    written stream read back surfaces WriteAbortedException wrapping the cause
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(new ExplodingWrite());
            check(false, "throwing writeObject must abort the write");
        } catch (IOException expected) {
            check("disk died mid-write".equals(expected.getMessage()),
                  "write side propagates the original IOException");
        }
        oos.flush();
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.readObject();
            check(false, "aborted stream must fail on read");
        } catch (WriteAbortedException readSide) {
            check(readSide.detail instanceof IOException
                      && "disk died mid-write".equals(readSide.detail.getMessage()),
                  "read side wraps the original IOException: " + readSide.detail);
        }

        // 3. real trigger: non-Serializable member poisons the stream mid-write
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        try {
            oos2.writeObject(new Wrapper(new Object()));
            check(false, "non-Serializable member must abort the write");
        } catch (java.io.NotSerializableException expected) {
            passed++;
        }
        oos2.flush();
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()))) {
            ois.readObject();
            check(false, "poisoned stream must fail on read");
        } catch (WriteAbortedException readSide) {
            check(readSide.detail instanceof java.io.NotSerializableException,
                  "NotSerializableException wrapped as detail: " + readSide.detail);
        }

        // 4. caught as ObjectStreamException
        try {
            throw new WriteAbortedException("m", root);
        } catch (ObjectStreamException ose) {
            check(ose instanceof WriteAbortedException, "catchable as ObjectStreamException");
        }
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

    // ---- message composition corners + detail/cause coupling (实测双侧一致) ----
    static void testMessageComposition() {
        Exception root = new IOException("root");
        // null message + detail: the null prefix renders as the literal "null"
        WriteAbortedException w = new WriteAbortedException(null, root);
        check("null; java.io.IOException: root".equals(w.getMessage()),
              "null message composes to literal null: " + w.getMessage());
        check(w.toString().equals("java.io.WriteAbortedException: null; java.io.IOException: root"),
              "toString carries the composed message");
        check(w.getLocalizedMessage().equals(w.getMessage()),
              "getLocalizedMessage returns the composed message");
        // null detail: no composition at all — the plain message survives
        WriteAbortedException w2 = new WriteAbortedException("m", null);
        check("m".equals(w2.getMessage()), "null detail leaves the plain message");
        check(w2.getCause() == null && w2.detail == null, "null detail yields null cause");
        // both null: getMessage() is a real null
        check(new WriteAbortedException(null, null).getMessage() == null,
              "both-null leaves getMessage null");
        // the detail field is public and LIVE: reassigning it changes message and cause
        WriteAbortedException w3 = new WriteAbortedException("m", new IOException("first"));
        w3.detail = new IllegalStateException("second");
        check("m; java.lang.IllegalStateException: second".equals(w3.getMessage()),
              "detail reassignment recomposes the message");
        check("second".equals(w3.getCause().getMessage()),
              "detail reassignment redirects getCause");
        // clearing detail later returns the message to the plain form
        w3.detail = null;
        check("m".equals(w3.getMessage()) && w3.getCause() == null,
              "detail = null collapses the composition");
    }

    // ---- initCause is permanently closed by the constructor (实测双侧一致) ----
    static void testCauseProtocol() {
        // the ctor performs initCause(null) itself: any later initCause is refused,
        // even when detail is null
        WriteAbortedException withDetail = new WriteAbortedException("m", new IOException("d"));
        try {
            withDetail.initCause(new RuntimeException("injected"));
            check(false, "initCause on non-null detail must throw");
        } catch (IllegalStateException expected) {
            check(expected.getMessage().startsWith("Can't overwrite cause"),
                  "refusal diagnostic: " + expected.getMessage());
        }
        WriteAbortedException nullDetail = new WriteAbortedException("m", null);
        try {
            nullDetail.initCause(new RuntimeException("injected"));
            check(false, "initCause on null detail must also throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        // addSuppressed still works normally alongside the frozen cause
        withDetail.addSuppressed(new Exception("x"));
        withDetail.addSuppressed(new Exception("y"));
        check(withDetail.getSuppressed().length == 2, "suppressed kept in order");
        try {
            withDetail.addSuppressed(withDetail);
            check(false, "self-suppression must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        // Throwable protocol
        check(withDetail.fillInStackTrace() == withDetail, "fillInStackTrace returns this");
        StackTraceElement[] st = {new StackTraceElement("O", "readObject0", "O.java", 2)};
        withDetail.setStackTrace(st);
        check(withDetail.getStackTrace().length == 1
                  && "readObject0".equals(withDetail.getStackTrace()[0].getMethodName()),
              "setStackTrace/getStackTrace round-trip");
        withDetail.setStackTrace(new StackTraceElement[0]);
        check(withDetail.getStackTrace().length == 0, "setStackTrace(empty) yields empty view");
        check(WriteAbortedException.class.getSuperclass() == ObjectStreamException.class,
              "direct child of ObjectStreamException");
    }

    // ---- serialization contract: detail is @serial and travels with the exception ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(WriteAbortedException.class).getSerialVersionUID()
                  == -3326426625597282442L, "serialVersionUID == -3326426625597282442");
        WriteAbortedException w = new WriteAbortedException("m", new IOException("first"));
        w.detail = new IllegalStateException("second");            // even reassigned detail travels
        WriteAbortedException r = (WriteAbortedException) back(serOf(w));
        check("m; java.lang.IllegalStateException: second".equals(r.getMessage()),
              "composed message survives round-trip");
        check(r.detail instanceof IllegalStateException
                  && "second".equals(r.detail.getMessage()),
              "reassigned detail survives round-trip");
        check(r.getCause() == r.detail, "getCause==detail identity preserved after round-trip");
        WriteAbortedException sup = new WriteAbortedException("m", new IOException("d"));
        sup.addSuppressed(new Exception("s"));
        check(((WriteAbortedException) back(serOf(sup))).getSuppressed().length == 1,
              "suppressed survive round-trip");
        WriteAbortedException nullD = (WriteAbortedException)
            back(serOf(new WriteAbortedException("m", null)));
        check(nullD.detail == null && nullD.getCause() == null && "m".equals(nullD.getMessage()),
              "null detail round-trips");
    }

    // ---- read-side triggered instance: exact message shape + double round-trip (实测) ----
    static void testTriggeredInstance() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(new ExplodingWrite());
        } catch (IOException expected) {
            passed++;                                  // write side sees the raw IOException
        }
        oos.flush();
        WriteAbortedException caught;
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.readObject();
            caught = null;
        } catch (WriteAbortedException readSide) {
            caught = readSide;
        }
        check(caught != null, "read side delivered a WriteAbortedException");
        check("writing aborted; java.io.IOException: disk died mid-write".equals(caught.getMessage()),
              "triggered message shape: " + caught.getMessage());
        check(caught.detail instanceof IOException
                  && "disk died mid-write".equals(caught.detail.getMessage()),
              "triggered detail is the write-side exception");
        check(caught.getCause() == caught.detail, "triggered getCause is the detail");
        // re-serializing the triggered exception keeps everything intact
        WriteAbortedException again = (WriteAbortedException) back(serOf(caught));
        check("writing aborted; java.io.IOException: disk died mid-write".equals(again.getMessage()),
              "triggered instance round-trips");
        check(again.detail instanceof IOException, "triggered detail round-trips");
        check(caught.getStackTrace().length > 0, "triggered instance carries a stack trace");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMessageComposition();
        testCauseProtocol();
        testSerializationContract();
        testTriggeredInstance();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
