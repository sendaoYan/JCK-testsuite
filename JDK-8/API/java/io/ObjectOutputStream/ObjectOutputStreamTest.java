/*
 * @test
 * @summary java.io.ObjectOutputStream API conformance: full contract coverage.
 *          Object + primitive streams, default/custom writeObject, PutField,
 *          writeUnshared, reset back-references, useProtocolVersion, replaceObject,
 *          annotateClass, NotActiveException, NotSerializableException, close.
 * @run main ObjectOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
