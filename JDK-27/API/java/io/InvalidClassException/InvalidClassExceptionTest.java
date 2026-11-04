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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
