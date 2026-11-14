/*
 * @test
 * @summary java.io.FileInputStream conformance: full contract coverage.
 *          File/String constructors, single/offset reads, skip, available,
 *          EOF, getFD/getChannel, FileNotFoundException for a missing file.
 * @run main FileInputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // FNFE 的平台消息形如 "<path> (<OS 原因>)"；原因文本由 OS/strerror 与默认
    // locale 决定（zh_CN Windows 下是中文），只断言结构，不锁英文措辞。
    static boolean hasOsReason(String msg, String path) {
        return msg != null && (path == null || msg.contains(path))
                && msg.lastIndexOf('(') > 0 && msg.trim().endsWith(")");
    }

    static File newTempDir() throws Exception {
        File d = File.createTempFile("jck-fis", ".tmp");
        if (!d.delete() || !d.mkdir()) throw new AssertionError("temp dir");
        return d;
    }

    static void deleteRecursively(File f) {
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids != null) for (File k : kids) deleteRecursively(k);
        }
        f.delete();
    }

    // read all remaining bytes without JDK9+ readAllBytes
    static byte[] drain(FileInputStream fis) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[16];
        int n;
        while ((n = fis.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    static void testBehavior() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "data.bin");
            byte[] data = {10, 20, 30, 40, 50, 60, 70};
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(data);
            fos.close();

            FileInputStream fis = new FileInputStream(f);
            check(fis.getFD() != null, "getFD non-null");
            check(fis.getChannel() != null, "getChannel non-null");
            check(fis.available() == data.length, "available == remaining length");
            check(fis.read() == 10, "read() == 10");
            byte[] buf = new byte[3];
            check(fis.read(buf, 1, 2) == 2 && buf[1] == 20 && buf[2] == 30,
                  "read(byte[],off,len)");
            check(fis.skip(1) == 1, "skip returns count");
            check(fis.read() == 50, "read after skip");
            check(fis.read() == 60 && fis.read() == 70, "remaining bytes consumed");
            check(fis.read() == -1, "EOF read() == -1");
            check(fis.available() == 0, "available 0 at EOF");
            fis.close();

            // String constructor
            FileInputStream fis2 = new FileInputStream(f.getPath());
            check(Arrays.equals(drain(fis2), data), "String ctor + drain equals content");
            fis2.close();

            // read(byte[]) full
            FileInputStream fis3 = new FileInputStream(f);
            byte[] all = new byte[data.length];
            check(fis3.read(all) == data.length && Arrays.equals(all, data), "read(byte[]) full");
            check(fis3.read() == -1, "EOF after full read");
            fis3.close();

            // missing file
            try {
                new FileInputStream(new File(dir, "missing.bin"));
                check(false, "missing file must throw FileNotFoundException");
            } catch (FileNotFoundException expected) {
                passed++;
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- 深化追加（契约级），消息与行为全部经 JDK-8 实测锁定 ----

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

    /** 构造负例矩阵：缺失文件/目录/null，消息含路径与原因子串。 */
    static void testConstructorNegatives() throws Exception {
        File dir = newTempDir();
        try {
            File missing = new File(dir, "missing.bin");
            checkThrows(FileNotFoundException.class, () -> new FileInputStream(missing),
                        "missing File -> FNFE");
            try {
                new FileInputStream(missing);
            } catch (FileNotFoundException e) {
                String m = e.getMessage();
                check(hasOsReason(m, missing.getPath()),
                      "missing-file message has path and reason: " + m);
            }
            File nested = new File(dir, "no/such/deep.bin");
            checkThrows(FileNotFoundException.class, () -> new FileInputStream(nested.getPath()),
                        "missing String -> FNFE");
            try {
                new FileInputStream(nested.getPath());
            } catch (FileNotFoundException e) {
                check(hasOsReason(e.getMessage(), nested.getPath()),
                      "missing-parent message reason: " + e.getMessage());
            }
            checkThrows(FileNotFoundException.class, () -> new FileInputStream(dir),
                        "directory File -> FNFE");
            try {
                new FileInputStream(dir);
            } catch (FileNotFoundException e) {
                String m = e.getMessage();
                // 目录被当文件打开：Linux 是 "(Is a directory)"，Windows 是 "(拒绝访问。)"
                // 一类的 OS 文本 —— 只锁 "<path> (<原因>)" 的形状。
                check(hasOsReason(m, dir.getPath()),
                      "JDK-8 directory message shape: " + m);
            }
            checkThrows(NullPointerException.class, () -> new FileInputStream((File) null),
                        "null File -> NPE");
            checkThrows(NullPointerException.class, () -> new FileInputStream((String) null),
                        "null String -> NPE");
        } finally {
            deleteRecursively(dir);
        }
    }

    /** close 语义：幂等、关闭后 read/available 抛 "Stream Closed"、getFD/getChannel 仍可调用。 */
    static void testCloseSemantics() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "cs.bin");
            try (FileOutputStream fos = new FileOutputStream(f)) { fos.write(new byte[]{1, 2, 3}); }

            FileInputStream fis = new FileInputStream(f);
            FileDescriptor fd = fis.getFD();
            java.nio.channels.FileChannel ch = fis.getChannel();
            check(fd.valid(), "fd valid while open");
            check(fis.getFD() == fd, "getFD returns same instance");
            check(ch.isOpen(), "channel open while stream open");
            fis.close();
            fis.close();
            passed++;                                    // double close is idempotent
            check(!fd.valid(), "fd invalid after close");
            try {
                fis.read();
                check(false, "read after close must throw");
            } catch (IOException ioe) {
                check("Stream Closed".equals(ioe.getMessage()),
                      "JDK-8 read-after-close message: " + ioe.getMessage());
            }
            try {
                fis.available();
                check(false, "available after close must throw");
            } catch (IOException ioe) {
                check("Stream Closed".equals(ioe.getMessage()), "available message");
            }
            try {
                fis.skip(1);
                check(false, "skip after close must throw");
            } catch (IOException ioe) {
                // JDK-8：skip 路径同样报 Stream Closed（JDK-27 报 Bad file descriptor，见 DESIGN 差异表）
                check("Stream Closed".equals(ioe.getMessage()),
                      "JDK-8 skip-after-close message: " + ioe.getMessage());
            }
            check(fis.getFD() == fd, "getFD still callable after close");
            check(fis.getChannel() != null, "getChannel still returns after close");
            check(!ch.isOpen(), "channel obtained before close is closed with the stream");
        } finally {
            deleteRecursively(dir);
        }
    }

    /** read 参数校验矩阵（native readBytes 层）。 */
    static void testReadBounds() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "rb.bin");
            try (FileOutputStream fos = new FileOutputStream(f)) { fos.write(new byte[]{1, 2, 3, 4}); }
            FileInputStream fis = new FileInputStream(f);
            checkThrows(IndexOutOfBoundsException.class, () -> fis.read(new byte[2], 1, 2),
                        "read(b,1,2) overflow -> IOOBE");
            checkThrows(IndexOutOfBoundsException.class, () -> fis.read(new byte[2], 3, 0),
                        "read(b,3,0) off beyond -> IOOBE");
            checkThrows(IndexOutOfBoundsException.class, () -> fis.read(new byte[2], -1, 1),
                        "read(b,-1,1) -> IOOBE");
            checkThrows(NullPointerException.class, () -> fis.read((byte[]) null),
                        "read(null) -> NPE");
            checkThrows(NullPointerException.class, () -> fis.read(null, 0, 1),
                        "read(null,0,1) -> NPE");
            check(fis.read(new byte[2], 2, 0) == 0, "read(b,b.length,0) == 0");
            check(fis.read(new byte[0]) == 0, "read(empty array) == 0");
            check(fis.read() == 1, "position preserved across failed arg checks");
            fis.close();
        } finally {
            deleteRecursively(dir);
        }
    }

    /** skip 契约：精确、可越过 EOF（lseek 语义）、空文件。 */
    static void testSkipSemantics() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "sk.bin");
            try (FileOutputStream fos = new FileOutputStream(f)) { fos.write(new byte[]{1, 2, 3, 4}); }
            try (FileInputStream fis = new FileInputStream(f)) {
                check(fis.skip(0) == 0, "skip(0) == 0");
                // FIS 覆写了 skip：负数不像基类那样返回 0，而是 native lseek
                // EINVAL → IOException("Invalid argument")（双侧实测一致）
                try {
                    fis.skip(-5);
                    check(false, "FIS skip(negative) must throw");
                } catch (IOException ioe) {
                    // lseek 失败时消息就是 OS 的 errno 文本：POSIX 是
                    // "Invalid argument"，Windows 是本地化系统消息。
                    if (File.separatorChar == '/') {
                        check("Invalid argument".equals(ioe.getMessage()),
                              "negative skip message: " + ioe.getMessage());
                    } else {
                        check(ioe.getMessage() != null && !ioe.getMessage().isEmpty(),
                              "negative skip message non-empty: " + ioe.getMessage());
                    }
                }
                check(fis.read() == 1, "failed negative skip did not move position");
                check(fis.skip(2) == 2 && fis.read() == 4, "skip(2) precise");
                check(fis.skip(100) == 100, "skip may run past EOF (lseek semantics)");
                check(fis.read() == -1, "read after over-skip is EOF");
                // 注：越过 EOF 之后再做小 skip 的返回值/异常在 native 层非确定
                // （有时返回 n，有时抛 "Invalid argument"），不属稳定契约，不作断言。
            }
            File empty = new File(dir, "empty.bin");
            new FileOutputStream(empty).close();
            try (FileInputStream fis = new FileInputStream(empty)) {
                check(fis.read() == -1 && fis.read(new byte[1]) == -1, "empty file EOF reads");
                check(fis.available() == 0, "empty file available 0");
                check(fis.skip(3) == 3, "empty file skip still reports lseek result (3)");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    /** FileChannel 位置随读前进；String 与 File 构造读回一致。 */
    static void testChannelAndCtorConsistency() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "ch.bin");
            try (FileOutputStream fos = new FileOutputStream(f)) { fos.write(new byte[]{1, 2, 3, 4, 5}); }
            try (FileInputStream fis = new FileInputStream(f)) {
                check(fis.getChannel().position() == 0, "channel starts at 0");
                fis.read();
                fis.read(new byte[2]);
                check(fis.getChannel().position() == 3, "channel position tracks reads");
            }
            byte[] viaFile, viaString;
            try (FileInputStream a = new FileInputStream(f)) { viaFile = drain(a); }
            try (FileInputStream b = new FileInputStream(f.getPath())) { viaString = drain(b); }
            check(java.util.Arrays.equals(viaFile, viaString) && viaFile.length == 5,
                  "File ctor and String ctor read identical content");
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        testConstructorNegatives();
        testCloseSemantics();
        testReadBounds();
        testSkipSemantics();
        testChannelAndCtorConsistency();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
