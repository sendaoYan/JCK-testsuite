/*
 * @test
 * @summary java.io.DataInput conformance: white-box structure (machine-independent
 *          primitive input contract) + black-box behavior (every method exercised
 *          polymorphically through two independent implementations: DataInputStream
 *          and RandomAccessFile; big-endian framing verified against hand-built bytes;
 *          readFully/skipBytes contract; readLine terminator rules; readUTF).
 *          Deepening: cross-implementation boundary-value matrix (MIN/MAX /
 *          -0.0 / NaN / Inf through both implementations), EOF and short-
 *          readFully prefix parity, skipBytes negative/clamp parity, malformed
 *          modified-UTF-8 parity, readLine terminator parity on RAF, and the
 *          pure-contract shape of the interface (15 abstract methods, no
 *          static/default members).
 * @run main DataInputTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class DataInputTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Canonical wire image, all big-endian; the tail feeds readLine. */
    static byte[] wire() {
        return new byte[]{
            0x01,                                                // boolean true
            (byte) 0xFE,                                         // byte -2
            (byte) 0xFF, (byte) 0xFE,                            // short -2
            (byte) 0xFE,                                         // unsigned byte 254
            0, 'Q',                                              // char
            (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF,  // int
            0, 0, 0, 0, 0, 0, 0, 9,                              // long
            0x40, 0x20, 0, 0,                                    // float 2.5
            (byte) 0xC0, 0x04, 0, 0, 0, 0, 0, 0,                 // double -2.5
            0, 3, 'u', 't', 'f',                                 // modified-UTF "utf"
            'l', 'i', 'n', 'e', '\n', 'x',                       // readLine fodder + tail
        };
    }

    /** Reads the canonical image in the canonical order through any DataInput. */
    static void exercise(DataInput in) throws IOException {
        check(in.readBoolean(), "readBoolean");
        check(in.readByte() == -2, "readByte");
        check(in.readShort() == -2, "readShort");
        check(in.readUnsignedByte() == 0xFE, "readUnsignedByte");
        check(in.readChar() == 'Q', "readChar");
        check(in.readInt() == 0xDEADBEEF, "readInt big-endian");
        check(in.readLong() == 9L, "readLong");
        check(in.readFloat() == 2.5f, "readFloat");
        check(in.readDouble() == -2.5, "readDouble");
        check("utf".equals(in.readUTF()), "readUTF");
        check("line".equals(in.readLine()), "readLine stops at \\n");
        check(in.readByte() == 'x', "trailing byte positioned after readLine");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.DataInput");
        check(c.isInterface(), "is an interface");
        for (Method m : c.getMethods())
            check(Modifier.isAbstract(m.getModifiers()), "abstract method " + m.getName());
        for (String m : new String[]{"readFully", "skipBytes", "readBoolean", "readByte",
                                     "readUnsignedByte", "readShort", "readUnsignedShort",
                                     "readChar", "readInt", "readLong", "readFloat",
                                     "readDouble", "readLine", "readUTF"})
            check(has(c, m), "has method " + m);
        check(hasOverload(c, "readFully", byte[].class), "readFully(byte[]) overload");
        check(hasOverload(c, "readFully", byte[].class, int.class, int.class),
              "readFully(byte[],int,int) overload");
        check(java.io.DataInputStream.class.getInterfaces()[0] == c
              || java.util.Arrays.asList(java.io.DataInputStream.class.getInterfaces()).contains(c),
              "DataInputStream implements DataInput");
        check(java.util.Arrays.asList(RandomAccessFile.class.getInterfaces()).contains(c),
              "RandomAccessFile implements DataInput");
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
        // 1. implementation A: DataInputStream through the interface reference
        exercise(new DataInputStream(new ByteArrayInputStream(wire())));

        // 2. implementation B: RandomAccessFile through the same interface reference
        File tmp = File.createTempFile("datain", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.write(wire());
            raf.seek(0);
            exercise(raf);
            check(raf.getFilePointer() == wire().length, "pointer at end after exercise");
        } finally {
            tmp.delete();
        }

        // 3. readUnsignedByte / readUnsignedShort widen to the positive range
        DataInput u = new DataInputStream(new ByteArrayInputStream(
            new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF}));
        check(u.readUnsignedByte() == 255, "readUnsignedByte 255");
        check(u.readUnsignedShort() == 0xFFFF, "readUnsignedShort 65535");

        // 4. readFully: exact fill; short stream fills the prefix then throws
        DataInput full = new DataInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4}));
        byte[] buf = new byte[4];
        full.readFully(buf);
        check(buf[0] == 1 && buf[3] == 4, "readFully(byte[]) exact fill");
        try {
            full.readFully(new byte[1]);
            check(false, "readFully at EOF must throw");
        } catch (EOFException expected) {
            passed++;
        }
        DataInput partial = new DataInputStream(new ByteArrayInputStream(new byte[]{7, 8}));
        byte[] region = new byte[4];
        try {
            partial.readFully(region, 0, 4);
            check(false, "short readFully(region) must throw");
        } catch (EOFException expected) {
            check(region[0] == 7 && region[2] == 0,
                  "readFully(region) filled the available prefix only");
        }

        // 5. skipBytes: reports what it skipped, clamps to availability, 0 at EOF
        DataInput skip = new DataInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}));
        check(skip.skipBytes(2) == 2 && skip.readByte() == 3, "skipBytes(2) then read");
        check(skip.skipBytes(10) == 2, "skipBytes clamps to the remaining 2 bytes and consumes them");
        check(skip.skipBytes(1) == 0, "skipBytes at EOF returns 0");
        try {
            skip.readByte();
            check(false, "readByte after clamped skip must throw");
        } catch (EOFException expected) {
            passed++;
        }

        // 6. readLine: accepts \n, \r and \r\n; null at EOF
        DataInput lines = new DataInputStream(
            new ByteArrayInputStream("a\r\nb\rc\nd".getBytes("UTF-8")));
        check("a".equals(lines.readLine()), "\\r\\n terminator");
        check("b".equals(lines.readLine()), "\\r terminator");
        check("c".equals(lines.readLine()), "\\n terminator");
        check("d".equals(lines.readLine()), "final line without terminator");
        check(lines.readLine() == null, "null at EOF");
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

    /** Boundary-value wire image produced with DataOutputStream (the reference writer). */
    static byte[] boundaryWire() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(bos);
        d.writeByte(Byte.MIN_VALUE);
        d.writeByte(Byte.MAX_VALUE);
        d.writeByte(0xFF);
        d.writeShort(Short.MIN_VALUE);
        d.writeShort(Short.MAX_VALUE);
        d.writeShort(0xFFFF);
        d.writeChar('\u0000');
        d.writeChar('￿');
        d.writeInt(Integer.MIN_VALUE);
        d.writeInt(Integer.MAX_VALUE);
        d.writeLong(Long.MIN_VALUE);
        d.writeLong(Long.MAX_VALUE);
        d.writeFloat(0.0f);
        d.writeFloat(-0.0f);
        d.writeFloat(Float.NaN);
        d.writeFloat(Float.NEGATIVE_INFINITY);
        d.writeDouble(0.0);
        d.writeDouble(-0.0);
        d.writeDouble(Double.NaN);
        d.writeDouble(Double.POSITIVE_INFINITY);
        d.writeUTF("a\u0000中é");
        return bos.toByteArray();
    }

    /** Reads the boundary wire through any DataInput; every value is checked absolutely. */
    static void exerciseBoundary(DataInput in, String tag) throws IOException {
        check(in.readByte() == Byte.MIN_VALUE, tag + " readByte MIN");
        check(in.readByte() == Byte.MAX_VALUE, tag + " readByte MAX");
        check(in.readUnsignedByte() == 255, tag + " readUnsignedByte 0xFF -> 255");
        check(in.readShort() == Short.MIN_VALUE, tag + " readShort MIN");
        check(in.readShort() == Short.MAX_VALUE, tag + " readShort MAX");
        check(in.readUnsignedShort() == 65535, tag + " readUnsignedShort 0xFFFF -> 65535");
        check(in.readChar() == '\u0000', tag + " readChar \\u0000");
        check(in.readChar() == '￿', tag + " readChar \\uFFFF");
        check(in.readInt() == Integer.MIN_VALUE, tag + " readInt MIN");
        check(in.readInt() == Integer.MAX_VALUE, tag + " readInt MAX");
        check(in.readLong() == Long.MIN_VALUE, tag + " readLong MIN");
        check(in.readLong() == Long.MAX_VALUE, tag + " readLong MAX");
        float f0 = in.readFloat();
        float fnz = in.readFloat();
        float fn = in.readFloat();
        float fi = in.readFloat();
        check(f0 == 0.0f && 1.0f / f0 == Float.POSITIVE_INFINITY, tag + " readFloat +0.0");
        check(Float.floatToRawIntBits(fnz) == 0x80000000, tag + " readFloat -0.0 bits");
        check(Float.isNaN(fn) && fi == Float.NEGATIVE_INFINITY, tag + " readFloat NaN/-Inf");
        double d0 = in.readDouble();
        double dnz = in.readDouble();
        double dn = in.readDouble();
        double dp = in.readDouble();
        check(d0 == 0.0 && 1.0 / d0 == Double.POSITIVE_INFINITY, tag + " readDouble +0.0");
        check(Double.doubleToRawLongBits(dnz) == 0x8000000000000000L,
              tag + " readDouble -0.0 bits");
        check(Double.isNaN(dn) && dp == Double.POSITIVE_INFINITY, tag + " readDouble NaN/+Inf");
        check("a\u0000中é".equals(in.readUTF()), tag + " readUTF mixed NUL+latin+CJK");
    }

    // ---- deepening: cross-implementation boundary matrix ----
    static void testCrossImplBoundaryMatrix() throws Exception {
        byte[] wire = boundaryWire();
        exerciseBoundary(new DataInputStream(new ByteArrayInputStream(wire)), "DIS");

        File tmp = File.createTempFile("datainbound", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.write(wire);
            raf.seek(0);
            exerciseBoundary(raf, "RAF");
            check(raf.getFilePointer() == wire.length,
                  "RAF pointer consumed exactly the wire after the matrix");
        } finally {
            tmp.delete();
        }
    }

    // ---- deepening: EOF / short readFully / skipBytes parity ----
    static void testEofParity() throws Exception {
        final DataInput dis = new DataInputStream(new ByteArrayInputStream(new byte[0]));
        checkThrows(EOFException.class, () -> dis.readInt(), "DIS readInt EOF");
        checkThrows(EOFException.class, () -> dis.readBoolean(), "DIS readBoolean EOF");
        checkThrows(EOFException.class, () -> dis.readUTF(), "DIS readUTF EOF");
        checkThrows(EOFException.class, () -> dis.readFully(new byte[1]), "DIS readFully EOF");
        check(dis.readLine() == null, "DIS readLine null at EOF");

        File tmp = File.createTempFile("dataineof", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            checkThrows(EOFException.class, () -> raf.readInt(), "RAF readInt EOF");
            checkThrows(EOFException.class, () -> raf.readBoolean(), "RAF readBoolean EOF");
            checkThrows(EOFException.class, () -> raf.readUTF(), "RAF readUTF EOF");
            checkThrows(EOFException.class, () -> raf.readFully(new byte[1]),
                  "RAF readFully EOF");
            check(raf.readLine() == null, "RAF readLine null at EOF");

            // short readFully keeps the transferred prefix on both implementations
            raf.write(new byte[]{4, 5});
            raf.seek(0);
            byte[] c = new byte[4];
            checkThrows(EOFException.class, () -> raf.readFully(c, 0, 4),
                  "RAF partial readFully throws");
            check(c[0] == 4 && c[1] == 5 && c[2] == 0, "RAF readFully kept the read prefix");

            // skipBytes parity: negative is 0 without moving; clamp at EOF
            raf.seek(0);
            check(raf.skipBytes(-3) == 0 && raf.getFilePointer() == 0,
                  "RAF skipBytes(-3) = 0 and does not move");
            check(raf.skipBytes(1) == 1 && raf.getFilePointer() == 1,
                  "RAF skipBytes(1) advances the pointer");
            check(raf.skipBytes(100) == 1 && raf.getFilePointer() == 2,
                  "RAF skipBytes clamps to the file length");
        } finally {
            tmp.delete();
        }

        byte[] a = new byte[4];
        checkThrows(EOFException.class,
              () -> new DataInputStream(new ByteArrayInputStream(new byte[]{4, 5})).readFully(a, 0, 4),
              "DIS partial readFully throws");
        check(a[0] == 4 && a[1] == 5 && a[2] == 0, "DIS readFully kept the read prefix");

        final DataInput sneg = new DataInputStream(new ByteArrayInputStream(new byte[]{1}));
        check(sneg.skipBytes(-1) == 0 && sneg.readByte() == 1,
              "DIS skipBytes(-1) = 0 and does not move");
        check(sneg.skipBytes(0) == 0, "DIS skipBytes(0) = 0");
    }

    // ---- deepening: malformed modified-UTF-8 parity ----
    static void testMalformedUtfParity() throws Exception {
        final byte[] bad = new byte[]{0, 1, (byte) 0x80};
        checkThrows(UTFDataFormatException.class,
              () -> new DataInputStream(new ByteArrayInputStream(bad)).readUTF(),
              "DIS readUTF malformed -> UTFDataFormatException");
        File tmp = File.createTempFile("datainbad", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.write(bad);
            raf.seek(0);
            checkThrows(UTFDataFormatException.class, () -> raf.readUTF(),
                  "RAF readUTF same bytes -> same UTFDataFormatException");
        } finally {
            tmp.delete();
        }

        // legal 2-byte form decodes identically on both implementations
        byte[] good = new byte[]{0, 2, (byte) 0xC3, (byte) 0xA9};
        String viaDis = new DataInputStream(new ByteArrayInputStream(good)).readUTF();
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.write(good);
            raf.seek(0);
            String viaRaf = raf.readUTF();
            check(viaDis.length() == 1 && viaDis.charAt(0) == 'é', "DIS decodes {C3 A9} -> é");
            check(viaRaf.length() == 1 && viaRaf.charAt(0) == 'é', "RAF decodes {C3 A9} -> é");
        } finally {
            tmp.delete();
        }
    }

    // ---- deepening: readLine terminator parity on RandomAccessFile ----
    static void testReadLineParity() throws Exception {
        byte[] data = "a\r\nb\rc\nd".getBytes("UTF-8");
        File tmp = File.createTempFile("datainlines", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.write(data);
            raf.seek(0);
            check("a".equals(raf.readLine()), "RAF readLine \\r\\n terminator");
            check("b".equals(raf.readLine()), "RAF readLine \\r terminator");
            check("c".equals(raf.readLine()), "RAF readLine \\n terminator");
            check("d".equals(raf.readLine()), "RAF readLine tail without terminator");
            check(raf.readLine() == null, "RAF readLine null at EOF");
        } finally {
            tmp.delete();
        }
    }

    // ---- deepening: the interface itself is a pure contract ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.io.DataInput");
        check(c.isInterface(), "DataInput is an interface");
        Method[] ms = c.getMethods();
        int abstractCount = 0;
        int staticCount = 0;
        for (Method m : ms) {
            if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
            if (Modifier.isStatic(m.getModifiers())) staticCount++;
        }
        check(staticCount == 0, "no static methods: the interface defines no factory/contract");
        check(abstractCount == ms.length, "every interface method is abstract (no defaults)");
        check(ms.length == 15, "exactly 15 methods (2 readFully overloads + 13 single-value reads)");
        check(Arrays.asList(DataInputStream.class.getInterfaces()).contains(c),
              "DataInputStream implements DataInput");
        check(Arrays.asList(RandomAccessFile.class.getInterfaces()).contains(c),
              "RandomAccessFile implements DataInput");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCrossImplBoundaryMatrix();
        testEofParity();
        testMalformedUtfParity();
        testReadLineParity();
        testStructure();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
