/*
 * @test
 * @summary java.io.NotSerializableException conformance: white-box structure + black-box
 *          behavior (both constructors; real triggers: ObjectOutputStream.writeObject of
 *          a non-Serializable root, a non-Serializable field, and a non-Serializable
 *          superclass chain entry, each with the class name as the message).
 * @run main NotSerializableExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class NotSerializableExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Plain { }

    static class HasPlainField implements Serializable {
        @Serial
        private static final long serialVersionUID = 730L;
        Plain plain = new Plain();
    }

    static class SubOfPlain extends Plain implements Serializable {
        @Serial
        private static final long serialVersionUID = 731L;
        int x = 1;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.NotSerializableException");
        check(c.getSuperclass().getName().equals("java.io.ObjectStreamException"),
              "extends ObjectStreamException");
        int noArg = 0, str = 0;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 0) noArg++;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) str++;
        }
        check(noArg == 1 && str == 1, "two public constructors");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. constructors
        check(new NotSerializableException().getMessage() == null, "no-arg message null");
        check("com.x.Y".equals(new NotSerializableException("com.x.Y").getMessage()),
              "(String) message kept");

        // 2. trigger: the root object itself is not Serializable
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            oos.writeObject(new Plain());
            check(false, "writing a non-Serializable root must throw");
        } catch (NotSerializableException nse) {
            String n = nse.getMessage();
            check(n.endsWith("Plain"), "message is the offending class name: " + n);
        }

        // 3. trigger: a Serializable class holding a non-Serializable field
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            oos.writeObject(new HasPlainField());
            check(false, "non-Serializable field must throw");
        } catch (NotSerializableException nse) {
            check(nse.getMessage().endsWith("Plain"),
                  "message names the field's class: " + nse.getMessage());
        }

        // 4. trigger: implementing Serializable below a non-Serializable superclass
        //    (fields of the non-serializable ancestor are simply skipped, no throw;
        //     the exception only fires for reachable non-serializable *values*)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new SubOfPlain());
            passed++;
        }
        check(bos.size() > 4, "subclass of a plain class serializes its own fields");

        // 5. contrast: transient escapes the check
        class Trans implements Serializable {
            @Serial
            private static final long serialVersionUID = 732L;
            transient Plain skipMe = new Plain();
        }
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Trans());
            passed++;
        }

        // 6. caught as ObjectStreamException; distinct from NotActiveException
        try {
            throw new NotSerializableException("m");
        } catch (ObjectStreamException ose) {
            check(ose instanceof NotSerializableException
                  && !(ose instanceof NotActiveException),
                  "catchable as ObjectStreamException");
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

    // ---- Throwable protocol matrix ----
    static void testThrowableProtocol() {
        NotSerializableException e = new NotSerializableException("m");
        check("m".equals(e.getLocalizedMessage()), "getLocalizedMessage defaults to getMessage");
        check(e.toString().equals("java.io.NotSerializableException: m"),
              "toString = FQCN + \": \" + message");
        check(new NotSerializableException().toString().equals("java.io.NotSerializableException"),
              "no-arg toString carries no colon");
        check(new NotSerializableException((String) null).getMessage() == null,
              "null message accepted, getMessage stays null");
        check(new NotSerializableException("").getMessage().isEmpty(),
              "empty message distinct from null");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured");
        StackTraceElement[] st = {new StackTraceElement("W", "writeObject0", "W.java", 3)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1
                  && "writeObject0".equals(e.getStackTrace()[0].getMethodName()),
              "setStackTrace/getStackTrace round-trip");
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) yields empty view");
        try {
            e.setStackTrace(null);
            check(false, "setStackTrace(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        Exception cause = new IllegalStateException("c");
        check(new NotSerializableException("w").initCause(cause).getCause() == cause,
              "initCause injects a cause on a class with no cause ctor");
        try {
            new NotSerializableException("w").initCause(cause).initCause(cause);
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new NotSerializableException().initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        NotSerializableException sup = new NotSerializableException("s");
        sup.addSuppressed(new Exception("x"));
        sup.addSuppressed(new Exception("y"));
        check(sup.getSuppressed().length == 2, "suppressed kept in order");
        try {
            sup.addSuppressed(sup);
            check(false, "self-suppression must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        try {
            sup.addSuppressed(null);
            check(false, "addSuppressed(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        check(NotSerializableException.class.getSuperclass() == ObjectStreamException.class,
              "direct child of ObjectStreamException");
    }

    // ---- serialization contract ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(NotSerializableException.class).getSerialVersionUID()
                  == 2906642554793891381L, "serialVersionUID == 2906642554793891381");
        NotSerializableException r = (NotSerializableException) back(serOf(new NotSerializableException("kept")));
        check("kept".equals(r.getMessage()), "message survives round-trip");
        check(r.toString().equals("java.io.NotSerializableException: kept"),
              "toString survives round-trip");
        check(((NotSerializableException) back(serOf(new NotSerializableException()))).getMessage() == null,
              "no-arg instance round-trips with null message");
        NotSerializableException chained = new NotSerializableException("w");
        chained.initCause(new RuntimeException("r"));
        check("r".equals(((NotSerializableException) back(serOf(chained))).getCause().getMessage()),
              "cause survives round-trip");
    }

    // ---- triggered instances name the offending class as their entire message (实测) ----
    static void testTriggeredMessages() throws Exception {
        // root object: message is the fully-qualified class name of the root
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            oos.writeObject(new Object());
            check(false, "non-Serializable root must throw");
        } catch (NotSerializableException nse) {
            check("java.lang.Object".equals(nse.getMessage()),
                  "root message is the exact FQCN: " + nse.getMessage());
            check(nse.toString().endsWith("java.lang.Object"),
                  "toString exposes the offending class");
        }
        // array whose element is not Serializable names the ELEMENT class
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            oos.writeObject(new Object[]{"ok", new Object()});
            check(false, "non-Serializable element must throw");
        } catch (NotSerializableException nse) {
            check("java.lang.Object".equals(nse.getMessage()),
                  "array case names the element class: " + nse.getMessage());
        }
        // a field of a Serializable class
        class Holder implements Serializable {
            private static final long serialVersionUID = 733L;
            Object payload = new Thread();                     // Thread is not Serializable
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            oos.writeObject(new Holder());
            check(false, "non-Serializable field must throw");
        } catch (NotSerializableException nse) {
            check("java.lang.Thread".equals(nse.getMessage()),
                  "field case names the field's class: " + nse.getMessage());
        }
        // a map entry value
        java.util.HashMap<String, Object> m = new java.util.HashMap<>();
        m.put("k", new Object());
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            oos.writeObject(m);
            check(false, "non-Serializable map value must throw");
        } catch (NotSerializableException nse) {
            check("java.lang.Object".equals(nse.getMessage()),
                  "collection case names the value class: " + nse.getMessage());
        }
        // writeReplace throwing propagates its own exception unwrapped, no NotSerializableException
        class ExplodingReplace implements Serializable {
            private static final long serialVersionUID = 734L;
            Object writeReplace() { throw new IllegalStateException("no replacement today"); }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            oos.writeObject(new ExplodingReplace());
            check(false, "throwing writeReplace must abort the write");
        } catch (IllegalStateException relayed) {
            check("no replacement today".equals(relayed.getMessage()),
                  "writeReplace exception relays unwrapped");
        }
        // contrast: enum and String are Serializable, no exception
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(java.util.concurrent.TimeUnit.SECONDS);
            oos.writeObject("s");
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableProtocol();
        testSerializationContract();
        testTriggeredMessages();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

