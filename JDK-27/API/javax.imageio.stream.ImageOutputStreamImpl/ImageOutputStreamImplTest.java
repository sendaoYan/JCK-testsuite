/*
 * @test
 * @summary javax.imageio.stream.ImageOutputStreamImpl conformance: white-box abstract hierarchy + black-box behavior.
 * @run main ImageOutputStreamImplTest
 */

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageOutputStreamImpl;

public class ImageOutputStreamImplTest {
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
    static class ByteArrayImageOutputStream extends ImageOutputStreamImpl {
        final byte[] data;
        ByteArrayImageOutputStream(int size) { data = new byte[size]; }
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
        public void write(int b) throws IOException {
            flushBits();
            data[(int) streamPos++] = (byte) b;
        }
        public void write(byte[] b, int off, int len) throws IOException {
            flushBits();
            System.arraycopy(b, off, data, (int) streamPos, len);
            streamPos += len;
        }
        public long length() { return data.length; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.ImageOutputStreamImpl");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.stream.ImageInputStreamImpl"), "extends ImageInputStreamImpl");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.stream.ImageOutputStream"), "implements ImageOutputStream");
        Set<String> dm = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("write"), "method write");
        check(dm.contains("writeBoolean"), "method writeBoolean");
        check(dm.contains("writeByte"), "method writeByte");
        check(dm.contains("writeShort"), "method writeShort");
        check(dm.contains("writeChar"), "method writeChar");
        check(dm.contains("writeInt"), "method writeInt");
        check(dm.contains("writeLong"), "method writeLong");
        check(dm.contains("writeFloat"), "method writeFloat");
        check(dm.contains("writeDouble"), "method writeDouble");
        check(dm.contains("writeBytes"), "method writeBytes");
        check(dm.contains("writeChars"), "method writeChars");
        check(dm.contains("writeUTF"), "method writeUTF");
        check(dm.contains("writeShorts"), "method writeShorts");
        check(dm.contains("writeInts"), "method writeInts");
        check(dm.contains("writeLongs"), "method writeLongs");
        check(dm.contains("writeFloats"), "method writeFloats");
        check(dm.contains("writeDoubles"), "method writeDoubles");
        check(dm.contains("writeBit"), "method writeBit");
        check(dm.contains("writeBits"), "method writeBits");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ByteArrayImageOutputStream os = new ByteArrayImageOutputStream(256);
        check(os.length() == 256L, "subclass length");
        check(os.getStreamPosition() == 0L, "initial position");

        os.writeByte(0x11);
        os.writeInt(0x12345678);
        os.writeLong(0x123456789ABCDEF0L);
        os.writeFloat(1.5f);
        os.writeDouble(2.25);
        os.writeBoolean(true);
        os.writeChar('Z');
        os.writeUTF("hello");
        os.writeBytes("AB");
        os.writeChars("CD");
        os.writeShorts(new short[]{0x1111, 0x2222}, 0, 2);
        os.writeInts(new int[]{0x33333333}, 0, 1);
        os.writeLongs(new long[]{0x4444444444444444L}, 0, 1);
        os.writeFloats(new float[]{3.5f}, 0, 1);
        os.writeDoubles(new double[]{4.5}, 0, 1);
        check(os.getStreamPosition() == 69L, "total bytes written");

        os.seek(0L);
        check(os.readByte() == 0x11, "readByte back");
        check(os.readInt() == 0x12345678, "readInt back");
        check(os.readLong() == 0x123456789ABCDEF0L, "readLong back");
        check(os.readFloat() == 1.5f, "readFloat back");
        check(os.readDouble() == 2.25, "readDouble back");
        check(os.readBoolean(), "readBoolean back");
        check(os.readChar() == 'Z', "readChar back");
        check("hello".equals(os.readUTF()), "readUTF back");
        IIOByteBuffer iio = new IIOByteBuffer(new byte[0], 0, 0);
        os.readBytes(iio, 2);
        check(iio.getLength() == 2 && iio.getData()[0] == 'A' && iio.getData()[1] == 'B', "readBytes AB back");
        check(os.readChar() == 'C', "readChar C back");
        check(os.readChar() == 'D', "readChar D back");
        check(os.readShort() == 0x1111, "writeShorts[0] back");
        check(os.readShort() == 0x2222, "writeShorts[1] back");
        check(os.readInt() == 0x33333333, "writeInts back");
        check(os.readLong() == 0x4444444444444444L, "writeLongs back");
        check(os.readFloat() == 3.5f, "writeFloats back");
        check(os.readDouble() == 4.5, "writeDoubles back");

        // seek / mark / reset / flush inherited
        os.seek(0L);
        os.mark();
        os.readInt();
        os.reset();
        check(os.getStreamPosition() == 0L, "reset to mark");
        os.flush();
        check(os.getFlushedPosition() == os.getStreamPosition(), "flush sets flushedPos");
        check(os.getFlushedPosition() == 0L, "flush at position 0");
        os.seek(10L);
        os.flushBefore(10L);
        check(os.getFlushedPosition() == 10L, "flushBefore flushedPos");
        expectThrows(IndexOutOfBoundsException.class, () -> os.seek(9L), "seek below flushedPos");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
