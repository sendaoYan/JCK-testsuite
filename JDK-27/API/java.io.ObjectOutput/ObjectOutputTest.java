/*
 * @test
 * @summary java.io.ObjectOutput conformance: white-box structure + black-box behavior
 *          (polymorphic writes of objects and primitives through ObjectOutputStream,
 *          flush/close contract, framing readable by ObjectInputStream).
 * @run main ObjectOutputTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.Flushable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ObjectOutputTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Bean implements Serializable {
        @Serial
        private static final long serialVersionUID = 400L;
        int n; String s;
        Bean(int n, String s) { this.n = n; this.s = s; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectOutput");
        check(c.isInterface(), "is an interface");
        boolean dataOut = false, autoClose = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == DataOutput.class) dataOut = true;
            if (i.getName().equals("java.lang.AutoCloseable")) autoClose = true;
        }
        check(dataOut, "extends DataOutput");
        check(autoClose, "extends AutoCloseable");
        for (String m : new String[]{"writeObject", "write", "flush", "close", "writeInt",
                                     "writeUTF", "writeBoolean", "writeLong", "writeDouble",
                                     "writeBytes", "writeChars"})
            check(has(c, m), "has method " + m);
        check(hasOverload(c, "write", byte[].class), "write(byte[]) overload");
        check(hasOverload(c, "write", byte[].class, int.class, int.class),
              "write(byte[], int, int) overload");
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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);

        // 1. the whole surface through the interface reference
        out.writeObject(new Bean(5, "five"));
        out.writeObject(null);
        out.writeBoolean(true);
        out.writeByte(0x7F);
        out.writeShort(-300);
        out.writeChar('X');
        out.writeInt(0xDEADBEEF);
        out.writeLong(-1L);
        out.writeFloat(0.25f);
        out.writeDouble(-0.5);
        out.writeUTF("iface");
        out.writeBytes("AB");       // low 8 bits, 2 bytes
        out.writeChars("CD");       // 2 chars = 4 bytes
        out.write(new byte[]{1, 2, 3});
        out.write(new byte[]{9, 8, 7, 6}, 1, 2);
        out.flush();
        check(bos.size() > 0, "flush made bytes visible");

        try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Bean b = (Bean) in.readObject();
            check(b.n == 5 && "five".equals(b.s), "object written through interface");
            check(in.readObject() == null, "null object round-trip");
            check(in.readBoolean() && in.readByte() == 0x7F && in.readShort() == -300
                  && in.readChar() == 'X', "boolean/byte/short/char");
            check(in.readInt() == 0xDEADBEEF && in.readLong() == -1L
                  && in.readFloat() == 0.25f && in.readDouble() == -0.5, "int/long/float/double");
            check("iface".equals(in.readUTF()), "UTF string");
            check(in.readByte() == 'A' && in.readByte() == 'B', "writeBytes bytes");
            check(in.readChar() == 'C' && in.readChar() == 'D', "writeChars chars");
            byte[] three = new byte[3];
            in.readFully(three);
            check(Arrays.equals(three, new byte[]{1, 2, 3}), "write(byte[]) region");
            byte[] two = new byte[2];
            in.readFully(two);
            check(Arrays.equals(two, new byte[]{8, 7}), "write(byte[],off,len) region");
            check(in.read() == -1, "everything consumed in order");
        }

        // 2. null object writes/reads a TC_NULL through the interface
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        try (ObjectOutput o = new ObjectOutputStream(b2)) {
            o.writeObject(null);
        }
        try (ObjectInput i = new ObjectInputStream(new ByteArrayInputStream(b2.toByteArray()))) {
            check(i.readObject() == null, "standalone null round-trip");
        }

        // 3. close() propagates to the underlying stream; further writes surface the
        //    underlying failure (verified with a stream that rejects writes when closed)
        class Strict extends java.io.OutputStream {
            boolean closed;
            int written;
            @Override public void close() throws IOException { closed = true; }
            @Override public void write(int b) throws IOException {
                if (closed) throw new IOException("Stream closed");
                written++;
            }
            @Override public void write(byte[] b, int off, int len) throws IOException {
                if (closed) throw new IOException("Stream closed");
                written += len;
            }
            @Override public void flush() throws IOException {
                if (closed) throw new IOException("Stream closed");
            }
        }
        Strict b3 = new Strict();
        ObjectOutput o3 = new ObjectOutputStream(b3);
        o3.writeObject("before");
        o3.flush();
        check(b3.written > 0, "bytes reached the underlying stream");
        o3.close();
        check(b3.closed, "close() closed the underlying stream");
        try {
            o3.writeObject("after");
            check(false, "writeObject after close must throw");
        } catch (java.io.IOException expected) {
            passed++;
        }

        // 4. flush() is idempotent and the interface is AutoCloseable/Flushable-usable
        ByteArrayOutputStream b4 = new ByteArrayOutputStream();
        try (ObjectOutputStream raw = new ObjectOutputStream(b4)) {
            ObjectOutput view = raw;
            view.flush();
            view.flush();
            view.writeObject("tw");
        }
        check(b4.size() > 0, "flush twice is safe");
        check(ObjectOutput.class.isAssignableFrom(ObjectOutputStream.class),
              "ObjectOutputStream implements ObjectOutput");
        check(Closeable.class.isAssignableFrom(ObjectOutputStream.class)
              && Flushable.class.isAssignableFrom(ObjectOutputStream.class),
              "implementing class is Closeable and Flushable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
