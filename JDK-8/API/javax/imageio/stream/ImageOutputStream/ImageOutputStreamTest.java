/*
 * @test
 * @summary javax.imageio.stream.ImageOutputStream conformance: interface hierarchy and methods.
 * @run main ImageOutputStreamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.nio.ByteOrder;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingAction { void run() throws Exception; }
    static void expectExact(Class<? extends Throwable> expected, ThrowingAction r, String msg) {
        boolean threw = false;
        Throwable caught = null;
        try { r.run(); } catch (Throwable t) { threw = true; caught = t; }
        check(threw, msg + " should throw");
        check(caught.getClass() == expected,
              msg + " expected " + expected.getName() + " but got " + caught.getClass().getName());
    }

    // ---- Part 2: precise method signatures ----
    static void testSignatures() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.ImageOutputStream");
        int writeOverloads = 0, writeCharsOverloads = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("write")) writeOverloads++;
            if (m.getName().equals("writeChars")) writeCharsOverloads++;
        }
        check(writeOverloads == 3, "write overloads == 3");
        check(writeCharsOverloads == 2, "writeChars overloads == 2 (String + char[])");

        Method m;
        m = c.getMethod("write", int.class);
        check(m.getReturnType() == void.class, "write(int) returns void");
        m = c.getMethod("write", byte[].class);
        check(m.getReturnType() == void.class, "write(byte[]) returns void");
        m = c.getMethod("write", byte[].class, int.class, int.class);
        check(m.getReturnType() == void.class, "write(byte[],int,int) returns void");
        m = c.getMethod("writeShort", int.class);
        check(m.getReturnType() == void.class, "writeShort(int) returns void");
        m = c.getMethod("writeChar", int.class);
        check(m.getReturnType() == void.class, "writeChar(int) returns void");
        m = c.getMethod("writeByte", int.class);
        check(m.getReturnType() == void.class, "writeByte(int) takes int");
        m = c.getMethod("writeBits", long.class, int.class);
        check(m.getReturnType() == void.class, "writeBits(long,int) returns void");
        m = c.getMethod("writeBit", int.class);
        check(m.getReturnType() == void.class, "writeBit(int) returns void");
        m = c.getMethod("writeShorts", short[].class, int.class, int.class);
        check(m.getReturnType() == void.class, "writeShorts(short[],int,int)");
        m = c.getMethod("writeChars", char[].class, int.class, int.class);
        check(m.getReturnType() == void.class, "writeChars(char[],int,int)");
        m = c.getMethod("writeInts", int[].class, int.class, int.class);
        check(m.getReturnType() == void.class, "writeInts(int[],int,int)");
        m = c.getMethod("writeLongs", long[].class, int.class, int.class);
        check(m.getReturnType() == void.class, "writeLongs(long[],int,int)");
        m = c.getMethod("writeFloats", float[].class, int.class, int.class);
        check(m.getReturnType() == void.class, "writeFloats(float[],int,int)");
        m = c.getMethod("writeDoubles", double[].class, int.class, int.class);
        check(m.getReturnType() == void.class, "writeDoubles(double[],int,int)");
        m = c.getMethod("writeBytes", String.class);
        check(m.getExceptionTypes().length == 1
              && m.getExceptionTypes()[0] == IOException.class, "writeBytes throws IOException");
        m = c.getMethod("writeUTF", String.class);
        check(m.getExceptionTypes().length == 1
              && m.getExceptionTypes()[0] == IOException.class, "writeUTF throws IOException");
        m = c.getMethod("flushBefore", long.class);
        check(m.getExceptionTypes().length == 1
              && m.getExceptionTypes()[0] == IOException.class, "flushBefore redeclared throws IOException");
        // DataOutput contract inherited: all its methods visible through the interface
        Class<?> dataOutput = Class.forName("java.io.DataOutput");
        int missing = 0;
        for (Method dm : dataOutput.getMethods()) {
            try {
                c.getMethod(dm.getName(), dm.getParameterTypes());
            } catch (NoSuchMethodException e) {
                missing++;
            }
        }
        check(missing == 0, "every DataOutput method re-exposed by ImageOutputStream");
    }

    // ---- Part 3: behavioral contract via a JDK-provided implementation ----
    static byte[] bytesOf(MemoryCacheImageOutputStream s, ByteArrayOutputStream underlying) throws IOException {
        s.seek(s.length()); // jump past everything written so far
        s.flush();          // push it all to the underlying stream
        return underlying.toByteArray();
    }

    static void testBehavior() throws Exception {
        // byte-order layouts
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream os = new MemoryCacheImageOutputStream(bos);
        try {
            os.writeShort(0x1234);
            os.writeInt(0x12345678);
            os.writeLong(0x0102030405060708L);
            os.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            os.writeShort(0x1234);
            os.writeInt(0x12345678);
            os.writeLong(0x0102030405060708L);
            // cross-order interchange: LITTLE-written int read back as BIG swaps halves
            os.setByteOrder(ByteOrder.BIG_ENDIAN);
            os.seek(16);
            check(os.readInt() == 0x78563412, "LITTLE-written bytes read BIG under default order");
            os.seek(2);
            check(os.readInt() == 0x12345678, "write/read self-consistent BIG");
            byte[] b = bytesOf(os, bos);
            check(b.length == 28, "14 bytes BIG + 14 bytes LITTLE");
            check(b[0] == 0x12 && b[1] == 0x34, "writeShort BIG layout");
            check(b[2] == 0x12 && b[3] == 0x34 && b[4] == 0x56 && b[5] == 0x78, "writeInt BIG layout");
            check(b[6] == 0x01 && b[13] == 0x08, "writeLong BIG layout");
            check((b[14] & 0xFF) == 0x34 && (b[15] & 0xFF) == 0x12, "writeShort LITTLE layout");
            check((b[16] & 0xFF) == 0x78 && (b[19] & 0xFF) == 0x12, "writeInt LITTLE layout");
            check((b[20] & 0xFF) == 0x08 && (b[27] & 0xFF) == 0x01, "writeLong LITTLE layout");

            // writeBytes truncates chars to low 8 bits; writeChars writes full chars
            ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
            MemoryCacheImageOutputStream os2 = new MemoryCacheImageOutputStream(bos2);
            os2.writeBytes("AŁB"); // low bytes: 0x41, 0x41, 0x42
            check(os2.getStreamPosition() == 3L, "writeBytes writes one byte per char");
            byte[] b2 = bytesOf(os2, bos2);
            check((b2[0] & 0xFF) == 0x41 && (b2[1] & 0xFF) == 0x41 && (b2[2] & 0xFF) == 0x42, "writeBytes keeps low byte only");
            os2.setByteOrder(ByteOrder.BIG_ENDIAN);
            os2.writeChars("AB");
            check(os2.getStreamPosition() == 7L, "writeChars(String) writes two bytes per char");
            byte[] b3 = bytesOf(os2, bos2);
            check(b3[3] == 0x00 && (b3[4] & 0xFF) == 0x41 && b3[5] == 0x00 && (b3[6] & 0xFF) == 0x42, "writeChars BIG layout");
            ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
            MemoryCacheImageOutputStream os3 = new MemoryCacheImageOutputStream(bos3);
            os3.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            os3.writeChars("AB");
            byte[] b4 = bytesOf(os3, bos3);
            check((b4[0] & 0xFF) == 0x41 && b4[1] == 0x00 && (b4[2] & 0xFF) == 0x42 && b4[3] == 0x00, "writeChars LITTLE layout");
        } finally {
            try { os.close(); } catch (IOException ignore) {}
        }

        // writeUTF: 2-byte big-endian length prefix regardless of byte order
        ByteArrayOutputStream bos4 = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream os4 = new MemoryCacheImageOutputStream(bos4);
        os4.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        os4.writeUTF("hi");
        check(os4.getStreamPosition() == 4L, "writeUTF length is 2-byte prefix + payload");
        os4.seek(0);
        check("hi".equals(os4.readUTF()), "writeUTF/readUTF round-trip under LITTLE order");
        check(os4.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "byteOrder unchanged by writeUTF/readUTF");
        byte[] u = bytesOf(os4, bos4);
        check(u[0] == 0x00 && u[1] == 0x02, "writeUTF prefix is big-endian even under LITTLE order");
        check(u[2] == 'h' && u[3] == 'i', "writeUTF ASCII payload");
        // writeUTF encodes NUL as 2 bytes and U+0800.. as 3 bytes (modified UTF-8)
        os4.writeUTF("a\u0000bࠀ");
        check(os4.getStreamPosition() == 4L + 1 + 2 + 1 + 3 + 2, "modified-UTF8 width accounting");
        os4.seek(4);
        check("a\u0000bࠀ".equals(os4.readUTF()), "writeUTF/readUTF round-trip NUL + 3-byte");
        os4.writeUTF("");
        os4.seek(os4.getStreamPosition() - 2);
        check(os4.readUnsignedShort() == 0, "writeUTF(\"\") emits zero-length prefix");
        os4.close();

        // writeUTF longer than 65535 encoded bytes
        final StringBuilder big = new StringBuilder();
        for (int i = 0; i < 65536; i++) big.append('a');
        ByteArrayOutputStream bos5 = new ByteArrayOutputStream();
        final MemoryCacheImageOutputStream os5 = new MemoryCacheImageOutputStream(bos5);
        expectExact(java.io.UTFDataFormatException.class,
                    new ThrowingAction() { public void run() throws Exception { os5.writeUTF(big.toString()); } },
                    "writeUTF with utflen 65536");
        check(os5.getStreamPosition() == 0L, "failed writeUTF writes nothing");
        final StringBuilder big2 = new StringBuilder();
        for (int i = 0; i < 32768; i++) big2.append('ࠀ');
        expectExact(java.io.UTFDataFormatException.class,
                    new ThrowingAction() { public void run() throws Exception { os5.writeUTF(big2.toString()); } },
                    "writeUTF 32768 x U+0800 (utflen 65536)");
        StringBuilder big3 = new StringBuilder();
        for (int i = 0; i < 65535; i++) big3.append('a');
        os5.writeUTF(big3.toString());
        check(os5.getStreamPosition() == 65537L, "writeUTF utflen 65535 boundary accepted");
        os5.seek(0);
        String rt = os5.readUTF();
        check(rt.length() == 65535 && rt.charAt(0) == 'a' && rt.charAt(65534) == 'a', "65535-char round-trip");
        os5.close();

        // bit-level writing: MSB-first, zero padding, position bookkeeping
        ByteArrayOutputStream bos6 = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream os6 = new MemoryCacheImageOutputStream(bos6);
        os6.writeBit(1);
        check(os6.getStreamPosition() == 0L && os6.getBitOffset() == 1, "writeBit stays inside byte until 8 bits");
        os6.write(96); // flushBits pads the partial byte with zeros
        byte[] p = bytesOf(os6, bos6);
        check(p.length == 2, "writeBit + write(int) produce two bytes");
        check((p[0] & 0xFF) == 0x80, "first byte is 1 followed by zero padding");
        check((p[1] & 0xFF) == 96, "second byte is the raw write(int)");
        check(os6.getStreamPosition() == 2L && os6.getBitOffset() == 0, "bit offset cleared by byte write");

        ByteArrayOutputStream bos7 = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream os7 = new MemoryCacheImageOutputStream(bos7);
        os7.writeBit(3); // masked to a single bit
        check(os7.getBitOffset() == 1, "writeBit consumes exactly 1 bit offset");
        os7.writeBits(1L, 2); // bits 1..2
        check(os7.getBitOffset() == 3 && os7.getStreamPosition() == 0L, "writeBits continues at bit 3");
        os7.writeBits(-1L, 5); // fill rest of byte with 1s
        check(os7.getBitOffset() == 0 && os7.getStreamPosition() == 1L, "writeBits completes the byte");
        byte[] q = bytesOf(os7, bos7);
        check((q[0] & 0xFF) == 0xBF, "bit pattern 1,11,11111 == 0xBF");
        os7.writeBits(0xFL, 4);
        check(os7.getStreamPosition() == 1L && os7.getBitOffset() == 4, "writeBits(4) starts new byte");
        os7.writeByte(0x5A);
        byte[] q2 = bytesOf(os7, bos7);
        check((q2[1] & 0xFF) == 0xF0 && (q2[2] & 0xFF) == 0x5A, "writeByte pads remaining 4 bits with zeros");

        // writeBits over existing bytes preserves surrounding bits
        ByteArrayOutputStream bos8 = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream os8 = new MemoryCacheImageOutputStream(bos8);
        os8.writeByte(0xFF);
        os8.seek(0);
        os8.setBitOffset(4);
        os8.writeBits(0L, 4);
        byte[] r = bytesOf(os8, bos8);
        check((r[0] & 0xFF) == 0xF0, "writeBits mid-byte preserves high bits");
        check(os8.getStreamPosition() == 1L && os8.getBitOffset() == 0, "byte completed on bit boundary");
        os8.writeByte(0xFF);
        os8.writeByte(0xFF);
        os8.seek(1);
        os8.setBitOffset(4);
        os8.writeBits(0L, 8); // spans low nibble of byte1 and high nibble of byte2
        check(os8.getStreamPosition() == 2L && os8.getBitOffset() == 4, "cross-byte writeBits bookkeeping");
        byte[] r2 = bytesOf(os8, bos8);
        check((r2[1] & 0xFF) == 0xF0 && (r2[2] & 0xFF) == 0x0F, "writeBits spans byte boundary");

        // writeBits validation and zero-width no-op
        final MemoryCacheImageOutputStream os9 = new MemoryCacheImageOutputStream(new ByteArrayOutputStream());
        expectExact(IllegalArgumentException.class,
                    new ThrowingAction() { public void run() throws Exception { os9.writeBits(1L, -1); } },
                    "writeBits numBits -1");
        expectExact(IllegalArgumentException.class,
                    new ThrowingAction() { public void run() throws Exception { os9.writeBits(1L, 65); } },
                    "writeBits numBits 65");
        os9.writeBits(0xFFL, 0);
        check(os9.getStreamPosition() == 0L && os9.getBitOffset() == 0, "writeBits(_,0) is a no-op");
        os9.writeBits(-1L, 64);
        check(os9.getStreamPosition() == 8L, "writeBits 64 bits consumes 8 bytes");
        os9.seek(0);
        byte[] w = new byte[8];
        os9.readFully(w);
        for (int i = 0; i < 8; i++) check((w[i] & 0xFF) == 0xFF, "writeBits(-1,64) byte " + i + " is 0xFF");

        // array write bounds: all six primitive array writers validate off/len
        final MemoryCacheImageOutputStream os10 = new MemoryCacheImageOutputStream(new ByteArrayOutputStream());
        expectExact(IndexOutOfBoundsException.class,
                    new ThrowingAction() { public void run() throws Exception { os10.writeShorts(new short[2], -1, 1); } },
                    "writeShorts off<0");
        expectExact(IndexOutOfBoundsException.class,
                    new ThrowingAction() { public void run() throws Exception { os10.writeInts(new int[2], 0, 3); } },
                    "writeInts len>length");
        expectExact(IndexOutOfBoundsException.class,
                    new ThrowingAction() { public void run() throws Exception { os10.writeLongs(new long[2], 0, 3); } },
                    "writeLongs len>length");
        expectExact(IndexOutOfBoundsException.class,
                    new ThrowingAction() { public void run() throws Exception { os10.writeFloats(new float[2], 0, 3); } },
                    "writeFloats len>length");
        expectExact(IndexOutOfBoundsException.class,
                    new ThrowingAction() { public void run() throws Exception { os10.writeDoubles(new double[2], 0, 3); } },
                    "writeDoubles len>length");
        expectExact(IndexOutOfBoundsException.class,
                    new ThrowingAction() { public void run() throws Exception { os10.writeChars(new char[2], 0, 3); } },
                    "writeChars(char[]) len>length");
        os10.writeShorts(new short[3], 1, 0);
        os10.writeInts(new int[3], 1, 0);
        os10.writeLongs(new long[3], 1, 0);
        os10.writeFloats(new float[3], 1, 0);
        os10.writeDoubles(new double[3], 1, 0);
        os10.writeChars(new char[3], 1, 0);
        check(os10.getStreamPosition() == 0L, "zero-length array writes emit no bytes");

        // writeShorts/writeInts under LITTLE_ENDIAN round-trip through readFully arrays
        ByteArrayOutputStream bos11 = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream os11 = new MemoryCacheImageOutputStream(bos11);
        os11.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        os11.writeShorts(new short[]{0x0201, 0x0403}, 0, 2);
        os11.writeInts(new int[]{0x04030201}, 0, 1);
        os11.setByteOrder(ByteOrder.BIG_ENDIAN);
        os11.writeShorts(new short[]{0x0201, 0x0403}, 0, 2);
        os11.writeInts(new int[]{0x04030201}, 0, 1);
        os11.seek(0);
        os11.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        short[] sh = new short[2];
        os11.readFully(sh, 0, 2);
        check(sh[0] == 0x0201 && sh[1] == 0x0403, "writeShorts LITTLE round-trip");
        int[] ia = new int[1];
        os11.readFully(ia, 0, 1);
        check(ia[0] == 0x04030201, "writeInts LITTLE round-trip");
        os11.setByteOrder(ByteOrder.BIG_ENDIAN);
        os11.readFully(sh, 0, 2);
        check(sh[0] == 0x0201 && sh[1] == 0x0403, "writeShorts BIG round-trip");
        os11.readFully(ia, 0, 1);
        check(ia[0] == 0x04030201, "writeInts BIG round-trip");
        os11.close();

        // random-access write via seek overwrites in place
        ByteArrayOutputStream bos12 = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream os12 = new MemoryCacheImageOutputStream(bos12);
        os12.writeInt(0x11223344);
        os12.writeInt(0x55667788);
        os12.seek(2);
        os12.writeShort(0xABCD);
        byte[] ow = bytesOf(os12, bos12);
        check(ow.length == 8, "overwrite keeps total length");
        check((ow[2] & 0xFF) == 0xAB && (ow[3] & 0xFF) == 0xCD, "seek+writeShort overwrites in place");
        check((ow[0] & 0xFF) == 0x11 && (ow[7] & 0xFF) == 0x88, "surrounding bytes untouched");

        // flushBefore pushes the prefix to the underlying stream
        ByteArrayOutputStream bos13 = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream os13 = new MemoryCacheImageOutputStream(bos13);
        os13.writeInt(0x11223344);
        os13.flushBefore(2);
        check(os13.getFlushedPosition() == 2L, "flushBefore sets flushed position");
        byte[] fl = bos13.toByteArray();
        check(fl.length == 2 && (fl[0] & 0xFF) == 0x11 && (fl[1] & 0xFF) == 0x22,
              "flushBefore(2) pushes first 2 bytes to underlying stream");
        expectExact(IndexOutOfBoundsException.class,
                    new ThrowingAction() { public void run() throws Exception { os13.flushBefore(1); } },
                    "flushBefore below flushed position");
        expectExact(IndexOutOfBoundsException.class,
                    new ThrowingAction() { public void run() throws Exception { os13.flushBefore(100); } },
                    "flushBefore beyond current position");
        os13.close();

        // write after close
        ByteArrayOutputStream bos14 = new ByteArrayOutputStream();
        final MemoryCacheImageOutputStream os14 = new MemoryCacheImageOutputStream(bos14);
        os14.close();
        expectExact(IOException.class,
                    new ThrowingAction() { public void run() throws Exception { os14.write(1); } },
                    "write(int) after close");
        expectExact(IOException.class,
                    new ThrowingAction() { public void run() throws Exception { os14.writeInt(1); } },
                    "writeInt after close");
        expectExact(IOException.class,
                    new ThrowingAction() { public void run() throws Exception { os14.writeBit(1); } },
                    "writeBit after close");
        expectExact(IOException.class,
                    new ThrowingAction() { public void run() throws Exception { os14.writeUTF("x"); } },
                    "writeUTF after close");
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.ImageOutputStream");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 2, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.stream.ImageInputStream"), "extends javax.imageio.stream.ImageInputStream");
        check(si.contains("java.io.DataOutput"), "extends java.io.DataOutput");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("write"), "method write");
        check(dm.contains("write"), "method write");
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
        check(dm.contains("writeChars"), "method writeChars");
        check(dm.contains("writeInts"), "method writeInts");
        check(dm.contains("writeLongs"), "method writeLongs");
        check(dm.contains("writeFloats"), "method writeFloats");
        check(dm.contains("writeDoubles"), "method writeDoubles");
        check(dm.contains("writeBit"), "method writeBit");
        check(dm.contains("writeBits"), "method writeBits");
        check(dm.contains("flushBefore"), "method flushBefore");
        testSignatures();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
