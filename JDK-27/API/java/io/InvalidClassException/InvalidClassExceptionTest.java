/*
 * @test
 * @summary java.io.InvalidClassException conformance: white-box structure + black-box
 *          behavior (cname/reason message composition, the public classname field,
 *          cause-carrying constructors; real triggers: a serialization filter rejecting
 *          a class, and an Externalizable class without a public no-arg constructor).
 * @run main InvalidClassExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class InvalidClassExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Good implements Serializable {
        @Serial
        private static final long serialVersionUID = 700L;
        int v = 1;
    }

    /** Externalizable with no no-arg constructor: deserialization requires one. */
    static class NoArgMissing implements Externalizable {
        @Serial
        private static final long serialVersionUID = 701L;
        NoArgMissing(int ignored) {}
        public void writeExternal(ObjectOutput out) { }
        public void readExternal(ObjectInput in) { }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.InvalidClassException");
        check(c.getSuperclass().getName().equals("java.io.ObjectStreamException"),
              "extends ObjectStreamException");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getField("classname").getType() == String.class, "public String classname field");
        check(c.getMethod("getMessage").getDeclaringClass() == c,
              "getMessage overridden to append the classname");
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) ctors++;
        check(ctors == 4, "four public constructors (reason / cname+reason / +cause variants)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. reason-only constructor: message verbatim, classname null
        InvalidClassException r = new InvalidClassException("local class incompatible");
        check("local class incompatible".equals(r.getMessage()), "reason-only message");
        check(r.classname == null, "classname null when not supplied");

        // 2. cname+reason: message composed as "cname; reason"
        InvalidClassException cr = new InvalidClassException("com.acme.Widget", "serialVersionUID mismatch");
        check("com.acme.Widget".equals(cr.classname), "classname field set from first arg");
        check("com.acme.Widget; serialVersionUID mismatch".equals(cr.getMessage()),
              "getMessage = classname + \"; \" + reason");

        // 3. cause-carrying constructors keep the cause and the composed message
        Exception cause = new IllegalStateException("boom");
        InvalidClassException rc = new InvalidClassException("reason2", cause);
        check("reason2".equals(rc.getMessage()) && rc.getCause() == cause
              && rc.classname == null, "(reason, cause)");
        InvalidClassException crc = new InvalidClassException("cls2", "reason3", cause);
        check(crc.classname.equals("cls2") && crc.getCause() == cause
              && "cls2; reason3".equals(crc.getMessage()), "(cname, reason, cause)");

        // 4. real trigger: ObjectInputFilter rejection surfaces as InvalidClassException
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Good());
        }
        ObjectInputStream rej = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        rej.setObjectInputFilter(ObjectInputFilter.Config.createFilter("!*"));
        try {
            rej.readObject();
            check(false, "filter rejection must abort deserialization");
        } catch (InvalidClassException ice) {
            check(ice.getMessage() != null && ice.getMessage().contains("REJECTED"),
                  "filter rejection message: " + ice.getMessage());
        }
        rej.close();

        // 5. real trigger: Externalizable without an accessible no-arg constructor
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos2)) {
            oos.writeObject(new NoArgMissing(1));
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()))) {
            ois.readObject();
            check(false, "missing no-arg Externalizable ctor must fail");
        } catch (InvalidClassException ice) {
            check(ice.getMessage() != null && ice.getMessage().contains("no valid constructor"),
                  "no-valid-constructor message: " + ice.getMessage());
        }

        // 6. caught as ObjectStreamException / IOException
        try {
            throw new InvalidClassException("c", "r");
        } catch (ObjectStreamException ose) {
            check(ose instanceof InvalidClassException, "catchable as ObjectStreamException");
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

    /** Fixed-UID class whose stream descriptor we corrupt to force a mismatch. */
    static class UidVersion implements Serializable {
        private static final long serialVersionUID = 0xBEEFL;
        int v = 7;
    }

    // ---- message composition corners (实测双侧一致) ----
    static void testMessageComposition() {
        InvalidClassException e = new InvalidClassException("Cn", "reason");
        check(e.getMessage().equals(e.getLocalizedMessage()),
              "getLocalizedMessage delegates to the composed getMessage");
        check(e.toString().equals("java.io.InvalidClassException: Cn; reason"),
              "toString = FQCN + \": \" + composed message");
        check("Cn; null".equals(new InvalidClassException("Cn", (String) null).getMessage()),
              "null reason string-concatenates as \"null\"");
        check("r".equals(new InvalidClassException(null, "r").getMessage()),
              "null classname falls back to the bare reason");
        check(((InvalidClassException) new InvalidClassException(null, "r")).classname == null,
              "classname field stays null");
        check(new InvalidClassException((String) null, (String) null).getMessage() == null,
              "both-null: classname null falls back to the null reason");
        // classname is a public live field: reassigning it changes getMessage()
        InvalidClassException mut = new InvalidClassException("Before", "reason");
        mut.classname = "After";
        check("After; reason".equals(mut.getMessage()),
              "getMessage reads the current classname field");
        check(mut.toString().contains("After; reason"), "toString reflects the mutation");
    }

    // ---- Throwable protocol matrix ----
    static void testThrowableProtocol() {
        InvalidClassException e = new InvalidClassException("c", "r");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured");
        StackTraceElement[] st = {new StackTraceElement("K", "m", "K.java", 1)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1, "setStackTrace/getStackTrace round-trip");
        try {
            e.setStackTrace(null);
            check(false, "setStackTrace(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        check(e.getSuppressed().length == 0, "suppressed empty by default");
        e.addSuppressed(new Exception("s"));
        check(e.getSuppressed().length == 1, "addSuppressed appends");
        try {
            e.addSuppressed(e);
            check(false, "self-suppression must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        try {
            e.addSuppressed(null);
            check(false, "addSuppressed(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        // JDK-8 has no cause ctors; initCause is available but a second call fails
        Exception cause = new IllegalStateException("c");
        check(new InvalidClassException("w").initCause(cause).getCause() == cause,
              "initCause injects a cause");
        try {
            new InvalidClassException("w2").initCause(cause).initCause(cause);
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new InvalidClassException("w3").initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
    }

    // ---- serialization contract: the public classname field is part of the serial form ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(InvalidClassException.class).getSerialVersionUID()
                  == -4333316296251054416L, "serialVersionUID == -4333316296251054416");
        InvalidClassException r = (InvalidClassException) back(serOf(new InvalidClassException("Klass", "why")));
        check("Klass".equals(r.classname), "classname survives round-trip");
        check("Klass; why".equals(r.getMessage()), "composed message survives round-trip");
        check(r.toString().equals("java.io.InvalidClassException: Klass; why"),
              "toString survives round-trip");
        InvalidClassException reasonOnly = (InvalidClassException) back(serOf(new InvalidClassException("just")));
        check(reasonOnly.classname == null && "just".equals(reasonOnly.getMessage()),
              "reason-only instance round-trips");
    }

    // ---- real trigger: stream serialVersionUID != local serialVersionUID ----
    static void testUidMismatchTrigger() throws Exception {
        byte[] s = serOf(new UidVersion());
        // locate the class-name bytes in the class descriptor, the 8-byte suid follows
        String name = "InvalidClassExceptionTest$UidVersion";
        int nameStart = -1;
        for (int i = 0; i <= s.length - name.length(); i++) {
            boolean match = true;
            for (int j = 0; j < name.length(); j++) {
                if (s[i + j] != (byte) name.charAt(j)) { match = false; break; }
            }
            if (match) { nameStart = i; break; }
        }
        check(nameStart > 0, "class name located in the stream descriptor");
        s[nameStart + name.length()] ^= 0x7F;           // flip a suid byte
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(s))) {
            ois.readObject();
            check(false, "suid mismatch must abort deserialization");
        } catch (InvalidClassException ice) {
            check(ice.getMessage() != null
                      && ice.getMessage().contains("local class incompatible"),
                  "mismatch message: " + ice.getMessage());
            check(ice.getMessage().contains("stream classdesc serialVersionUID")
                      && ice.getMessage().contains("local class serialVersionUID"),
                  "both UIDs reported: " + ice.getMessage());
            check("InvalidClassExceptionTest$UidVersion".equals(ice.classname),
                  "classname field carries the offending class");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMessageComposition();
        testThrowableProtocol();
        testSerializationContract();
        testUidMismatchTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

