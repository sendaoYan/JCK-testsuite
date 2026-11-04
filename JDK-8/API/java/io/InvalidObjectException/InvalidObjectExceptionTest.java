/*
 * @test
 * @summary java.io.InvalidObjectException conformance (pure black-box):
 *          constructors incl. cause; real triggers: an ObjectInputValidation
 *          callback rejecting the graph, and readUnshared over a back-reference.
 * @run main InvalidObjectExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class InvalidObjectExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        return bos.toByteArray();
    }

    static class Rejected implements Serializable {
        private static final long serialVersionUID = 710L;
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() throws InvalidObjectException {
                    throw new InvalidObjectException("invariant broken");
                }
            }, 0);
        }
    }

    static void testBlackBox() throws Exception {
        // 1. constructors
        InvalidObjectException e = new InvalidObjectException("bad object");
        check("bad object".equals(e.getMessage()) && e.getCause() == null, "(String) message");
        check(new InvalidObjectException("bad2").getCause() == null,
              "JDK-8 has only the (String) constructor; cause stays null");

        // 2. real trigger: registerValidation callback throwing InvalidObjectException
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(ser(new Rejected())))) {
            ois.readObject();
            check(false, "a rejecting validation must abort readObject");
        } catch (InvalidObjectException ioe) {
            check("invariant broken".equals(ioe.getMessage()),
                  "validation exception propagated with its message");
        }

        // 3. real trigger: reading a back-reference through readUnshared
        String shared = "dup";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(shared);
            oos.writeObject(shared);      // second write is a back-reference
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check("dup".equals(ois.readObject()), "first read ok");
            try {
                ois.readUnshared();
                check(false, "back-reference read as unshared must fail");
            } catch (InvalidObjectException expected) {
                passed++;
            }
        }

        // 4. caught as ObjectStreamException and IOException
        try {
            throw new InvalidObjectException("x");
        } catch (java.io.IOException io) {
            check(io instanceof InvalidObjectException, "catchable as IOException");
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

    /** readResolve returning null: readObject must deliver the null as-is. */
    static class ResolveNull implements Serializable {
        private static final long serialVersionUID = 711L;
        Object readResolve() { return null; }
    }

    /** Registers three validators under priorities 2/1/0 to pin the dispatch order. */
    static class OrderedValidations implements Serializable {
        private static final long serialVersionUID = 712L;
        static final StringBuilder order = new StringBuilder();
        private void readObject(ObjectInputStream in) throws Exception {
            in.defaultReadObject();
            in.registerValidation(new ObjectInputValidation() {
                public void validateObject() { order.append('A'); }}, 1);
            in.registerValidation(new ObjectInputValidation() {
                public void validateObject() { order.append('B'); }}, 0);
            in.registerValidation(new ObjectInputValidation() {
                public void validateObject() { order.append('C'); }}, 2);
        }
    }

    // ---- Throwable protocol matrix ----
    static void testThrowableProtocol() {
        InvalidObjectException e = new InvalidObjectException("m");
        check("m".equals(e.getLocalizedMessage()), "getLocalizedMessage defaults to getMessage");
        check(e.toString().equals("java.io.InvalidObjectException: m"),
              "toString = FQCN + \": \" + message");
        check(new InvalidObjectException((String) null).getMessage() == null,
              "null message accepted, getMessage stays null");
        check(new InvalidObjectException((String) null).toString().equals("java.io.InvalidObjectException"),
              "toString of a null-message instance carries no colon");
        check(new InvalidObjectException("").getMessage().isEmpty(),
              "empty message distinct from null");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured");
        StackTraceElement[] st = {new StackTraceElement("O", "validateObject", "O.java", 4)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1
                  && "validateObject".equals(e.getStackTrace()[0].getMethodName()),
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
        check(new InvalidObjectException("w").initCause(cause).getCause() == cause,
              "initCause injects a cause on a class with no cause ctor");
        try {
            new InvalidObjectException("w").initCause(cause).initCause(cause);
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new InvalidObjectException("w3").initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        InvalidObjectException sup = new InvalidObjectException("s");
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
        check(InvalidObjectException.class.getSuperclass() == ObjectStreamException.class,
              "direct child of ObjectStreamException");
    }

    // ---- serialization contract ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(InvalidObjectException.class).getSerialVersionUID()
                  == 3233174318281839583L, "serialVersionUID == 3233174318281839583");
        InvalidObjectException r = (InvalidObjectException) back(serOf(new InvalidObjectException("kept")));
        check("kept".equals(r.getMessage()), "message survives round-trip");
        check(r.toString().equals("java.io.InvalidObjectException: kept"),
              "toString survives round-trip");
        InvalidObjectException chained = new InvalidObjectException("w");
        chained.initCause(new RuntimeException("r"));
        check("r".equals(((InvalidObjectException) back(serOf(chained))).getCause().getMessage()),
              "cause survives round-trip");
        InvalidObjectException sup = new InvalidObjectException("s");
        sup.addSuppressed(new Exception("a"));
        sup.addSuppressed(new Exception("b"));
        InvalidObjectException rs = (InvalidObjectException) back(serOf(sup));
        check(rs.getSuppressed().length == 2 && "a".equals(rs.getSuppressed()[0].getMessage())
                  && "b".equals(rs.getSuppressed()[1].getMessage()),
              "suppressed survive round-trip in order");
    }

    // ---- readUnshared of an explicit null round-trips null without exception (实测双侧一致) ----
    static void testReadUnsharedNull() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(null);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check(ois.readUnshared() == null, "readUnshared over a written null delivers null");
        }
    }

    // ---- triggered instances are family members with usable diagnostics ----
    static void testTriggeredDiagnostics() throws Exception {
        try {
            back(ser(new Rejected()));
        } catch (InvalidObjectException e) {
            check("invariant broken".equals(e.getLocalizedMessage()),
                  "triggered message equals getLocalizedMessage");
            check(e.getStackTrace().length > 0, "triggered instance carries a stack trace");
            check(java.io.IOException.class.isInstance(e),
                  "triggered instance catchable as IOException");
        }
    }

    // ---- readResolve returning null is legal: no InvalidObjectException (实测双侧一致) ----
    static void testReadResolveNull() throws Exception {
        Object o = back(serOf(new ResolveNull()));
        check(o == null, "readResolve() == null delivers null without InvalidObjectException");
    }

    // ---- validation dispatch order: highest priority first (实测双侧一致: CAB) ----
    static void testValidationOrdering() throws Exception {
        OrderedValidations.order.setLength(0);
        back(serOf(new OrderedValidations()));
        check("CAB".equals(OrderedValidations.order.toString()),
              "validators run highest priority first: " + OrderedValidations.order);
    }

    // ---- a RuntimeException from validateObject relays unwrapped (实测双侧一致) ----
    static void testValidationExceptionRelay() throws Exception {
        class Relay implements Serializable {
            private static final long serialVersionUID = 713L;
            private void readObject(ObjectInputStream in) throws Exception {
                in.defaultReadObject();
                in.registerValidation(new ObjectInputValidation() {
                    public void validateObject() { throw new IllegalStateException("unwound"); }}, 0);
            }
        }
        try {
            back(serOf(new Relay()));
            check(false, "a failing validator must abort readObject");
        } catch (InvalidObjectException wrong) {
            check(false, "runtime exceptions must not be wrapped into InvalidObjectException");
        } catch (IllegalStateException relayed) {
            check("unwound".equals(relayed.getMessage()),
                  "original runtime exception relayed unwrapped");
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testThrowableProtocol();
        testSerializationContract();
        testReadResolveNull();
        testValidationOrdering();
        testValidationExceptionRelay();
        testReadUnsharedNull();
        testTriggeredDiagnostics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
