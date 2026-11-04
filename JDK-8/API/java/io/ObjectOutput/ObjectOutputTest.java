/*
 * @test
 * @summary java.io.ObjectOutput API conformance: full contract coverage.
 *          Polymorphic writes of objects and primitives through ObjectOutputStream,
 *          null object round-trip, flush/close contract, framing readable by
 *          ObjectInputStream.
 * @run main ObjectOutputTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class ObjectOutputTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Bean implements Serializable {
        private static final long serialVersionUID = 400L;
        int n; String s;
        Bean(int n, String s) { this.n = n; this.s = s; }
    }

    // ---- 1. the whole surface through the interface reference ----
    static void testFullSurface() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);

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
        out.writeBytes("AB");
        out.writeChars("CD");
        out.write(new byte[]{1, 2, 3});
        out.write(new byte[]{9, 8, 7, 6}, 1, 2);
        out.flush();
        check(bos.size() > 0, "flush made bytes visible");

        ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
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
        in.close();
        out.close();
    }

    // ---- 2. standalone null round-trip ----
    static void testNullObject() throws Exception {
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        ObjectOutput o = new ObjectOutputStream(b2);
        o.writeObject(null);
        o.close();
        ObjectInput i = new ObjectInputStream(new ByteArrayInputStream(b2.toByteArray()));
        check(i.readObject() == null, "standalone null round-trip");
        i.close();
    }

    // ---- 3. close propagates to the underlying stream ----
    static void testClosePropagation() throws Exception {
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
    }

    // ---- 4. flush idempotence ----
    static void testFlushIdempotent() throws Exception {
        ByteArrayOutputStream b4 = new ByteArrayOutputStream();
        ObjectOutput view = new ObjectOutputStream(b4);
        view.flush();
        view.flush();
        view.writeObject("tw");
        view.close();
        check(b4.size() > 0, "flush twice is safe");
    }

    public static void main(String[] args) throws Exception {
        testFullSurface();
        testNullObject();
        testClosePropagation();
        testFlushIdempotent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
