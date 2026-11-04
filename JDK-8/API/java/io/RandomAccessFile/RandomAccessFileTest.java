/*
 * @test
 * @summary java.io.RandomAccessFile API conformance: full contract coverage.
 *          Modes r/rw/rws/rwd + illegal-mode rejection, DataInput/DataOutput
 *          round-trips incl. readUTF/writeUTF, getFilePointer/seek/setLength/length/
 *          skipBytes, sparse extension, negative seek, readLine, readFully, missing
 *          file -> FileNotFoundException, closed-stream errors. Uses a temp file
 *          removed in a finally block.
 * @run main RandomAccessFileTest
 */

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileTest {
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

    // ---- 1. DataOutput round-trips through seek(0) ----
    static void testDataRoundTrip() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
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
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 2. mode "r" is read-only ----
    static void testReadOnlyMode() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.writeInt(7);
            raf.close();

            raf = new RandomAccessFile(tmp, "r");
            check(raf.getFD().valid(), "valid FD for an open file");
            check(raf.getChannel() != null, "non-null channel");
            final RandomAccessFile ro = raf;
            checkThrows(IOException.class, () -> ro.write(1), "write in r mode -> IOException");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 3. rws/rwd modes ----
    static void testSyncModes() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rws");
            raf.setLength(0);
            raf.write('A');
            raf.close();
            raf = new RandomAccessFile(tmp, "rwd");
            check(raf.length() == 1 && raf.read() == 'A', "rws write visible in rwd");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 4. illegal modes ----
    static void testIllegalModes() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        try {
            for (String bad : new String[]{"x", "", "rwxd", "R", "rw+"}) {
                final String mode = bad;
                try {
                    new RandomAccessFile(tmp, mode);
                    check(false, "mode '" + mode + "' must be rejected");
                } catch (IllegalArgumentException iae) {
                    check(iae.getMessage().equals(
                              "Illegal mode \"" + mode + "\" must be one of \"r\", \"rw\", \"rws\", or \"rwd\""),
                          "message for '" + mode + "': " + iae.getMessage());
                }
            }
        } finally {
            tmp.delete();
        }
    }

    // ---- 5. missing file -> FileNotFoundException ----
    static void testMissingFile() throws Exception {
        File missing = new File(System.getProperty("java.io.tmpdir"), "jck-raf-no-such.dat");
        missing.delete();
        checkThrows(FileNotFoundException.class,
                    () -> new RandomAccessFile(missing, "r"),
                    "missing file in r mode -> FileNotFoundException");
    }

    // ---- 6. readLine ----
    static void testReadLine() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.writeBytes("alpha\r\nbeta\ngamma");
            raf.seek(0);
            check("alpha".equals(raf.readLine()), "CRLF line");
            check(raf.getFilePointer() == 7, "pointer past CRLF");
            check("beta".equals(raf.readLine()) && "gamma".equals(raf.readLine()),
                  "LF line and unterminated final line");
            check(raf.readLine() == null, "null at EOF");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 7. setLength truncate/extend ----
    static void testSetLength() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.seek(0);
            raf.setLength(2);
            check(raf.length() == 2, "truncated length");
            final RandomAccessFile r = raf;
            checkThrows(EOFException.class, () -> r.readInt(), "readInt past EOF -> EOFException");
            raf.setLength(10);
            check(raf.length() == 10, "extended length");
            raf.seek(5);
            check(raf.read() == 0, "extension fills with zeros");
            raf.setLength(0);
            check(raf.length() == 0, "zero length");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 8. skipBytes ----
    static void testSkipBytes() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.writeBytes("0123456789");
            raf.seek(0);
            check(raf.skipBytes(4) == 4 && raf.getFilePointer() == 4, "skip forward");
            check(raf.skipBytes(100) == 6 && raf.getFilePointer() == 10,
                  "skip clamps at EOF returning the remainder");
            check(raf.skipBytes(-3) == 0, "negative skip returns 0");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 9. sparse extension ----
    static void testSparseExtension() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.seek(50);
            raf.write(9);
            check(raf.length() == 51, "sparse extension");
            raf.seek(49);
            check(raf.read() == 0, "hole reads as zero");
            check(raf.read() == 9, "byte at the seek target");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 10. negative seek ----
    static void testNegativeSeek() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            try {
                raf.seek(-1);
                check(false, "negative seek must throw");
            } catch (IOException ioe) {
                check("Negative seek offset".equals(ioe.getMessage()),
                      "message: " + ioe.getMessage());
            }
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 11. readFully ----
    static void testReadFully() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.writeBytes("0123");
            raf.seek(0);
            final RandomAccessFile r = raf;
            checkThrows(EOFException.class,
                        () -> r.readFully(new byte[(int) r.length() + 1]),
                        "readFully past EOF -> EOFException");
            raf.seek(0);
            byte[] exact = new byte[4];
            raf.readFully(exact);
            check(exact[0] == '0' && exact[3] == '3', "readFully exact region");
            check(raf.read(new byte[0]) == 0, "zero-length read returns 0");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 12. closed-stream errors ----
    static void testClosed() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = new RandomAccessFile(tmp, "rw");
        try {
            raf.write(1);
            raf.close();
            checkThrows(IOException.class, () -> raf.read(), "read after close -> IOException");
            checkThrows(IOException.class, () -> raf.seek(0), "seek after close -> IOException");
            raf.close();                       // close is idempotent
            passed++;
        } finally {
            try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    public static void main(String[] args) throws Exception {
        testDataRoundTrip();
        testReadOnlyMode();
        testSyncModes();
        testIllegalModes();
        testMissingFile();
        testReadLine();
        testSetLength();
        testSkipBytes();
        testSparseExtension();
        testNegativeSeek();
        testReadFully();
        testClosed();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
