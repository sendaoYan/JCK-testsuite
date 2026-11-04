/*
 * @test
 * @summary java.io.ObjectInputStream.GetField conformance: full contract coverage.
 *          getObjectStreamClass identity, defaulted() for present fields, all nine
 *          typed get() overloads routing correctly, and unknown field names / signature
 *          mismatches raising IllegalArgumentException with the exact message.
 *          Deepening: field evolution via byte-level descriptor surgery
 *          (defaulted()/defaults for fields absent from the stream), exact
 *          type-mismatch message table, defaulted() for every present field,
 *          descriptor name/SUID/field-count contract, extreme values incl.
 *          NaN/-0.0 bit patterns, readFields context rules, shared object
 *          fields keep identity.
 * @run main GetFieldTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
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


    // =====================================================================
    // Deepening additions (contract-level). All fixtures are static nested
    // classes with fixed serialVersionUIDs so the stream layout (and the
    // byte-level surgery on it) is stable.
    // =====================================================================

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return bos.toByteArray();
    }

    /**
     * Removes one primitive int field descriptor from the first object's class
     * descriptor AND its 4 data bytes. Stream layout:
     * [0..3]=header, [4]=TC_OBJECT(0x73), [5]=TC_CLASSDESC(0x72),
     * [6..7]=name length, name, 8-byte SUID, 1-byte flags, 2-byte field count,
     * field descriptors (typecode + 2-byte-length name), TC_ENDBLOCKDATA(0x78),
     * super descriptor (TC_NULL 0x70 for Object), then the field data.
     * ObjectStreamClass writes fields sorted by name, so for a class with
     * fields {a, b, c} the dropped "c" is the last 4 data bytes.
     */
    static byte[] removeIntField(byte[] s, String cls, String fieldName) {
        if ((s[4] & 0xFF) != 0x73) throw new IllegalStateException("no TC_OBJECT at 4");
        if ((s[5] & 0xFF) != 0x72) throw new IllegalStateException("no TC_CLASSDESC at 5");
        int nlen = ((s[6] & 0xFF) << 8) | (s[7] & 0xFF);
        String name = new String(s, 8, nlen, java.nio.charset.StandardCharsets.US_ASCII);
        if (!name.equals(cls)) throw new IllegalStateException("wrong class in stream: " + name);
        int cntOff = 8 + nlen + 8 + 1;              // after uid(8) + flags(1)
        int count = ((s[cntOff] & 0xFF) << 8) | (s[cntOff + 1] & 0xFF);
        int off = cntOff + 2;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(s, 0, cntOff + 2);                // copy through the count bytes
        int kept = 0;
        boolean dropped = false;
        for (int i = 0; i < count; i++) {
            char tc = (char) s[off];
            int fnlen = ((s[off + 1] & 0xFF) << 8) | (s[off + 2] & 0xFF);
            String fn = new String(s, off + 3, fnlen, java.nio.charset.StandardCharsets.US_ASCII);
            int descLen = 3 + fnlen;
            if (tc == 'L' || tc == '[') {
                if ((s[off + descLen] & 0xFF) == 0x71) descLen += 5;
                else {
                    int l2 = ((s[off + descLen + 1] & 0xFF) << 8) | (s[off + descLen + 2] & 0xFF);
                    descLen += 3 + l2;
                }
            }
            if (fn.equals(fieldName)) {
                if (tc != 'I') throw new IllegalStateException("fixture requires an int field");
                dropped = true;
            } else {
                out.write(s, off, descLen);
                kept++;
            }
            off += descLen;
        }
        if (!dropped) throw new IllegalStateException("field not found: " + fieldName);
        byte[] head = out.toByteArray();
        head[cntOff] = (byte) (kept >> 8);          // patch the count
        head[cntOff + 1] = (byte) kept;
        if ((s[off] & 0xFF) != 0x78) throw new IllegalStateException("expected TC_ENDBLOCKDATA");
        int dataStart = off + 2;                    // skip ENDBLOCKDATA + TC_NULL super
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        res.write(head, 0, head.length);
        res.write(s, off, dataStart - off);
        res.write(s, dataStart, kept * 4);          // surviving int data bytes
        res.write(s, dataStart + count * 4, s.length - dataStart - count * 4);
        return res.toByteArray();
    }

    /** Simulates reading an older stream (fields a, b) with a newer class (a, b, c). */
    static class Evolved implements Serializable {
        private static final long serialVersionUID = 42L; // fixed: same UID on both sides of the evolution
        int a, b, c;
        static int readA, readB, readC;
        static boolean cDefaulted, aDefaulted;
        Evolved(int a, int b, int c) { this.a = a; this.b = b; this.c = c; }
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = in.readFields();
            readA = gf.get("a", 0);
            readB = gf.get("b", 0);
            readC = gf.get("c", -77);
            aDefaulted = gf.defaulted("a");
            cDefaulted = gf.defaulted("c");
        }
    }

    /** Fixture for exact IllegalArgumentException messages. */
    static class MsgSample implements Serializable {
        private static final long serialVersionUID = 43L; // fixed: cross-version wire stability
        int i = 1; boolean z = true; long j = 2L; double d = 3.5; Object o = "obj";
        static java.util.List<String> messages = new java.util.ArrayList<String>();
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = in.readFields();
            messages.clear();
            try { gf.defaulted("noSuchField"); lastDefaultedProbe = "NO-THROW"; }
            catch (IllegalArgumentException iae) { lastDefaultedProbe = iae.getMessage(); }
            messages.add(expectIae(gf, "i", 1L));          // long default for int field
            messages.add(expectIae(gf, "z", 1));           // int default for boolean field
            messages.add(expectIae(gf, "o", 5));           // overload resolution picks get(String,int)
            messages.add(expectIae(gf, "j", 1));           // int default for long field
            messages.add(expectIae(gf, "d", 1.5f));        // float default for double field
            messages.add(expectIae(gf, "missing", "s"));   // unknown name, Object overload
        }
    }

    static String expectIae(ObjectInputStream.GetField gf, String field, Object def) {
        try {
            if (def instanceof Long) gf.get(field, ((Long) def).longValue());
            else if (def instanceof Integer) gf.get(field, ((Integer) def).intValue());
            else if (def instanceof Float) gf.get(field, ((Float) def).floatValue());
            else if (def instanceof Boolean) gf.get(field, ((Boolean) def).booleanValue());
            else gf.get(field, def);
            check(false, "get(" + field + ", " + def + ") must throw IllegalArgumentException");
            return "NO-THROW";
        } catch (IllegalArgumentException iae) {
            check(iae.getMessage() != null, "IAE carries a message");
            return iae.getMessage();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /** Every typed field at an extreme value. */
    static class ExtremeSample implements Serializable {
        private static final long serialVersionUID = 44L; // fixed: cross-version wire stability
        boolean z = true;
        byte b = Byte.MIN_VALUE;
        char c = Character.MAX_VALUE;
        short s = Short.MIN_VALUE;
        int i = Integer.MIN_VALUE;
        long j = Long.MIN_VALUE;
        float f = Float.NaN;
        double d = -0.0;
        Object o = "keep";
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = in.readFields();
            rZ = gf.get("z", false);
            rB = gf.get("b", (byte) 0);
            rC = gf.get("c", 'x');
            rS = gf.get("s", (short) 0);
            rI = gf.get("i", 0);
            rJ = gf.get("j", 0L);
            rF = gf.get("f", 0f);
            rD = gf.get("d", 0.0);
            rO = gf.get("o", null);
        }
    }

    static boolean rZ; static byte rB; static char rC; static short rS;
    static int rI; static long rJ; static float rF; static double rD; static Object rO;

    static class ShareNode implements Serializable {
        private static final long serialVersionUID = 45L; // fixed: cross-version wire stability
        String tag;
        ShareNode(String t) { tag = t; }
    }

    static class SharedFields implements Serializable {
        private static final long serialVersionUID = 46L; // fixed: cross-version wire stability
        ShareNode x, y;
        static Object rx, ry;
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = in.readFields();
            rx = gf.get("x", null);
            ry = gf.get("y", null);
        }
    }

    static class ReadFieldsTwice implements Serializable {
        private static final long serialVersionUID = 47L; // fixed: cross-version wire stability
        static String second;
        static String afterFields;
        int a = 4;
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.readFields();
            try { in.readFields(); second = "NO-THROW"; }
            catch (IllegalArgumentException e) { second = "wrong-type: " + e.getMessage(); }
            catch (IOException e) { second = e.getClass().getName() + ": " + e.getMessage(); }
            try { in.defaultReadObject(); afterFields = "NO-THROW"; }
            catch (IOException e) { afterFields = e.getClass().getName() + ": " + e.getMessage(); }
        }
    }

    // ---- G1. field evolution: fields missing from the stream are defaulted ----
    static void testEvolutionDefaults() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new Evolved(1, 2, 3));
        oos.close();
        byte[] older = removeIntField(bos.toByteArray(), Evolved.class.getName(), "c");
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(older));
        Evolved back = (Evolved) ois.readObject();
        check(back instanceof Evolved, "older stream still resolves to the (newer) local class");
        check(Evolved.readA == 1 && Evolved.readB == 2, "present fields restored from the older stream");
        check(Evolved.readC == -77, "field absent from the stream returns the caller default");
        check(Evolved.cDefaulted, "defaulted(c) is true for a stream-missing field");
        check(!Evolved.aDefaulted, "defaulted(a) is false for a streamed field");
        check(ois.read() == -1, "surgically trimmed stream still terminates cleanly");
        ois.close();
    }

    // ---- G2. exact IllegalArgumentException message table ----
    static void testTypeMismatchMessages() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(ser(new MsgSample())));
        ois.readObject();
        check(MsgSample.messages.size() == 6, "all six mismatch probes threw");
        check("no such field i with type long".equals(MsgSample.messages.get(0)),
              "long default vs int field: " + MsgSample.messages.get(0));
        check("no such field z with type int".equals(MsgSample.messages.get(1)),
              "int default vs boolean field: " + MsgSample.messages.get(1));
        check("no such field o with type int".equals(MsgSample.messages.get(2)),
              "primitive overload wins resolution: " + MsgSample.messages.get(2));
        check("no such field j with type int".equals(MsgSample.messages.get(3)),
              "int default vs long field: " + MsgSample.messages.get(3));
        check("no such field d with type float".equals(MsgSample.messages.get(4)),
              "float default vs double field: " + MsgSample.messages.get(4));
        check("no such field missing with type class java.lang.Object".equals(MsgSample.messages.get(5)),
              "unknown name, Object overload: " + MsgSample.messages.get(5));
        ois.close();
        // double-sided defaulted() rejection for unknown names (exact message form)
        check(lastDefaultedProbe.contains("no such field"),
              "defaulted(unknown) message: " + lastDefaultedProbe);
    }

    static String lastDefaultedProbe = "not-run";

    // ---- G3. defaulted() is false for every streamed field ----
    static boolean[] marks = new boolean[9];
    static boolean defaultsIgnored;

    static void testDefaultedAllFields() throws Exception {
        for (int k = 0; k < 9; k++) marks[k] = false;
        class Collector implements Serializable {
            private static final long serialVersionUID = 48L; // fixed: cross-version wire stability
            boolean z; byte b; char c; short s; int i; long j; float f; double d; Object o;
            Collector() { z = true; b = 1; c = 2; s = 3; i = 4; j = 5; f = 6; d = 7; o = "8"; }
            private void readObject(ObjectInputStream in)
                    throws IOException, ClassNotFoundException {
                ObjectInputStream.GetField gf = in.readFields();
                marks[0] = gf.defaulted("z"); marks[1] = gf.defaulted("b");
                marks[2] = gf.defaulted("c"); marks[3] = gf.defaulted("s");
                marks[4] = gf.defaulted("i"); marks[5] = gf.defaulted("j");
                marks[6] = gf.defaulted("f"); marks[7] = gf.defaulted("d");
                marks[8] = gf.defaulted("o");
            }
        }
        new ObjectInputStream(new ByteArrayInputStream(ser(new Collector()))).readObject();
        boolean anyDefaulted = false;
        for (int k = 0; k < 9; k++) anyDefaulted |= marks[k];
        check(!anyDefaulted, "no streamed field of any type reports defaulted");
    }

    // ---- G4. descriptor contract ----
    static Object descCapture;

    static void testDescriptorContract() throws Exception {
        class DescProbe implements Serializable {
            private static final long serialVersionUID = 50L; // fixed: cross-version wire stability
            int a = 3; String s = "str";
            private void readObject(ObjectInputStream in)
                    throws IOException, ClassNotFoundException {
                descCapture = in.readFields().getObjectStreamClass();
            }
        }
        new ObjectInputStream(new ByteArrayInputStream(ser(new DescProbe()))).readObject();
        ObjectStreamClass desc = (ObjectStreamClass) descCapture;
        check(desc.getName().equals(DescProbe.class.getName()), "descriptor names the local class");
        check(desc.getSerialVersionUID() == 50L, "descriptor carries the declared SUID");
        check(desc.getFields().length == 2, "descriptor lists the two serializable fields: "
              + desc.getFields().length);
        ObjectStreamClass lookup = ObjectStreamClass.lookup(DescProbe.class);
        check(lookup.getName().equals(desc.getName())
              && lookup.getSerialVersionUID() == desc.getSerialVersionUID(),
              "consistent with ObjectStreamClass.lookup");
        check(ObjectStreamClass.lookup(java.lang.Object.class) == null,
              "lookup of a non-Serializable class returns null");
    }

    // ---- G5. extreme typed values incl. NaN and -0.0 bit patterns ----
    static void testExtremeValues() throws Exception {
        new ObjectInputStream(new ByteArrayInputStream(ser(new ExtremeSample()))).readObject();
        check(rZ, "boolean true restored");
        check(rB == Byte.MIN_VALUE, "byte MIN restored");
        check(rC == Character.MAX_VALUE, "char MAX restored");
        check(rS == Short.MIN_VALUE, "short MIN restored");
        check(rI == Integer.MIN_VALUE, "int MIN restored");
        check(rJ == Long.MIN_VALUE, "long MIN restored");
        check(Float.isNaN(rF), "float NaN restored");
        check(Double.doubleToRawLongBits(rD) == Double.doubleToRawLongBits(-0.0),
              "double -0.0 restored with exact bit pattern");
        check("keep".equals(rO), "object field restored");
        // caller defaults are ignored whenever the field IS present
        defaultsIgnored = false;
        class WithDefaults implements Serializable {
            private static final long serialVersionUID = 51L; // fixed: cross-version wire stability
            int i = 4; String s = "real";
            private void readObject(ObjectInputStream in)
                    throws IOException, ClassNotFoundException {
                ObjectInputStream.GetField gf = in.readFields();
                defaultsIgnored = gf.get("i", 999) == 4 && "real".equals(gf.get("s", "fallback"));
            }
        }
        new ObjectInputStream(new ByteArrayInputStream(ser(new WithDefaults()))).readObject();
        check(defaultsIgnored, "defaults are only used for stream-missing fields, never override stored values");
    }

    // ---- G6. readFields context rules ----
    static void testReadFieldsContextRules() throws Exception {
        Object r = null;
        try {
            new ObjectInputStream(new ByteArrayInputStream(ser(new ReadFieldsTwice()))).readObject();
        } catch (IOException e) {
            r = e;
        }
        check(r == null, "fixture read completes");
        check(ReadFieldsTwice.second.equals(
              "java.io.NotActiveException: not in readObject invocation or fields already read"),
              "second readFields -> NotActiveException: " + ReadFieldsTwice.second);
        check(ReadFieldsTwice.afterFields.equals(
              "java.io.NotActiveException: not in readObject invocation or fields already read"),
              "defaultReadObject after readFields -> NotActiveException: " + ReadFieldsTwice.afterFields);
    }

    // ---- G7. object fields keep graph identity ----
    static void testSharedObjectFields() throws Exception {
        SharedFields sf = new SharedFields();
        ShareNode one = new ShareNode("one");
        sf.x = one;
        sf.y = one;
        new ObjectInputStream(new ByteArrayInputStream(ser(sf))).readObject();
        check(SharedFields.rx instanceof ShareNode && SharedFields.ry instanceof ShareNode,
              "object fields typed correctly");
        check(SharedFields.rx == SharedFields.ry, "shared reference reads back identical via GetField");
        check(((ShareNode) SharedFields.rx).tag.equals("one"), "object field content intact");

        // a field PRESENT in the stream with a null value is distinct from a
        // MISSING field: the former yields null, the latter the caller default
        class NullField implements Serializable {
            private static final long serialVersionUID = 52L; // fixed: cross-version wire stability
            Object present = null;
            private void readObject(ObjectInputStream in)
                    throws IOException, ClassNotFoundException {
                ObjectInputStream.GetField gf = in.readFields();
                nullRead = gf.get("present", "fallback");
                nullDefaulted = gf.defaulted("present");
            }
        }
        new ObjectInputStream(new ByteArrayInputStream(ser(new NullField()))).readObject();
        check(nullRead == null, "present-but-null field returns null, not the default");
        check(!nullDefaulted, "present-but-null field is not defaulted");
    }

    static Object nullRead;
    static boolean nullDefaulted;

    public static void main(String[] args) throws Exception {
        testGetFieldRoundTrip();
        // deepening additions (contract-level)
        testEvolutionDefaults();
        testTypeMismatchMessages();
        testDefaultedAllFields();
        testDescriptorContract();
        testExtremeValues();
        testReadFieldsContextRules();
        testSharedObjectFields();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
