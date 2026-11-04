/*
 * @test
 * @summary java.io.ObjectInputStream.GetField conformance: full contract coverage.
 *          getObjectStreamClass identity, defaulted() for present fields, all nine
 *          typed get() overloads routing correctly, and unknown field names / signature
 *          mismatches raising IllegalArgumentException with the exact message.
 * @run main GetFieldTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class GetFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample implements Serializable {
        private static final long serialVersionUID = 42L;
        boolean z = true;
        byte b = -5;
        char c = 'Q';
        short s = 1234;
        int i = 987654;
        long j = 0xDEADBEEFCAFEL;
        float f = 3.5f;
        double d = -1.25;
        Object o = "payload";
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            exercise(in.readFields());
        }
    }

    // behavioral assertions run inside the readObject callback
    static void exercise(ObjectInputStream.GetField gf) {
        try {
            // descriptor identity
            check(gf.getObjectStreamClass().getName().equals(Sample.class.getName()),
                  "descriptor class");
            check(gf.getObjectStreamClass().getSerialVersionUID() == 42L, "descriptor SUID");

            // fields present in the stream are not defaulted
            check(!gf.defaulted("i"), "i came from the stream");
            check(!gf.defaulted("o"), "o came from the stream");

            // unknown field name -> IllegalArgumentException
            try {
                gf.defaulted("nope");
                check(false, "defaulted(unknown) must throw");
            } catch (IllegalArgumentException iae) {
                check(iae.getMessage() != null && iae.getMessage().contains("no such field"),
                      "defaulted message: " + iae.getMessage());
            }

            // every typed get() returns the serialized value
            check(gf.get("z", false), "boolean get");
            check(gf.get("b", (byte) 0) == -5, "byte get");
            check(gf.get("c", 'x') == 'Q', "char get");
            check(gf.get("s", (short) 0) == 1234, "short get");
            check(gf.get("i", 0) == 987654, "int get");
            check(gf.get("j", 0L) == 0xDEADBEEFCAFEL, "long get");
            check(gf.get("f", 0f) == 3.5f, "float get");
            check(gf.get("d", 0.0) == -1.25, "double get");
            check("payload".equals(gf.get("o", null)), "Object get");

            // signature mismatch is reported as "no such field ... with type ..."
            try {
                gf.get("i", 1.0);
                check(false, "type mismatch must throw");
            } catch (IllegalArgumentException iae) {
                check("no such field i with type double".equals(iae.getMessage()),
                      "mismatch message: " + iae.getMessage());
            }

            // unknown field with any type
            try {
                gf.get("missing", 5);
                check(false, "unknown field get must throw");
            } catch (IllegalArgumentException iae) {
                check("no such field missing with type int".equals(iae.getMessage()),
                      "unknown message: " + iae.getMessage());
            }
        } catch (IOException e) {
            // JDK 8: no GetField method declares ClassNotFoundException (the
            // Object overload only declares IOException); it arrived in JDK 9.
            throw new AssertionError(e);
        }
    }

    static void testGetFieldRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new Sample());
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
        int before = passed;
        ois.readObject();                                // triggers the callback
        check(passed > before, "readFields callback executed its assertions");
        check(ois.read() == -1, "stream exhausted after the single object");
        ois.close();
    }

    public static void main(String[] args) throws Exception {
        testGetFieldRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
