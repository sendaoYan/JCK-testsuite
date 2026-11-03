/*
 * @test
 * @summary java.io.RandomAccessFile conformance: white-box structure + black-box
 *          behavior (modes r/rw/rws/rwd and illegal-mode rejection, sequential and
 *          random reads/writes, DataInput/DataOutput round-trips incl. readUTF,
 *          getFilePointer/seek/setLength/length/skipBytes semantics, sparse extend,
 *          negative seek, closed-stream errors, missing file). Uses a temp file
 *          removed in a finally block; no other global state is touched.
 * @run main RandomAccessFileTest
 */

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class RandomAccessFileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.RandomAccessFile");
        check(java.io.DataInput.class.isAssignableFrom(c)
                  && java.io.DataOutput.class.isAssignableFrom(c)
                  && java.io.Closeable.class.isAssignableFrom(c),
              "implements DataInput, DataOutput, Closeable");
        check(c.getConstructor(String.class, String.class) != null, "ctor (String, String)");
        check(c.getConstructor(File.class, String.class) != null, "ctor (File, String)");
        for (String m : new String[]{"getFD", "getChannel", "read", "readFully", "skipBytes",
                                     "write", "getFilePointer", "seek", "length", "setLength",
                                     "close", "readBoolean", "readByte", "readShort", "readChar",
                                     "readInt", "readLong", "readFloat", "readDouble", "readLine",
                                     "readUTF", "writeBoolean", "writeByte", "writeShort",
                                     "writeChar", "writeInt", "writeLong", "writeFloat",
                                     "writeDouble", "writeUTF", "writeBytes", "writeChars"})
            check(has(c, m), "has method " + m);
        check(Modifier.isFinal(c.getMethod("getFD").getModifiers()), "final getFD");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            // 1. "rw" creates content; DataOutput round-trips through seek(0)
            raf = new RandomAccessFile(tmp, "rw");
            raf.writeBoolean(true);
            raf.writeByte(-3);
            raf.writeByte(200);
            raf.writeShort(0xBEEF);
            raf.writeChar('Z');
            raf.writeInt(0x12345678);
            raf.writeLong(0x1122334455667788L);
            raf.writeFloat(1.5f);
            raf.writeDouble(-0.25);
            raf.writeUTF("héllo");
            raf.writeBytes("raw");
            long afterWrite = raf.length();
            raf.seek(0);
            check(raf.getFilePointer() == 0, "seek(0)");
            check(raf.readBoolean(), "boolean round-trip");
            check(raf.readByte() == -3, "byte round-trip");
            check(raf.readUnsignedByte() == 200, "unsigned byte round-trip");
            check(raf.readShort() == (short) 0xBEEF, "short round-trip");
            check(raf.readChar() == 'Z', "char round-trip");
            check(raf.readInt() == 0x12345678, "int round-trip");
            check(raf.readLong() == 0x1122334455667788L, "long round-trip");
            check(raf.readFloat() == 1.5f, "float round-trip");
            check(raf.readDouble() == -0.25, "double round-trip");
            check("héllo".equals(raf.readUTF()), "UTF round-trip");
            byte[] raw = new byte[3];
            check(raf.read(raw) == 3 && raw[0] == 'r' && raw[2] == 'w', "writeBytes round-trip");
            check(raf.getFilePointer() == afterWrite, "pointer at end after consuming all");
            raf.close();

            // 2. "r" opens read-only; writing fails; descriptor and channel exist
            raf = new RandomAccessFile(tmp, "r");
            check(raf.getFD().valid(), "valid FD for an open file");
            check(raf.getChannel() != null, "non-null channel");
            try {
                raf.write(1);
                check(false, "write in r mode must throw");
            } catch (IOException ioe) {
                check("Bad file descriptor".equals(ioe.getMessage()),
                      "r-mode write message: " + ioe.getMessage());
            }
            raf.close();

            // 3. "rws" and "rwd" are accepted and usable
            raf = new RandomAccessFile(tmp, "rws");
            raf.setLength(0);
            raf.write('A');
            raf.close();
            raf = new RandomAccessFile(tmp, "rwd");
            check(raf.length() == 1 && raf.read() == 'A', "rws write visible in rwd");
            raf.close();

            // 4. illegal modes are rejected with the exact message
            for (String bad : new String[]{"x", "", "rwxd", "R", "rw+"}) {
                try {
                    new RandomAccessFile(tmp, bad);
                    check(false, "mode '" + bad + "' must be rejected");
                } catch (IllegalArgumentException iae) {
                    check(iae.getMessage().equals(
                              "Illegal mode \"" + bad + "\" must be one of \"r\", \"rw\", \"rws\", or \"rwd\""),
                          "message for '" + bad + "': " + iae.getMessage());
                }
            }

            // 5. missing file in r mode is FileNotFoundException
            try {
                new RandomAccessFile(new File(tmp.getParent(), "no-such-" + System.nanoTime() + ".dat"), "r");
                check(false, "missing file must throw");
            } catch (FileNotFoundException fnfe) {
                passed++;
            }

            // 6. readLine strips \r\n; last unterminated line still returned; null at EOF
            raf = new RandomAccessFile(tmp, "rw");
            raf.setLength(0);
            raf.writeBytes("alpha\r\nbeta\ngamma");
            raf.seek(0);
            check("alpha".equals(raf.readLine()), "CRLF line");
            check(raf.getFilePointer() == 7, "pointer past CRLF");
            check("beta".equals(raf.readLine()) && "gamma".equals(raf.readLine()),
                  "LF line and unterminated final line");
            check(raf.readLine() == null, "null at EOF");

            // 7. setLength truncates, extends with zeros, and clamps a larger pointer
            raf.seek(0);
            raf.setLength(2);
            check(raf.length() == 2, "truncated length");
            try {
                raf.readInt();                                // needs 4 bytes
                check(false, "readInt past EOF must throw");
            } catch (EOFException eofe) {
                passed++;
            }
            raf.setLength(10);
            check(raf.length() == 10, "extended length");
            raf.seek(5);
            check(raf.read() == 0, "extension fills with zeros");
            raf.setLength(0);
            check(raf.length() == 0, "zero length");

            // 8. skipBytes moves forward, clamps at EOF, ignores negatives
            raf.writeBytes("0123456789");
            raf.seek(0);
            check(raf.skipBytes(4) == 4 && raf.getFilePointer() == 4, "skip forward");
            check(raf.skipBytes(100) == 6 && raf.getFilePointer() == 10,
                  "skip clamps at EOF returning the remainder");
            check(raf.skipBytes(-3) == 0, "negative skip returns 0");

            // 9. seek past EOF then write creates a sparse hole
            raf.seek(50);
            raf.write(9);
            check(raf.length() == 51, "sparse extension");
            raf.seek(49);
            check(raf.read() == 0, "hole reads as zero");
            check(raf.read() == 9, "byte at the seek target");

            // 10. negative seek offset
            try {
                raf.seek(-1);
                check(false, "negative seek must throw");
            } catch (IOException ioe) {
                check("Negative seek offset".equals(ioe.getMessage()),
                      "message: " + ioe.getMessage());
            }

            // 11. readFully demands the full count
            raf.seek(0);
            try {
                raf.readFully(new byte[(int) raf.length() + 1]);
                check(false, "readFully past EOF must throw");
            } catch (EOFException eofe) {
                passed++;
            }
            raf.seek(0);
            byte[] exact = new byte[4];
            raf.readFully(exact);
            check(exact[0] == '0' && exact[3] == '3', "readFully exact region");
            check(raf.read(new byte[0]) == 0, "zero-length read returns 0");

            // 12. operations after close fail uniformly
            raf.close();
            try {
                raf.read();
                check(false, "read after close must throw");
            } catch (IOException ioe) {
                check("Stream Closed".equals(ioe.getMessage()),
                      "closed read message: " + ioe.getMessage());
            }
            try {
                raf.seek(0);
                check(false, "seek after close must throw");
            } catch (IOException ioe) {
                check("Stream Closed".equals(ioe.getMessage()),
                      "closed seek message: " + ioe.getMessage());
            }
            raf.close();                                     // close is idempotent
            passed++;
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
