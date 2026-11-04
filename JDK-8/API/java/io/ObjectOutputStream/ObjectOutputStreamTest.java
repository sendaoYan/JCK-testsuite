/*
 * @test
 * @summary java.io.ObjectOutputStream API conformance: full contract coverage.
 *          Object + primitive streams, default/custom writeObject, PutField,
 *          writeUnshared, reset back-references, useProtocolVersion, replaceObject,
 *          annotateClass, NotActiveException, NotSerializableException, close.
 *          Deepening: full round-trip matrix (wrappers/arrays/collections/enums/
 *          Class), static fields, writeReplace/readResolve hooks incl. null and
 *          throwing hooks, Externalizable write path + no-arg-ctor requirement,
 *          UID byte tampering, header flushed by ctor, close propagation rules,
 *          protocol version contract, reset byte growth, unshared pairings,
 *          interleaved DataOutput, NotSerializable matrix, non-serializable
 *          parent, hook signature matching, WriteAbortedException, PutField
 *          lifecycle, writeStreamHeader/writeObjectOverride hooks.
 * @run main ObjectOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamConstants;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.io.WriteAbortedException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

public class ObjectOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    static class Rec implements Serializable {
        private static final long serialVersionUID = 100L;
        int a; String b; long c; double d; boolean e; byte f; char g; short h; float i2;
        transient int t = 999;
        int[] arr;
        Rec(int a, String b) {
            this.a = a; this.b = b; c = 1L << 33; d = 2.25; e = true; f = -5; g = 'Z'; h = 300; i2 = 1.5f;
            arr = new int[]{a, a + 1};
        }
    }

    static class Custom implements Serializable {
        private static final long serialVersionUID = 101L;
        int v;
        int extra = -1;
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            out.writeInt(7000);
        }
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            extra = in.readInt();
        }
    }

    static class PutFieldUser implements Serializable {
        private static final long serialVersionUID = 102L;
        int i;
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("i", i * 2);
            out.writeFields();
        }
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = in.readFields();
            i = gf.get("i", 0);
        }
    }

    static class Holder implements Serializable {
        private static final long serialVersionUID = 103L;
        int v = 1;
    }

    static class Replaceable implements Serializable {
        private static final long serialVersionUID = 104L;
        String tag;
        Replaceable(String tag) { this.tag = tag; }
    }

    /** Subclass enabling replaceObject and recording annotateClass. */
    static class InstrumentedOOS extends ObjectOutputStream {
        String annotated;
        Object replaced;
        InstrumentedOOS(OutputStream out) throws IOException {
            super(out);
            enableReplaceObject(true);
        }
        @Override protected Object replaceObject(Object obj) {
            if (obj instanceof Replaceable) {
                replaced = obj;
                return new Replaceable(((Replaceable) obj).tag + "-replaced");
            }
            return obj;
        }
        @Override protected void annotateClass(Class<?> cl) { annotated = cl.getName(); }
    }

    // ---- 1. full object graph round-trip ----
    static void testObjectRoundTrip() throws Exception {
        Rec back = (Rec) de(ser(new Rec(42, "hi")));
        check(back.a == 42 && "hi".equals(back.b), "int/String fields");
        check(back.c == (1L << 33) && back.d == 2.25 && back.e && back.f == -5
              && back.g == 'Z' && back.h == 300 && back.i2 == 1.5f, "all primitive fields");
        check(back.t == 0, "transient not written (defaults to 0)");
        check(Arrays.equals(back.arr, new int[]{42, 43}), "array field");
    }

    // ---- 2. back-reference vs reset ----
    static void testReset() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Holder h = new Holder();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(h);
        oos.writeObject(h);
        oos.reset();
        oos.writeObject(h);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object x1 = ois.readObject(), x2 = ois.readObject(), x3 = ois.readObject();
        ois.close();
        check(x1 == x2, "duplicate write shares identity (back-reference)");
        check(x1 != x3, "reset() forces a fresh copy");
    }

    // ---- 3. writeUnshared ----
    static void testWriteUnshared() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String shared = "sh";
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeUnshared(shared);
        oos.writeUnshared(shared);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object u1 = ois.readObject(), u2 = ois.readObject();
        ois.close();
        check(u1 != u2 && u1.equals(u2), "writeUnshared copies differ but are equal");
    }

    // ---- 4. primitive block data ----
    static void testPrimitiveBlockData() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeBoolean(true); oos.writeByte(7); oos.writeShort(300); oos.writeChar('A');
        oos.writeInt(-5); oos.writeLong(1L << 40); oos.writeFloat(1.5f); oos.writeDouble(2.25);
        oos.writeUTF("u"); oos.writeBytes("bc"); oos.writeChars("de");
        oos.flush();
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(ois.available() > 0, "block data available before reads");
        check(ois.readBoolean() && ois.readByte() == 7 && ois.readUnsignedShort() == 300
              && ois.readChar() == 'A' && ois.readInt() == -5 && ois.readLong() == (1L << 40)
              && ois.readFloat() == 1.5f && ois.readDouble() == 2.25 && "u".equals(ois.readUTF()),
              "primitive block-data round-trip");
        check(ois.readByte() == 'b' && ois.readByte() == 'c', "writeBytes bytes");
        check(ois.readChar() == 'd' && ois.readChar() == 'e', "writeChars chars");
        check(ois.available() == 0 && ois.read() == -1, "stream exhausted");
        ois.close();
    }

    // ---- 5. custom writeObject/readObject pair ----
    static void testCustomWriteObject() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Custom cu = new Custom();
        cu.v = 3;
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(cu);
        oos.writeInt(1234);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Custom backCu = (Custom) ois.readObject();
        check(backCu.v == 3, "defaultWriteObject wrote the field");
        check(backCu.extra == 7000, "peer readObject consumed the custom writeInt");
        check(ois.readInt() == 1234, "stream-level primitive follows the object");
        ois.close();
    }

    // ---- 6. PutField ----
    static void testPutField() throws Exception {
        PutFieldUser p = new PutFieldUser();
        p.i = 21;
        PutFieldUser pf = (PutFieldUser) de(ser(p));
        check(pf.i == 42, "PutField.put wrote i*2 and readFields restored it");
    }

    // ---- 7. NotActiveException ----
    static void testNotActive() throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
        try { oos.defaultWriteObject(); check(false, "defaultWriteObject must throw"); }
        catch (NotActiveException expected) { passed++; }
        try { oos.putFields(); check(false, "putFields must throw"); }
        catch (NotActiveException expected) { passed++; }
        try { oos.writeFields(); check(false, "writeFields must throw"); }
        catch (NotActiveException expected) { passed++; }
        oos.close();
    }

    // ---- 8. NotSerializableException ----
    static void testNotSerializable() throws Exception {
        try {
            ser(new Object());
            check(false, "writeObject(non-Serializable) must throw");
        } catch (NotSerializableException nse) {
            check("java.lang.Object".equals(nse.getMessage()), "NSE message is class name");
        }
    }

    // ---- 9. replaceObject + annotateClass ----
    static void testReplaceAnnotate() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InstrumentedOOS io = new InstrumentedOOS(bos);
        io.writeObject(new Replaceable("tag"));
        io.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Replaceable r = (Replaceable) ois.readObject();
        ois.close();
        check("tag-replaced".equals(r.tag), "replaceObject swapped the object on write");

        InstrumentedOOS io2 = new InstrumentedOOS(new ByteArrayOutputStream());
        io2.writeObject(new Holder());
        io2.close();
        check(io2.annotated != null && io2.annotated.endsWith("Holder"),
              "annotateClass invoked with the class being written: " + io2.annotated);
    }

    // ---- 10. useProtocolVersion ----
    static void testProtocolVersion() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_1);
        oos.writeObject("legacy");
        oos.close();
        check("legacy".equals(de(bos.toByteArray())), "protocol version 1 round-trip");
    }

    // ---- 11. Class object round-trip ----
    static void testClassObject() throws Exception {
        check(de(ser(String.class)) == String.class, "Class object round-trips");
    }

    // ---- 12. flush/close idempotence ----
    static void testFlushClose() throws Exception {
        ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
        ObjectOutputStream oos3 = new ObjectOutputStream(bos3);
        oos3.writeObject("z");
        oos3.flush();
        oos3.close();
        oos3.close();
        check(bos3.size() > 4, "flush pushed the header+object, close twice safe");
    }

    // ---- 13. header bytes ----
    static void testHeader() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject("z");
        oos.close();
        DataInputStream d = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(d.readShort() == ObjectStreamConstants.STREAM_MAGIC
              && d.readShort() == ObjectStreamConstants.STREAM_VERSION, "wire header constants");
    }

    // ---- 14. ObjectOutput view ----
    static void testObjectOutputView() throws Exception {
        ObjectOutput oo = new ObjectOutputStream(new ByteArrayOutputStream());
        oo.writeObject("io-view");
        oo.writeInt(1);
        oo.flush();
        oo.close();
        passed++;
    }

    // ---- 15. descriptor SUID agreement ----
    static void testSuid() {
        check(ObjectStreamClass.lookup(Rec.class).getSerialVersionUID() == 100L,
              "descriptor SUID agrees with the class declaration");
    }

    // =====================================================================
    // Deepening additions (contract-level). Every POJO is a static nested
    // class with an explicit fixed serialVersionUID so the wire format (and
    // any byte-level surgery on it) is stable across JDK versions.
    // =====================================================================

    interface Snippet { void run() throws Exception; }

    static Throwable capture(Snippet s) {
        try { s.run(); return null; } catch (Throwable t) { return t; }
    }

    /**
     * Overwrites the 8-byte serialVersionUID of the first class descriptor.
     * Layout: [0..3]=AC ED 00 05, [4]=TC_OBJECT(0x73), [5]=TC_CLASSDESC(0x72),
     * [6..7]=name length, name bytes, then the 8-byte serialVersionUID.
     */
    static byte[] tamperUid(byte[] s, String cls, long newUid) {
        if ((s[4] & 0xFF) != ObjectStreamConstants.TC_OBJECT
                || (s[5] & 0xFF) != ObjectStreamConstants.TC_CLASSDESC)
            throw new IllegalStateException("expected TC_OBJECT TC_CLASSDESC at 4/5");
        int nlen = ((s[6] & 0xFF) << 8) | (s[7] & 0xFF);
        String name = new String(s, 8, nlen, java.nio.charset.StandardCharsets.US_ASCII);
        if (!name.equals(cls)) throw new IllegalStateException("wrong class in stream: " + name);
        int uidAt = 8 + nlen;
        byte[] c = s.clone();
        long v = newUid;
        for (int i = 7; i >= 0; i--) { c[uidAt + i] = (byte) v; v >>= 8; }
        return c;
    }

    /** All eight primitive fields at extreme values. */
    static class Pojo8 implements Serializable {
        private static final long serialVersionUID = 4001L; // fixed: cross-version wire stability
        byte b = Byte.MIN_VALUE; short sh = Short.MAX_VALUE; int i = Integer.MIN_VALUE;
        long l = Long.MAX_VALUE; float f = Float.MIN_VALUE; double d = -Double.MAX_VALUE;
        boolean z = true; char c = Character.MAX_VALUE;
    }

    static class StaticS2 implements Serializable {
        private static final long serialVersionUID = 4002L; // fixed: cross-version wire stability
        static int S = 1;
        int x = 5;
    }

    static class SingletonW implements Serializable {
        private static final long serialVersionUID = 4003L; // fixed: cross-version wire stability
        static final SingletonW INSTANCE = new SingletonW();
        private SingletonW() {}
        Object readResolve() { return INSTANCE; }
    }

    static class ResolveNullW implements Serializable {
        private static final long serialVersionUID = 4004L; // fixed: cross-version wire stability
        Object readResolve() { return null; }
    }

    static class HookEx extends ObjectStreamException {
        private static final long serialVersionUID = 4005L; // fixed: cross-version wire stability
        HookEx(String m) { super(m); }
    }

    static class ResolveThrowW implements Serializable {
        private static final long serialVersionUID = 4006L; // fixed: cross-version wire stability
        Object readResolve() throws ObjectStreamException { throw new HookEx("w-resolve-boom"); }
    }

    static class ReplaceStr implements Serializable {
        private static final long serialVersionUID = 4007L; // fixed: cross-version wire stability
        String v;
        ReplaceStr(String v) { this.v = v; }
        Object writeReplace() { return "REPLACED-" + v; }
    }

    static class ReplaceNullW implements Serializable {
        private static final long serialVersionUID = 4008L; // fixed: cross-version wire stability
        Object writeReplace() { return null; }
    }

    static class ReplaceThrowW implements Serializable {
        private static final long serialVersionUID = 4009L; // fixed: cross-version wire stability
        Object writeReplace() throws ObjectStreamException { throw new HookEx("w-replace-boom"); }
    }

    static class ExtB implements Externalizable {
        private static final long serialVersionUID = 4010L; // fixed: cross-version wire stability
        static int ctorCount, weCount;
        int v; String s;
        public ExtB() { ctorCount++; }
        public ExtB(int v, String s) { this.v = v; this.s = s; }
        public void writeExternal(ObjectOutput out) throws IOException { weCount++; out.writeInt(v); out.writeUTF(s); }
        public void readExternal(ObjectInput in) throws IOException { v = in.readInt(); s = in.readUTF(); }
    }

    /** Externalizable without a public no-arg constructor: write succeeds, read must fail. */
    static class ExtBadW implements Externalizable {
        private static final long serialVersionUID = 4011L; // fixed: cross-version wire stability
        int v;
        ExtBadW(int v) { this.v = v; }
        public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(v); }
        public void readExternal(ObjectInput in) throws IOException { v = in.readInt(); }
    }

    static class ExtWriteBoom implements Externalizable {
        private static final long serialVersionUID = 4012L; // fixed: cross-version wire stability
        public ExtWriteBoom() {}
        public void writeExternal(ObjectOutput out) throws IOException { throw new IOException("w-ext-boom"); }
        public void readExternal(ObjectInput in) throws IOException {}
    }

    static class NonSerParent {
        int p;
        NonSerParent() { p = 9; }
    }

    static class ChildSer extends NonSerParent implements Serializable {
        private static final long serialVersionUID = 4013L; // fixed: cross-version wire stability
        int c = 3;
    }

    static class BadField {
        int x = 1;
    }

    static class Good implements Serializable {
        private static final long serialVersionUID = 4014L; // fixed: cross-version wire stability
        BadField b = new BadField();
        int k = 7;
    }

    /** A writeObject with the WRONG signature must never be invoked. */
    static class BadSigWrite implements Serializable {
        private static final long serialVersionUID = 4015L; // fixed: cross-version wire stability
        static int called;
        int f = 42;
        private void writeObject(ObjectInputStream in) throws IOException { called++; }
    }

    static class RtWrite extends RuntimeException {
        private static final long serialVersionUID = 4016L; // fixed: cross-version wire stability
    }

    static class CustomWriteBoom implements Serializable {
        private static final long serialVersionUID = 4017L; // fixed: cross-version wire stability
        private void writeObject(ObjectOutputStream out) throws IOException { throw new IOException("inner-boom"); }
    }

    static class CustomWriteRt implements Serializable {
        private static final long serialVersionUID = 4018L; // fixed: cross-version wire stability
        private void writeObject(ObjectOutputStream out) { throw new RtWrite(); }
    }

    /** putFields() covers only the explicitly put() fields; the rest go out as zeros. */
    static class PutSubsetW implements Serializable {
        private static final long serialVersionUID = 4019L; // fixed: cross-version wire stability
        int a = 1, b = 2;
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("a", 55);
            out.writeFields();
        }
    }

    static class PutStaticW implements Serializable {
        private static final long serialVersionUID = 4020L; // fixed: cross-version wire stability
        static int STAT = 3;
        int a;
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            Throwable t = capture(new Snippet() { public void run() throws Exception { pf.put("STAT", 1); } });
            check(t instanceof IllegalArgumentException
                  && "no such field STAT with type int".equals(t.getMessage()),
                  "put on a static field name rejected: " + t);
            pf.put("a", 1);
            out.writeFields();
        }
    }

    static class FieldsAfterDefaultW implements Serializable {
        private static final long serialVersionUID = 4021L; // fixed: cross-version wire stability
        static String caught;
        int a = 4;
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            Throwable t = capture(new Snippet() { public void run() throws Exception { out.putFields(); } });
            caught = t == null ? "NO-THROW" : t.getClass().getName() + ": " + t.getMessage();
        }
    }

    static class DoubleWriteFields implements Serializable {
        private static final long serialVersionUID = 4022L; // fixed: cross-version wire stability
        int a = 4;
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("a", 4);
            out.writeFields();
            out.writeFields();   // second write: succeeds at write time, corrupts the stream
        }
    }

    static class HolderA implements Serializable {
        private static final long serialVersionUID = 4023L; // fixed: cross-version wire stability
        int v = 1;
    }

    static class HolderB implements Serializable {
        private static final long serialVersionUID = 4024L; // fixed: cross-version wire stability
        int v = 2;
    }

    enum Coin { HEADS, TAILS }

    // ---- W1. null object ----
    static void testNullWrite() throws Exception {
        byte[] nb = ser(null);
        check(nb.length == 5 && (nb[4] & 0xFF) == ObjectStreamConstants.TC_NULL,
              "writeObject(null) emits exactly header + TC_NULL (5 bytes)");
        check(de(nb) == null, "null round-trips");
    }

    // ---- W2. String matrix + byte determinism ----
    static void testStringMatrixWrite() throws Exception {
        check("".equals(de(ser(""))), "empty String");
        String zh = "中文序列化测试";
        check(zh.equals(de(ser(zh))), "Chinese String (modified UTF-8)");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) sb.append((char) ('a' + (i % 26)));
        String long1 = sb.toString();
        check(long1.equals(de(ser(long1))), "10k String");
        check(Arrays.equals(ser(zh), ser(zh)), "identical bytes across two ObjectOutputStreams");
        check(Arrays.equals(ser(long1), ser(long1)), "10k String bytes deterministic");
    }

    // ---- W3. wrapper extremes ----
    static void testWrapperMatrixWrite() throws Exception {
        check(Byte.MIN_VALUE == (Byte) de(ser(Byte.MIN_VALUE)), "Byte.MIN");
        check(Byte.MAX_VALUE == (Byte) de(ser(Byte.MAX_VALUE)), "Byte.MAX");
        check(Short.MIN_VALUE == (Short) de(ser(Short.MIN_VALUE)), "Short.MIN");
        check(Short.MAX_VALUE == (Short) de(ser(Short.MAX_VALUE)), "Short.MAX");
        check(Integer.MIN_VALUE == (Integer) de(ser(Integer.MIN_VALUE)), "Integer.MIN");
        check(Integer.MAX_VALUE == (Integer) de(ser(Integer.MAX_VALUE)), "Integer.MAX");
        check(Long.MIN_VALUE == (Long) de(ser(Long.MIN_VALUE)), "Long.MIN");
        check(Long.MAX_VALUE == (Long) de(ser(Long.MAX_VALUE)), "Long.MAX");
        check(Boolean.TRUE.equals(de(ser(Boolean.TRUE))), "Boolean.TRUE (fresh instance on read)");
        check(Character.MIN_VALUE == (Character) de(ser(Character.MIN_VALUE)), "Character.MIN");
        check(Character.MAX_VALUE == (Character) de(ser(Character.MAX_VALUE)), "Character.MAX");
        check(Double.isNaN((Double) de(ser(Double.NaN))), "Double.NaN");
        check(Double.doubleToRawLongBits((Double) de(ser(-0.0)))
              == Double.doubleToRawLongBits(-0.0), "Double -0.0 bit pattern");
        check(Double.POSITIVE_INFINITY == (Double) de(ser(Double.POSITIVE_INFINITY)), "Double +Inf");
        check(Double.NEGATIVE_INFINITY == (Double) de(ser(Double.NEGATIVE_INFINITY)), "Double -Inf");
        check(Float.isNaN((Float) de(ser(Float.NaN))), "Float.NaN");
        check(Float.floatToRawIntBits((Float) de(ser(-0.0f)))
              == Float.floatToRawIntBits(-0.0f), "Float -0.0 bit pattern");
        check(Float.MIN_VALUE == (Float) de(ser(Float.MIN_VALUE)), "Float.MIN_VALUE");
    }

    // ---- W4. primitive POJO ----
    static void testPrimitivePojoWrite() throws Exception {
        Pojo8 p = (Pojo8) de(ser(new Pojo8()));
        check(p.b == Byte.MIN_VALUE && p.sh == Short.MAX_VALUE, "byte/short extremes written");
        check(p.i == Integer.MIN_VALUE && p.l == Long.MAX_VALUE, "int/long extremes written");
        check(p.f == Float.MIN_VALUE && p.d == -Double.MAX_VALUE, "float/double extremes written");
        check(p.z && p.c == Character.MAX_VALUE, "boolean/char extremes written");
    }

    // ---- W5. arrays ----
    static void testArraysWrite() throws Exception {
        byte[] all = new byte[256];
        for (int i = 0; i < 256; i++) all[i] = (byte) i;
        check(Arrays.equals((byte[]) de(ser(all)), all), "byte[0..255]");
        int[] ex = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE};
        check(Arrays.equals((int[]) de(ser(ex)), ex), "int[] extremes");
        Object[] objs = {null, "s", null, 5};
        Object[] back = (Object[]) de(ser(objs));
        check(back.length == 4 && back[0] == null && "s".equals(back[1]) && back[2] == null
              && Integer.valueOf(5).equals(back[3]), "Object[] with null elements");
        String[] strs = {"a", null, "c"};
        String[] sback = (String[]) de(ser(strs));
        check(sback.length == 3 && sback[1] == null, "String[] with null element");
        check(((byte[]) de(ser(new byte[0]))).length == 0, "zero-length byte[]");
        check(((Object[]) de(ser(new Object[0]))).length == 0, "zero-length Object[]");
        int[][] md = (int[][]) de(ser(new int[2][3]));
        check(md.length == 2 && md[0].length == 3 && md[1].length == 3, "int[2][3] shape");
        byte[] orig = {9, 8, 7};
        check(de(ser(orig)) != orig, "read-back array is a fresh instance");
    }

    // ---- W6. collections ----
    static void testCollectionsWrite() throws Exception {
        List<String> al = new ArrayList<String>();
        al.add("x"); al.add(null); al.add("y");
        @SuppressWarnings("unchecked")
        List<String> alBack = (List<String>) de(ser(al));
        check(alBack.size() == 3 && alBack.get(1) == null, "ArrayList with null element");

        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("a", "1"); hm.put("b", null); hm.put(null, "4");
        @SuppressWarnings("unchecked")
        HashMap<String, String> hmBack = (HashMap<String, String>) de(ser(hm));
        check(hmBack.equals(hm), "HashMap incl. null key/value");

        TreeMap<String, Integer> tm = new TreeMap<String, Integer>();
        tm.put("m", 1); tm.put("a", 2); tm.put("z", 3); tm.put("b", 4);
        @SuppressWarnings("unchecked")
        TreeMap<String, Integer> tmBack = (TreeMap<String, Integer>) de(ser(tm));
        check(Arrays.equals(tmBack.keySet().toArray(new String[0]), new String[]{"a", "b", "m", "z"}),
              "TreeMap sorted order preserved");

        LinkedHashMap<String, Integer> lhm = new LinkedHashMap<String, Integer>();
        lhm.put("3", 3); lhm.put("1", 1); lhm.put("2", 2);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Integer> lhmBack = (LinkedHashMap<String, Integer>) de(ser(lhm));
        check(Arrays.equals(lhmBack.keySet().toArray(new String[0]), new String[]{"3", "1", "2"}),
              "LinkedHashMap insertion order preserved");

        HashSet<String> hs = new HashSet<String>();
        hs.add("p"); hs.add("q");
        check(de(ser(hs)).equals(hs), "HashSet");

        LinkedList<Integer> ll = new LinkedList<Integer>();
        ll.add(1); ll.add(null); ll.add(3);
        @SuppressWarnings("unchecked")
        LinkedList<Integer> llBack = (LinkedList<Integer>) de(ser(ll));
        check(llBack.size() == 3 && llBack.get(1) == null, "LinkedList with null element");

        Properties pr = new Properties();
        pr.setProperty("k1", "v1");
        check("v1".equals(((Properties) de(ser(pr))).getProperty("k1")), "Properties");

        check(((Date) de(ser(new Date(1234567890123L)))).getTime() == 1234567890123L, "Date fixed millis");
        BigInteger bi = new BigInteger("123456789012345678901234567890123456789012345678901234567890");
        check(bi.equals(de(ser(bi))), "BigInteger exact");
        BigDecimal bd = new BigDecimal("12345.6789012345678901234567890123456789");
        check(bd.equals(de(ser(bd))), "BigDecimal incl. scale");
    }

    // ---- W7. enums: constant identity + 5-byte back-reference ----
    static void testEnumWrite() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(Coin.HEADS);
        int first = bos.size();
        oos.writeObject(Coin.HEADS);
        check(bos.size() - first == 5,
              "second write of the same constant adds exactly TC_REFERENCE+handle (5 bytes)");
        oos.writeObject(Coin.TAILS);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object h1 = ois.readObject(), h2 = ois.readObject(), t3 = ois.readObject();
        check(h1 == Coin.HEADS && h2 == Coin.HEADS && h1 == h2, "constant resolves by name (==)");
        check(t3 == Coin.TAILS, "second constant distinct");
        ois.close();
    }

    // ---- W8. Class objects ----
    static void testClassWrite() throws Exception {
        check(de(ser(String.class)) == String.class, "String.class identity");
        check(de(ser(Integer.class)) == Integer.class, "Integer.class identity");
        check(de(ser(int[].class)) == int[].class, "array Class identity");
    }

    // ---- W9. static fields are not written ----
    static void testStaticFieldsWrite() throws Exception {
        StaticS2.S = 1;
        byte[] b = ser(new StaticS2());
        StaticS2.S = 2;
        StaticS2 back = (StaticS2) de(b);
        check(back.x == 5, "instance field written/restored");
        check(StaticS2.S == 2, "static keeps read-time value (never part of the stream)");
    }

    // ---- W10. writeReplace contract ----
    static void testWriteReplaceContract() throws Exception {
        Object o = de(ser(new ReplaceStr("x")));
        check(o instanceof String && "REPLACED-x".equals(o), "replaced object replaces the streamed type");
        check(de(ser(new ReplaceNullW())) == null, "writeReplace returning null writes TC_NULL");

        Object t = capture(new Snippet() { public void run() throws Exception { ser(new ReplaceThrowW()); } });
        check(t instanceof HookEx && "w-replace-boom".equals(((Throwable) t).getMessage()),
              "writeReplace exception propagates unwrapped: " + t);

        // writeReplace runs once per substituted instance: the replacement is
        // itself back-referenced on the second write
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        ReplaceStr r = new ReplaceStr("y");
        oos.writeObject(r);
        oos.writeObject(r);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object a1 = ois.readObject(), a2 = ois.readObject();
        check("REPLACED-y".equals(a1) && a1 == a2,
              "the replacement is registered and back-referenced (== on read)");
        ois.close();
    }

    // ---- W11. readResolve seen from the write side ----
    static void testReadResolveWriteSide() throws Exception {
        check(de(ser(SingletonW.INSTANCE)) == SingletonW.INSTANCE, "readResolve yields the singleton (==)");
        check(de(ser(new ResolveNullW())) == null, "readResolve returning null yields null");
        Object t = capture(new Snippet() { public void run() throws Exception { de(ser(new ResolveThrowW())); } });
        check(t instanceof HookEx && "w-resolve-boom".equals(((Throwable) t).getMessage()),
              "readResolve exception propagates unwrapped from readObject: " + t);
    }

    // ---- W12. Externalizable write path ----
    static void testExternalizableWrite() throws Exception {
        ExtB.ctorCount = 0; ExtB.weCount = 0;
        byte[] b = ser(new ExtB(5, "five"));
        check(ExtB.ctorCount == 0, "writing never runs the constructor");
        check(ExtB.weCount == 1, "writeExternal invoked exactly once");
        ExtB back = (ExtB) de(b);
        check(back.v == 5 && "five".equals(back.s), "external data restored");

        Object t = capture(new Snippet() { public void run() throws Exception { ser(new ExtWriteBoom()); } });
        check(t instanceof IOException && "w-ext-boom".equals(((Throwable) t).getMessage()),
              "writeExternal exception propagates unwrapped from writeObject: " + t);
    }

    // ---- W13. Externalizable without public no-arg ctor ----
    static void testExternalizableNoCtorWrite() throws Exception {
        byte[] b = ser(new ExtBadW(5));
        check(b.length > 0, "write side succeeds (constructor checked only on deserialization)");
        Object t = capture(new Snippet() { public void run() throws Exception { de(b); } });
        check(t instanceof InvalidClassException
              && ((Throwable) t).getMessage() != null
              && ((Throwable) t).getMessage().contains("no valid constructor"),
              "read side: InvalidClassException 'no valid constructor': " + t);
    }

    // ---- W14. serialVersionUID mismatch via byte tampering ----
    static void testUidTamperStream() throws Exception {
        check(ObjectStreamClass.lookup(Rec.class).getSerialVersionUID() == 100L,
              "descriptor SUID agrees with the class declaration");
        byte[] b = ser(new Rec(1, "uid"));
        final byte[] tampered = tamperUid(b, Rec.class.getName(), 0xDEADBEEFCAFEL);
        Object t = capture(new Snippet() { public void run() throws Exception { de(tampered); } });
        check(t instanceof InvalidClassException, "tampered UID -> InvalidClassException: " + t);
        String m = ((InvalidClassException) t).getMessage();
        check(m != null && m.contains("local class incompatible"), "incompatibility message: " + m);
        check(m != null && m.contains("244837814094590"), "streamed (tampered) UID reported: " + m);
        check(m != null && m.contains("100"), "local UID 100 reported: " + m);
    }

    // ---- W15. the header is flushed by the constructor ----
    static void testHeaderSunkImmediately() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        byte[] sunk = bos.toByteArray();
        check(sunk.length == 4, "constructor sinks exactly 4 bytes immediately");
        check((sunk[0] & 0xFF) == 0xAC && (sunk[1] & 0xFF) == 0xED
              && (sunk[2] & 0xFF) == 0x00 && (sunk[3] & 0xFF) == 0x05,
              "sunk bytes are AC ED 00 05 (STREAM_MAGIC/STREAM_VERSION)");
        oos.close();
    }

    // ---- W16. close semantics: no built-in flag; propagation only ----
    static void testWriteAfterClose() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject("x");
        oos.close();
        int before = bos.size();
        Object a = capture(new Snippet() { public void run() throws Exception { oos.writeObject("y"); } });
        Object b = capture(new Snippet() { public void run() throws Exception { oos.writeInt(1); } });
        Object c = capture(new Snippet() { public void run() throws Exception { oos.flush(); } });
        check(a == null && b == null && c == null && bos.size() > before,
              "after close(), writes into a live ByteArrayOutputStream still succeed (no internal closed flag)");
        OutputStream refusing = new OutputStream() {
            boolean shut;
            @Override public void write(int x) throws IOException {
                if (shut) throw new IOException("underlying closed");
            }
            @Override public void write(byte[] bb, int off, int len) throws IOException {
                if (shut) throw new IOException("underlying closed");
            }
            @Override public void close() { shut = true; }
        };
        ObjectOutputStream ro = new ObjectOutputStream(refusing);
        ro.writeObject("x");
        ro.close();
        Object t = capture(new Snippet() { public void run() throws Exception { ro.writeObject("y"); } });
        check(t instanceof IOException && "underlying closed".equals(((Throwable) t).getMessage()),
              "failures only surface when the underlying stream refuses: " + t);
    }

    // ---- W17. useProtocolVersion contract ----
    static void testProtocolVersionContract() throws Exception {
        ObjectOutputStream late = new ObjectOutputStream(new ByteArrayOutputStream());
        late.writeObject("first");
        Object a = capture(new Snippet() { public void run() throws Exception {
            late.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_1);
        } });
        check(a instanceof IllegalStateException && "stream non-empty".equals(((Throwable) a).getMessage()),
              "useProtocolVersion after a write -> IllegalStateException 'stream non-empty': " + a);
        late.close();

        Object b = capture(new Snippet() { public void run() throws Exception {
            new ObjectOutputStream(new ByteArrayOutputStream()).useProtocolVersion(99);
        } });
        check(b instanceof IllegalArgumentException && "unknown version: 99".equals(((Throwable) b).getMessage()),
              "unknown version -> IllegalArgumentException 'unknown version: 99': " + b);

        // both protocols carry objects, custom data and primitives
        for (int proto : new int[]{ObjectStreamConstants.PROTOCOL_VERSION_1,
                                   ObjectStreamConstants.PROTOCOL_VERSION_2}) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.useProtocolVersion(proto);
            oos.writeObject(new Custom());
            oos.writeInt(31);
            oos.writeObject("tail");
            oos.close();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            Custom cu = (Custom) ois.readObject();
            check(cu.v == 0 && cu.extra == 7000, "proto " + proto + ": default + custom data restored");
            check(ois.readInt() == 31 && "tail".equals(ois.readObject()),
                  "proto " + proto + ": stream-level data after the object");
            ois.close();
        }
    }

    // ---- W18. reset() semantics ----
    static void testResetByteGrowth() throws Exception {
        Holder h = new Holder();
        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        ObjectOutputStream p = new ObjectOutputStream(plain);
        p.writeObject(h);
        p.writeObject(h);
        p.close();
        ByteArrayOutputStream withReset = new ByteArrayOutputStream();
        ObjectOutputStream r = new ObjectOutputStream(withReset);
        r.writeObject(h);
        r.writeObject(h);
        r.reset();
        r.writeObject(h);
        r.reset();                     // reset twice in a row is allowed
        r.writeObject("after");
        r.close();
        check(withReset.size() > plain.size(),
              "reset() forces a fresh copy so the stream grows (" + plain.size() + " -> " + withReset.size() + ")");
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(withReset.toByteArray()));
        Object x1 = ois.readObject(), x2 = ois.readObject(), x3 = ois.readObject();
        check(x1 == x2, "before reset: back-reference identity");
        check(x1 != x3, "after reset: fresh copy");
        check("after".equals(ois.readObject()), "stream usable after reset");
        ois.close();
    }

    // ---- W19. writeUnshared pairings ----
    static void testWriteUnsharedDetails() throws Exception {
        String sh = "same";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeUnshared(sh);
        oos.writeObject(sh);
        oos.writeObject(sh);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object u1 = ois.readObject(), o2 = ois.readObject(), o3 = ois.readObject();
        check(u1 != o2, "writeUnshared: no back-reference to the unshared copy");
        check(o2 == o3, "subsequent writeObject shares its own handle");
        check("same".equals(u1) && "same".equals(o2), "values equal");
        ois.close();

        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        ObjectOutputStream w2 = new ObjectOutputStream(b2);
        w2.writeObject(sh);
        w2.writeObject(sh);
        w2.writeUnshared(sh);
        w2.close();
        ObjectInputStream r2 = new ObjectInputStream(new ByteArrayInputStream(b2.toByteArray()));
        Object q1 = r2.readObject(), q2 = r2.readObject(), q3 = r2.readUnshared();
        check(q1 == q2, "normal writes back-reference");
        check(q3 != q1 && "same".equals(q3), "trailing writeUnshared emits a fresh copy");
        r2.close();
    }

    // ---- W20. DataOutput writes interleave with objects ----
    static void testInterleavedDataOutput() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeInt(5);
        oos.writeObject("mid");
        oos.writeUTF("tail");
        oos.writeDouble(1.25);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(ois.readInt() == 5, "primitive first");
        check("mid".equals(ois.readObject()), "object second");
        check("tail".equals(ois.readUTF()) && ois.readDouble() == 1.25, "primitives after");
        check(ois.read() == -1, "stream exhausted");
        ois.close();

        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        ObjectOutputStream w2 = new ObjectOutputStream(b2);
        w2.writeObject("first");
        w2.writeInt(9);
        w2.close();
        ObjectInputStream r2 = new ObjectInputStream(new ByteArrayInputStream(b2.toByteArray()));
        check("first".equals(r2.readObject()) && r2.readInt() == 9, "object then primitive");
        r2.close();
    }

    // ---- W21. NotSerializableException matrix ----
    static void testNotSerializableMatrix() throws Exception {
        Object a = capture(new Snippet() { public void run() throws Exception { ser(new Object()); } });
        check(a instanceof NotSerializableException && "java.lang.Object".equals(((Throwable) a).getMessage()),
              "plain Object -> NSE with FQCN message");
        Object b = capture(new Snippet() { public void run() throws Exception { ser(new Good()); } });
        check(b instanceof NotSerializableException
              && ((Throwable) b).getMessage().endsWith("BadField"),
              "non-serializable field type -> NSE naming the field's class: " + b);
        final HashMap<String, Object> bad = new HashMap<String, Object>();
        bad.put("k", new Object());
        Object c = capture(new Snippet() { public void run() throws Exception { ser(bad); } });
        check(c instanceof NotSerializableException && "java.lang.Object".equals(((Throwable) c).getMessage()),
              "HashMap value -> NSE naming the value class");
        Object d = capture(new Snippet() { public void run() throws Exception {
            ser(new Object[]{new Object()});
        } });
        check(d instanceof NotSerializableException && "java.lang.Object".equals(((Throwable) d).getMessage()),
              "array element -> NSE naming the element class");
    }

    // ---- W22. non-serializable parent of a serializable child ----
    static void testNonSerializableParent() throws Exception {
        ChildSer ch = new ChildSer();
        ch.p = 100;
        ch.c = 200;
        ChildSer back = (ChildSer) de(ser(ch));
        check(back.c == 200, "child (serializable) field preserved");
        check(back.p == 9, "parent field re-initialized by its no-arg ctor, not the pre-write value 100");
    }

    // ---- W23. writeObject signature matching + abort behavior ----
    static void testCustomWriteSignatures() throws Exception {
        BadSigWrite.called = 0;
        BadSigWrite back = (BadSigWrite) de(ser(new BadSigWrite()));
        check(BadSigWrite.called == 0, "writeObject with wrong parameter type is never invoked");
        check(back.f == 42, "default mechanism still writes the field");

        // correct signature IS invoked (the existing Custom fixture proves it: extra==7000)
        check(((Custom) de(ser(new Custom()))).extra == 7000, "correctly signed writeObject invoked");

        Object rt = capture(new Snippet() { public void run() throws Exception { ser(new CustomWriteRt()); } });
        check(rt instanceof RtWrite, "RuntimeException propagates unwrapped from writeObject: " + rt);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try { oos.writeObject(new CustomWriteRt()); } catch (RuntimeException expected) { passed++; }
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object eof = capture(new Snippet() { public void run() throws Exception { ois.readObject(); } });
        check(eof instanceof EOFException,
              "RuntimeException abort leaves no WriteAborted marker; read hits EOF: " + eof);
        ois.close();
    }

    // ---- W24. WriteAbortedException for IOException aborts ----
    static void testWriteAbortedContract() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(new CustomWriteBoom());
            check(false, "custom writeObject throwing IOException must fail the write");
        } catch (IOException expected) { passed++; }
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        try {
            ois.readObject();
            check(false, "aborted write must surface on read");
        } catch (WriteAbortedException wae) {
            check(wae.getMessage() != null && wae.getMessage().startsWith("writing aborted; "),
                  "message prefix 'writing aborted; ': " + wae.getMessage());
            check(wae.getCause() instanceof IOException
                  && "inner-boom".equals(wae.getCause().getMessage()),
                  "getCause carries the original write-side exception");
        }
        ois.close();
    }

    // ---- W25. PutField lifecycle ----
    static void testPutFieldsLifecycle() throws Exception {
        PutSubsetW ps = (PutSubsetW) de(ser(new PutSubsetW()));
        check(ps.a == 55, "explicitly put value written");
        check(ps.b == 0, "field not put() goes out as zero (PutField starts zeroed)");

        check(de(ser(new PutStaticW())) instanceof PutStaticW,
              "PutField rejects static field names but completes the write");

        Object t = capture(new Snippet() { public void run() throws Exception {
            de(ser(new FieldsAfterDefaultW()));
        } });
        check(t == null && FieldsAfterDefaultW.caught.equals(
              "java.io.NotActiveException: not in readObject invocation or fields already read"),
              "putFields after defaultWriteObject -> NotActiveException: " + FieldsAfterDefaultW.caught);

        Object d = capture(new Snippet() { public void run() throws Exception {
            de(ser(new DoubleWriteFields()));
        } });
        check(d instanceof StreamCorruptedException,
              "double writeFields(): the write succeeds but the stream is unreadable: " + d);

        // putFields() twice returns the same buffer
        class TwicePut implements Serializable {
            private static final long serialVersionUID = 4025L; // fixed: cross-version wire stability
            int a;
            private void writeObject(ObjectOutputStream out) throws IOException {
                ObjectOutputStream.PutField f1 = out.putFields();
                ObjectOutputStream.PutField f2 = out.putFields();
                samePutField = (f1 == f2);
                f1.put("a", 6);
                out.writeFields();
            }
        }
        TwicePut tp = (TwicePut) de(ser(new TwicePut()));
        check(samePutField, "putFields() twice returns the same buffer");
        check(tp.a == 6, "value put through the first reference lands in the stream");
    }

    static boolean samePutField;

    // ---- W26. writeStreamHeader hook ----
    static void testWriteStreamHeaderHook() throws Exception {
        class HdrOOS extends ObjectOutputStream {
            HdrOOS(OutputStream os) throws IOException { super(os); }
            @Override protected void writeStreamHeader() throws IOException {
                writeShort(0x1234); writeShort(0x5678);
            }
        }
        class HdrOIS extends ObjectInputStream {
            HdrOIS(java.io.InputStream is) throws IOException { super(is); }
            @Override protected void readStreamHeader() throws IOException {
                short a = readShort(), b = readShort();
                if (a != 0x1234 || b != 0x5678) throw new StreamCorruptedException("custom header mismatch");
            }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HdrOOS ho = new HdrOOS(bos);
        ho.writeObject("hh");
        ho.close();
        byte[] b = bos.toByteArray();
        check((b[0] & 0xFF) == 0x12 && (b[1] & 0xFF) == 0x34
              && (b[2] & 0xFF) == 0x56 && (b[3] & 0xFF) == 0x78,
              "custom header bytes land at the head of the stream");
        HdrOIS hi = new HdrOIS(new ByteArrayInputStream(b));
        check("hh".equals(hi.readObject()), "matching readStreamHeader round-trips");
        hi.close();
        Object t = capture(new Snippet() { public void run() throws Exception {
            new ObjectInputStream(new ByteArrayInputStream(b)).readObject();
        } });
        check(t instanceof StreamCorruptedException
              && "invalid stream header: 12345678".equals(((Throwable) t).getMessage()),
              "default reader rejects the custom header: " + t);
    }

    // ---- W27. writeObjectOverride hook (protected constructor) ----
    static void testWriteObjectOverrideHook() throws Exception {
        class OverrideOOS extends ObjectOutputStream {
            Object seen;
            OverrideOOS() throws IOException { super(); }   // protected ctor: no underlying stream
            @Override protected void writeObjectOverride(Object obj) { seen = obj; }
        }
        OverrideOOS oo = new OverrideOOS();
        oo.writeObject("ovr");
        check("ovr".equals(oo.seen), "writeObjectOverride takes over writeObject");
        // note: reset()/close() on a protected-ctor OOS that never opened a
        // stream NPE internally (no drain buffer) - do not call them.
    }

    // ---- W28. replaceObject visits nested graph objects ----
    static void testReplaceObjectNested() throws Exception {
        class CountingReplace extends ObjectOutputStream {
            int strings;
            Object replacement;
            CountingReplace(OutputStream os) throws IOException { super(os); enableReplaceObject(true); }
            @Override protected Object replaceObject(Object obj) {
                if (obj instanceof String) strings++;
                if ("secret".equals(obj)) { replacement = "PUBLIC"; return "PUBLIC"; }
                return obj;
            }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CountingReplace cr = new CountingReplace(bos);
        cr.writeObject(new PutFieldUser());   // no nested strings
        cr.writeObject(java.util.Arrays.asList("a", "b"));
        cr.writeObject("secret");
        cr.close();
        check(cr.strings == 3, "replaceObject offered every nested String: " + cr.strings);
        check("PUBLIC".equals(cr.replacement), "replacement returned by the hook");
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ois.readObject();
        @SuppressWarnings("unchecked")
        List<String> l = (List<String>) ois.readObject();
        check(l.equals(java.util.Arrays.asList("a", "b")), "unreplaced values pass through");
        check("PUBLIC".equals(ois.readObject()), "replacement is what lands in the stream");
        ois.close();
    }

    // ---- W29. annotateClass runs once per distinct class descriptor ----
    static void testAnnotateClassPerDistinctClass() throws Exception {
        class CountingAnnotate extends ObjectOutputStream {
            int annotations;
            CountingAnnotate(OutputStream os) throws IOException { super(os); }
            @Override protected void annotateClass(Class<?> cl) { annotations++; }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CountingAnnotate ca = new CountingAnnotate(bos);
        ca.writeObject(new HolderA());
        ca.writeObject(new HolderA());
        check(ca.annotations == 1, "same class twice: descriptor back-referenced, annotated once");
        ca.writeObject(new HolderB());
        check(ca.annotations == 2, "distinct class annotated exactly once more");
        ca.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(ois.readObject() instanceof HolderA && ois.readObject() instanceof HolderA
              && ois.readObject() instanceof HolderB, "annotated stream still readable");
        ois.close();
    }

    public static void main(String[] args) throws Exception {
        testObjectRoundTrip();
        testReset();
        testWriteUnshared();
        testPrimitiveBlockData();
        testCustomWriteObject();
        testPutField();
        testNotActive();
        testNotSerializable();
        testReplaceAnnotate();
        testProtocolVersion();
        testClassObject();
        testFlushClose();
        testHeader();
        testObjectOutputView();
        testSuid();
        // deepening additions (contract-level)
        testNullWrite();
        testStringMatrixWrite();
        testWrapperMatrixWrite();
        testPrimitivePojoWrite();
        testArraysWrite();
        testCollectionsWrite();
        testEnumWrite();
        testClassWrite();
        testStaticFieldsWrite();
        testWriteReplaceContract();
        testReadResolveWriteSide();
        testExternalizableWrite();
        testExternalizableNoCtorWrite();
        testUidTamperStream();
        testHeaderSunkImmediately();
        testWriteAfterClose();
        testProtocolVersionContract();
        testResetByteGrowth();
        testWriteUnsharedDetails();
        testInterleavedDataOutput();
        testNotSerializableMatrix();
        testNonSerializableParent();
        testCustomWriteSignatures();
        testWriteAbortedContract();
        testPutFieldsLifecycle();
        testWriteStreamHeaderHook();
        testWriteObjectOverrideHook();
        testReplaceObjectNested();
        testAnnotateClassPerDistinctClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
