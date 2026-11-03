/*
 * @test
 * @summary javax.imageio.stream.ImageOutputStream conformance: white-box interface + black-box behavior.
 * @run main ImageOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageOutputStream;

public class ImageOutputStreamTest {
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
        Class<?> c = Class.forName("javax.imageio.stream.ImageOutputStream");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 2, "direct interface count");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.stream.ImageInputStream"), "extends javax.imageio.stream.ImageInputStream");
        check(si.contains("java.io.DataOutput"), "extends java.io.DataOutput");
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
        check(dm.contains("flushBefore"), "method flushBefore");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
        check(ios != null, "createImageOutputStream non-null");
        try {
            check(ios.getByteOrder() == ByteOrder.BIG_ENDIAN, "default byte order BIG_ENDIAN");
            ios.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            check(ios.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "setByteOrder round-trip");
            ios.setByteOrder(ByteOrder.BIG_ENDIAN);
            check(ios.isCached(), "isCached");

            ios.writeByte(0x7F);
            ios.writeShort(0x1234);
            ios.writeInt(0x12345678);
            ios.writeLong(0x123456789ABCDEF0L);
            ios.writeFloat(1.5f);
            ios.writeDouble(2.25);
            ios.writeBoolean(true);
            ios.writeChar('Z');
            ios.writeUTF("hello");
            ios.writeBytes("AB");
            ios.writeChars("CD");
            ios.write(new byte[]{9, 8});
            ios.write(new byte[]{0, 7, 6}, 1, 2);
            ios.writeShorts(new short[]{0x1111, 0x2222}, 0, 2);
            ios.writeInts(new int[]{0x33333333, 0x44444444}, 0, 2);
            ios.writeLongs(new long[]{0x5555555555555555L}, 0, 1);
            ios.writeFloats(new float[]{3.5f}, 0, 1);
            ios.writeDoubles(new double[]{4.5}, 0, 1);

            check(ios.length() == 79L, "length after writes");
            check(ios.getStreamPosition() == 79L, "position after writes");

            ios.seek(0L);
            check(ios.readByte() == 0x7F, "readByte back");
            check(ios.readShort() == 0x1234, "readShort back");
            check(ios.readInt() == 0x12345678, "readInt back");
            check(ios.readLong() == 0x123456789ABCDEF0L, "readLong back");
            check(ios.readFloat() == 1.5f, "readFloat back");
            check(ios.readDouble() == 2.25, "readDouble back");
            check(ios.readBoolean(), "readBoolean back");
            check(ios.readChar() == 'Z', "readChar back");
            check("hello".equals(ios.readUTF()), "readUTF back");
            IIOByteBuffer iio = new IIOByteBuffer(new byte[0], 0, 0);
            ios.readBytes(iio, 2);
            check(iio.getLength() == 2 && iio.getData()[0] == 'A' && iio.getData()[1] == 'B', "readBytes AB back");
            check(ios.readChar() == 'C', "readChar C back");
            check(ios.readChar() == 'D', "readChar D back");
            byte[] two = new byte[2];
            ios.readFully(two);
            check(two[0] == 9 && two[1] == 8, "write(byte[]) read back");
            byte[] offTwo = new byte[2];
            ios.readFully(offTwo);
            check(offTwo[0] == 7 && offTwo[1] == 6, "write(byte[],off,len) read back");
            check(ios.readShort() == 0x1111, "writeShorts[0] read back");
            check(ios.readShort() == 0x2222, "writeShorts[1] read back");
            check(ios.readInt() == 0x33333333, "writeInts[0] read back");
            check(ios.readInt() == 0x44444444, "writeInts[1] read back");
            check(ios.readLong() == 0x5555555555555555L, "writeLongs read back");
            check(ios.readFloat() == 3.5f, "writeFloats read back");
            check(ios.readDouble() == 4.5, "writeDoubles read back");
            check(ios.getStreamPosition() == 79L, "position at end");

            ios.seek(0L);
            ios.mark();
            ios.readInt();
            ios.readInt();
            ios.reset();
            check(ios.getStreamPosition() == 0L, "reset to mark");

            ios.seek(0L);
            long skipped = ios.skipBytes(4);
            check(skipped == 4L, "skipBytes count");

            ios.seek(8L);
            ios.flushBefore(8L);
            check(ios.getFlushedPosition() == 8L, "flushBefore flushedPos");
            byte[] flushed = bos.toByteArray();
            check(flushed.length == 8, "flushed 8 bytes to underlying");
            final ImageOutputStream stream = ios;
            expectThrows(IndexOutOfBoundsException.class, () -> stream.seek(7L), "seek below flushedPos");
            ios.close();
            ios = null;
            check(bos.size() == 79, "close flushes all bytes");
        } finally {
            if (ios != null) { try { ios.close(); } catch (IOException ignore) {} }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
