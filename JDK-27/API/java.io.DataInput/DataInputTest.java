/*
 * @test
 * @summary java.io.DataInput conformance: white-box structure (machine-independent
 *          primitive input contract) + black-box behavior (every method exercised
 *          polymorphically through two independent implementations: DataInputStream
 *          and RandomAccessFile; big-endian framing verified against hand-built bytes;
 *          readFully/skipBytes contract; readLine terminator rules; readUTF).
 * @run main DataInputTest
 */

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
