/*
 * @test
 * @summary java.io.ObjectInputStream API conformance: full contract coverage.
 *          Object + primitive streams, GetField/readFields, readUnshared,
 *          registerValidation, resolveObject/enableResolveObject, OptionalDataException,
 *          StreamCorruptedException, NotActiveException, WriteAbortedException,
 *          NotSerializableException, resolveClass, readFully/skipBytes, close.
 * @run main ObjectInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectInputValidation;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.io.WriteAbortedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObjectInputStreamTest {
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

    static class Rec implements Serializable {
        private static final long serialVersionUID = 200L;
        int a; String b; long c; double d; boolean e;
        transient int t = 5;
        Rec(int a, String b) { this.a = a; this.b = b; c = -3L; d = 0.5; e = false; }
    }

    static class WithGetField implements Serializable {
        private static final long serialVersionUID = 201L;
        int i; boolean bo; long lo;
        private void readObject(ObjectInputStream s)
                throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField gf = s.readFields();
            i = gf.get("i", 0);
            bo = gf.get("bo", false);
            lo = gf.get("lo", 0L);
        }
    }

    static class Validated implements Serializable {
        private static final long serialVersionUID = 202L;
        static final List<String> log = new ArrayList<String>();
        private void readObject(ObjectInputStream s)
                throws IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("prio9"); }
            }, 9);
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("prio0"); }
            }, 0);
        }
    }

    static class FailingValidation implements Serializable {
        private static final long serialVersionUID = 203L;
        private void readObject(ObjectInputStream s)
                throws IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() throws InvalidObjectException {
                    throw new InvalidObjectException("bad state");
                }
            }, 0);
        }
    }

    static class ResolveSub extends ObjectInputStream {
        ResolveSub(java.io.InputStream is) throws IOException { super(is); }
        @Override protected Object resolveObject(Object obj) {
            return obj instanceof String ? ((String) obj).toUpperCase() : obj;
        }
        boolean[] flipResolve() throws IOException {
            return new boolean[]{enableResolveObject(true), enableResolveObject(false)};
        }
    }

    static class BadWrite implements Serializable {
        private static final long serialVersionUID = 204L;
        private void writeObject(ObjectOutputStream out) throws IOException {
            throw new IOException("boom-write");
        }
    }

    // ---- 1. object round-trip incl. every primitive field + transient reset ----
    static void testObjectRoundTrip() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ser(new Rec(7, "seven"))));
        Rec r = (Rec) ois.readObject();
        ois.close();
        check(r.a == 7 && "seven".equals(r.b) && r.c == -3L && r.d == 0.5 && !r.e,
              "all fields restored");
        check(r.t == 0, "transient reset to default");
    }

    // ---- 2. mixed stream: object then primitives then raw reads ----
    static void testMixedStream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new Rec(1, "one"));
        oos.writeInt(5);
        oos.flush();
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Rec r = (Rec) ois.readObject();
        check(r.a == 1, "object read");
        check(ois.readInt() == 5, "primitive after object");
        check(ois.read() == -1, "EOF after all data");
        check(ois.available() == 0, "available 0 at EOF");
        check(ois.skipBytes(3) == 0, "skipBytes 0 at EOF");
        check(ois.readLine() == null, "readLine null at EOF");
        ois.close();
    }

    // ---- 3. GetField typed access restored through readFields ----
    static void testGetField() throws Exception {
        WithGetField g = new WithGetField();
        g.i = 9; g.bo = true; g.lo = 1L << 35;
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ser(g)));
        WithGetField back = (WithGetField) ois.readObject();
        ois.close();
        check(back.i == 9 && back.bo && back.lo == (1L << 35), "GetField restored typed values");
    }

    // ---- 4. registerValidation priority order + failure ----
    static void testValidation() throws Exception {
        Validated.log.clear();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ser(new Validated())));
        ois.readObject();
        ois.close();
        check(Validated.log.equals(Arrays.asList("prio9", "prio0")),
              "validations run high-priority first: " + Validated.log);

        ObjectInputStream f = new ObjectInputStream(new ByteArrayInputStream(ser(new FailingValidation())));
        try {
            f.readObject();
            check(false, "failing validation must propagate");
        } catch (InvalidObjectException expected) {
            check("bad state".equals(expected.getMessage()), "validation exception surfaced");
        }
        f.close();
    }

    // ---- 5. resolveObject only takes effect after enableResolveObject(true) ----
    static void testResolveObject() throws Exception {
        ResolveSub rs = new ResolveSub(new ByteArrayInputStream(ser("orig")));
        check("orig".equals(rs.readObject()), "resolve disabled by default");
        rs.close();

        ResolveSub rs2 = new ResolveSub(new ByteArrayInputStream(ser("orig")));
        boolean[] prev = rs2.flipResolve();
        check(!prev[0] && prev[1], "enableResolveObject returns previous state (false then true)");
        rs2.close();

        ResolveSub enabled = new ResolveSub(new ByteArrayInputStream(ser("orig"))) {
            { enableResolveObject(true); }
        };
        check("ORIG".equals(enabled.readObject()), "resolveObject substitutes when enabled");
        enabled.close();
    }

    // ---- 6. OptionalDataException ----
    static void testOptionalDataException() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeInt(42);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        try {
            ois.readObject();
            check(false, "object read over primitive data must throw OptionalDataException");
        } catch (OptionalDataException ode) {
            check(!ode.eof && ode.length == 4, "OptionalDataException length=4, eof=false");
        }
        ois.close();
    }

    // ---- 7. StreamCorruptedException on bad header ----
    static void testStreamCorrupted() throws Exception {
        try {
            new ObjectInputStream(new ByteArrayInputStream(new byte[]{0, 1, 2, 3}));
            check(false, "invalid stream header must throw");
        } catch (StreamCorruptedException sce) {
            check(sce.getMessage() != null && sce.getMessage().startsWith("invalid stream header"),
                  "StreamCorruptedException on bad header: " + sce.getMessage());
        }
    }

    // ---- 8. NotActiveException for reads outside readObject ----
    static void testNotActive() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ser("x")));
        try { ois.defaultReadObject(); check(false, "defaultReadObject must throw"); }
        catch (NotActiveException expected) { passed++; }
        try { ois.readFields(); check(false, "readFields must throw"); }
        catch (NotActiveException expected) { passed++; }
        try {
            ois.registerValidation(new ObjectInputValidation() {
                public void validateObject() { }
            }, 0);
            check(false, "registerValidation must throw");
        } catch (NotActiveException expected) { passed++; }
        ois.close();
    }

    // ---- 9. readUnshared ----
    static void testReadUnshared() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String shared = "dup";
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(shared);
        oos.writeObject(shared);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object a1 = ois.readObject(), a2 = ois.readObject();
        check(a1 == a2, "duplicate reads share identity");
        ois.close();

        ObjectInputStream ois2 = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ois2.readObject();
        try {
            ois2.readUnshared();
            check(false, "back-reference read as unshared must fail");
        } catch (InvalidObjectException ex) {
            passed++;
        }
        ois2.close();
    }

    // ---- 10. WriteAbortedException ----
    static void testWriteAborted() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(new BadWrite());
        } catch (IOException expected) {
            passed++;
        }
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        try {
            ois.readObject();
            check(false, "aborted write must surface on read");
        } catch (WriteAbortedException wae) {
            check(wae.getCause() instanceof IOException, "WriteAbortedException carries the original cause");
        }
        ois.close();
    }

    // ---- 11. NotSerializableException wraps in WriteAbortedException ----
    static void testNotSerializableOnRead() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(new Object());
            check(false, "non-serializable write must throw");
        } catch (NotSerializableException expected) {
            passed++;
        }
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        try {
            ois.readObject();
            check(false, "poisoned stream must throw");
        } catch (WriteAbortedException wae) {
            check(wae.getCause() instanceof NotSerializableException,
                  "write-side NotSerializableException wrapped in WriteAbortedException");
        }
        ois.close();
    }

    // ---- 12. resolveClass hook ----
    static void testResolveClass() throws Exception {
        class ResolvingIn extends ObjectInputStream {
            String resolved;
            ResolvingIn(java.io.InputStream is) throws IOException { super(is); }
            @Override protected Class<?> resolveClass(ObjectStreamClass desc)
                    throws IOException, ClassNotFoundException {
                resolved = desc.getName();
                return super.resolveClass(desc);
            }
        }
        ResolvingIn ri = new ResolvingIn(new ByteArrayInputStream(ser(new Rec(1, "x"))));
        Object o = ri.readObject();
        check(o instanceof Rec && ri.resolved != null && ri.resolved.endsWith("Rec"),
              "resolveClass invoked with the streamed descriptor: " + ri.resolved);
        ri.close();
    }

    // ---- 13. primitives-only stream ----
    static void testPrimitives() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeBoolean(true); oos.writeByte(-1); oos.writeShort(-2);
        oos.writeChar('Q'); oos.writeInt(-5); oos.writeLong(-6L);
        oos.writeFloat(-7.5f); oos.writeDouble(-8.25); oos.writeUTF("utf8");
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(ois.readBoolean(), "readBoolean");
        check(ois.readByte() == -1, "readByte");
        check(ois.readShort() == -2, "readShort");
        check(ois.readChar() == 'Q', "readChar");
        check(ois.readInt() == -5 && ois.readLong() == -6L, "readInt/readLong");
        check(ois.readFloat() == -7.5f && ois.readDouble() == -8.25, "readFloat/readDouble");
        check("utf8".equals(ois.readUTF()), "readUTF");
        ois.close();

        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        oos.writeByte(0xFE);
        oos.writeShort(0xFFFE);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(ois.readUnsignedByte() == 254, "readUnsignedByte");
        check(ois.readUnsignedShort() == 65534, "readUnsignedShort");
        ois.close();
    }

    // ---- 14. readFully/skipBytes within primitive block data ----
    static void testReadFullySkip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.write(new byte[]{10, 20, 30, 40});
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        byte[] four = new byte[4];
        ois.readFully(four);
        check(four[0] == 10 && four[3] == 40, "readFully over block data");
        ois.close();

        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        oos.write(new byte[]{1, 2, 3, 4, 5});
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(ois.skipBytes(2) == 2 && ois.read() == 3, "skipBytes then read");
        ois.close();
    }

    // ---- 15. close propagates ----
    static void testClose() throws Exception {
        java.io.InputStream strict = new java.io.InputStream() {
            final java.io.InputStream src = new ByteArrayInputStream(ser("x"));
            boolean closed;
            @Override public int read() throws IOException {
                if (closed) throw new IOException("Stream closed");
                return src.read();
            }
            @Override public int read(byte[] b, int off, int len) throws IOException {
                if (closed) throw new IOException("Stream closed");
                return src.read(b, off, len);
            }
            @Override public void close() throws IOException { closed = true; src.close(); }
        };
        ObjectInputStream ois = new ObjectInputStream(strict);
        check("x".equals(ois.readObject()), "object read before close");
        ois.close();
        try { ois.readObject(); check(false, "readObject after close must throw"); }
        catch (IOException expected) { passed++; }
        try { ois.readInt(); check(false, "readInt after close must throw"); }
        catch (IOException expected) { passed++; }
    }

    public static void main(String[] args) throws Exception {
        testObjectRoundTrip();
        testMixedStream();
        testGetField();
        testValidation();
        testResolveObject();
        testOptionalDataException();
        testStreamCorrupted();
        testNotActive();
        testReadUnshared();
        testWriteAborted();
        testNotSerializableOnRead();
        testResolveClass();
        testPrimitives();
        testReadFullySkip();
        testClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
