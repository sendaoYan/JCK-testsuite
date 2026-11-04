/*
 * @test
 * @summary java.io.InvalidClassException conformance (pure black-box):
 *          cname/reason message composition, the public classname field,
 *          cause-carrying constructors; real trigger: an Externalizable class
 *          without a public no-arg constructor.
 * @run main InvalidClassExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;

public class InvalidClassExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Externalizable with no no-arg constructor: deserialization requires one. */
    static class NoArgMissing implements Externalizable {
        private static final long serialVersionUID = 701L;
        NoArgMissing(int ignored) {}
        public void writeExternal(ObjectOutput out) { }
        public void readExternal(ObjectInput in) { }
    }

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

        // 3. JDK-8 has no cause-carrying constructors (they arrived in JDK 9):
        //    getCause() is always null for both available constructors
        check(new InvalidClassException("c", "r").getCause() == null, "no cause in JDK-8");

        // 4. real trigger: Externalizable without an accessible no-arg constructor
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

        // 5. caught as ObjectStreamException / IOException
        try {
            throw new InvalidClassException("c", "r");
        } catch (ObjectStreamException ose) {
            check(ose instanceof InvalidClassException, "catchable as ObjectStreamException");
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
