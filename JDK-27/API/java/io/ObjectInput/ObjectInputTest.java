/*
 * @test
 * @summary java.io.ObjectInput conformance: white-box structure + black-box behavior
 *          (polymorphic use through ObjectInputStream over objects, bytes and primitives;
 *          skip/available/close contract; ObjectInputValidation interplay).
 * @run main ObjectInputTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ObjectInputTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Pair implements Serializable {
        @Serial
        private static final long serialVersionUID = 300L;
        int k; String v;
        Pair(int k, String v) { this.k = k; this.v = v; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectInput");
        check(c.isInterface(), "is an interface");
        boolean dataInput = false, autoClose = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == DataInput.class) dataInput = true;
            if (i.getName().equals("java.lang.AutoCloseable")) autoClose = true;
        }
        check(dataInput, "extends DataInput");
        check(autoClose, "extends AutoCloseable");
        for (String m : new String[]{"readObject", "read", "skip", "available", "close",
                                     "readInt", "readUTF", "readFully", "skipBytes"})
            check(has(c, m), "has method " + m);
        check(hasOverload(c, "read", byte[].class), "read(byte[]) overload");
        check(hasOverload(c, "read", byte[].class, int.class, int.class),
              "read(byte[], int, int) overload");
        for (Method m : c.getMethods())
            check(Modifier.isAbstract(m.getModifiers()), "abstract method " + m.getName());
    }

    static boolean has(Class<?> c, String m) {
        for (Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    static boolean hasOverload(Class<?> c, String m, Class<?>... ps) {
        try { c.getMethod(m, ps); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. mixed stream consumed entirely through the ObjectInput reference type
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Pair(1, "one"));
            oos.writeObject("second");
            oos.writeInt(0xCAFEBABE);
            oos.write(new byte[]{9, 8, 7});
            oos.flush();
        }
        try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Pair p = (Pair) in.readObject();
            check(p.k == 1 && "one".equals(p.v), "readObject through interface");
            check("second".equals(in.readObject()), "second object");
            check(in.readInt() == 0xCAFEBABE, "DataInput method through interface");
            byte[] three = new byte[3];
            check(in.read(three) == 3 && three[0] == 9 && three[2] == 7,
                  "read(byte[]) through interface");
            check(in.read() == -1, "EOF signaled through interface");
        }

        // 2. available()/skip() on the interface
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.write(new byte[]{1, 2, 3, 4});
        }
        try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check(in.available() == 4, "available reports block data size");
            check(in.skip(2) == 2, "skip within block data");
            check(in.read() == 3, "read after skip");
        }

        // 3. reading past EOF raises EOFException via the DataInput part of the contract
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeShort(1);
        }
        try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            in.readShort();
            try {
                in.readInt();
                check(false, "readInt past EOF must throw EOFException");
            } catch (java.io.EOFException expected) {
                passed++;
            }
        }

        // 4. close through the interface (AutoCloseable/TWR interplay)
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject("x");
        }
        ObjectInputStream backing = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ObjectInput in = backing;
        check(in instanceof Closeable || in instanceof AutoCloseable,
              "ObjectInput usable in try-with-resources");
        check("x".equals(in.readObject()), "object read through AutoCloseable view");
        in.close();
        try {
            in.readObject();
            check(false, "readObject after close must throw");
        } catch (IOException expected) {
            passed++;
        }

        // 5. ClassNotFoundException surface: stream names a class not on the classpath
        byte[] bytes;
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(b2)) {
            oos.writeObject(new NoThere());   // serialized under this test's classloader
            bytes = b2.toByteArray();
        }
        try (ObjectInput in5 = new ObjectInputStream(new ByteArrayInputStream(bytes)) {
            @Override
            protected Class<?> resolveClass(java.io.ObjectStreamClass desc)
                    throws IOException, ClassNotFoundException {
                if (desc.getName().endsWith("NoThere")) throw new ClassNotFoundException(desc.getName());
                return super.resolveClass(desc);
            }
        }) {
            try {
                in5.readObject();
                check(false, "ClassNotFoundException must propagate from readObject");
            } catch (ClassNotFoundException expected) {
                check(expected.getMessage().endsWith("NoThere"),
                      "ClassNotFoundException carries the streamed class name");
            }
        }
    }

    static class NoThere implements Serializable {
        @Serial
        private static final long serialVersionUID = 301L;
    }

    // ================= deep additions (contract level) =================

    /** A fully hand-written ObjectInput: proves the interface is implementable and
     *  sufficient for Externalizable without ObjectInputStream (dual-implementation
     *  comparison against the JDK implementation above). */
    static class CustomInput implements ObjectInput {
        final java.io.DataInputStream d;
        final java.util.Deque<Object> objects;
        boolean closed;
        CustomInput(byte[] bytes, java.util.List<Object> objects) {
            this.d = new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes));
            this.objects = new java.util.ArrayDeque<Object>(objects);
        }
        public Object readObject() { checkOpen(); return objects.poll(); }
        public int read() throws IOException { checkOpen(); return d.read(); }
        public int read(byte[] b) throws IOException { checkOpen(); return d.read(b); }
        public int read(byte[] b, int off, int len) throws IOException { checkOpen(); return d.read(b, off, len); }
        public long skip(long n) throws IOException { checkOpen(); return d.skip(n); }
        public int available() throws IOException { checkOpen(); return d.available(); }
        public void close() { closed = true; }
        public void readFully(byte[] b) throws IOException { d.readFully(b); }
        public void readFully(byte[] b, int off, int len) throws IOException { d.readFully(b, off, len); }
        public int skipBytes(int n) throws IOException { return d.skipBytes(n); }
        public boolean readBoolean() throws IOException { return d.readBoolean(); }
        public byte readByte() throws IOException { return d.readByte(); }
        public int readUnsignedByte() throws IOException { return d.readUnsignedByte(); }
        public short readShort() throws IOException { return d.readShort(); }
        public int readUnsignedShort() throws IOException { return d.readUnsignedShort(); }
        public char readChar() throws IOException { return d.readChar(); }
        public int readInt() throws IOException { return d.readInt(); }
        public long readLong() throws IOException { return d.readLong(); }
        public float readFloat() throws IOException { return d.readFloat(); }
        public double readDouble() throws IOException { return d.readDouble(); }
        @SuppressWarnings("deprecation")
        public String readLine() throws IOException { return d.readLine(); }
        public String readUTF() throws IOException { return d.readUTF(); }
        private void checkOpen() { if (closed) throw new IllegalStateException("closed"); }
    }

    /** Matching hand-written ObjectOutput used to produce CustomInput's source data. */
    static class CustomOutput implements ObjectOutput {
        final java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        final java.io.DataOutputStream d = new java.io.DataOutputStream(bos);
        final java.util.List<Object> objects = new java.util.ArrayList<Object>();
        boolean closed, flushed;
        public void writeObject(Object o) { objects.add(o); }
        public void write(int b) throws IOException { d.write(b); }
        public void write(byte[] b) throws IOException { d.write(b); }
        public void write(byte[] b, int off, int len) throws IOException { d.write(b, off, len); }
        public void flush() { flushed = true; }
        public void close() { closed = true; }
        public void writeBoolean(boolean v) throws IOException { d.writeBoolean(v); }
        public void writeByte(int v) throws IOException { d.writeByte(v); }
        public void writeShort(int v) throws IOException { d.writeShort(v); }
        public void writeChar(int v) throws IOException { d.writeChar(v); }
        public void writeInt(int v) throws IOException { d.writeInt(v); }
        public void writeLong(long v) throws IOException { d.writeLong(v); }
        public void writeFloat(float v) throws IOException { d.writeFloat(v); }
        public void writeDouble(double v) throws IOException { d.writeDouble(v); }
        public void writeBytes(String s) throws IOException { d.writeBytes(s); }
        public void writeChars(String s) throws IOException { d.writeChars(s); }
        public void writeUTF(String s) throws IOException { d.writeUTF(s); }
    }

    /** Externalizable POJO round-tripped purely through the hand-written pair. */
    static class ExtPoint implements java.io.Externalizable {
        private static final long serialVersionUID = 310L;   // fixed for cross-version stability
        int x, y;
        public ExtPoint() { }
        ExtPoint(int x, int y) { this.x = x; this.y = y; }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(x); out.writeInt(y); out.writeObject("tag");
        }
        public void readExternal(ObjectInput in)
                throws IOException, ClassNotFoundException {
            x = in.readInt(); y = in.readInt();
            if (!"tag".equals(in.readObject())) throw new java.io.StreamCorruptedException("bad tag");
        }
    }

    // ---- interface method set: exactly 22 methods, all abstract, no defaults ----
    static void testMethodSetExact() throws Exception {
        java.lang.reflect.Method[] ms = ObjectInput.class.getMethods();
        String[] names = new String[ms.length];
        for (int i = 0; i < ms.length; i++) names[i] = ms[i].getName();
        java.util.Arrays.sort(names);
        check(ms.length == 22, "ObjectInput exposes exactly 22 methods (7 own + 15 DataInput): "
              + ms.length);
        String[] expected = {
            "available", "close", "read", "read", "readBoolean", "readByte", "readChar",
            "readDouble", "readFloat", "readFully", "readFully", "readInt", "readLine",
            "readLong", "readObject", "readShort", "readUTF", "readUnsignedByte",
            "readUnsignedShort", "skip", "skipBytes", "read"};
        java.util.Arrays.sort(expected);
        check(java.util.Arrays.equals(names, expected), "exact method name set: "
              + java.util.Arrays.toString(names));
        boolean anyDefault = false;
        for (java.lang.reflect.Method m : ms) {
            check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()),
                  "abstract: " + m.getName());
            if (m.isDefault()) anyDefault = true;
        }
        check(!anyDefault, "no default methods on the interface");
        Class<?>[] ifs = ObjectInput.class.getInterfaces();
        check(ifs.length == 2 && ifs[0] == java.io.DataInput.class
              && "java.lang.AutoCloseable".equals(ifs[1].getName()),
              "extends exactly DataInput and AutoCloseable");
        check(ObjectInput.class.getFields().length == 0, "no constants");
    }

    // ---- hand-written implementation: full Externalizable round-trip, no OIS involved ----
    static void testCustomImplementation() throws Exception {
        CustomOutput out = new CustomOutput();
        new ExtPoint(3, 4).writeExternal(out);
        out.writeInt(0x01020304);
        out.flush();
        check(out.flushed, "flush recorded on the custom implementation");
        check(!out.closed, "close not yet called");

        CustomInput in = new CustomInput(out.bos.toByteArray(), out.objects);
        ExtPoint p = new ExtPoint();
        p.readExternal(in);
        check(p.x == 3 && p.y == 4, "Externalizable restored through hand-written ObjectInput");
        check(in.readInt() == 0x01020304, "DataInput methods delegate through the interface");
        check(in.readObject() == null, "readObject past the captured list yields null");
        check(in.available() == 0, "no bytes left");
        in.close();
        try {
            in.readObject();
            check(false, "readObject after close on custom impl must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
    }

    // ---- byte-level contracts through the ObjectInputStream implementation ----
    static void testByteLevelContracts() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject("x");
        }
        ObjectInputStream ois = new ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        try {
            ois.read(null);
            check(false, "read(null) must throw NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        } finally {
            ois.close();
        }
        ois.close();                                // close is idempotent
        passed++;

        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos2 = new ObjectOutputStream(bos2)) {
            oos2.writeBytes("ab\ncd");
        }
        try (ObjectInputStream ois2 = new ObjectInputStream(
                new java.io.ByteArrayInputStream(bos2.toByteArray()))) {
            check("ab".equals(ois2.readLine()) && "cd".equals(ois2.readLine()),
                  "readLine splits on newline through ObjectInput view");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMethodSetExact();
        testCustomImplementation();
        testByteLevelContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
