/*
 * @test
 * @summary java.io.DataOutputStream conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructor null semantics, write(int) low 8 bits,
 *          write(byte[],off,len) validation (NPE/IOBE), primitive round-trip, size()
 *          counter, writeBytes/writeChars layout, writeUTF length prefix + >65535 -> UTFDataFormatException
 *          + null -> NPE, flush delegation.
 * @run main DataOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.UTFDataFormatException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class DataOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.DataOutputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.FilterOutputStream.class, "extends FilterOutputStream");
        check(hasInterface(c, "java.io.DataOutput"), "implements DataOutput");
        hasMethod(c, "writeInt", int.class);
        hasMethod(c, "writeLong", long.class);
        hasMethod(c, "writeUTF", String.class);
        hasMethod(c, "size");
    }

    // ---- 1. constructor null semantics ----
    static void testConstructorNull() throws Exception {
        DataOutputStream dos = new DataOutputStream(null);
        checkThrows(NullPointerException.class, () -> dos.writeByte(1),
                    "writeByte on null-backed -> NPE");
    }

    // ---- 2. write(int) low 8 bits ----
    static void testWriteInt() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.write(0x1FF);
        dos.write(256);
        dos.flush();
        byte[] b = bos.toByteArray();
        check(b.length == 2 && (b[0] & 0xFF) == 0xFF && b[1] == 0, "write(int) low 8 bits");
        check(dos.size() == 2, "size after write(int)");
    }

    // ---- 3. write(byte[],off,len) validation ----
    static void testWriteArrayValidation() {
        final DataOutputStream dos = new DataOutputStream(new ByteArrayOutputStream());
        checkThrows(NullPointerException.class, () -> dos.write(null, 0, 1),
                    "write(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> dos.write(new byte[2], 0, 3),
                    "write(b,0,3) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> dos.write(new byte[2], -1, 1),
                    "write(b,-1,1) -> IndexOutOfBoundsException");
    }

    // ---- 4. primitive round-trip ----
    static void testPrimitivesRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeBoolean(true);
        dos.writeBoolean(false);
        dos.writeByte(0x7F);
        dos.writeShort(0x1234);
        dos.writeChar('Z');
        dos.writeInt(0x12345678);
        dos.writeLong(0x1234567890ABCDEFL);
        dos.writeFloat(1.5f);
        dos.writeDouble(-2.25);
        dos.flush();

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(dis.readBoolean(), "writeBoolean true round-trip");
        check(!dis.readBoolean(), "writeBoolean false round-trip");
        check(dis.readByte() == 0x7F, "writeByte round-trip");
        check(dis.readShort() == 0x1234, "writeShort round-trip");
        check(dis.readChar() == 'Z', "writeChar round-trip");
        check(dis.readInt() == 0x12345678, "writeInt round-trip");
        check(dis.readLong() == 0x1234567890ABCDEFL, "writeLong round-trip");
        check(dis.readFloat() == 1.5f, "writeFloat round-trip");
        check(dis.readDouble() == -2.25, "writeDouble round-trip");
    }

    // ---- 5. size() counter ----
    static void testSize() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        check(dos.size() == 0, "initial size 0");
        dos.writeBoolean(true);
        dos.writeByte(1);
        dos.writeShort(1);
        dos.writeChar('a');
        dos.writeInt(1);
        dos.writeLong(1);
        dos.writeFloat(1);
        dos.writeDouble(1);
        check(dos.size() == 30, "size sums primitive widths (1+1+2+2+4+8+4+8=30)");
        dos.writeUTF("hello");
        check(dos.size() == 37, "size after writeUTF (2 + utf length)");
    }

    // ---- 6. writeBytes / writeChars byte layout ----
    static void testWriteBytesChars() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeBytes("AB");
        dos.writeChars("CD");
        byte[] b = bos.toByteArray();
        check(Arrays.equals(b, new byte[]{0x41, 0x42, 0x00, 0x43, 0x00, 0x44}),
              "writeBytes/writeChars layout");
    }

    // ---- 7. writeUTF length prefix + >65535 + null ----
    static void testWriteUTF() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeUTF("héllo");
        byte[] b = bos.toByteArray();
        check((b[0] & 0xFF) == 0 && (b[1] & 0xFF) == 6, "writeUTF length prefix (6 UTF bytes)");
        check(b.length == 2 + 6, "writeUTF total length");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 70000; i++) sb.append('a');
        final String tooLong = sb.toString();
        final DataOutputStream big = new DataOutputStream(new ByteArrayOutputStream());
        checkThrows(UTFDataFormatException.class, () -> big.writeUTF(tooLong),
                    "writeUTF > 65535 bytes -> UTFDataFormatException");

        checkThrows(NullPointerException.class, () -> dos.writeUTF(null),
                    "writeUTF(null) -> NPE");
    }

    // ---- 8. flush delegation ----
    static void testFlush() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(7);
        dos.flush();
        check(bos.size() == 1, "flush flushes to underlying");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructorNull();
        testWriteInt();
        testWriteArrayValidation();
        testPrimitivesRoundTrip();
        testSize();
        testWriteBytesChars();
        testWriteUTF();
        testFlush();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
