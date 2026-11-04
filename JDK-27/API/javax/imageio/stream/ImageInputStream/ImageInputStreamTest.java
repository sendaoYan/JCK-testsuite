/*
 * @test
 * @summary javax.imageio.stream.ImageInputStream conformance: white-box interface + black-box behavior.
 * @run main ImageInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;

public class ImageInputStreamTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.ImageInputStream");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 2, "direct interface count");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.io.DataInput"), "extends java.io.DataInput");
        check(si.contains("java.io.Closeable"), "extends java.io.Closeable");
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
            dos.writeBytes("AB");
            dos.writeChars("CD");
        }
        byte[] bytes = bos.toByteArray();

        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
        check(iis != null, "createImageInputStream non-null");
        try {
            check(iis.getByteOrder() == ByteOrder.BIG_ENDIAN, "default byte order BIG_ENDIAN");
            iis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            check(iis.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "setByteOrder round-trip");
            iis.setByteOrder(ByteOrder.BIG_ENDIAN);
            check(iis.isCached(), "isCached");
            check(iis.getStreamPosition() == 0L, "initial position 0");
            check(iis.getBitOffset() == 0, "initial bit offset 0");
            iis.setBitOffset(3);
            check(iis.getBitOffset() == 3, "setBitOffset round-trip");
            iis.setBitOffset(0);
            check(iis.readByte() == 0x7F, "readByte");
            check(iis.readShort() == 0x1234, "readShort");
            check(iis.readInt() == 0x12345678, "readInt");
            check(iis.readLong() == 0x123456789ABCDEF0L, "readLong");
            check(iis.readFloat() == 1.5f, "readFloat");
            check(iis.readDouble() == 2.25, "readDouble");
            check(iis.readBoolean(), "readBoolean");
            check(iis.readChar() == 'Z', "readChar");
            check("hello".equals(iis.readUTF()), "readUTF");
            IIOByteBuffer iio = new IIOByteBuffer(new byte[0], 0, 0);
            iis.readBytes(iio, 2);
            check(iio.getLength() == 2 && iio.getData()[0] == 'A' && iio.getData()[1] == 'B', "readBytes AB");
            check(iis.readChar() == 'C', "readChar C");
            check(iis.readChar() == 'D', "readChar D");
            check(iis.getStreamPosition() == bytes.length, "position at end");

            iis.seek(0L);
            iis.mark();
            iis.readInt();
            iis.readInt();
            iis.reset();
            check(iis.getStreamPosition() == 0L, "reset to mark");

            iis.seek(0L);
            byte[] partial = new byte[4];
            int n = iis.read(partial, 1, 2);
            check(n == 2, "read(off,len) count");
            check(partial[1] == 0x7F && partial[2] == 0x12, "read(off,len) values");

            iis.seek(0L);
            byte[] whole = new byte[bytes.length];
            iis.readFully(whole);
            check(Arrays.equals(whole, bytes), "readFully whole round-trip");

            iis.seek(0L);
            int skipped = iis.skipBytes(3);
            check(skipped == 3, "skipBytes(int) count");
            check(iis.getStreamPosition() == 3L, "position after skipBytes");

            check(iis.length() == -1L, "length unknown -1");

            iis.flushBefore(3L);
            check(iis.getFlushedPosition() == 3L, "flushBefore flushedPos");
            iis.flush();
            check(iis.getFlushedPosition() == iis.getStreamPosition(), "flush sets flushedPos to current position");
            expectThrows(IndexOutOfBoundsException.class, () -> iis.seek(2L), "seek below flushedPos");
            iis.close();
        } finally {
            try { iis.close(); } catch (IOException ignore) {}
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
