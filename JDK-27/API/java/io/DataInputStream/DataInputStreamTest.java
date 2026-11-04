/*
 * @test
 * @summary java.io.DataInputStream conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructor null semantics, primitive round-trip,
 *          unsigned reads, EOFException for every readXxx, readFully (exact/EOF/IOBE/NPE),
 *          skipBytes actual-count, readUTF modified-UTF-8 + static readUTF(DataInput),
 *          readLine, read(byte[],off,len) validation.
 * @run main DataInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class DataInputStreamTest {
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

    static DataInputStream stream(byte[] data) {
        return new DataInputStream(new ByteArrayInputStream(data));
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.DataInputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.FilterInputStream.class, "extends FilterInputStream");
        check(hasInterface(c, "java.io.DataInput"), "implements DataInput");
        hasMethod(c, "readInt");
        hasMethod(c, "readLong");
        hasMethod(c, "readUTF");
        hasMethod(c, "readFully", byte[].class);
        hasMethod(c, "readUnsignedByte");
    }

    // ---- 1. constructor null semantics ----
    static void testConstructorNull() throws Exception {
        DataInputStream dis = new DataInputStream(null);
        checkThrows(NullPointerException.class, () -> dis.readByte(), "readByte on null-backed");
    }

    // ---- 2. primitive round-trip ----
    static void testReadPrimitivesRoundTrip() throws Exception {
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

        DataInputStream dis = stream(bos.toByteArray());
        check(dis.readBoolean(), "readBoolean true");
        check(!dis.readBoolean(), "readBoolean false");
        check(dis.readByte() == 0x7F, "readByte");
        check(dis.readShort() == 0x1234, "readShort");
        check(dis.readChar() == 'Z', "readChar");
        check(dis.readInt() == 0x12345678, "readInt");
        check(dis.readLong() == 0x1234567890ABCDEFL, "readLong");
        check(dis.readFloat() == 1.5f, "readFloat");
        check(dis.readDouble() == -2.25, "readDouble");
    }

    // ---- 3. unsigned reads ----
    static void testUnsignedReads() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(0xFF);
        dos.writeShort(0xFFFF);
        dos.flush();
        DataInputStream dis = stream(bos.toByteArray());
        check(dis.readUnsignedByte() == 255, "readUnsignedByte 0xFF -> 255");
        check(dis.readUnsignedShort() == 65535, "readUnsignedShort 0xFFFF -> 65535");
    }

    // ---- 4. EOFException for every readXxx ----
    static void testEOFExceptions() {
        final DataInputStream empty = stream(new byte[0]);
        checkThrows(EOFException.class, () -> empty.readBoolean(), "readBoolean EOF");
        checkThrows(EOFException.class, () -> empty.readByte(), "readByte EOF");
        checkThrows(EOFException.class, () -> empty.readUnsignedByte(), "readUnsignedByte EOF");
        checkThrows(EOFException.class, () -> empty.readShort(), "readShort EOF");
        checkThrows(EOFException.class, () -> empty.readUnsignedShort(), "readUnsignedShort EOF");
        checkThrows(EOFException.class, () -> empty.readChar(), "readChar EOF");
        checkThrows(EOFException.class, () -> empty.readInt(), "readInt EOF");
        checkThrows(EOFException.class, () -> empty.readLong(), "readLong EOF");
        checkThrows(EOFException.class, () -> empty.readFloat(), "readFloat EOF");
        checkThrows(EOFException.class, () -> empty.readDouble(), "readDouble EOF");
        checkThrows(EOFException.class, () -> empty.readUTF(), "readUTF EOF");

        final DataInputStream oneByte = stream(new byte[]{0x00});
        checkThrows(EOFException.class, () -> oneByte.readShort(), "readShort truncated");
    }

    // ---- 5. readFully ----
    static void testReadFully() throws Exception {
        DataInputStream dis = stream(new byte[]{1, 2, 3, 4});
        byte[] b = new byte[4];
        dis.readFully(b);
        check(Arrays.equals(b, new byte[]{1, 2, 3, 4}), "readFully exact");

        DataInputStream off = stream(new byte[]{10, 20, 30});
        byte[] ob = new byte[4];
        off.readFully(ob, 1, 2);
        check(ob[0] == 0 && ob[1] == 10 && ob[2] == 20 && ob[3] == 0, "readFully(off,len)");

        final DataInputStream shortIn = stream(new byte[]{1, 2});
        checkThrows(EOFException.class, () -> shortIn.readFully(new byte[3]),
                    "readFully short -> EOFException");

        final DataInputStream neg = stream(new byte[]{1, 2, 3});
        checkThrows(IndexOutOfBoundsException.class, () -> neg.readFully(new byte[3], 0, -1),
                    "readFully len<0 -> IndexOutOfBoundsException");

        final DataInputStream nul = stream(new byte[]{1, 2, 3});
        checkThrows(NullPointerException.class, () -> nul.readFully(null, 0, 1),
                    "readFully null -> NPE");
    }

    // ---- 6. skipBytes ----
    static void testSkipBytes() throws Exception {
        DataInputStream dis = stream(new byte[]{1, 2, 3, 4, 5});
        check(dis.skipBytes(2) == 2, "skipBytes(2)");
        check(dis.readByte() == 3, "read after skipBytes");
        check(dis.skipBytes(100) == 2, "skipBytes past end returns available");
        check(dis.skipBytes(1) == 0, "skipBytes at EOF returns 0");
    }

    // ---- 7. readUTF ----
    static void testReadUTF() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeUTF("");
        dos.writeUTF("hello");
        dos.writeUTF("héllo é中文");
        dos.flush();

        DataInputStream dis = stream(bos.toByteArray());
        check("".equals(dis.readUTF()), "readUTF empty");
        check("hello".equals(dis.readUTF()), "readUTF ascii");
        check("héllo é中文".equals(dis.readUTF()), "readUTF non-ASCII modified UTF-8");

        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        DataOutputStream d2 = new DataOutputStream(b2);
        d2.writeUTF("static");
        d2.flush();
        DataInputStream s = stream(b2.toByteArray());
        check("static".equals(DataInputStream.readUTF(s)), "static readUTF(DataInput)");
    }

    // ---- 8. readLine (deprecated but present) ----
    static void testReadLine() throws Exception {
        DataInputStream dis = stream("one\ntwo\r\nthree".getBytes("US-ASCII"));
        check("one".equals(dis.readLine()), "readLine first");
        check("two".equals(dis.readLine()), "readLine second (CRLF)");
        check("three".equals(dis.readLine()), "readLine third (EOF)");
    }

    // ---- 9. read(byte[],off,len) validation ----
    static void testReadArrayValidation() {
        final DataInputStream dis = stream(new byte[]{1, 2, 3});
        checkThrows(NullPointerException.class, () -> dis.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> dis.read(new byte[2], 0, 3),
                    "read(b,0,3) -> IndexOutOfBoundsException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructorNull();
        testReadPrimitivesRoundTrip();
        testUnsignedReads();
        testEOFExceptions();
        testReadFully();
        testSkipBytes();
        testReadUTF();
        testReadLine();
        testReadArrayValidation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
