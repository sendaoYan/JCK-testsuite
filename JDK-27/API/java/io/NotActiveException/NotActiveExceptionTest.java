/*
 * @test
 * @summary java.io.NotActiveException conformance: white-box structure + black-box
 *          behavior (both constructors; real triggers from ObjectOutputStream and
 *          ObjectInputStream for every serialization call that requires an active
 *          (de)serialization context).
 * @run main NotActiveExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class NotActiveExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.NotActiveException");
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
        check(new NotActiveException().getMessage() == null, "no-arg message null");
        check("not in writeObject".equals(new NotActiveException("not in writeObject").getMessage()),
              "(String) message kept");

        // 2. triggers on the write side: calls needing an active writeObject context
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            for (int i = 0; i < 3; i++) {
                try {
                    switch (i) {
                        case 0 -> oos.defaultWriteObject();
                        case 1 -> oos.putFields();
                        default -> oos.writeFields();
                    }
                    check(false, "write-side call " + i + " must throw NotActiveException");
                } catch (NotActiveException expected) {
                    passed++;
                }
            }
        }

        // 3. triggers on the read side: calls needing an active readObject context
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject("payload");
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            for (int i = 0; i < 3; i++) {
                try {
                    switch (i) {
                        case 0 -> ois.defaultReadObject();
                        case 1 -> ois.readFields();
                        default -> ois.registerValidation(() -> {}, 0);
                    }
                    check(false, "read-side call " + i + " must throw NotActiveException");
                } catch (NotActiveException expected) {
                    passed++;
                }
            }
        }

        // 4. the same calls succeed from inside a Serializable class's callbacks
        class Proper implements Serializable {
            @Serial
            private static final long serialVersionUID = 720L;
            int v;
            boolean writeOk, readOk;
            private void writeObject(ObjectOutputStream out)
                    throws java.io.IOException {
                out.defaultWriteObject();                       // active here
                writeOk = true;
            }
            private void readObject(ObjectInputStream in)
                    throws java.io.IOException, ClassNotFoundException {
                in.defaultReadObject();                         // active here
                readOk = true;
            }
        }
        Proper p = new Proper();
        p.v = 3;
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(p);
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Proper back = (Proper) ois.readObject();
            check(back.readOk && back.v == 3, "defaultReadObject works inside readObject");
        }
        check(p.writeOk, "defaultWriteObject works inside writeObject");

        // 5. caught as ObjectStreamException
        try {
            throw new NotActiveException("m");
        } catch (ObjectStreamException ose) {
            check(ose instanceof NotActiveException, "catchable as ObjectStreamException");
        }
    }

    // ================= 深化追加（契约级） =================

    static byte[] serOf(Object o) throws Exception {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(b)) {
            oos.writeObject(o);
        }
        return b.toByteArray();
    }

    static Object back(byte[] b) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new java.io.ByteArrayInputStream(b))) {
            return ois.readObject();
        }
    }

    // ---- Throwable protocol matrix ----
    static void testThrowableProtocol() {
        NotActiveException e = new NotActiveException("m");
        check("m".equals(e.getLocalizedMessage()), "getLocalizedMessage defaults to getMessage");
        check(e.toString().equals("java.io.NotActiveException: m"),
              "toString = FQCN + \": \" + message");
        check(new NotActiveException().toString().equals("java.io.NotActiveException"),
              "no-arg toString carries no colon");
        check(new NotActiveException((String) null).getMessage() == null,
              "null message accepted, getMessage stays null");
        check(new NotActiveException("").getMessage().isEmpty(),
              "empty message distinct from null");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured");
        StackTraceElement[] st = {new StackTraceElement("N", "defaultReadObject", "N.java", 8)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1
                  && "defaultReadObject".equals(e.getStackTrace()[0].getMethodName()),
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
        check(new NotActiveException("w").initCause(cause).getCause() == cause,
              "initCause injects a cause on a class with no cause ctor");
        try {
            new NotActiveException("w").initCause(cause).initCause(cause);
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new NotActiveException().initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        NotActiveException sup = new NotActiveException("s");
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
        check(NotActiveException.class.getSuperclass() == ObjectStreamException.class,
              "direct child of ObjectStreamException");
    }

    // ---- serialization contract ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(NotActiveException.class).getSerialVersionUID()
                  == -3893467273049808895L, "serialVersionUID == -3893467273049808895");
        NotActiveException r = (NotActiveException) back(serOf(new NotActiveException("kept")));
        check("kept".equals(r.getMessage()), "message survives round-trip");
        check(r.toString().equals("java.io.NotActiveException: kept"),
              "toString survives round-trip");
        check(((NotActiveException) back(serOf(new NotActiveException()))).getMessage() == null,
              "no-arg instance round-trips with null message");
        NotActiveException chained = new NotActiveException("w");
        chained.initCause(new RuntimeException("r"));
        check("r".equals(((NotActiveException) back(serOf(chained))).getCause().getMessage()),
              "cause survives round-trip");
    }

    // ---- triggered message precision (实测双侧一致: "stream inactive") ----
    static void testTriggeredMessage() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject("payload");
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            try {
                ois.registerValidation(() -> {}, 0);
                check(false, "registerValidation outside readObject must throw");
            } catch (NotActiveException nae) {
                check("stream inactive".equals(nae.getMessage()),
                      "registerValidation message: " + nae.getMessage());
            }
            try {
                ois.readFields();
                check(false, "readFields outside readObject must throw");
            } catch (NotActiveException nae) {
                check("not in call to readObject".equals(nae.getMessage()),
                      "readFields message: " + nae.getMessage());
            }
            try {
                ois.defaultReadObject();
                check(false, "defaultReadObject outside readObject must throw");
            } catch (NotActiveException nae) {
                check("not in call to readObject".equals(nae.getMessage()),
                      "defaultReadObject message: " + nae.getMessage());
            }
        }
        // write side: putFields/writeFields/defaultWriteObject outside a writeObject callback
        try (ObjectOutputStream oos = new ObjectOutputStream(new java.io.ByteArrayOutputStream())) {
            try {
                oos.putFields();
                check(false, "putFields outside writeObject must throw");
            } catch (NotActiveException nae) {
                check("not in call to writeObject".equals(nae.getMessage()),
                      "putFields message: " + nae.getMessage());
            }
            try {
                oos.writeFields();
                check(false, "writeFields without a PutField must throw");
            } catch (NotActiveException nae) {
                check("no current PutField object".equals(nae.getMessage()),
                      "writeFields message: " + nae.getMessage());
            }
            try {
                oos.defaultWriteObject();
                check(false, "defaultWriteObject outside writeObject must throw");
            } catch (NotActiveException nae) {
                check("not in call to writeObject".equals(nae.getMessage()),
                      "defaultWriteObject message: " + nae.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableProtocol();
        testSerializationContract();
        testTriggeredMessage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

