/*
 * @test
 * @summary java.io.ObjectOutputStream conformance: white-box structure + black-box behavior
 *          (object + primitive streams, default/custom writeObject, PutField,
 *          writeUnshared, reset back-references, useProtocolVersion, replaceObject,
 *          annotateClass, NotActiveException, NotSerializableException, close).
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
import java.io.Serial;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ObjectOutputStreamTest {
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

    static Object de(byte[] b) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return ois.readObject();
        }
    }

    static class Rec implements Serializable {
        @Serial
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
        @Serial
        private static final long serialVersionUID = 101L;
        int v;
        int extra = -1;
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            out.writeInt(7000);            // custom primitive data after the default form
        }
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            extra = in.readInt();          // peer reads exactly what writeObject appended
        }
    }

    static class PutFieldUser implements Serializable {
        @Serial
        private static final long serialVersionUID = 102L;
        int i;
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField pf = out.putFields();
            pf.put("i", i * 2);            // transform on the way out
            out.writeFields();
        }
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = in.readFields();
            i = gf.get("i", 0);
        }
    }

    static class Holder implements Serializable {
        @Serial
        private static final long serialVersionUID = 103L;
        int v = 1;
    }

    static class Replaceable implements Serializable {
        @Serial
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
        @Override protected boolean enableReplaceObject(boolean enable) { return super.enableReplaceObject(enable); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectOutputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.io.OutputStream"), "extends OutputStream");
        boolean objOut = false;
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals("java.io.ObjectOutput")) objOut = true;
        check(objOut, "implements ObjectOutput");
        boolean osc = false;
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals("java.io.ObjectStreamConstants")) osc = true;
        check(osc, "implements ObjectStreamConstants");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()),
              "protected no-arg ctor for subclasses");
        for (String m : new String[]{"writeObject", "writeUnshared", "defaultWriteObject",
                                     "putFields", "writeFields", "reset", "useProtocolVersion",
                                     "writeBoolean", "writeByte", "writeShort", "writeChar",
                                     "writeInt", "writeLong", "writeFloat", "writeDouble",
                                     "writeUTF", "writeBytes", "writeChars", "write",
                                     "flush", "close", "annotateClass", "replaceObject",
                                     "writeStreamHeader", "drain"})
            check(hasSome(c, m), "has method " + m);
    }

    static boolean hasSome(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        for (java.lang.reflect.Method mm : c.getDeclaredMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. full object graph round-trip incl. every primitive kind, String and array field
        Rec back = (Rec) de(ser(new Rec(42, "hi")));
        check(back.a == 42 && "hi".equals(back.b), "int/String fields");
        check(back.c == (1L << 33) && back.d == 2.25 && back.e && back.f == -5
              && back.g == 'Z' && back.h == 300 && back.i2 == 1.5f, "all primitive fields");
        check(back.t == 0, "transient not written (defaults to 0)");
        check(Arrays.equals(back.arr, new int[]{42, 43}), "array field");

        // 2. same object written twice => back-reference; reset() breaks the table
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Holder h = new Holder();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(h);
            oos.writeObject(h);
            oos.reset();
            oos.writeObject(h);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Object x1 = ois.readObject(), x2 = ois.readObject(), x3 = ois.readObject();
            check(x1 == x2, "duplicate write shares identity (back-reference)");
            check(x1 != x3, "reset() forces a fresh copy");
        }

        // 3. writeUnshared writes a fresh copy even for the same instance
        bos = new ByteArrayOutputStream();
        String shared = "sh";
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeUnshared(shared);
            oos.writeUnshared(shared);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Object u1 = ois.readObject(), u2 = ois.readObject();
            check(u1 != u2 && u1.equals(u2), "writeUnshared copies differ but are equal");
        }

        // 4. primitive block data: write the whole DataOutput surface, read it back,
        //    and confirm the stream is framed (4-byte header + block data visible)
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeBoolean(true); oos.writeByte(7); oos.writeShort(300); oos.writeChar('A');
            oos.writeInt(-5); oos.writeLong(1L << 40); oos.writeFloat(1.5f); oos.writeDouble(2.25);
            oos.writeUTF("u"); oos.writeBytes("bc"); oos.writeChars("de");
            oos.flush();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check(ois.available() > 0, "block data available before reads");
            check(ois.readBoolean() && ois.readByte() == 7 && ois.readUnsignedShort() == 300
                  && ois.readChar() == 'A' && ois.readInt() == -5 && ois.readLong() == (1L << 40)
                  && ois.readFloat() == 1.5f && ois.readDouble() == 2.25 && "u".equals(ois.readUTF()),
                  "primitive block-data round-trip");
            check(ois.readByte() == 'b' && ois.readByte() == 'c', "writeBytes bytes");
            check(ois.readChar() == 'd' && ois.readChar() == 'e', "writeChars chars");
            check(ois.available() == 0 && ois.read() == -1, "stream exhausted");
        }

        // 5. custom writeObject/readObject pair: default form + trailing custom primitive
        bos = new ByteArrayOutputStream();
        Custom cu = new Custom();
        cu.v = 3;
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(cu);
            oos.writeInt(1234);            // stream-level primitive after the object
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Custom backCu = (Custom) ois.readObject();
            check(backCu.v == 3, "defaultWriteObject wrote the field");
            check(backCu.extra == 7000, "peer readObject consumed the custom writeInt");
            check(ois.readInt() == 1234, "stream-level primitive follows the object");
        }

        // 6. PutField: write transformed values through the typed put() surface
        PutFieldUser pf = (PutFieldUser) de(ser(newPutField(21)));
        check(pf.i == 42, "PutField.put wrote i*2 and readFields restored it");

        // 7. NotActiveException: defaultWriteObject/putFields/writeFields outside an
        //    active writeObject call
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            try { oos.defaultWriteObject(); check(false, "defaultWriteObject must throw"); }
            catch (NotActiveException expected) { passed++; }
            try { oos.putFields(); check(false, "putFields must throw"); }
            catch (NotActiveException expected) { passed++; }
            try { oos.writeFields(); check(false, "writeFields must throw"); }
            catch (NotActiveException expected) { passed++; }
        }

        // 8. NotSerializableException for a non-Serializable object, message = class name
        try {
            ser(new Object());
            check(false, "writeObject(non-Serializable) must throw");
        } catch (NotSerializableException nse) {
            check("java.lang.Object".equals(nse.getMessage()), "NSE message is class name");
        }

        // 9. replaceObject + annotateClass through a subclass
        bos = new ByteArrayOutputStream();
        try (InstrumentedOOS io = new InstrumentedOOS(bos)) {
            io.writeObject(new Replaceable("tag"));
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Replaceable r = (Replaceable) ois.readObject();
            check("tag-replaced".equals(r.tag), "replaceObject swapped the object on write");
        }
        try (InstrumentedOOS io = new InstrumentedOOS(new ByteArrayOutputStream())) {
            io.writeObject(new Holder());     // a real class descriptor triggers annotateClass
            check(io.annotated != null && io.annotated.endsWith("Holder"),
                  "annotateClass invoked with the class being written: " + io.annotated);
        }

        // 10. useProtocolVersion(PROTOCOL_VERSION_1) still round-trips
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_1);
            oos.writeObject("legacy");
        }
        check("legacy".equals(de(bos.toByteArray())), "protocol version 1 round-trip");

        // 11. writing a Class object serializes as TC_CLASS and reads back the class
        check(de(ser(String.class)) == String.class, "Class object round-trips");

        // 12. drain/flush/close idempotence: close() twice is fine, flush() is fine
        ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
        ObjectOutputStream oos3 = new ObjectOutputStream(bos3);
        oos3.writeObject("z");
        oos3.flush();
        oos3.close();
        oos3.close();
        check(bos3.size() > 4, "flush pushed the header+object, close twice safe");

        // 13. header bytes: STREAM_MAGIC/STREAM_VERSION as raw shorts (wire contract)
        DataInputStream d = new DataInputStream(new ByteArrayInputStream(bos3.toByteArray()));
        check(d.readShort() == ObjectStreamConstants.STREAM_MAGIC
              && d.readShort() == ObjectStreamConstants.STREAM_VERSION, "wire header constants");

        // 14. ObjectOutput view: the stream object is usable through its interface
        ObjectOutput oo = new ObjectOutputStream(new ByteArrayOutputStream());
        oo.writeObject("io-view");
        oo.writeInt(1);
        oo.flush();
        oo.close();
        passed++;

        // 15. ObjectStreamClass of the written class matches lookup
        check(ObjectStreamClass.lookup(Rec.class).getSerialVersionUID() == 100L,
              "descriptor SUID agrees with the class declaration");
    }

    static PutFieldUser newPutField(int i) {
        PutFieldUser p = new PutFieldUser();
        p.i = i;
        return p;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
