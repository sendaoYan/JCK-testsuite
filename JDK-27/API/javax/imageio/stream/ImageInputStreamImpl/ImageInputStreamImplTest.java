/*
 * @test
 * @summary javax.imageio.stream.ImageInputStreamImpl conformance: white-box abstract hierarchy + black-box behavior.
 * @run main ImageInputStreamImplTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.stream.ImageInputStreamImpl;

public class ImageInputStreamImplTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try { r.run(); } catch (Throwable t) {
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
            return;
        }
        check(false, msg + " should throw " + expected.getSimpleName());
    }

    // Minimal concrete backing store driven by the inherited streamPos/seek.
    static class ByteArrayImageInputStream extends ImageInputStreamImpl {
        final byte[] data;
        ByteArrayImageInputStream(byte[] data) { this.data = data; }
        public int read() throws IOException {
            checkClosed();
            bitOffset = 0;
            if (streamPos >= data.length) return -1;
            return data[(int) streamPos++] & 0xff;
        }
        public int read(byte[] b, int off, int len) throws IOException {
            checkClosed();
            bitOffset = 0;
            if (b == null) throw new NullPointerException("b == null!");
            if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
                throw new IndexOutOfBoundsException("off < 0 || len < 0 || off+len > b.length || off+len < 0!");
            }
            if (len == 0) return 0;
            if (streamPos >= data.length) return -1;
            int n = (int) Math.min(len, data.length - streamPos);
            System.arraycopy(data, (int) streamPos, b, off, n);
            streamPos += n;
            return n;
        }
        public long length() { return data.length; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.ImageInputStreamImpl");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.stream.ImageInputStream"), "implements ImageInputStream");
        Set<String> dm = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("setByteOrder"), "method setByteOrder");
        check(dm.contains("getByteOrder"), "method getByteOrder");
        check(dm.contains("read"), "method read");
        check(dm.contains("readBytes"), "method readBytes");
        check(dm.contains("readBoolean"), "method readBoolean");
        check(dm.contains("readByte"), "method readByte");
        check(dm.contains("readUnsignedByte"), "method readUnsignedByte");
        check(dm.contains("readShort"), "method readShort");
        check(dm.contains("readUnsignedShort"), "method readUnsignedShort");
        check(dm.contains("readChar"), "method readChar");
        check(dm.contains("readInt"), "method readInt");
        check(dm.contains("readUnsignedInt"), "method readUnsignedInt");
        check(dm.contains("readLong"), "method readLong");
        check(dm.contains("readFloat"), "method readFloat");
        check(dm.contains("readDouble"), "method readDouble");
        check(dm.contains("readLine"), "method readLine");
        check(dm.contains("readUTF"), "method readUTF");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("getStreamPosition"), "method getStreamPosition");
        check(dm.contains("getBitOffset"), "method getBitOffset");
        check(dm.contains("setBitOffset"), "method setBitOffset");
        check(dm.contains("readBit"), "method readBit");
        check(dm.contains("readBits"), "method readBits");
        check(dm.contains("length"), "method length");
        check(dm.contains("skipBytes"), "method skipBytes");
        check(dm.contains("seek"), "method seek");
        check(dm.contains("mark"), "method mark");
        check(dm.contains("reset"), "method reset");
        check(dm.contains("flushBefore"), "method flushBefore");
        check(dm.contains("flush"), "method flush");
        check(dm.contains("getFlushedPosition"), "method getFlushedPosition");
        check(dm.contains("isCached"), "method isCached");
        check(dm.contains("isCachedMemory"), "method isCachedMemory");
        check(dm.contains("isCachedFile"), "method isCachedFile");
        check(dm.contains("close"), "method close");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeByte(0x7F);
            dos.writeShort(0x1234);
            dos.writeInt(0x12345678);
            dos.writeLong(0x123456789ABCDEF0L);
            dos.writeFloat(1.5f);
            dos.writeDouble(2.25);
            dos.writeBoolean(true);
            dos.writeChar('Z');
            dos.writeUTF("hello");
        }
        byte[] data = bos.toByteArray();

        ByteArrayImageInputStream iis = new ByteArrayImageInputStream(data);
        check(iis.length() == data.length, "subclass length");
        check(iis.getStreamPosition() == 0L, "initial position");
        check(iis.getByteOrder() == ByteOrder.BIG_ENDIAN, "default byte order BIG_ENDIAN");
        iis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        check(iis.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "setByteOrder round-trip");
        iis.setByteOrder(ByteOrder.BIG_ENDIAN);

        check(iis.readByte() == 0x7F, "readByte");
        check(iis.readShort() == 0x1234, "readShort");
        check(iis.readInt() == 0x12345678, "readInt");
        check(iis.readLong() == 0x123456789ABCDEF0L, "readLong");
        check(iis.readFloat() == 1.5f, "readFloat");
        check(iis.readDouble() == 2.25, "readDouble");
        check(iis.readBoolean(), "readBoolean");
        check(iis.readChar() == 'Z', "readChar");
        check("hello".equals(iis.readUTF()), "readUTF");
        check(iis.getStreamPosition() == data.length, "position at end");

        // unsigned reads at fixed offsets
        iis.seek(0L);
        check(iis.readUnsignedByte() == 0x7F, "readUnsignedByte");
        iis.seek(1L);
        check(iis.readUnsignedShort() == 0x1234, "readUnsignedShort");
        iis.seek(3L);
        check(iis.readUnsignedInt() == 0x12345678L, "readUnsignedInt");

        iis.seek(0L);
        iis.mark();
        iis.readInt();
        iis.readInt();
        iis.reset();
        check(iis.getStreamPosition() == 0L, "reset to mark");

        // readFully overloads (big-endian interpretation)
        iis.seek(0L);
        byte[] all = new byte[data.length];
        iis.readFully(all);
        check(Arrays.equals(all, data), "readFully(byte[]) round-trip");

        iis.seek(0L);
        short[] shorts = new short[1];
        iis.readFully(shorts, 0, 1);
        check(shorts[0] == (short) 0x7F12, "readFully(short[])");

        iis.seek(0L);
        char[] chars = new char[1];
        iis.readFully(chars, 0, 1);
        check(chars[0] == (char) 0x7F12, "readFully(char[])");

        iis.seek(0L);
        int[] ints = new int[1];
        iis.readFully(ints, 0, 1);
        check(ints[0] == 0x7F123412, "readFully(int[])");

        iis.seek(0L);
        long[] longs = new long[1];
        iis.readFully(longs, 0, 1);
        check(longs[0] == 0x7F12341234567812L, "readFully(long[])");

        iis.seek(0L);
        float[] floats = new float[1];
        iis.readFully(floats, 0, 1);
        check(floats[0] == Float.intBitsToFloat(0x7F123412), "readFully(float[])");

        iis.seek(0L);
        double[] doubles = new double[1];
        iis.readFully(doubles, 0, 1);
        check(doubles[0] == Double.longBitsToDouble(0x7F12341234567812L), "readFully(double[])");

        // bit operations
        check(iis.getBitOffset() == 0, "initial bit offset");
        iis.setBitOffset(3);
        check(iis.getBitOffset() == 3, "setBitOffset round-trip");
        iis.setBitOffset(0);
        iis.seek(0L);
        check(iis.readBit() == 0, "readBit high bit of 0x7F");
        check(iis.getBitOffset() == 1, "bitOffset after readBit");
        iis.setBitOffset(0);
        iis.seek(0L);
        check(iis.readBits(4) == 7L, "readBits 4 high bits of 0x7F");
        check(iis.getBitOffset() == 4, "bitOffset after readBits");
        iis.setBitOffset(0);

        // skip / flush
        iis.seek(0L);
        int skippedInt = iis.skipBytes(2);
        check(skippedInt == 2, "skipBytes(int) count");
        long skippedLong = iis.skipBytes(2);
        check(skippedLong == 2L, "skipBytes(long) count");
        check(iis.getStreamPosition() == 4L, "position after skips");
        iis.flushBefore(4L);
        check(iis.getFlushedPosition() == 4L, "flushBefore flushedPos");
        iis.flush();
        check(iis.getFlushedPosition() == iis.getStreamPosition(), "flush sets flushedPos");
        expectThrows(IndexOutOfBoundsException.class, () -> iis.seek(3L), "seek below flushedPos");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
