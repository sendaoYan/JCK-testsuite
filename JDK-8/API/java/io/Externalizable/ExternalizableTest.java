/*
 * @test
 * @summary java.io.Externalizable conformance (pure black-box): writeExternal /
 *          readExternal full control of the stream, no-arg constructor
 *          requirement, DataOutput/DataInput-style primitive round-trip.
 * @run main ExternalizableTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class ExternalizableTest {
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

    /** Fully hand-written serial form using the ObjectOutput/DataOutput surface. */
    static class Ext implements Externalizable {
        private static final long serialVersionUID = 11L;
        int i;
        String s;
        long l;
        boolean b;
        transient int marker = 99;          // Externalizable ignores transient: WE decide
        static int instances;

        public Ext() { instances++; }

        Ext(int i, String s, long l, boolean b) {
            this.i = i; this.s = s; this.l = l; this.b = b;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(i);
            out.writeUTF(s);
            out.writeLong(l);
            out.writeBoolean(b);
            out.writeInt(777);              // transient field written deliberately
        }

        public void readExternal(ObjectInput in) throws IOException {
            i = in.readInt();
            s = in.readUTF();
            l = in.readLong();
            b = in.readBoolean();
            marker = in.readInt();
        }
    }

    /** Externalizable class missing the public no-arg constructor. */
    static class NoCtor implements Externalizable {
        private static final long serialVersionUID = 12L;
        public NoCtor(int x) {}
        public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(1); }
        public void readExternal(ObjectInput in) throws IOException {}
    }

    static void testBlackBox() throws Exception {
        // 1. full round-trip with hand-written form
        Ext src = new Ext(42, "ext", 1L << 40, true);
        Ext dst = (Ext) de(ser(src));
        check(dst.i == 42, "readExternal int");
        check("ext".equals(dst.s), "readExternal UTF string");
        check(dst.l == (1L << 40), "readExternal long");
        check(dst.b, "readExternal boolean");
        check(dst.marker == 777, "transient is honored only if the author omits it (we chose to write it)");

        // 2. the no-arg constructor is invoked on deserialization (fresh instance counter)
        Ext.instances = 0;
        de(ser(new Ext(1, "x", 2L, false)));
        check(Ext.instances >= 1, "public no-arg constructor invoked during readObject");

        // 3. Externalizable class without an accessible no-arg ctor is rejected on read
        byte[] bytes = ser(new NoCtor(1));
        try {
            de(bytes);
            check(false, "missing no-arg ctor must fail deserialization");
        } catch (InvalidClassException ice) {
            check(ice.getMessage() != null && ice.getMessage().endsWith("no valid constructor"),
                  "InvalidClassException reports no valid constructor, got: " + ice.getMessage());
        }

        // 4. field-order contract: readExternal consumes exactly what writeExternal wrote,
        //    and both directions share the DataOutput/DataInput primitive encoding
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Ext(7, "v", -8L, false));
            oos.writeInt(1234);             // primitive data after the object
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Ext e = (Ext) ois.readObject();
            check(e.i == 7 && "v".equals(e.s) && e.l == -8L && !e.b, "mixed object+primitive stream");
            check(ois.readInt() == 1234, "trailing primitive data still readable");
        }

        // 5. polymorphic use through the interface types
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(bos2);
        oo.writeObject(new Ext(3, "p", 3L, true));
        oo.close();
        check(bos2.size() > 0, "ObjectOutput view of Externalizable works");
    }

    // ================= deep additions (contract level) =================

    /** Order probe: no-arg ctor must run before readExternal on the read side. */
    static class ExtOrder implements Externalizable {
        private static final long serialVersionUID = 13L;   // fixed for cross-version stability
        static final java.util.List<String> order = new java.util.ArrayList<String>();
        int v;
        public ExtOrder() { order.add("ctor"); }
        public ExtOrder(int v) { this.v = v; }
        public void writeExternal(ObjectOutput out) throws IOException { order.add("writeExt"); out.writeInt(v); }
        public void readExternal(ObjectInput in) throws IOException { order.add("readExt"); v = in.readInt(); }
    }

    /** Nothing written: instance fields must come back as constructor defaults, not stream data. */
    static class ExtEmpty implements Externalizable {
        private static final long serialVersionUID = 14L;
        int a = 111; String b = "bb";
        public ExtEmpty() { }
        ExtEmpty(int a, String b) { this.a = a; this.b = b; }
        public void writeExternal(ObjectOutput out) throws IOException { }
        public void readExternal(ObjectInput in) throws IOException { }
    }

    /** Non-public no-arg constructor: still "no valid constructor". */
    static class ExtProtCtor implements Externalizable {
        private static final long serialVersionUID = 15L;
        public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(1); }
        public void readExternal(ObjectInput in) throws IOException { }
        protected ExtProtCtor() { }
    }

    /** writeExternal throwing a checked IOException subtype. */
    static class ExtThrowW implements Externalizable {
        private static final long serialVersionUID = 16L;
        public ExtThrowW() { }
        public void writeExternal(ObjectOutput out) throws IOException { throw new java.io.UTFDataFormatException("wboom"); }
        public void readExternal(ObjectInput in) throws IOException { }
    }

    /** readExternal throwing a checked IOException subtype. */
    static class ExtThrowR implements Externalizable {
        private static final long serialVersionUID = 17L;
        public ExtThrowR() { }
        public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(1); }
        public void readExternal(ObjectInput in) throws IOException { throw new java.io.UTFDataFormatException("rboom"); }
    }

    /** readExternal throwing an unchecked exception. */
    static class ExtThrowRT implements Externalizable {
        private static final long serialVersionUID = 18L;
        public ExtThrowRT() { }
        public void writeExternal(ObjectOutput out) throws IOException { }
        public void readExternal(ObjectInput in) throws IOException { throw new IllegalStateException("rtboom"); }
    }

    /** Two classes with the same stream identity but different serialVersionUID. */
    static class ExtUID11 implements Externalizable {
        private static final long serialVersionUID = 11L;
        int v;
        public ExtUID11() { }
        ExtUID11(int v) { this.v = v; }
        public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(v); }
        public void readExternal(ObjectInput in) throws IOException { v = in.readInt(); }
    }

    static class ExtUID99 implements Externalizable {
        private static final long serialVersionUID = 99L;
        public ExtUID99() { }
        public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(1); }
        public void readExternal(ObjectInput in) throws IOException { }
    }

    /** The whole DataOutput/DataInput primitive surface through Externalizable. */
    static class ExtFull implements Externalizable {
        private static final long serialVersionUID = 19L;
        byte by; short sh; char ch; int i; long l; float f; double d; boolean bo; String u;
        public ExtFull() { }
        ExtFull(byte by, short sh, char ch, int i, long l, float f, double d, boolean bo, String u) {
            this.by = by; this.sh = sh; this.ch = ch; this.i = i; this.l = l;
            this.f = f; this.d = d; this.bo = bo; this.u = u;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeByte(by); out.writeShort(sh); out.writeChar(ch); out.writeInt(i);
            out.writeLong(l); out.writeFloat(f); out.writeDouble(d); out.writeBoolean(bo);
            out.writeUTF(u);
        }
        public void readExternal(ObjectInput in) throws IOException {
            by = in.readByte(); sh = in.readShort(); ch = in.readChar(); i = in.readInt();
            l = in.readLong(); f = in.readFloat(); d = in.readDouble(); bo = in.readBoolean();
            u = in.readUTF();
        }
    }

    // ---- interface structure: extends Serializable, two abstract methods ----
    static void testInterfaceStructure() throws Exception {
        Class<?> c = Class.forName("java.io.Externalizable");
        check(c.isInterface(), "Externalizable is an interface");
        check(c.getInterfaces().length == 1
              && c.getInterfaces()[0] == java.io.Serializable.class,
              "Externalizable extends Serializable");
        check(c.getDeclaredFields().length == 0, "no constants");
        java.lang.reflect.Method w = c.getMethod("writeExternal", ObjectOutput.class);
        java.lang.reflect.Method r = c.getMethod("readExternal", ObjectInput.class);
        check(java.lang.reflect.Modifier.isAbstract(w.getModifiers())
              && java.lang.reflect.Modifier.isAbstract(r.getModifiers()),
              "both contract methods are abstract");
        check(w.getExceptionTypes().length == 1
              && w.getExceptionTypes()[0] == IOException.class,
              "writeExternal declares IOException");
        boolean cnfe = false;
        for (Class<?> ex : r.getExceptionTypes()) if (ex == ClassNotFoundException.class) cnfe = true;
        check(cnfe, "readExternal declares ClassNotFoundException");
    }

    // ---- read side order: public no-arg constructor, then readExternal ----
    static void testCtorBeforeReadExternal() throws Exception {
        ExtOrder.order.clear();
        ExtOrder back = (ExtOrder) de(ser(new ExtOrder(77)));
        check(back.v == 77, "ExtOrder round-trips");
        check(ExtOrder.order.indexOf("ctor") < ExtOrder.order.indexOf("readExt"),
              "no-arg ctor runs before readExternal: " + ExtOrder.order);
        check(ExtOrder.order.indexOf("writeExt") == 0,
              "writeExternal runs on the write side first: " + ExtOrder.order);
        int ctors = 0;
        for (String s : ExtOrder.order) if (s.equals("ctor")) ctors++;
        check(ctors == 1, "exactly one no-arg ctor invocation per readObject");
    }

    // ---- fields are NOT auto-saved: unwritten fields read back as ctor defaults ----
    static void testFieldsNotAutoSaved() throws Exception {
        ExtEmpty back = (ExtEmpty) de(ser(new ExtEmpty(999, "zzz")));
        check(back.a == 111, "unwritten int comes back as the constructor default: " + back.a);
        check("bb".equals(back.b), "unwritten reference comes back as the constructor default");
    }

    // ---- no valid constructor: exact message, classname, protected ctor, write OK ----
    static void testNoValidConstructorExact() throws Exception {
        byte[] bytes = ser(new NoCtor(1));            // write side works without no-arg ctor
        check(bytes.length > 4, "writing needs no no-arg constructor");
        try {
            de(bytes);
            check(false, "NoCtor must be rejected");
        } catch (InvalidClassException ice) {
            check(ice.getMessage() != null
                  && ice.getMessage().endsWith("no valid constructor"),
                  "message ends with 'no valid constructor': " + ice.getMessage());
            check(NoCtor.class.getName().equals(ice.classname),
                  "InvalidClassException.classname is the FQCN: " + ice.classname);
        }
        try {
            de(ser(new ExtProtCtor()));
            check(false, "protected no-arg ctor must be rejected");
        } catch (InvalidClassException ice) {
            check(ice.getMessage() != null
                  && ice.getMessage().endsWith("no valid constructor"),
                  "protected no-arg ctor rejected identically: " + ice.getMessage());
        }
    }

    // ---- exceptions from the hooks propagate unchanged ----
    static void testHookExceptionPropagation() throws Exception {
        try {
            ser(new ExtThrowW());
            check(false, "writeExternal exception must propagate");
        } catch (java.io.UTFDataFormatException e) {
            check("wboom".equals(e.getMessage()), "write-side exception passes through as-is");
        }
        final byte[] rb = ser(new ExtThrowR());
        try {
            de(rb);
            check(false, "readExternal exception must propagate");
        } catch (java.io.UTFDataFormatException e) {
            check("rboom".equals(e.getMessage()), "read-side checked exception passes through as-is");
        }
        final byte[] rtb = ser(new ExtThrowRT());
        try {
            de(rtb);
            check(false, "readExternal runtime exception must propagate");
        } catch (IllegalStateException e) {
            check("rtboom".equals(e.getMessage()), "read-side runtime exception passes through as-is");
        }
    }

    // ---- serialVersionUID still participates in compatibility ----
    static void testUidMismatch() throws Exception {
        final byte[] bytes = ser(new ExtUID11(5));    // descriptor says serialVersionUID = 11
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes)) {
            @Override
            protected Class<?> resolveClass(java.io.ObjectStreamClass desc)
                    throws IOException, ClassNotFoundException {
                if (desc.getName().endsWith("ExtUID11")) return ExtUID99.class;  // local UID = 99
                return super.resolveClass(desc);
            }
        };
        try {
            ois.readObject();
            check(false, "UID mismatch must be rejected");
        } catch (InvalidClassException ice) {
            String m = ice.getMessage();
            check(m != null && m.contains("local class incompatible"),
                  "message says local class incompatible: " + m);
            check(m.contains("stream classdesc serialVersionUID = 11"),
                  "message carries the stream UID: " + m);
            check(m.contains("local class serialVersionUID = 99"),
                  "message carries the local UID: " + m);
        }
        ois.close();
    }

    // ---- the stream really carries the declared UID and the Externalizable flags ----
    static void testUidAndFlagsInStream() throws Exception {
        byte[] bytes = ser(new ExtUID11(5));
        java.io.DataInputStream d = new java.io.DataInputStream(
                new java.io.ByteArrayInputStream(bytes));
        d.readShort(); d.readShort();                 // magic + version
        check(d.readUnsignedByte() == 0x73, "first tag is TC_OBJECT");
        check(d.readUnsignedByte() == 0x72, "next tag is TC_CLASSDESC");
        check(d.readUTF().endsWith("ExtUID11"), "descriptor names the class");
        check(d.readLong() == 11L, "declared serialVersionUID written into the descriptor");
        int flags = d.readUnsignedByte();
        check(flags == (java.io.ObjectStreamConstants.SC_EXTERNALIZABLE
                        | java.io.ObjectStreamConstants.SC_BLOCK_DATA),
              "flags are SC_EXTERNALIZABLE|SC_BLOCK_DATA: 0x" + Integer.toHexString(flags));
        d.close();
    }

    // ---- full DataOutput/DataInput primitive surface incl. non-ASCII UTF ----
    static void testFullPrimitiveSurface() throws Exception {
        ExtFull src = new ExtFull((byte) -5, (short) 300, '中', -42, 1L << 41,
                                  0.25f, -0.5d, true, "héllo");
        ExtFull back = (ExtFull) de(ser(src));
        check(back.by == (byte) -5 && back.sh == (short) 300, "byte/short");
        check(back.ch == '中', "char");
        check(back.i == -42 && back.l == (1L << 41), "int/long");
        check(back.f == 0.25f && back.d == -0.5d, "float/double bit-exact");
        check(back.bo, "boolean");
        check("héllo".equals(back.u), "non-ASCII UTF round-trip");
    }

    /** writeExternal may serialize whole objects; readExternal reads them back. */
    static class ExtHolder implements Externalizable {
        private static final long serialVersionUID = 20L;
        static Object received;
        public ExtHolder() { }
        public void writeExternal(ObjectOutput out) throws IOException { out.writeObject("inner"); }
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            received = in.readObject();
        }
    }

    /** Reading past the data written by writeExternal ends in EOFException. */
    static class ExtShort implements Externalizable {
        private static final long serialVersionUID = 21L;
        public ExtShort() { }
        public void writeExternal(ObjectOutput out) throws IOException { }
        public void readExternal(ObjectInput in) throws IOException { in.readInt(); }
    }

    /** Two writes of one instance: back-reference semantics still apply. */
    static class ExtTwice implements Externalizable {
        private static final long serialVersionUID = 22L;
        static int ctors;
        int v;
        public ExtTwice() { ctors++; }
        ExtTwice(int v) { this.v = v; }
        public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(v); }
        public void readExternal(ObjectInput in) throws IOException { v = in.readInt(); }
    }

    // ---- the ObjectOutput/ObjectInput handed to the hooks are full-fledged ----
    static void testNestedObjectsInHooks() throws Exception {
        ExtHolder.received = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new ExtHolder());
        oos.writeObject("trailing");
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ois.readObject();
        check("inner".equals(ExtHolder.received),
              "readObject() works inside readExternal on the supplied ObjectInput");
        check("trailing".equals(ois.readObject()),
              "trailing objects after the Externalizable still readable");
        ois.close();
    }

    // ---- reading more than writeExternal wrote hits clean EOF ----
    static void testShortRead() throws Exception {
        try {
            de(ser(new ExtShort()));
            check(false, "reading past external data must fail");
        } catch (java.io.EOFException e) {
            passed++;                               // exact EOF, not corruption
        }
    }

    // ---- reference identity: one instance written twice reads back identical ----
    static void testBackReference() throws Exception {
        ExtTwice.ctors = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        ExtTwice t = new ExtTwice(9);
        oos.writeObject(t);
        oos.writeObject(t);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ExtTwice x = (ExtTwice) ois.readObject();
        ExtTwice y = (ExtTwice) ois.readObject();
        ois.close();
        check(x == y, "same instance twice -> back-reference (identity)");
        check(ExtTwice.ctors == 1, "one no-arg ctor for a back-referenced instance: " + ExtTwice.ctors);
        check(x.v == 9 && y.v == 9, "external data only written/read once");
    }

    /** writeReplace also applies to Externalizable instances. */
    static class ExtReplace implements Externalizable {
        private static final long serialVersionUID = 23L;
        static int wr;
        public ExtReplace() { }
        public void writeExternal(ObjectOutput out) throws IOException { }
        public void readExternal(ObjectInput in) throws IOException { }
        private Object writeReplace() { wr++; return "subst"; }
    }

    // ---- writeReplace substitution and interface hierarchy ----
    static void testExtReplace() throws Exception {
        ExtReplace.wr = 0;
        Object r = de(ser(new ExtReplace()));
        check("subst".equals(r), "writeReplace applies to Externalizable instances");
        check(ExtReplace.wr == 1, "writeReplace invoked once");
        check(java.io.Serializable.class.isAssignableFrom(ExtReplace.class),
              "Externalizable instance IS Serializable via the interface hierarchy");
        check(ObjectOutput.class.isInterface() && ObjectInput.class.isInterface(),
              "hook parameter types are the public interfaces");
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testInterfaceStructure();
        testCtorBeforeReadExternal();
        testFieldsNotAutoSaved();
        testNoValidConstructorExact();
        testHookExceptionPropagation();
        testUidMismatch();
        testUidAndFlagsInStream();
        testFullPrimitiveSurface();
        testExtReplace();
        testNestedObjectsInHooks();
        testShortRead();
        testBackReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
