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

    // ================= 深化追加（契约级） =================
    // 以下行为均先以探针在 JDK-8 与 JDK-27 双侧实测锁定（见 DESIGN.md「深化追加」）。
    // 注意：setLength(-1) 双侧均为 IOException("Invalid argument")，不是 IAE。

    static interface Thrower { void run() throws Throwable; }

    // 平台消息形如 "<path> (<OS 原因>)"；原因文本随 OS/strerror 与默认 locale 变化
    // （zh_CN Windows 下是中文），只锁结构与路径。
    static boolean hasOsReason(String msg, String path) {
        return msg != null && (path == null || msg.contains(path))
                && msg.lastIndexOf('(') > 0 && msg.trim().endsWith(")");
    }

    static void mustThrowOsReason(Class<? extends Throwable> type, String path,
                                  Thrower r, String what) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t) && hasOsReason(t.getMessage(), path)) {
                passed++;
                return;
            }
            throw new AssertionError(what + ": expected " + type.getSimpleName()
                    + " with \"<path> (<OS reason>)\" but got " + t.getClass().getName()
                    + " [" + t.getMessage() + "]");
        }
        throw new AssertionError(what + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static void mustThrow(Class<? extends Throwable> type, String msgPart, Thrower r, String what) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t) && (msgPart == null
                    || (t.getMessage() != null && t.getMessage().contains(msgPart)))) {
                passed++;
                return;
            }
            throw new AssertionError(what + ": expected " + type.getSimpleName()
                    + (msgPart != null ? " containing \"" + msgPart + "\"" : "")
                    + " but got " + t.getClass().getName() + " [" + t.getMessage() + "]");
        }
        throw new AssertionError(what + ": expected " + type.getSimpleName() + " but nothing was thrown");
    }

    // ---- 13. ctor nulls + (String,String) == (File,String) ----
    static void testCtorNullsAndEquivalence() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.writeBytes("0123456789");
            raf.close();
            mustThrow(NullPointerException.class, null,
                      () -> new RandomAccessFile((String) null, "r"), "null name");
            mustThrow(NullPointerException.class, null,
                      () -> new RandomAccessFile((File) null, "r"), "null File");
            mustThrow(NullPointerException.class, null,
                      () -> new RandomAccessFile(tmp, (String) null), "null mode (File)");
            mustThrow(NullPointerException.class, null,
                      () -> new RandomAccessFile(tmp.getPath(), (String) null), "null mode (String)");
            RandomAccessFile byStr = new RandomAccessFile(tmp.getPath(), "r");
            RandomAccessFile byFile = new RandomAccessFile(tmp, "r");
            check(byStr.length() == byFile.length(), "(String,String)/(File,String) same length");
            check(byStr.getFilePointer() == 0 && byFile.getFilePointer() == 0, "both start at 0");
            check(byStr.read() == byFile.read(), "same first byte");
            byStr.close();
            byFile.close();
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 14. near-miss modes are rejected with the exact message ----
    static void testIllegalModesExtended() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        try {
            for (String bad : new String[]{"rw ", "rwS", "RWS", "r ws", "rwsx", "rws "}) {
                final String mode = bad;
                mustThrow(IllegalArgumentException.class,
                          "Illegal mode \"" + mode + "\" must be one of \"r\", \"rw\", \"rws\", or \"rwd\"",
                          () -> new RandomAccessFile(tmp, mode), "mode '" + mode + "'");
            }
        } finally {
            tmp.delete();
        }
    }

    // ---- 15. missing files, creation semantics, opening directories ----
    static void testMissingAndDirectories() throws Exception {
        File dir = java.nio.file.Files.createTempDirectory("rafdir").toFile();
        try {
            File missing = new File(dir, "no-such.dat");
            try {
                new RandomAccessFile(missing, "r");
                check(false, "r on missing must throw");
            } catch (FileNotFoundException fnfe) {
                check(fnfe.getMessage() != null && fnfe.getMessage().contains(missing.getPath()),
                      "FNFE message contains the path");
                check(hasOsReason(fnfe.getMessage(), missing.getPath()),
                      "FNFE message carries the OS reason");
            }
            File created = new File(dir, "created.dat");
            RandomAccessFile raf = new RandomAccessFile(created, "rw");
            check(created.exists() && raf.length() == 0, "rw creates a missing file, empty");
            raf.close();
            created.delete();
            File createdSync = new File(dir, "created-sync.dat");
            raf = new RandomAccessFile(createdSync, "rws");
            check(createdSync.exists(), "rws creates a missing file");
            raf.close();
            createdSync.delete();

            // opening a directory fails in every mode;原因文本随 OS（Linux "Is a directory"，
            // Windows 中文 "拒绝访问。"），只锁异常类型 + 路径 + 括号原因结构。
            mustThrowOsReason(FileNotFoundException.class, dir.getPath(),
                      () -> new RandomAccessFile(dir, "r"), "r on a directory");
            mustThrowOsReason(FileNotFoundException.class, dir.getPath(),
                      () -> new RandomAccessFile(dir, "rw"), "rw on a directory");
        } finally {
            for (File k : dir.listFiles()) k.delete();
            dir.delete();
        }
    }

    // ---- 16. read-only mode failure messages ----
    static void testReadOnlyMessages() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            RandomAccessFile seed = new RandomAccessFile(tmp, "rw");
            seed.write(65);
            seed.close();
            raf = new RandomAccessFile(tmp, "r");
            check(raf.getFilePointer() == 0, "r mode starts at 0");
            check(raf.length() == 1, "r mode sees the length");
            final RandomAccessFile ro2 = raf;
            mustThrow(IOException.class, "Bad file descriptor",
                      () -> ro2.write(66), "r write(int)");
            mustThrow(IOException.class, "Bad file descriptor",
                      () -> ro2.writeInt(1), "r writeInt");
            mustThrow(IOException.class, "Invalid argument",
                      () -> ro2.setLength(0), "r setLength");
            check(raf.getFD().valid(), "FD valid while open");
            check(raf.getChannel() != null && raf.getChannel().isOpen(), "channel open while open");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 17. closed-stream failures all say "Stream Closed" ----
    static void testClosedMessages() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        try {
            RandomAccessFile raf = new RandomAccessFile(tmp, "rw");
            raf.write(1);
            raf.close();
            final RandomAccessFile c = raf;
            mustThrow(IOException.class, "Stream Closed", () -> c.read(), "read after close");
            mustThrow(IOException.class, "Stream Closed", () -> c.write(1), "write after close");
            mustThrow(IOException.class, "Stream Closed", () -> c.seek(0), "seek after close");
            mustThrow(IOException.class, "Stream Closed", () -> c.getFilePointer(), "getFilePointer after close");
            mustThrow(IOException.class, "Stream Closed", () -> c.length(), "length after close");
            mustThrow(IOException.class, "Stream Closed", () -> c.readUTF(), "readUTF after close");
            check(c.getFD() != null, "getFD still returns the descriptor");
            check(!c.getFD().valid(), "FD invalid after close");
            // a channel obtained BEFORE the close is closed by it (both JDKs)
            RandomAccessFile pre = new RandomAccessFile(tmp, "rw");
            java.nio.channels.FileChannel ch = pre.getChannel();
            check(ch.isOpen(), "channel open while RAF is open");
            pre.close();
            check(!ch.isOpen(), "closing the RAF closes a channel obtained earlier");
            c.close();                                      // idempotent
            passed++;
            // JDK-8 quirk: a channel lazily created AFTER close still reports open
            check(raf.getChannel().isOpen(),
                  "JDK-8: lazily created channel after close reports open (recorded quirk)");
        } finally {
            tmp.delete();
        }
    }

    // ---- 18. seek edges: negative, Long.MIN_VALUE, beyond EOF ----
    static void testSeekEdges() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.writeBytes("0123");
            final RandomAccessFile fs = raf;
            mustThrow(IOException.class, "Negative seek offset",
                      () -> fs.seek(-1), "seek(-1)");
            mustThrow(IOException.class, "Negative seek offset",
                      () -> fs.seek(Long.MIN_VALUE), "seek(Long.MIN_VALUE)");
            raf.seek(100);
            check(raf.getFilePointer() == 100, "pointer beyond EOF allowed");
            check(raf.read() == -1, "read beyond EOF is -1");
            check(raf.getFilePointer() == 100, "EOF read does not advance the pointer");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 19. setLength: negative -> IOException("Invalid argument"), pointer clamp ----
    static void testSetLengthEdges() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.writeBytes("0123456789");
            final RandomAccessFile fl = raf;
            mustThrow(IOException.class, "Invalid argument",
                      () -> fl.setLength(-1), "setLength(-1)");
            raf.seek(8);
            raf.setLength(4);
            check(raf.length() == 4, "truncated");
            check(raf.getFilePointer() == 4, "pointer clamped down to the new length");
            raf.setLength(20);
            raf.seek(12);
            check(raf.read() == 0, "extended region reads as zeros");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 20. DataOutput wire format is fixed BIG-endian (raw bytes verified) ----
    static void testWireFormat() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.setLength(0);
            raf.writeBoolean(true);
            raf.writeShort(0xBEEF);
            raf.writeInt(0x12345678);
            raf.writeLong(0x1122334455667788L);
            raf.writeChar('Z');
            byte[] raw = new byte[(int) raf.length()];
            raf.seek(0);
            raf.readFully(raw);
            byte[] expected = new byte[]{
                    1, (byte) 0xBE, (byte) 0xEF, 0x12, 0x34, 0x56, 0x78,
                    0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88,
                    0x00, 0x5A};
            check(java.util.Arrays.equals(raw, expected), "boolean/short/int/long/char raw BIG-endian layout");
            raf.setLength(0);
            raf.writeUTF("A");
            raw = new byte[3];
            raf.seek(0);
            raf.readFully(raw);
            check(raw[0] == 0 && raw[1] == 1 && raw[2] == 'A', "writeUTF: 2-byte big-endian length + bytes");
            raf.setLength(0);
            raf.writeChars("AB");
            raw = new byte[4];
            raf.seek(0);
            raf.readFully(raw);
            check(raw[0] == 0 && raw[1] == 'A' && raw[2] == 0 && raw[3] == 'B', "writeChars: 2 bytes/char BIG-endian");
            raf.setLength(0);
            raf.writeBytes("héllo");
            raw = new byte[5];
            raf.seek(0);
            raf.readFully(raw);
            check((raw[1] & 0xFF) == 0xE9, "writeBytes keeps only the low 8 bits of each char");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 21. modified-UTF-8 edges: NUL, surrogates, overlong, too long ----
    static void testUtfEdges() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.setLength(0);
            raf.writeUTF("");
            raf.seek(0);
            check("".equals(raf.readUTF()), "empty UTF round-trip");
            raf.setLength(0);
            raf.writeUTF("\u0000");                        // modified UTF-8: 2 bytes, not 1
            byte[] raw = new byte[2];
            raf.seek(0);
            raf.readFully(raw);
            check(raw[0] == 0 && raw[1] == 2, "NUL encodes as 2 bytes (length prefix counts them)");
            raf.seek(0);
            check("\u0000".equals(raf.readUTF()), "NUL round-trips");
            raf.setLength(0);
            String supra = "𐐀";                 // surrogate pair
            raf.writeUTF(supra);
            raf.seek(0);
            check(supra.equals(raf.readUTF()), "surrogate pair round-trips");
            raf.setLength(0);
            StringBuilder big = new StringBuilder();
            for (int i = 0; i < 70000; i++) big.append('a');
            final RandomAccessFile r = raf;
            final String s = big.toString();
            mustThrow(java.io.UTFDataFormatException.class, "too long: 70000 bytes",
                      () -> r.writeUTF(s), "writeUTF longer than 65535 encoded bytes");
            raf.setLength(0);
            raf.write(new byte[]{0, 1, (byte) 0xFF});      // 1-char string with a malformed byte
            raf.seek(0);
            mustThrow(java.io.UTFDataFormatException.class, null,
                      () -> r.readUTF(), "readUTF malformed byte");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 22. typed reads at EOF raise EOFException ----
    static void testEofTypedReads() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.write(1);
            raf.seek(0);
            raf.read();
            final RandomAccessFile r = raf;
            mustThrow(EOFException.class, null, () -> r.readBoolean(), "readBoolean at EOF");
            mustThrow(EOFException.class, null, () -> r.readInt(), "readInt at EOF");
            mustThrow(EOFException.class, null, () -> r.readUTF(), "readUTF at EOF");
            mustThrow(EOFException.class, null, () -> r.readFully(new byte[2], 0, 2), "readFully at EOF");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 23. index bounds: IndexOutOfBoundsException (plain, not Array...) ----
    static void testIndexBounds() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.writeBytes("0123456789");
            raf.seek(0);
            byte[] b = new byte[4];
            final RandomAccessFile r = raf;
            mustThrow(IndexOutOfBoundsException.class, null, () -> r.read(b, -1, 0), "read(b,-1,0)");
            mustThrow(IndexOutOfBoundsException.class, null, () -> r.read(b, 0, -1), "read(b,0,-1)");
            mustThrow(IndexOutOfBoundsException.class, null, () -> r.read(b, 3, 2), "read(b,3,2)");
            mustThrow(IndexOutOfBoundsException.class, null, () -> r.write(b, -1, 0), "write(b,-1,0)");
            mustThrow(IndexOutOfBoundsException.class, null, () -> r.write(b, 0, -1), "write(b,0,-1)");
            mustThrow(IndexOutOfBoundsException.class, null, () -> r.write(b, 3, 2), "write(b,3,2)");
            mustThrow(IndexOutOfBoundsException.class, null, () -> r.readFully(b, -1, 0), "readFully(b,-1,0)");
            mustThrow(IndexOutOfBoundsException.class, null, () -> r.readFully(b, 3, 2), "readFully(b,3,2)");
            check(r.read(b, 4, 0) == 0, "zero-length read at off==length is legal");
            r.write(b, 4, 0);                               // zero-length write: legal
            passed++;
            check(r.getFilePointer() == 0, "zero-length ops do not move the pointer");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 24. FileChannel shares the file pointer with the RAF ----
    static void testChannelInterop() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.writeBytes("0123456789");
            raf.seek(3);
            check(raf.getChannel().position() == 3, "channel position mirrors seek");
            java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(2);
            raf.getChannel().read(buf);
            check(raf.getFilePointer() == 5, "channel read advances the RAF pointer");
            check(raf.getChannel().position() == 5, "and the channel position");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 25. readLine treats a lone '\r' as a line terminator ----
    static void testReadLineLoneCr() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.writeBytes("a\rb\rc");
            raf.seek(0);
            check("a".equals(raf.readLine()), "lone CR ends a line");
            check("b".equals(raf.readLine()), "second lone CR line");
            check("c".equals(raf.readLine()), "final unterminated line");
            check(raf.readLine() == null, "EOF after last line");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
            tmp.delete();
        }
    }

    // ---- 26. partial reads, offset variants, unsigned reads ----
    static void testReadFragments() throws Exception {
        File tmp = File.createTempFile("raf", ".dat");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmp, "rw");
            raf.writeBytes("0123456789");
            raf.seek(0);
            check(raf.skipBytes(0) == 0, "skipBytes(0) is 0");
            raf.seek(8);
            byte[] b = new byte[10];
            check(raf.read(b, 0, 10) == 2, "read past the tail returns the remainder");
            raf.seek(0);
            java.util.Arrays.fill(b, (byte) 0);
            int n = raf.read(b, 2, 4);
            check(n == 4, "read(b,off,len) count");
            check(b[0] == 0 && b[1] == 0, "bytes before off untouched");
            check(b[2] == '0' && b[3] == '1' && b[4] == '2' && b[5] == '3', "data lands at off..off+len");
            raf.seek(0);
            java.util.Arrays.fill(b, (byte) 0);
            raf.readFully(b, 2, 4);
            check(b[2] == '0' && b[5] == '3' && b[6] == 0, "readFully(b,off,len) region only");
            final RandomAccessFile fr = raf;
            mustThrow(EOFException.class, null,
                      () -> fr.readFully(new byte[10], 0, 10), "readFully needs the full count");
            raf.setLength(0);
            raf.writeShort(0xBEEF);
            raf.seek(0);
            check(raf.readUnsignedShort() == 0xBEEF, "readUnsignedShort does not sign-extend");
        } finally {
            if (raf != null) try { raf.close(); } catch (IOException ignored) { }
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
        testCtorNullsAndEquivalence();
        testIllegalModesExtended();
        testMissingAndDirectories();
        testReadOnlyMessages();
        testClosedMessages();
        testSeekEdges();
        testSetLengthEdges();
        testWireFormat();
        testUtfEdges();
        testEofTypedReads();
        testIndexBounds();
        testChannelInterop();
        testReadLineLoneCr();
        testReadFragments();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
