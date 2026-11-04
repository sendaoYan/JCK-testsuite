/*
 * @test
 * @summary java.io.ObjectInputStream conformance: white-box structure + black-box behavior
 *          (object + primitive streams, GetField, readUnshared, registerValidation,
 *          resolveObject/enableResolveObject, ObjectInputFilter wiring, OptionalDataException,
 *          StreamCorruptedException, NotActiveException, close).
 * @run main ObjectInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputValidation;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.Serial;
import java.io.StreamCorruptedException;
import java.io.WriteAbortedException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ObjectInputStreamTest {
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

    static ObjectInputStream in(Object o) throws Exception {
        return new ObjectInputStream(new ByteArrayInputStream(ser(o)));
    }

    static class Rec implements Serializable {
        @Serial
        private static final long serialVersionUID = 200L;
        int a; String b; long c; double d; boolean e;
        transient int t = 5;
        Rec(int a, String b) { this.a = a; this.b = b; c = -3L; d = 0.5; e = false; }
    }

    static class WithGetField implements Serializable {
        @Serial
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
        @Serial
        private static final long serialVersionUID = 202L;
        static final List<String> log = new ArrayList<>();
        private void readObject(ObjectInputStream s)
                throws IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(() -> log.add("prio9"), 9);
            s.registerValidation(() -> log.add("prio0"), 0);
        }
    }

    static class FailingValidation implements Serializable {
        @Serial
        private static final long serialVersionUID = 203L;
        private void readObject(ObjectInputStream s)
                throws IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(() -> { throw new InvalidObjectException("bad state"); }, 0);
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
        @Serial
        private static final long serialVersionUID = 204L;
        private void writeObject(ObjectOutputStream out) throws IOException {
            throw new IOException("boom-write");
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectInputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.io.InputStream"), "extends InputStream");
        check(java.io.ObjectInput.class.isAssignableFrom(c), "implements ObjectInput");
        check(java.io.ObjectStreamConstants.class.isAssignableFrom(c), "implements ObjectStreamConstants");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()),
              "protected no-arg ctor for subclasses");
        check(Modifier.isFinal(c.getMethod("readObject").getModifiers()), "readObject is final");
        check(Modifier.isFinal(c.getMethod("getObjectInputFilter").getModifiers()),
              "getObjectInputFilter is final");
        check(Modifier.isFinal(c.getMethod("setObjectInputFilter", ObjectInputFilter.class).getModifiers()),
              "setObjectInputFilter is final");
        for (String m : new String[]{"readUnshared", "defaultReadObject", "readFields",
                                     "registerValidation", "readBoolean", "readByte",
                                     "readUnsignedByte", "readShort", "readUnsignedShort",
                                     "readChar", "readInt", "readLong", "readFloat",
                                     "readDouble", "readFully", "skipBytes", "readLine",
                                     "readUTF", "available", "read", "close", "resolveClass",
                                     "resolveObject", "enableResolveObject", "readStreamHeader"})
            check(hasMethod(c, m), "has method " + m);
    }

    static boolean hasMethod(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        for (java.lang.reflect.Method mm : c.getDeclaredMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. object round-trip incl. every primitive field + transient reset
        try (ObjectInputStream ois = in(new Rec(7, "seven"))) {
            Rec r = (Rec) ois.readObject();
            check(r.a == 7 && "seven".equals(r.b) && r.c == -3L && r.d == 0.5 && !r.e,
                  "all fields restored");
            check(r.t == 0, "transient reset to default");
        }

        // 2. mixed stream: object then primitives then raw reads
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Rec(1, "one"));
            oos.writeInt(5);
            oos.flush();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Rec r = (Rec) ois.readObject();
            check(r.a == 1, "object read");
            check(ois.readInt() == 5, "primitive after object");
            check(ois.read() == -1, "EOF after all data");
            check(ois.available() == 0, "available 0 at EOF");
            check(ois.skipBytes(3) == 0, "skipBytes 0 at EOF");
            check(ois.readLine() == null, "readLine null at EOF");
        }

        // 3. GetField typed access restored through readFields
        WithGetField g = new WithGetField();
        g.i = 9; g.bo = true; g.lo = 1L << 35;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ser(g)))) {
            WithGetField back = (WithGetField) ois.readObject();
            check(back.i == 9 && back.bo && back.lo == (1L << 35), "GetField restored typed values");
        }
        Class<?> gf = Class.forName("java.io.ObjectInputStream$GetField");
        check(Modifier.isAbstract(gf.getModifiers()) && gf.getDeclaredMethods().length >= 11,
              "GetField abstract class surface (typed get overloads + defaulted + getObjectStreamClass)");

        // 4. registerValidation: callbacks run in priority order after the graph is read
        Validated.log.clear();
        try (ObjectInputStream ois = in(new Validated())) {
            ois.readObject();
        }
        check(Validated.log.equals(List.of("prio9", "prio0")),
              "validations run high-priority first: " + Validated.log);

        // 4b. a validation throwing InvalidObjectException propagates from readObject
        try (ObjectInputStream ois = in(new FailingValidation())) {
            ois.readObject();
            check(false, "failing validation must propagate");
        } catch (InvalidObjectException expected) {
            check("bad state".equals(expected.getMessage()), "validation exception surfaced");
        }

        // 5. resolveObject only takes effect after enableResolveObject(true)
        try (ResolveSub rs = new ResolveSub(new ByteArrayInputStream(ser("orig")))) {
            check("orig".equals(rs.readObject()), "resolve disabled by default");
        }
        try (ResolveSub rs = new ResolveSub(new ByteArrayInputStream(ser("orig")))) {
            boolean[] prev = rs.flipResolve();
            check(!prev[0] && prev[1], "enableResolveObject returns previous state (false then true)");
        }
        ResolveSub enabled = new ResolveSub(new ByteArrayInputStream(ser("orig"))) {
            { enableResolveObject(true); }
        };
        check("ORIG".equals(enabled.readObject()), "resolveObject substitutes when enabled");

        // 6. ObjectInputFilter: initially null, settable once, second set is IllegalStateException
        ObjectInputStream ois6 = in("x");
        check(ois6.getObjectInputFilter() == null, "filter initially null");
        ois6.setObjectInputFilter(ObjectInputFilter.allowFilter(c2 -> true, ObjectInputFilter.Status.REJECTED));
        check(ois6.getObjectInputFilter() != null, "filter set and visible");
        try {
            ois6.setObjectInputFilter(null);
            check(false, "setting the filter twice must throw");
        } catch (IllegalStateException expected) {
            check(expected.getMessage() != null, "IllegalStateException on double set");
        }
        ois6.close();

        // 6b. a rejecting filter aborts readObject with InvalidClassException
        ObjectInputStream rej = in(new Rec(1, "one"));
        rej.setObjectInputFilter(ObjectInputFilter.Config.createFilter("!*"));
        try {
            rej.readObject();
            check(false, "rejecting filter must abort deserialization");
        } catch (InvalidClassException ice) {
            check(ice.getMessage() != null && ice.getMessage().contains("REJECTED"),
                  "InvalidClassException reports filter rejection: " + ice.getMessage());
        }

        // 7. OptionalDataException: reading an object where the stream has primitive data
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeInt(42);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.readObject();
            check(false, "object read over primitive data must throw OptionalDataException");
        } catch (OptionalDataException ode) {
            check(!ode.eof && ode.length == 4, "OptionalDataException length=4, eof=false");
        }

        // 8. StreamCorruptedException: garbage header
        try {
            new ObjectInputStream(new ByteArrayInputStream(new byte[]{0, 1, 2, 3}));
            check(false, "invalid stream header must throw");
        } catch (StreamCorruptedException sce) {
            check(sce.getMessage() != null && sce.getMessage().startsWith("invalid stream header"),
                  "StreamCorruptedException on bad header: " + sce.getMessage());
        }

        // 9. NotActiveException for reads outside an active readObject
        try (ObjectInputStream ois = in("x")) {
            try { ois.defaultReadObject(); check(false, "defaultReadObject must throw"); }
            catch (NotActiveException expected) { passed++; }
            try { ois.readFields(); check(false, "readFields must throw"); }
            catch (NotActiveException expected) { passed++; }
            try { ois.registerValidation(() -> {}, 0); check(false, "registerValidation must throw"); }
            catch (NotActiveException expected) { passed++; }
        }

        // 10. readUnshared: identity for duplicates; reading a back-reference as unshared fails
        bos = new ByteArrayOutputStream();
        String shared = "dup";
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(shared);
            oos.writeObject(shared);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Object a1 = ois.readObject(), a2 = ois.readObject();
            check(a1 == a2, "duplicate reads share identity");
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.readObject();
            try {
                ois.readUnshared();
                check(false, "back-reference read as unshared must fail");
            } catch (Exception ex) {
                check(ex instanceof java.io.InvalidObjectException,
                      "InvalidObjectException for unshared back-reference: " + ex);
            }
        }

        // 11. a stream whose write was aborted mid-object reads back as WriteAbortedException
        bos = new ByteArrayOutputStream();
        ObjectOutputStream oos11 = new ObjectOutputStream(bos);
        try {
            oos11.writeObject(new BadWrite());
        } catch (IOException expected) {
            passed++;
        }
        oos11.flush();
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.readObject();
            check(false, "aborted write must surface on read");
        } catch (WriteAbortedException wae) {
            check(wae.getCause() instanceof IOException, "WriteAbortedException carries the original cause");
        }

        // 12. NotSerializableException propagates through the stream on read too
        ByteArrayOutputStream bos12 = new ByteArrayOutputStream();
        ObjectOutputStream oos12 = new ObjectOutputStream(bos12);
        try {
            oos12.writeObject(new Object());
            check(false, "non-serializable write must throw");
        } catch (NotSerializableException expected) {
            passed++;
        }
        oos12.flush();
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos12.toByteArray()))) {
            ois.readObject();
            check(false, "poisoned stream must throw");
        } catch (WriteAbortedException wae) {
            check(wae.getCause() instanceof NotSerializableException,
                  "write-side NotSerializableException wrapped in WriteAbortedException");
        }

        // 13. resolveClass hook sees the descriptor of the class being read
        class ResolvingIn extends ObjectInputStream {
            String resolved;
            ResolvingIn(java.io.InputStream is) throws IOException { super(is); }
            @Override protected Class<?> resolveClass(ObjectStreamClass desc)
                    throws IOException, ClassNotFoundException {
                resolved = desc.getName();
                return super.resolveClass(desc);
            }
        }
        try (ResolvingIn ri = new ResolvingIn(new ByteArrayInputStream(ser(new Rec(1, "x"))))) {
            Object o = ri.readObject();
            check(o instanceof Rec && ri.resolved != null && ri.resolved.endsWith("Rec"),
                  "resolveClass invoked with the streamed descriptor: " + ri.resolved);
        }

        // 14. primitives-only stream fully exercised through the DataInput surface
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeBoolean(true); oos.writeByte(-1); oos.writeShort(-2);
            oos.writeChar('Q'); oos.writeInt(-5); oos.writeLong(-6L);
            oos.writeFloat(-7.5f); oos.writeDouble(-8.25); oos.writeUTF("utf8");
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check(ois.readBoolean(), "readBoolean");
            check(ois.readByte() == -1, "readByte");
            check(ois.readShort() == -2, "readShort");
            check(ois.readChar() == 'Q', "readChar");
            check(ois.readInt() == -5 && ois.readLong() == -6L, "readInt/readLong");
            check(ois.readFloat() == -7.5f && ois.readDouble() == -8.25, "readFloat/readDouble");
            check("utf8".equals(ois.readUTF()), "readUTF");
        }

        // 14b. unsigned reads widen to the positive range
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeByte(0xFE);
            oos.writeShort(0xFFFE);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check(ois.readUnsignedByte() == 254, "readUnsignedByte");
            check(ois.readUnsignedShort() == 65534, "readUnsignedShort");
        }

        // 15. readFully/skipBytes within primitive block data
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.write(new byte[]{10, 20, 30, 40});
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            byte[] four = new byte[4];
            ois.readFully(four);
            check(four[0] == 10 && four[3] == 40, "readFully over block data");
        }
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.write(new byte[]{1, 2, 3, 4, 5});
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check(ois.skipBytes(2) == 2 && ois.read() == 3, "skipBytes then read");
        }

        // 16. close propagates: with the underlying stream consumed and refusing reads,
        //     every further read surfaces an IOException
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
        ObjectInputStream ois16 = new ObjectInputStream(strict);
        check("x".equals(ois16.readObject()), "object read before close");
        ois16.close();
        try { ois16.readObject(); check(false, "readObject after close must throw"); }
        catch (IOException expected) { passed++; }
        try { ois16.readInt(); check(false, "readInt after close must throw"); }
        catch (IOException expected) { passed++; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
