/*
 * @test
 * @summary java.io.ObjectOutputStream.PutField conformance: white-box structure
 *          (abstract buffer of named field values with nine put overloads and a
 *          deprecated-for-removal write(ObjectOutput)) + black-box behavior
 *          (all typed puts round-trip through writeFields/readFields, unknown
 *          names and signature mismatches raise IllegalArgumentException with
 *          the exact message, writing to a foreign stream is refused).
 *          Deepening (contract-level): zero-initialized put buffer, null object puts,
 *          static/case-mismatched name rejection, all nine typed puts at extreme
 *          values (NaN, -0.0 bits), a seven-entry wrong-type message table,
 *          writeFields plus trailing custom data, putFields() buffer identity,
 *          double writeFields() stream corruption, deprecated write(owner)
 *          semantics, enum/Class/shared-reference object fields.
 * @run main PutFieldTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PutFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final boolean ZZ = true;
    static final byte BB = -5;
    static final char CC = 'Q';
    static final short SS = 1234;
    static final int II = 987654;
    static final long JJ = 0xDEADBEEFCAFEL;
    static final float FF = 3.5f;
    static final double DD = -1.25;
    static final String OO = "payload";

    // capture of what readFields observed on the other end
    static String report = "";
    static boolean okZ, okO;
    static byte okB; static char okC; static short okS; static int okI; static long okJ;
    static float okF; static double okD;

    static class Sample implements Serializable {
        private static final long serialVersionUID = 42L;
        boolean z; byte b; char c; short s; int i; long j; float f; double d; Object o;
        @SuppressWarnings("removal")
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            try {
                pf.put("nope", 1);
                check(false, "unknown field put must throw");
            } catch (IllegalArgumentException iae) {
                check("no such field nope with type int".equals(iae.getMessage()),
                      "unknown message: " + iae.getMessage());
            }
            try {
                pf.put("i", 1.0);
                check(false, "signature mismatch must throw");
            } catch (IllegalArgumentException iae) {
                check("no such field i with type double".equals(iae.getMessage()),
                      "mismatch message: " + iae.getMessage());
            }
            try {
                ByteArrayOutputStream foreign = new ByteArrayOutputStream();
                ObjectOutputStream other = new ObjectOutputStream(foreign);
                pf.write(other);
                check(false, "writing to a foreign stream must be refused");
            } catch (IllegalArgumentException iae) {
                check("wrong stream".equals(iae.getMessage()), "foreign message");
            }
            pf.put("z", ZZ);
            pf.put("b", BB);
            pf.put("c", CC);
            pf.put("s", SS);
            pf.put("i", II);
            pf.put("j", JJ);
            pf.put("f", FF);
            pf.put("d", DD);
            pf.put("o", OO);
            out.writeFields();
        }
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = in.readFields();
            okZ = gf.get("z", false); okB = gf.get("b", (byte) 0); okC = gf.get("c", 'x');
            okS = gf.get("s", (short) 0); okI = gf.get("i", 0); okJ = gf.get("j", 0L);
            okF = gf.get("f", 0f); okD = gf.get("d", 0.0);
            okO = OO.equals(gf.get("o", null));
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectOutputStream$PutField");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "direct Object subclass");
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isPublic(ctor.getModifiers()), "public no-arg constructor");
        int putCount = 0;
        for (Method m : c.getMethods()) {
            if (m.getName().equals("put") && m.getParameterCount() == 2) putCount++;
        }
        check(putCount == 9, "nine typed put() overloads: " + putCount);
        Method write = c.getMethod("write", java.io.ObjectOutput.class);
        check(write.getExceptionTypes().length == 1
                  && write.getExceptionTypes()[0] == IOException.class,
              "write(ObjectOutput) declares IOException");
        Deprecated dep = write.getAnnotation(Deprecated.class);
        check(dep != null && dep.forRemoval(), "write(ObjectOutput) deprecated for removal");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Sample());                  // runs the put() assertions
        }
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
        ois.readObject();                                   // runs the read-back captures

        // every typed put survived the round-trip exactly
        check(passed > 0, "writeObject callback ran");
        check(okZ == ZZ, "boolean put survived");
        check(okB == BB, "byte put survived");
        check(okC == CC, "char put survived");
        check(okS == SS, "short put survived");
        check(okI == II, "int put survived");
        check(okJ == JJ, "long put survived");
        check(okF == FF, "float put survived");
        check(okD == DD, "double put survived");
        check(okO, "Object put survived");
    }


    // =====================================================================
    // Deepening additions (contract-level). All fixtures are static nested
    // classes with fixed serialVersionUIDs so the wire format is stable.
    // =====================================================================

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return bos.toByteArray();
    }

    static Object de(byte[] b) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    interface Snippet { void run() throws Exception; }

    static Throwable capture(Snippet s) {
        try { s.run(); return null; } catch (Throwable t) { return t; }
    }

    /** putFields() covers only what is explicitly put(); the rest goes out zeroed. */
    static class SubsetPut implements Serializable {
        @Serial
        private static final long serialVersionUID = 6001L; // fixed: cross-version wire stability
        int a = 1, b = 2;
        boolean z = true;
        Object o = "x";
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("a", 55);
            out.writeFields();
        }
    }

    /** Null is a legal value for an object field. */
    static class NullPut implements Serializable {
        @Serial
        private static final long serialVersionUID = 6002L; // fixed: cross-version wire stability
        Object o = "initial";
        static Object nullWithDefault;
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("o", null);
            out.writeFields();
        }
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            nullWithDefault = in.readFields().get("o", "DEF");
        }
    }

    /** Static field names are not serializable fields. */
    static class StaticPut implements Serializable {
        @Serial
        private static final long serialVersionUID = 6003L; // fixed: cross-version wire stability
        static int STAT = 3;
        int a;
        private void writeObject(ObjectOutputStream out) throws IOException {
            final ObjectOutputStream.PutField pf = out.putFields();
            Throwable t = capture(new Snippet() { public void run() throws Exception { pf.put("STAT", 1); } });
            check(t instanceof IllegalArgumentException
                  && "no such field STAT with type int".equals(t.getMessage()),
                  "put() on a static field name rejected: " + t);
            t = capture(new Snippet() { public void run() throws Exception { pf.put("A", 1); } });
            check(t instanceof IllegalArgumentException
                  && "no such field A with type int".equals(t.getMessage()),
                  "field names are case sensitive: " + t);
            pf.put("a", 1);
            out.writeFields();
        }
    }

    /** All nine typed puts at extreme values. */
    static class ExtremePut implements Serializable {
        @Serial
        private static final long serialVersionUID = 6004L; // fixed: cross-version wire stability
        boolean z; byte b; char c; short s; int i; long j; float f; double d; Object o;
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("z", true);
            pf.put("b", Byte.MIN_VALUE);
            pf.put("c", Character.MAX_VALUE);
            pf.put("s", Short.MIN_VALUE);
            pf.put("i", Integer.MIN_VALUE);
            pf.put("j", Long.MIN_VALUE);
            pf.put("f", Float.NaN);
            pf.put("d", -0.0);
            pf.put("o", "keep");
            out.writeFields();
        }
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = in.readFields();
            ez = gf.get("z", false); eb = gf.get("b", (byte) 0); ec = gf.get("c", 'x');
            es = gf.get("s", (short) 0); ei = gf.get("i", 0); ej = gf.get("j", 0L);
            ef = gf.get("f", 0f); ed = gf.get("d", 0.0); eo = gf.get("o", null);
        }
    }

    static boolean ez; static byte eb; static char ec; static short es;
    static int ei; static long ej; static float ef; static double ed; static Object eo;

    /** Wrong-typed puts are rejected with an exact message. */
    static class WrongTypesPut implements Serializable {
        @Serial
        private static final long serialVersionUID = 6005L; // fixed: cross-version wire stability
        int i; boolean z; byte b;
        private void writeObject(ObjectOutputStream out) throws IOException {
            final ObjectOutputStream.PutField pf = out.putFields();
            record(1, capture(new Snippet() { public void run() throws Exception { pf.put("i", 1L); } }));
            record(2, capture(new Snippet() { public void run() throws Exception { pf.put("z", 1); } }));
            record(3, capture(new Snippet() { public void run() throws Exception { pf.put("b", 1); } }));
            record(4, capture(new Snippet() { public void run() throws Exception { pf.put("i", "x"); } }));
            record(5, capture(new Snippet() { public void run() throws Exception { pf.put(null, 1); } }));
            record(6, capture(new Snippet() { public void run() throws Exception { pf.put("i", 1.5f); } }));
            record(7, capture(new Snippet() { public void run() throws Exception { pf.put("b", true); } }));
            pf.put("i", 1); pf.put("z", true); pf.put("b", (byte) 2);
            out.writeFields();
        }
    }

    static void record(int slot, Throwable t) {
        check(t instanceof IllegalArgumentException, "probe " + slot + " -> IllegalArgumentException: " + t);
        wrong[slot - 1] = t == null ? "NO-THROW" : t.getMessage();
    }

    static final String[] wrong = new String[7];

    /** writeFields() followed by more custom primitive data. */
    static class FieldsThenData implements Serializable {
        @Serial
        private static final long serialVersionUID = 6006L; // fixed: cross-version wire stability
        int a;
        static int readA, readExtra;
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("a", 8);
            out.writeFields();
            out.writeInt(99);              // custom data after the field block
        }
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = in.readFields();
            readA = gf.get("a", 0);
            readExtra = in.readInt();
        }
    }

    /** putFields() twice hands back the same buffer. */
    static class TwicePut implements Serializable {
        @Serial
        private static final long serialVersionUID = 6007L; // fixed: cross-version wire stability
        int a;
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField f1 = out.putFields();
            ObjectOutputStream.PutField f2 = out.putFields();
            samePut = f1 == f2;
            f1.put("a", 6);
            out.writeFields();
        }
    }

    static boolean samePut;

    /** writeFields() twice: the write succeeds, the stream is unreadable. */
    static class DoubleWriteFields implements Serializable {
        @Serial
        private static final long serialVersionUID = 6008L; // fixed: cross-version wire stability
        int a = 4;
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("a", 4);
            out.writeFields();
            out.writeFields();
        }
    }

    /** Deprecated PutField.write(owner) with object-only fields still works. */
    static class LegacyObjOnly implements Serializable {
        @Serial
        private static final long serialVersionUID = 6009L; // fixed: cross-version wire stability
        Object o1, o2;
        @SuppressWarnings("removal")
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("o1", "one");
            pf.put("o2", null);
            pf.write(out);                  // deprecated legacy path, owner stream
        }
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = in.readFields();
            ro1 = gf.get("o1", null);
            ro2 = gf.get("o2", "DEF");
        }
    }

    static Object ro1, ro2;

    /** Deprecated PutField.write(owner) with primitive fields corrupts the stream. */
    static class LegacyIntPut implements Serializable {
        @Serial
        private static final long serialVersionUID = 6010L; // fixed: cross-version wire stability
        int a;
        @SuppressWarnings("removal")
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("a", 33);
            pf.write(out);
        }
    }

    enum Metal { GOLD, SILVER }

    /** Object fields carry enum constants and Class objects by reference. */
    static class RefPut implements Serializable {
        @Serial
        private static final long serialVersionUID = 6011L; // fixed: cross-version wire stability
        Object m, cl;
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("m", Metal.SILVER);
            pf.put("cl", String.class);
            out.writeFields();
        }
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = in.readFields();
            rm = gf.get("m", null);
            rcl = gf.get("cl", null);
        }
    }

    static Object rm, rcl;

    static class ShareNode2 implements Serializable {
        @Serial
        private static final long serialVersionUID = 6012L; // fixed: cross-version wire stability
        String tag;
        ShareNode2(String t) { tag = t; }
    }

    /** The same object put() into two object fields stays shared on read-back. */
    static class SharedPut implements Serializable {
        @Serial
        private static final long serialVersionUID = 6013L; // fixed: cross-version wire stability
        Object x, y;
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("x", shareOnce);
            pf.put("y", shareOnce);
            out.writeFields();
        }
    }

    static final ShareNode2 shareOnce = new ShareNode2("solo");

    // ---- P1. PutField starts zeroed: un-put fields go out as 0/false/null ----
    static void testPutSubsetZeroInit() throws Exception {
        SubsetPut back = (SubsetPut) de(ser(new SubsetPut()));
        check(back.a == 55, "explicitly put() value written");
        check(back.b == 0, "un-put int field written as 0 (buffer starts zeroed, not from the instance)");
        check(!back.z, "un-put boolean field written as false");
        check(back.o == null, "un-put object field written as null");
    }

    // ---- P2. null is a legal object-field value ----
    static void testPutNullObject() throws Exception {
        NullPut back = (NullPut) de(ser(new NullPut()));
        check(back.o == null, "put(o, null) writes null");
        check(NullPut.nullWithDefault == null,
              "an explicitly put null beats the get() default (field is present, not missing)");
    }

    // ---- P3. static and case-mismatched field names rejected ----
    static void testPutStaticRejected() throws Exception {
        check(de(ser(new StaticPut())) instanceof StaticPut,
              "put() on static/case-mismatched names rejected but the write completes");
    }

    // ---- P4. extreme typed puts ----
    static void testExtremePuts() throws Exception {
        new ObjectInputStream(new ByteArrayInputStream(ser(new ExtremePut()))).readObject();
        check(ez, "boolean true");
        check(eb == Byte.MIN_VALUE, "byte MIN");
        check(ec == Character.MAX_VALUE, "char MAX");
        check(es == Short.MIN_VALUE, "short MIN");
        check(ei == Integer.MIN_VALUE, "int MIN");
        check(ej == Long.MIN_VALUE, "long MIN");
        check(Float.isNaN(ef), "float NaN");
        check(Double.doubleToRawLongBits(ed) == Double.doubleToRawLongBits(-0.0),
              "double -0.0 exact bit pattern");
        check("keep".equals(eo), "object value");
    }

    // ---- P5. exact wrong-type message table ----
    static void testWrongTypeMessages() throws Exception {
        de(ser(new WrongTypesPut()));
        check("no such field i with type long".equals(wrong[0]), "int field, long value: " + wrong[0]);
        check("no such field z with type int".equals(wrong[1]), "boolean field, int value: " + wrong[1]);
        check("no such field b with type int".equals(wrong[2]), "byte field, int value: " + wrong[2]);
        check("no such field i with type class java.lang.Object".equals(wrong[3]),
              "int field, String value resolves to the Object overload: " + wrong[3]);
        check("no such field null with type int".equals(wrong[4]), "null field name: " + wrong[4]);
        check("no such field i with type float".equals(wrong[5]), "int field, float value: " + wrong[5]);
        check("no such field b with type boolean".equals(wrong[6]), "byte field, boolean value: " + wrong[6]);
    }

    // ---- P6. writeFields() plus trailing custom data ----
    static void testWriteFieldsThenCustomData() throws Exception {
        ObjectInputStream ois =
                new ObjectInputStream(new ByteArrayInputStream(ser(new FieldsThenData())));
        ois.readObject();
        check(FieldsThenData.readA == 8, "field block read via readFields");
        check(FieldsThenData.readExtra == 99, "trailing custom writeInt pairs with readInt");
        check(ois.read() == -1, "field block plus custom data account for every byte");
        ois.close();
    }

    // ---- P7. putFields() twice returns the same buffer ----
    static void testPutFieldsRepeat() throws Exception {
        TwicePut back = (TwicePut) de(ser(new TwicePut()));
        check(samePut, "putFields() twice hands back the same buffer");
        check(back.a == 6, "value put through the first reference lands in the stream");
    }

    // ---- P8. writeFields() twice corrupts the stream ----
    static void testDoubleWriteFields() throws Exception {
        final byte[] b = ser(new DoubleWriteFields());
        check(b.length > 0, "double writeFields() is accepted at write time");
        Throwable t = capture(new Snippet() { public void run() throws Exception { de(b); } });
        check(t instanceof java.io.StreamCorruptedException,
              "double writeFields() leaves the stream unreadable: " + t);
    }

    // ---- P9. deprecated write(ObjectOutput) semantics ----
    static void testLegacyWrite() throws Exception {
        ObjectInputStream lois =
                new ObjectInputStream(new ByteArrayInputStream(ser(new LegacyObjOnly())));
        lois.readObject();
        check("one".equals(ro1), "legacy write(owner) with object-only fields round-trips");
        check(ro2 == null, "legacy null object value reads back null (not the default)");
        check(lois.read() == -1, "legacy object-only output accounts for every byte");
        lois.close();

        final byte[] bad = ser(new LegacyIntPut());
        Throwable t = capture(new Snippet() { public void run() throws Exception { de(bad); } });
        check(t instanceof java.io.StreamCorruptedException,
              "legacy write(owner) with primitive fields corrupts the stream (why it is deprecated): " + t);
    }

    // ---- P10. object fields carry enums and Class objects ----
    static void testRefPuts() throws Exception {
        new ObjectInputStream(new ByteArrayInputStream(ser(new RefPut()))).readObject();
        check(rm == Metal.SILVER, "enum constant through PutField resolves to the same constant");
        check(rcl == String.class, "Class object through PutField keeps identity");
    }

    // ---- P11. shared references through put() stay shared ----
    static void testSharedPut() throws Exception {
        SharedPut back = (SharedPut) de(ser(new SharedPut()));
        check(back.x instanceof ShareNode2 && back.y instanceof ShareNode2,
              "object fields restored with correct runtime type");
        check(back.x == back.y, "the same object put() into two fields reads back identical");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPutSubsetZeroInit();
        testPutNullObject();
        testPutStaticRejected();
        testExtremePuts();
        testWrongTypeMessages();
        testWriteFieldsThenCustomData();
        testPutFieldsRepeat();
        testDoubleWriteFields();
        testLegacyWrite();
        testRefPuts();
        testSharedPut();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
