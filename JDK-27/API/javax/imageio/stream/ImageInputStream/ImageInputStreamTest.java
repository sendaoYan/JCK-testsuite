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
import javax.imageio.stream.MemoryCacheImageInputStream;

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

    interface ThrowingAction { void run() throws Exception; }
    static void expectExact(Class<? extends Throwable> expected, ThrowingAction r, String msg) {
        boolean threw = false;
        Throwable caught = null;
        try { r.run(); } catch (Throwable t) { threw = true; caught = t; }
        check(threw, msg + " should throw");
        check(caught.getClass() == expected,
              msg + " expected " + expected.getName() + " but got " + caught.getClass().getName());
    }

    // ---- Part 2: precise method signatures (overload counts, parameter types, throws clauses) ----
    static void testSignatures() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.ImageInputStream");
        int readOverloads = 0, readFullyOverloads = 0, skipBytesOverloads = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("read")) readOverloads++;
            if (m.getName().equals("readFully")) readFullyOverloads++;
            if (m.getName().equals("skipBytes")) skipBytesOverloads++;
        }
        check(readOverloads == 3, "read overloads == 3");
        check(readFullyOverloads == 8, "readFully overloads == 8");
        check(skipBytesOverloads == 2, "skipBytes overloads == 2");

        Method m;
        m = c.getMethod("read", byte[].class, int.class, int.class);
        check(m.getReturnType() == int.class, "read(byte[],int,int) returns int");
        m = c.getMethod("read", byte[].class);
        check(m.getReturnType() == int.class, "read(byte[]) returns int");
        m = c.getMethod("readBytes", Class.forName("javax.imageio.stream.IIOByteBuffer"), int.class);
        check(m.getReturnType() == void.class, "readBytes(IIOByteBuffer,int) returns void");
        m = c.getMethod("setByteOrder", ByteOrder.class);
        check(m.getReturnType() == void.class, "setByteOrder(ByteOrder) returns void");
        check(m.getExceptionTypes().length == 0, "setByteOrder throws nothing");
        m = c.getMethod("getByteOrder");
        check(m.getReturnType() == ByteOrder.class, "getByteOrder returns ByteOrder");
        m = c.getMethod("readUnsignedByte");
        check(m.getReturnType() == int.class, "readUnsignedByte returns int");
        m = c.getMethod("readUnsignedShort");
        check(m.getReturnType() == int.class, "readUnsignedShort returns int");
        m = c.getMethod("readUnsignedInt");
        check(m.getReturnType() == long.class, "readUnsignedInt returns long");
        m = c.getMethod("readByte");
        check(m.getReturnType() == byte.class, "readByte returns byte");
        m = c.getMethod("readShort");
        check(m.getReturnType() == short.class, "readShort returns short");
        m = c.getMethod("readChar");
        check(m.getReturnType() == char.class, "readChar returns char");
        m = c.getMethod("readBits", int.class);
        check(m.getReturnType() == long.class, "readBits(int) returns long");
        m = c.getMethod("readBit");
        check(m.getReturnType() == int.class, "readBit returns int");
        m = c.getMethod("skipBytes", int.class);
        check(m.getReturnType() == int.class, "skipBytes(int) returns int");
        m = c.getMethod("skipBytes", long.class);
        check(m.getReturnType() == long.class, "skipBytes(long) returns long");
        m = c.getMethod("length");
        check(m.getReturnType() == long.class, "length returns long");
        m = c.getMethod("getStreamPosition");
        check(m.getReturnType() == long.class, "getStreamPosition returns long");
        m = c.getMethod("getBitOffset");
        check(m.getReturnType() == int.class, "getBitOffset returns int");
        m = c.getMethod("readUTF");
        check(m.getReturnType() == String.class, "readUTF returns String");
        check(m.getExceptionTypes().length == 1
              && m.getExceptionTypes()[0] == IOException.class, "readUTF throws IOException");
        m = c.getMethod("readInt");
        check(m.getExceptionTypes().length == 1
              && m.getExceptionTypes()[0] == IOException.class, "readInt throws IOException");
        m = c.getMethod("mark");
        check(m.getExceptionTypes().length == 0, "mark() throws nothing");
        m = c.getMethod("getFlushedPosition");
        check(m.getExceptionTypes().length == 0, "getFlushedPosition throws nothing");
        m = c.getMethod("isCached");
        check(m.getExceptionTypes().length == 0, "isCached throws nothing");
        m = c.getMethod("isCachedMemory");
        check(m.getExceptionTypes().length == 0, "isCachedMemory throws nothing");
        m = c.getMethod("isCachedFile");
        check(m.getExceptionTypes().length == 0, "isCachedFile throws nothing");
        m = c.getMethod("close");
        check(m.getExceptionTypes().length == 1
              && m.getExceptionTypes()[0] == IOException.class, "close throws IOException");
        // readFully parameter type shapes across all 8 overloads
        boolean hasShortArr = false, hasCharArr = false, hasIntArr = false,
                hasLongArr = false, hasFloatArr = false, hasDoubleArr = false;
        for (Method mm : c.getDeclaredMethods()) {
            if (!mm.getName().equals("readFully")) continue;
            Class<?>[] ps = mm.getParameterTypes();
            if (ps.length == 2 && ps[0] == byte[].class) hasShortArr = true; // readFully(byte[])
            if (ps.length == 3) {
                if (ps[0] == short[].class) hasShortArr = true;
                if (ps[0] == char[].class) hasCharArr = true;
                if (ps[0] == int[].class) hasIntArr = true;
                if (ps[0] == long[].class) hasLongArr = true;
                if (ps[0] == float[].class) hasFloatArr = true;
                if (ps[0] == double[].class) hasDoubleArr = true;
            }
        }
        check(hasShortArr, "readFully(short[],int,int) exists");
        check(hasCharArr, "readFully(char[],int,int) exists");
        check(hasIntArr, "readFully(int[],int,int) exists");
        check(hasLongArr, "readFully(long[],int,int) exists");
        check(hasFloatArr, "readFully(float[],int,int) exists");
        check(hasDoubleArr, "readFully(double[],int,int) exists");
    }

    // ---- Part 3: behavioral contract via a JDK-provided implementation ----
    static void testBehavior() throws Exception {
        byte[] four = new byte[]{0x12, 0x34, 0x56, 0x78};
        ImageInputStream iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(four));
        try {
            check(iis.getByteOrder() == ByteOrder.BIG_ENDIAN, "default byte order BIG_ENDIAN");
            check(iis.readShort() == (short) 0x1234, "readShort BIG");
            iis.seek(0);
            iis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            check(iis.readShort() == (short) 0x3412, "readShort LITTLE swapped");
            iis.seek(0);
            check(iis.readInt() == 0x78563412, "readInt LITTLE swapped");
            iis.seek(0);
            iis.setByteOrder(ByteOrder.BIG_ENDIAN);
            check(iis.readInt() == 0x12345678, "readInt BIG");
            check(iis.getStreamPosition() == 4L, "position after reads");
            check(iis.length() == -1L, "MemoryCache length unknown -1");
            check(iis.isCached() && iis.isCachedMemory() && !iis.isCachedFile(), "memory cache flags");

            // unsigned semantics
            ImageInputStream u = new MemoryCacheImageInputStream(
                new ByteArrayInputStream(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                                                   (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}));
            check(u.readUnsignedByte() == 255, "readUnsignedByte 0xFF == 255");
            check(u.readUnsignedShort() == 65535, "readUnsignedShort 0xFFFF == 65535");
            check(u.readUnsignedInt() == 4294967295L, "readUnsignedInt 0xFFFFFFFF == 4294967295L");

            // setByteOrder(null) tolerated, getByteOrder returns null
            ImageInputStream n = new MemoryCacheImageInputStream(new ByteArrayInputStream(new byte[1]));
            n.setByteOrder(null);
            check(n.getByteOrder() == null, "setByteOrder(null) stored verbatim");
        } finally {
            try { iis.close(); } catch (IOException ignore) {}
        }

        // readUTF always network order; byte order restored after call
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeUTF("hé中");
        }
        ImageInputStream ru = new MemoryCacheImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ru.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        check("hé中".equals(ru.readUTF()), "readUTF under LITTLE_ENDIAN still BIG");
        check(ru.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "byteOrder restored after readUTF");

        // readLine semantics
        ImageInputStream rl = new MemoryCacheImageInputStream(
            new ByteArrayInputStream("ab\r\ncd\nef\rgh".getBytes("UTF-8")));
        check("ab".equals(rl.readLine()), "readLine CRLF");
        check(rl.getStreamPosition() == 4L, "readLine consumes CR+LF");
        check("cd".equals(rl.readLine()), "readLine LF");
        check("ef".equals(rl.readLine()), "readLine bare CR");
        check("gh".equals(rl.readLine()), "readLine no terminator");
        check(rl.readLine() == null, "readLine at EOF returns null");
        check(rl.readLine() == null, "readLine repeated EOF returns null");
        ImageInputStream rl2 = new MemoryCacheImageInputStream(new ByteArrayInputStream(new byte[0]));
        check(rl2.readLine() == null, "readLine on empty stream returns null");

        // EOF semantics
        final ImageInputStream empty = new MemoryCacheImageInputStream(new ByteArrayInputStream(new byte[0]));
        check(empty.read() == -1, "read() at EOF returns -1");
        check(empty.read(new byte[3]) == -1, "read(byte[]) at EOF returns -1");
        check(empty.read(new byte[3], 0, 0) == 0, "read(b,off,0) returns 0");
        expectExact(java.io.EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readInt(); } },
                    "readInt at EOF");
        expectExact(java.io.EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readBoolean(); } },
                    "readBoolean at EOF");
        expectExact(java.io.EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readUTF(); } },
                    "readUTF at EOF");
        expectExact(java.io.EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readFully(new byte[4]); } },
                    "readFully at EOF");
        expectExact(java.io.EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readBit(); } },
                    "readBit at EOF");
        final ImageInputStream one = new MemoryCacheImageInputStream(new ByteArrayInputStream(new byte[]{0x12}));
        expectExact(java.io.EOFException.class, new ThrowingAction() { public void run() throws Exception { one.readShort(); } },
                    "readShort with 1 of 2 bytes");

        // malformed modified-UTF8 payload
        final ImageInputStream bad = new MemoryCacheImageInputStream(
            new ByteArrayInputStream(new byte[]{0, 2, (byte) 0xC8, 0x41}));
        bad.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        expectExact(java.io.UTFDataFormatException.class, new ThrowingAction() { public void run() throws Exception { bad.readUTF(); } },
                    "readUTF malformed continuation");
        check(bad.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "byteOrder restored after readUTF failure");

        // bit-level reading: MSB first, cross-byte
        ImageInputStream bits = new MemoryCacheImageInputStream(
            new ByteArrayInputStream(new byte[]{(byte) 0xAB, (byte) 0xCD}));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(bits.readBit());
        check("10101011".equals(sb.toString()), "readBit x8 MSB-first of 0xAB");
        check(bits.getStreamPosition() == 1L && bits.getBitOffset() == 0, "byte consumed after 8 readBit");
        check(bits.readBits(8) == 0xCDL, "readBits(8) next byte");
        ImageInputStream bits2 = new MemoryCacheImageInputStream(
            new ByteArrayInputStream(new byte[]{(byte) 0xAB, (byte) 0xCD}));
        bits2.setBitOffset(4);
        check(bits2.readBits(8) == 0xBCL, "readBits(8) at bitOffset 4 crosses byte");
        check(bits2.getStreamPosition() == 1L && bits2.getBitOffset() == 4, "readBits cross-byte position/offset");
        ImageInputStream bits3 = new MemoryCacheImageInputStream(
            new ByteArrayInputStream(new byte[]{(byte) 0xA5}));
        bits3.setBitOffset(3);
        check(bits3.readBits(5) == 0x5L, "readBits(5) at bitOffset 3 of 0xA5");
        check(bits3.getStreamPosition() == 1L && bits3.getBitOffset() == 0, "readBits(5) lands on byte boundary");
        ImageInputStream bits4 = new MemoryCacheImageInputStream(
            new ByteArrayInputStream(new byte[8]));
        check(bits4.readBits(0) == 0L, "readBits(0) returns 0");
        check(bits4.getStreamPosition() == 0L && bits4.getBitOffset() == 0, "readBits(0) does not move");
        check(bits4.readBits(64) == 0L, "readBits(64) max width");
        check(bits4.getStreamPosition() == 8L, "readBits(64) consumes 8 bytes");

        // bit offset bounds
        final ImageInputStream bo = new MemoryCacheImageInputStream(new ByteArrayInputStream(new byte[1]));
        expectExact(IllegalArgumentException.class, new ThrowingAction() { public void run() throws Exception { bo.setBitOffset(-1); } },
                    "setBitOffset(-1)");
        expectExact(IllegalArgumentException.class, new ThrowingAction() { public void run() throws Exception { bo.setBitOffset(8); } },
                    "setBitOffset(8)");
        expectExact(IllegalArgumentException.class, new ThrowingAction() { public void run() throws Exception { bo.readBits(-1); } },
                    "readBits(-1)");
        expectExact(IllegalArgumentException.class, new ThrowingAction() { public void run() throws Exception { bo.readBits(65); } },
                    "readBits(65)");
        bo.setBitOffset(7);
        check(bo.getBitOffset() == 7, "setBitOffset(7) boundary accepted");

        // readBytes / IIOByteBuffer integration
        ImageInputStream rb = new MemoryCacheImageInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}));
        IIOByteBuffer buf = new IIOByteBuffer(new byte[0], 0, 0);
        rb.readBytes(buf, 3);
        check(buf.getLength() == 3 && buf.getOffset() == 0, "readBytes sets offset 0 and length");
        check(buf.getData()[0] == 1 && buf.getData()[2] == 3, "readBytes payload");
        rb.readByte();
        rb.readBytes(buf, 1); // reuse same buffer object
        check(buf.getLength() == 1 && buf.getData()[0] == 5, "readBytes buffer reuse replaces data");
        check(rb.read() == -1, "read after last byte");
        rb.readBytes(buf, 2); // at EOF: no exception, length becomes -1
        check(buf.getLength() == -1, "readBytes at EOF reports length -1");
        expectExact(NullPointerException.class, new ThrowingAction() { public void run() throws Exception { rb.readBytes(null, 1); } },
                    "readBytes(null, 1)");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { rb.readBytes(buf, -1); } },
                    "readBytes(buf, -1)");
        check(rb.getStreamPosition() == 5L, "failed readBytes leaves position");

        // readFully bounds
        final ImageInputStream rfb = new MemoryCacheImageInputStream(new ByteArrayInputStream(new byte[1]));
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { rfb.readFully(new byte[4], -1, 2); } },
                    "readFully off<0");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { rfb.readFully(new byte[4], 0, 5); } },
                    "readFully len>b.length");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { rfb.readFully(new short[2], 0, 3); } },
                    "readFully(short[]) len>s.length");

        // seek / flushBefore bounds
        final ImageInputStream sk = new MemoryCacheImageInputStream(new ByteArrayInputStream(new byte[4]));
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { sk.seek(-1); } },
                    "seek(-1)");
        sk.skipBytes(10); // skipping past the end is permitted
        check(sk.getStreamPosition() == 10L, "skipBytes may pass end of stream");
        check(sk.read() == -1, "read after skip past end");
        sk.seek(2);
        final ImageInputStream fl = new MemoryCacheImageInputStream(new ByteArrayInputStream(new byte[4]));
        fl.seek(2);
        fl.flushBefore(2);
        check(fl.getFlushedPosition() == 2L, "flushBefore sets flushed position");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { fl.flushBefore(1); } },
                    "flushBefore below flushed position");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { fl.flushBefore(3); } },
                    "flushBefore beyond current position");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { fl.seek(1); } },
                    "seek into flushed region");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { fl.skipBytes(-1); } },
                    "skipBytes(-1) back into flushed region");

        // mark/reset stack semantics
        ImageInputStream mr = new MemoryCacheImageInputStream(new ByteArrayInputStream(new byte[5]));
        mr.reset(); // no mark yet: no-op, must not throw
        check(mr.getStreamPosition() == 0L, "reset without mark is a no-op");
        mr.seek(1);
        mr.setBitOffset(3);
        mr.mark();
        check(mr.readBit() == 0, "readBit after mark");
        check(mr.getBitOffset() == 4, "bitOffset advanced past mark");
        mr.reset();
        check(mr.getStreamPosition() == 1L, "reset restores byte position");
        check(mr.getBitOffset() == 3, "reset restores bit offset");
        mr.mark();
        mr.seek(4);
        mr.mark();
        mr.seek(5);
        mr.reset();
        check(mr.getStreamPosition() == 4L, "nested reset LIFO top");
        mr.reset();
        check(mr.getStreamPosition() == 1L, "nested reset LIFO bottom");
        mr.reset();
        check(mr.getStreamPosition() == 1L, "reset on empty stack stays put");
        final ImageInputStream fr = new MemoryCacheImageInputStream(new ByteArrayInputStream(new byte[5]));
        fr.seek(1);
        fr.mark();
        fr.seek(3);
        fr.flushBefore(2);
        expectExact(javax.imageio.IIOException.class, new ThrowingAction() { public void run() throws Exception { fr.reset(); } },
                    "reset to flushed mark");

        // close contract
        final ImageInputStream cl = new MemoryCacheImageInputStream(new ByteArrayInputStream(new byte[3]));
        cl.read();
        cl.mark();
        cl.close();
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { cl.getStreamPosition(); } },
                    "getStreamPosition after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { cl.read(); } },
                    "read after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { cl.getBitOffset(); } },
                    "getBitOffset after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { cl.setBitOffset(0); } },
                    "setBitOffset after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { cl.seek(0); } },
                    "seek after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { cl.flushBefore(0); } },
                    "flushBefore after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { cl.skipBytes(1); } },
                    "skipBytes after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { cl.readBits(1); } },
                    "readBits after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { cl.reset(); } },
                    "reset to pre-close mark after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { cl.close(); } },
                    "second close");
        cl.mark(); // swallowed internally: must not throw
        check(true, "mark after close is silent");
        check(cl.length() == -1L, "length after close still answers");
        check(cl.getFlushedPosition() == 0L, "getFlushedPosition after close still answers");
        check(cl.isCached(), "isCached after close still answers");
        check(cl.getByteOrder() == ByteOrder.BIG_ENDIAN, "getByteOrder after close still answers");
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
        testSignatures();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
