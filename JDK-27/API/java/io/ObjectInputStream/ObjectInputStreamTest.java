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
import java.io.EOFException;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamConstants;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

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

    // =====================================================================
    // Deepening additions (contract-level). Every POJO is a static nested
    // class with an explicit fixed serialVersionUID so the wire format (and
    // any byte-level surgery on it) is stable across JDK versions.
    // =====================================================================

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

    /**
     * Overwrites the 8-byte serialVersionUID of the first class descriptor in
     * the stream. Layout: [0..3]=AC ED 00 05, [4]=TC_OBJECT(0x73),
     * [5]=TC_CLASSDESC(0x72), [6..7]=name length, name, then 8 UID bytes.
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
        @Serial
        private static final long serialVersionUID = 3001L; // fixed: cross-version wire stability
        byte b = Byte.MIN_VALUE; short sh = Short.MAX_VALUE; int i = Integer.MIN_VALUE;
        long l = Long.MAX_VALUE; float f = Float.MIN_VALUE; double d = -Double.MAX_VALUE;
        boolean z = true; char c = Character.MAX_VALUE;
    }

    static class StaticS implements Serializable {
        @Serial
        private static final long serialVersionUID = 3002L; // fixed: cross-version wire stability
        static int S = 1;
        int x = 5;
    }

    static class Node2 implements Serializable {
        @Serial
        private static final long serialVersionUID = 3003L; // fixed: cross-version wire stability
        Node2 next; String name;
        Node2(String n) { name = n; }
    }

    static class Diamond2 implements Serializable {
        @Serial
        private static final long serialVersionUID = 3004L; // fixed: cross-version wire stability
        Node2 b; Node2 c;
    }

    static class SingletonR implements Serializable {
        @Serial
        private static final long serialVersionUID = 3005L; // fixed: cross-version wire stability
        static final SingletonR INSTANCE = new SingletonR();
        private SingletonR() {}
        Object readResolve() { return INSTANCE; }
    }

    static class ResolveNull2 implements Serializable {
        @Serial
        private static final long serialVersionUID = 3006L; // fixed: cross-version wire stability
        Object readResolve() { return null; }
    }

    static class HookException extends ObjectStreamException {
        @Serial
        private static final long serialVersionUID = 3007L; // fixed: cross-version wire stability
        HookException(String m) { super(m); }
    }

    static class ResolveThrow2 implements Serializable {
        @Serial
        private static final long serialVersionUID = 3008L; // fixed: cross-version wire stability
        Object readResolve() throws ObjectStreamException { throw new HookException("resolve-boom"); }
    }

    static class ReplaceToTag implements Serializable {
        @Serial
        private static final long serialVersionUID = 3009L; // fixed: cross-version wire stability
        String v;
        ReplaceToTag(String v) { this.v = v; }
        Object writeReplace() { return "REPLACED-" + v; }
    }

    static class ReplaceNull2 implements Serializable {
        @Serial
        private static final long serialVersionUID = 3010L; // fixed: cross-version wire stability
        Object writeReplace() { return null; }
    }

    static class ReplaceThrow2 implements Serializable {
        @Serial
        private static final long serialVersionUID = 3011L; // fixed: cross-version wire stability
        Object writeReplace() throws ObjectStreamException { throw new HookException("replace-boom"); }
    }

    static class ExtA implements Externalizable {
        @Serial
        private static final long serialVersionUID = 3012L; // fixed: cross-version wire stability
        static int ctorCount, reCount;
        int v; String s;
        public ExtA() { ctorCount++; }
        public ExtA(int v, String s) { this.v = v; this.s = s; }
        public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(v); out.writeUTF(s); }
        public void readExternal(ObjectInput in) throws IOException { reCount++; v = in.readInt(); s = in.readUTF(); }
    }

    /** Externalizable without a public no-arg constructor: write succeeds, read must fail. */
    static class ExtBad implements Externalizable {
        @Serial
        private static final long serialVersionUID = 3013L; // fixed: cross-version wire stability
        int v;
        ExtBad(int v) { this.v = v; }
        public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(v); }
        public void readExternal(ObjectInput in) throws IOException { v = in.readInt(); }
    }

    static class ExtReadBoom implements Externalizable {
        @Serial
        private static final long serialVersionUID = 3014L; // fixed: cross-version wire stability
        public ExtReadBoom() {}
        public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(1); }
        public void readExternal(ObjectInput in) throws IOException { throw new IOException("ext-read-boom"); }
    }

    /** writeExternal emits int+UTF but readExternal consumes only the int. */
    static class ExtOverConsume implements Externalizable {
        @Serial
        private static final long serialVersionUID = 3015L; // fixed: cross-version wire stability
        static int ctorCount;
        int v;
        public ExtOverConsume() { ctorCount++; }
        public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(9); out.writeUTF("ok"); }
        public void readExternal(ObjectInput in) throws IOException { v = in.readInt(); }
    }

    /** A readObject with the WRONG signature must never be invoked. */
    static class BadSigRead implements Serializable {
        @Serial
        private static final long serialVersionUID = 3016L; // fixed: cross-version wire stability
        static int called;
        int f = 42;
        private void readObject(ObjectOutput in) throws IOException { called++; }
    }

    static class FieldsTwiceRead implements Serializable {
        @Serial
        private static final long serialVersionUID = 3017L; // fixed: cross-version wire stability
        static String second;
        int a = 4;
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.readFields();
            Throwable t = capture(new Snippet() { public void run() throws Exception { in.readFields(); } });
            second = t == null ? "NO-THROW" : t.getClass().getName() + ": " + t.getMessage();
        }
    }

    static class ValidationGraphDone implements Serializable {
        @Serial
        private static final long serialVersionUID = 3018L; // fixed: cross-version wire stability
        Node2 inner;
        String seen;
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            in.registerValidation(new ObjectInputValidation() {
                public void validateObject() {
                    seen = inner != null && "leaf".equals(inner.name) ? "complete" : "incomplete";
                }
            }, 0);
        }
    }

    static class RegNullIn implements Serializable {
        @Serial
        private static final long serialVersionUID = 3019L; // fixed: cross-version wire stability
        static String caught;
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            Throwable t = capture(new Snippet() { public void run() throws Exception {
                in.registerValidation(null, 0);
            } });
            caught = t == null ? "NO-THROW" : t.getClass().getName() + ": " + t.getMessage();
        }
    }

    /** Custom readObject that tries to read an object beyond the custom block data. */
    static class ObjBeyondCustom implements Serializable {
        @Serial
        private static final long serialVersionUID = 3020L; // fixed: cross-version wire stability
        static String exKind;
        int a = 1;
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            try { in.readObject(); exKind = "NO-THROW"; }
            catch (OptionalDataException ode) { exKind = "ODE eof=" + ode.eof + " len=" + ode.length; }
        }
    }

    enum Suit { HEARTS, SPADES }

    // ---- D1. null + String matrix ----
    static void testNullAndStrings() throws Exception {
        check(de(ser(null)) == null, "null round-trips to null");
        byte[] nb = ser(null);
        check(nb.length == 5 && (nb[4] & 0xFF) == ObjectStreamConstants.TC_NULL,
              "writeObject(null) emits exactly TC_NULL");
        check("".equals(de(ser(""))), "empty String");
        check("hello".equals(de(ser("hello"))), "ascii String");
        String zh = "中文序列化测试中文符串";
        check(zh.equals(de(ser(zh))), "Chinese String round-trips (modified UTF-8)");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) sb.append((char) ('a' + (i % 26)));
        String long1 = sb.toString();
        check(long1.equals(de(ser(long1))), "10k String");
        check(long1.length() == 10000 && ((String) de(ser(long1))).length() == 10000,
              "10k String length preserved");
        check(Arrays.equals(ser(zh), ser(zh)), "same String serializes to identical bytes");
    }

    // ---- D2. all wrapper classes incl. NaN/-0.0/±Inf ----
    static void testWrappers() throws Exception {
        check(Byte.MIN_VALUE == (Byte) de(ser(Byte.MIN_VALUE)), "Byte.MIN");
        check(Byte.MAX_VALUE == (Byte) de(ser(Byte.MAX_VALUE)), "Byte.MAX");
        check(Short.MIN_VALUE == (Short) de(ser(Short.MIN_VALUE)), "Short.MIN");
        check(Short.MAX_VALUE == (Short) de(ser(Short.MAX_VALUE)), "Short.MAX");
        check(Integer.MIN_VALUE == (Integer) de(ser(Integer.MIN_VALUE)), "Integer.MIN");
        check(Integer.MAX_VALUE == (Integer) de(ser(Integer.MAX_VALUE)), "Integer.MAX");
        check(Long.MIN_VALUE == (Long) de(ser(Long.MIN_VALUE)), "Long.MIN");
        check(Long.MAX_VALUE == (Long) de(ser(Long.MAX_VALUE)), "Long.MAX");
        check(Boolean.TRUE.equals(de(ser(Boolean.TRUE))) && Boolean.FALSE.equals(de(ser(Boolean.FALSE))),
              "Boolean TRUE/FALSE (equals; a fresh instance is created on read)");
        check(Character.MIN_VALUE == (Character) de(ser(Character.MIN_VALUE)), "Character.MIN");
        check(Character.MAX_VALUE == (Character) de(ser(Character.MAX_VALUE)), "Character.MAX");
        Double dNaN = (Double) de(ser(Double.NaN));
        check(Double.isNaN(dNaN), "Double.NaN");
        Double dNeg = (Double) de(ser(-0.0));
        check(Double.doubleToRawLongBits(dNeg) == Double.doubleToRawLongBits(-0.0), "Double -0.0 bit pattern");
        check(Double.POSITIVE_INFINITY == (Double) de(ser(Double.POSITIVE_INFINITY)), "Double +Inf");
        check(Double.NEGATIVE_INFINITY == (Double) de(ser(Double.NEGATIVE_INFINITY)), "Double -Inf");
        Float fNaN = (Float) de(ser(Float.NaN));
        check(Float.isNaN(fNaN), "Float.NaN");
        Float fNeg = (Float) de(ser(-0.0f));
        check(Float.floatToRawIntBits(fNeg) == Float.floatToRawIntBits(-0.0f), "Float -0.0 bit pattern");
        check(Float.MIN_VALUE == (Float) de(ser(Float.MIN_VALUE)), "Float.MIN_VALUE");
    }

    // ---- D3. every primitive field of a POJO restored exactly ----
    static void testPrimitivePojo() throws Exception {
        Pojo8 p = (Pojo8) de(ser(new Pojo8()));
        check(p.b == Byte.MIN_VALUE && p.sh == Short.MAX_VALUE, "byte/short extremes");
        check(p.i == Integer.MIN_VALUE && p.l == Long.MAX_VALUE, "int/long extremes");
        check(p.f == Float.MIN_VALUE && p.d == -Double.MAX_VALUE, "float/double extremes");
        check(p.z && p.c == Character.MAX_VALUE, "boolean/char extremes");
    }

    // ---- D4. arrays ----
    static void testArrays() throws Exception {
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
        check(sback.length == 3 && "a".equals(sback[0]) && sback[1] == null && "c".equals(sback[2]),
              "String[] with null element");
        byte[] emptyB = (byte[]) de(ser(new byte[0]));
        check(emptyB != null && emptyB.length == 0, "zero-length byte[]");
        Object[] emptyO = (Object[]) de(ser(new Object[0]));
        check(emptyO != null && emptyO.length == 0, "zero-length Object[]");
        int[][] md = (int[][]) de(ser(new int[2][3]));
        check(md.length == 2 && md[0].length == 3 && md[1].length == 3, "int[2][3] shape");
        md[1][2] = 7;
        check(md[1][2] == 7, "multi-dim rows are independent arrays");
        byte[] orig = {1, 2, 3};
        byte[] copy = (byte[]) de(ser(orig));
        check(copy != orig && Arrays.equals(copy, orig), "read-back array is a fresh instance");
    }

    // ---- D5. collections ----
    static void testCollections() throws Exception {
        List<String> al = new ArrayList<String>();
        al.add("x"); al.add(null); al.add("y");
        @SuppressWarnings("unchecked")
        List<String> alBack = (List<String>) de(ser(al));
        check(alBack.size() == 3 && "x".equals(alBack.get(0)) && alBack.get(1) == null
              && "y".equals(alBack.get(2)), "ArrayList with null element");
        check(alBack instanceof ArrayList, "ArrayList concrete type preserved");

        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("a", "1"); hm.put("b", null); hm.put(null, "4");
        @SuppressWarnings("unchecked")
        HashMap<String, String> hmBack = (HashMap<String, String>) de(ser(hm));
        check(hmBack.equals(hm), "HashMap incl. null key/value");

        TreeMap<String, Integer> tm = new TreeMap<String, Integer>();
        tm.put("m", 1); tm.put("a", 2); tm.put("z", 3); tm.put("b", 4);
        @SuppressWarnings("unchecked")
        TreeMap<String, Integer> tmBack = (TreeMap<String, Integer>) de(ser(tm));
        check(tmBack.equals(tm) && Arrays.equals(tmBack.keySet().toArray(new String[0]),
              new String[]{"a", "b", "m", "z"}), "TreeMap iteration order preserved");

        LinkedHashMap<String, Integer> lhm = new LinkedHashMap<String, Integer>();
        lhm.put("3", 3); lhm.put("1", 1); lhm.put("2", 2);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Integer> lhmBack = (LinkedHashMap<String, Integer>) de(ser(lhm));
        check(Arrays.equals(lhmBack.keySet().toArray(new String[0]), new String[]{"3", "1", "2"}),
              "LinkedHashMap insertion order preserved");

        HashSet<String> hs = new HashSet<String>();
        hs.add("p"); hs.add("q");
        @SuppressWarnings("unchecked")
        Set<String> hsBack = (Set<String>) de(ser(hs));
        check(hsBack.equals(hs) && hsBack instanceof HashSet, "HashSet");

        LinkedList<Integer> ll = new LinkedList<Integer>();
        ll.add(1); ll.add(null); ll.add(3);
        @SuppressWarnings("unchecked")
        LinkedList<Integer> llBack = (LinkedList<Integer>) de(ser(ll));
        check(llBack.size() == 3 && llBack.get(0) == 1 && llBack.get(1) == null && llBack.get(2) == 3
              && llBack instanceof LinkedList, "LinkedList with null element");

        Properties pr = new Properties();
        pr.setProperty("k1", "v1"); pr.setProperty("k2", "v2");
        Properties prBack = (Properties) de(ser(pr));
        check("v1".equals(prBack.getProperty("k1")) && "v2".equals(prBack.getProperty("k2"))
              && prBack.size() == 2, "Properties");

        Date dt = new Date(1234567890123L);
        check(((Date) de(ser(dt))).getTime() == 1234567890123L, "Date with fixed millis");

        BigInteger bi = new BigInteger("123456789012345678901234567890123456789012345678901234567890");
        check(bi.equals(de(ser(bi))), "BigInteger exact");
        BigDecimal bd = new BigDecimal("12345.6789012345678901234567890123456789");
        check(bd.equals(de(ser(bd))), "BigDecimal incl. scale");
    }

    // ---- D6. enums and Class objects ----
    static void testEnumAndClassObjects() throws Exception {
        check(de(ser(Suit.HEARTS)) == Suit.HEARTS, "enum constant resolves by name (identity)");
        check(de(ser(Suit.SPADES)) == Suit.SPADES, "second constant");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(Suit.HEARTS);
        oos.writeObject(Suit.HEARTS);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object e1 = ois.readObject(), e2 = ois.readObject();
        check(e1 == Suit.HEARTS && e2 == Suit.HEARTS && e1 == e2,
              "same constant written twice reads back identical (==)");
        ois.close();
        check(de(ser(String.class)) == String.class, "String.class identity");
        check(de(ser(Integer.class)) == Integer.class, "Integer.class identity");
        check(de(ser(int[].class)) == int[].class, "array Class object identity");
    }

    // ---- D7. static fields are not part of the stream ----
    static void testStaticFields() throws Exception {
        StaticS.S = 1;
        byte[] b = ser(new StaticS());
        StaticS.S = 2;
        StaticS back = (StaticS) de(b);
        check(back.x == 5, "instance field restored");
        check(StaticS.S == 2, "static field keeps read-time value (not serialized)");
    }

    // ---- D8. shared and cyclic references ----
    static void testSharedAndCyclic() throws Exception {
        Diamond2 d = new Diamond2();
        Node2 shared = new Node2("B");
        d.b = shared;
        d.c = shared;
        Diamond2 db = (Diamond2) de(ser(d));
        check(db.b == db.c && "B".equals(db.b.name), "diamond shares one Node2 instance");

        Node2 parent = new Node2("p");
        parent.next = new Node2("c");
        parent.next.next = parent;
        Node2 pb = (Node2) de(ser(parent));
        check(pb.next != null && "c".equals(pb.next.name), "cycle: child restored");
        check(pb.next.next == pb, "cycle closes back to the same instance");

        Object[] self = new Object[1];
        self[0] = self;
        Object[] selfBack = (Object[]) de(ser(self));
        check(selfBack[0] == selfBack, "self-referencing array");

        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("a", "1"); hm.put("b", "2");
        check(Arrays.equals(ser(hm), ser(hm)), "same graph serializes to identical bytes");
        Node2 g = new Node2("g");
        g.next = new Node2("h");
        check(Arrays.equals(ser(g), ser(g)), "same POJO graph serializes to identical bytes");
    }

    // ---- D9. readResolve ----
    static void testReadResolve() throws Exception {
        check(de(ser(SingletonR.INSTANCE)) == SingletonR.INSTANCE,
              "readResolve restores the singleton (==)");
        check(de(ser(new SingletonR())) == SingletonR.INSTANCE, "any instance resolves to INSTANCE");
        check(de(ser(new ResolveNull2())) == null, "readResolve returning null yields null");
        Object r = capture(new Snippet() { public void run() throws Exception {
            de(ser(new ResolveThrow2()));
        } });
        check(r instanceof HookException && "resolve-boom".equals(((Throwable) r).getMessage()),
              "readResolve exception propagates unwrapped: " + r);
    }

    // ---- D10. writeReplace seen from the read side ----
    static void testWriteReplaceReadSide() throws Exception {
        Object o = de(ser(new ReplaceToTag("x")));
        check(o instanceof String && "REPLACED-x".equals(o),
              "writeReplace swaps the streamed type (POJO read back as String)");
        check(de(ser(new ReplaceNull2())) == null, "writeReplace returning null writes TC_NULL");
        Object w = capture(new Snippet() { public void run() throws Exception {
            ser(new ReplaceThrow2());
        } });
        check(w instanceof HookException && "replace-boom".equals(((Throwable) w).getMessage()),
              "writeReplace exception propagates unwrapped from writeObject: " + w);
    }

    // ---- D11. Externalizable: ctor order, no-arg requirement, hook failure ----
    static void testExternalizable() throws Exception {
        ExtA.ctorCount = 0; ExtA.reCount = 0;
        byte[] b = ser(new ExtA(7, "seven"));
        check(ExtA.ctorCount == 0, "write side never invokes the constructor");
        ExtA.ctorCount = 0; ExtA.reCount = 0;
        ExtA back = (ExtA) de(b);
        check(ExtA.ctorCount == 1, "read side runs the public no-arg ctor exactly once");
        check(ExtA.reCount == 1, "readExternal invoked once");
        check(back.v == 7 && "seven".equals(back.s), "external data fully restored");

        byte[] bad = ser(new ExtBad(5));   // write side succeeds (no ctor check on write)
        check(bad.length > 0, "writing an Externalizable without no-arg ctor succeeds");
        Object r = capture(new Snippet() { public void run() throws Exception { de(bad); } });
        check(r instanceof InvalidClassException && ((Throwable) r).getMessage() != null
              && ((Throwable) r).getMessage().contains("no valid constructor"),
              "read side demands a valid constructor: " + r);

        Object rb = capture(new Snippet() { public void run() throws Exception { de(ser(new ExtReadBoom())); } });
        check(rb instanceof IOException && "ext-read-boom".equals(((Throwable) rb).getMessage()),
              "readExternal exception propagates unwrapped from readObject: " + rb);
    }

    // ---- D12. serialVersionUID mismatch via byte-level tampering ----
    static void testUidTamper() throws Exception {
        check(ObjectStreamClass.lookup(Rec.class).getSerialVersionUID() == 200L,
              "getSerialVersionUID agrees with the declared value");
        byte[] b = ser(new Rec(1, "x"));
        long evil = 0xDEADBEEFCAFEL;
        byte[] tampered = tamperUid(b, Rec.class.getName(), evil);
        Object r = capture(new Snippet() { public void run() throws Exception { de(tampered); } });
        check(r instanceof InvalidClassException, "tampered UID -> InvalidClassException: " + r);
        String m = ((InvalidClassException) r).getMessage();
        check(m != null && m.contains("local class incompatible"), "incompatibility message: " + m);
        check(m != null && m.contains("244837814094590"), "message carries the streamed (tampered) UID: " + m);
        check(m != null && m.contains("200"), "message carries the local UID 200: " + m);
    }

    // ---- D13. corrupt / truncated / trailing-garbage streams ----
    static void testCorruptStreams() throws Exception {
        Object r = capture(new Snippet() { public void run() throws Exception {
            new ObjectInputStream(new ByteArrayInputStream(new byte[]{0, 0, 0, 0}));
        } });
        check(r instanceof StreamCorruptedException
              && "invalid stream header: 00000000".equals(((Throwable) r).getMessage()),
              "zero magic yields exact message: " + r);
        Object e = capture(new Snippet() { public void run() throws Exception {
            new ObjectInputStream(new ByteArrayInputStream(new byte[0]));
        } });
        check(e instanceof EOFException, "empty input -> EOFException at construction: " + e);

        final byte[] full = ser(new Rec(9, "full"));
        for (final int cut : new int[]{2, 5, 8, full.length / 2, full.length - 1}) {
            Object t = capture(new Snippet() { public void run() throws Exception {
                new ObjectInputStream(new ByteArrayInputStream(Arrays.copyOf(full, cut))).readObject();
            } });
            check(t instanceof EOFException, "truncation at " + cut + " -> EOFException: " + t);
        }
        Object hdr = capture(new Snippet() { public void run() throws Exception {
            new ObjectInputStream(new ByteArrayInputStream(
                    Arrays.copyOf(full, 4))).readObject();
        } });
        check(hdr instanceof EOFException, "header-only stream -> EOFException on readObject: " + hdr);

        Throwable g1 = garbageRead(full, new byte[]{0, 0});
        check(g1 instanceof StreamCorruptedException && "invalid type code: 00".equals(g1.getMessage()),
              "trailing 0x0000 -> invalid type code 00");
        Throwable g2 = garbageRead(full, new byte[]{(byte) 0x99});
        check(g2 instanceof StreamCorruptedException && "invalid type code: 99".equals(g2.getMessage()),
              "trailing 0x99 -> invalid type code 99");
        check(garbageRead(full, new byte[]{0x71}) instanceof EOFException,
              "trailing TC_REFERENCE without handle -> EOFException");
    }

    static Throwable garbageRead(byte[] base, byte[] tail) {
        byte[] g = Arrays.copyOf(base, base.length + tail.length);
        System.arraycopy(tail, 0, g, base.length, tail.length);
        return capture(new Snippet() { public void run() throws Exception {
            ObjectInputStream gi = new ObjectInputStream(new ByteArrayInputStream(g));
            gi.readObject();
            gi.readObject();      // the second read is what hits the garbage
        } });
    }

    // ---- D14. reset() and writeUnshared/readUnshared pairings ----
    static void testUnsharedAndReset() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        String sh = "same";
        oos.writeUnshared(sh);
        oos.writeObject(sh);
        oos.writeObject(sh);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object u1 = ois.readUnshared(), o2 = ois.readObject(), o3 = ois.readObject();
        check("same".equals(u1) && "same".equals(o2) && "same".equals(o3), "values restored");
        check(u1 != o2, "writeUnshared does not register a back-reference handle");
        check(o2 == o3, "writeObject after writeUnshared registers its own handle (back-ref works)");
        ois.close();

        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        ObjectOutputStream w2 = new ObjectOutputStream(b2);
        Rec rec = new Rec(3, "three");
        w2.writeObject(rec);
        w2.writeObject(rec);
        w2.close();
        ObjectInputStream r2 = new ObjectInputStream(new ByteArrayInputStream(b2.toByteArray()));
        check(r2.readUnshared() instanceof Rec, "readUnshared over a fresh object succeeds");
        Object t = capture(new Snippet() { public void run() throws Exception { r2.readObject(); } });
        check(t instanceof InvalidObjectException
              && "cannot read back reference to unshared object".equals(((Throwable) t).getMessage()),
              "back-reference after readUnshared -> InvalidObjectException: " + t);
        r2.close();
    }

    // ---- D15. protocol version 1/2 cross-read ----
    static void testProtocolCrossRead() throws Exception {
        for (int proto : new int[]{ObjectStreamConstants.PROTOCOL_VERSION_1,
                                   ObjectStreamConstants.PROTOCOL_VERSION_2}) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.useProtocolVersion(proto);
            oos.writeObject(new Rec(11, "proto"));
            oos.writeInt(77);
            oos.writeObject("tail");
            oos.close();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            Rec rr = (Rec) ois.readObject();
            check(rr.a == 11 && "proto".equals(rr.b), "proto " + proto + ": object restored");
            check(ois.readInt() == 77, "proto " + proto + ": primitive after object");
            check("tail".equals(ois.readObject()), "proto " + proto + ": trailing object");
            ois.close();
        }
        // protocol 2 skips unconsumed block data; protocol 1 leaves it in the stream
        for (int proto : new int[]{ObjectStreamConstants.PROTOCOL_VERSION_1,
                                   ObjectStreamConstants.PROTOCOL_VERSION_2}) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.useProtocolVersion(proto);
            oos.writeObject(new ExtOverConsume());
            oos.writeObject("tail");
            oos.close();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            ExtOverConsume ec = (ExtOverConsume) ois.readObject();
            check(ec.v == 9, "proto " + proto + ": under-consumed external object itself ok");
            Object t = capture(new Snippet() { public void run() throws Exception { ois.readObject(); } });
            if (proto == ObjectStreamConstants.PROTOCOL_VERSION_2) {
                check(t == null, "proto 2: unconsumed custom data is skipped, next object reads cleanly");
            } else {
                check(t instanceof StreamCorruptedException,
                      "proto 1: unconsumed custom data corrupts the stream: " + t);
            }
            ois.close();
        }
    }

    // ---- D16. OptionalDataException eof flag and lengths ----
    static void testOptionalDataDetails() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeLong(9L);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        try {
            ois.readObject();
            check(false, "readObject over long block data must throw");
        } catch (OptionalDataException ode) {
            check(!ode.eof && ode.length == 8, "writeLong block: ODE eof=false length=8");
        }
        ois.close();

        Object r = capture(new Snippet() { public void run() throws Exception {
            de(ser(new ObjBeyondCustom()));
        } });
        check(r == null && "ODE eof=true len=0".equals(ObjBeyondCustom.exKind),
              "object read beyond custom block data: " + ObjBeyondCustom.exKind);
    }

    // ---- D17. validation: graph completeness, null callback ----
    static void testValidationDetails() throws Exception {
        ValidationGraphDone v = new ValidationGraphDone();
        v.inner = new Node2("leaf");
        ValidationGraphDone vb = (ValidationGraphDone) de(ser(v));
        check("complete".equals(vb.seen), "validateObject sees the fully populated graph");

        Object r = capture(new Snippet() { public void run() throws Exception {
            de(ser(new RegNullIn()));
        } });
        check(r == null && RegNullIn.caught.startsWith("java.io.InvalidObjectException")
              && RegNullIn.caught.contains("null callback"),
              "registerValidation(null, prio) inside readObject -> InvalidObjectException 'null callback': "
              + RegNullIn.caught);
    }

    // ---- D18. wrong-signature hooks and readFields context rules ----
    static void testHookSignatures() throws Exception {
        BadSigRead.called = 0;
        BadSigRead back = (BadSigRead) de(ser(new BadSigRead()));
        check(back.f == 42, "fields restored through the default path");
        check(BadSigRead.called == 0, "readObject with wrong parameter type is never invoked");

        Object r = capture(new Snippet() { public void run() throws Exception {
            de(ser(new FieldsTwiceRead()));
        } });
        check(r == null && FieldsTwiceRead.second.equals(
              "java.io.NotActiveException: not in readObject invocation or fields already read"),
              "second readFields in one readObject -> NotActiveException: " + FieldsTwiceRead.second);
    }

    // ---- D19. close/available/idempotence details ----
    static void testCloseDetails() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject("a");
        oos.writeInt(3);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(ois.available() >= 0, "available is never negative");
        ois.readObject();
        check(ois.available() >= 0, "available >= 0 between object and primitive data");
        ois.close();
        ois.close();
        check(ois.available() == 0, "close idempotent; available after close returns 0");
        try { ois.readObject(); check(false, "readObject after close must throw"); }
        catch (IOException expected) { passed++; }
    }

    // ---- D20. resolveObject applies to nested graph objects too ----
    static void testResolveNested() throws Exception {
        class CountingResolve extends ObjectInputStream {
            int strings;
            CountingResolve(java.io.InputStream is) throws IOException { super(is); }
            void enable() throws IOException { enableResolveObject(true); }
            @Override protected Object resolveObject(Object obj) {
                if (obj instanceof String) strings++;
                return obj;
            }
        }
        Node2 n = new Node2("a");
        n.next = new Node2("b");
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream w = new ObjectOutputStream(b);
        w.writeObject(n);
        w.close();
        CountingResolve cr = new CountingResolve(new ByteArrayInputStream(b.toByteArray()));
        cr.enable();
        check(cr.readObject() instanceof Node2, "graph read with resolver enabled");
        check(cr.strings == 2, "resolveObject offered every nested String (no SecurityManager): " + cr.strings);
        cr.close();
    }

    // ---- D21. subclass hooks: readObjectOverride + custom stream header ----
    static void testSubclassHooks() throws Exception {
        class OverrideOIS extends ObjectInputStream {
            Object seen;
            OverrideOIS() throws IOException { super(); }   // protected ctor: no header read
            @Override protected Object readObjectOverride() { seen = "SENTINEL"; return seen; }
        }
        OverrideOIS oo = new OverrideOIS();
        check("SENTINEL".equals(oo.readObject()) && oo.seen != null,
              "readObjectOverride takes over readObject (protected ctor path)");
        // note: close() on a protected-ctor OIS that never opened a stream NPEs
        // (no internal bin) on both JDK 8 and JDK 27 - do not close it.

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
                if (a != 0x1234 || b != 0x5678)
                    throw new StreamCorruptedException("custom header mismatch");
            }
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        HdrOOS ho = new HdrOOS(b);
        ho.writeObject("hh");
        ho.close();
        HdrOIS hi = new HdrOIS(new ByteArrayInputStream(b.toByteArray()));
        check("hh".equals(hi.readObject()), "custom header pair round-trips");
        hi.close();
        Object t = capture(new Snippet() { public void run() throws Exception {
            new ObjectInputStream(new ByteArrayInputStream(b.toByteArray())).readObject();
        } });
        check(t instanceof StreamCorruptedException
              && "invalid stream header: 12345678".equals(((Throwable) t).getMessage()),
              "default reader rejects custom header: " + t);
    }

    // ---- D22. JDK-27: JEP 290 ObjectInputFilter deep contract ----
    static void testObjectInputFilterDeep() throws Exception {
        Node2 deep = new Node2("0");
        Node2 cur = deep;
        for (int i = 1; i < 5; i++) { cur.next = new Node2("d" + i); cur = cur.next; }
        byte[] b = ser(deep);

        final StringBuilder depths = new StringBuilder();
        ObjectInputStream rec = new ObjectInputStream(new ByteArrayInputStream(b));
        rec.setObjectInputFilter(info -> { depths.append(info.depth()).append(' '); return ObjectInputFilter.Status.ALLOWED; });
        rec.readObject();
        check("1 2 3 4 5 ".equals(depths.toString()),
              "filter depth counts the root object as depth 1: " + depths);
        rec.close();

        final byte[] fb = b;
        ObjectInputStream d4 = new ObjectInputStream(new ByteArrayInputStream(fb));
        d4.setObjectInputFilter(ObjectInputFilter.Config.createFilter("maxdepth=4"));
        Object t = capture(new Snippet() { public void run() throws Exception { d4.readObject(); } });
        check(t instanceof InvalidClassException && ((Throwable) t).getMessage() != null
              && ((Throwable) t).getMessage().contains("filter status: REJECTED"),
              "maxdepth=4 rejects a 5-deep graph: " + t);
        d4.close();

        ObjectInputStream d5 = new ObjectInputStream(new ByteArrayInputStream(fb));
        d5.setObjectInputFilter(ObjectInputFilter.Config.createFilter("maxdepth=5"));
        Object ok = d5.readObject();
        check(ok instanceof Node2, "maxdepth=5 admits the same graph");
        d5.close();

        final byte[] arrb = ser(new byte[]{1, 2, 3});
        final StringBuilder classes = new StringBuilder();
        ObjectInputStream arr = new ObjectInputStream(new ByteArrayInputStream(arrb));
        arr.setObjectInputFilter(info -> { classes.append(info.serialClass()).append(' '); return ObjectInputFilter.Status.ALLOWED; });
        arr.readObject();
        check(classes.toString().equals("class [B class [B "), "array element classes offered to the filter: " + classes);
        arr.close();

        ObjectInputStream late = new ObjectInputStream(new ByteArrayInputStream(ser("x")));
        late.readObject();
        Object s = capture(new Snippet() { public void run() throws Exception {
            late.setObjectInputFilter(info -> ObjectInputFilter.Status.ALLOWED);
        } });
        check(s instanceof IllegalStateException
              && "filter can not be set after an object has been read".equals(((Throwable) s).getMessage()),
              "setObjectInputFilter after a read -> IllegalStateException: " + s);
        late.close();

        ObjectInputStream nul = new ObjectInputStream(new ByteArrayInputStream(ser("x")));
        nul.setObjectInputFilter(null);
        check(nul.getObjectInputFilter() == null, "setting null first is allowed and keeps the filter null");
        nul.close();

        ObjectInputStream rej = new ObjectInputStream(new ByteArrayInputStream(fb));
        rej.setObjectInputFilter(ObjectInputFilter.rejectFilter(cl -> cl == Node2.class, ObjectInputFilter.Status.ALLOWED));
        Object rj = capture(new Snippet() { public void run() throws Exception { rej.readObject(); } });
        check(rj instanceof InvalidClassException, "rejectFilter clamps to REJECTED: " + rj);
        rej.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // deepening additions (contract-level)
        testNullAndStrings();
        testWrappers();
        testPrimitivePojo();
        testArrays();
        testCollections();
        testEnumAndClassObjects();
        testStaticFields();
        testSharedAndCyclic();
        testReadResolve();
        testWriteReplaceReadSide();
        testExternalizable();
        testUidTamper();
        testCorruptStreams();
        testUnsharedAndReset();
        testProtocolCrossRead();
        testOptionalDataDetails();
        testValidationDetails();
        testHookSignatures();
        testCloseDetails();
        testResolveNested();
        testSubclassHooks();
        testObjectInputFilterDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
