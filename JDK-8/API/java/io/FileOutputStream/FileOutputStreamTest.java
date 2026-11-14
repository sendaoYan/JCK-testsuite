/*
 * @test
 * @summary java.io.FileOutputStream conformance: full contract coverage.
 *          File/String constructors, truncate vs append, single/offset writes,
 *          getFD/getChannel, FileNotFoundException for a directory target.
 * @run main FileOutputStreamTest
 */

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // 平台消息形如 "<path> (<OS 原因>)"；原因文本随 OS/strerror 与默认 locale 变化
    // （zh_CN Windows 下是中文），只锁结构与路径，不锁英文措辞。
    static boolean hasOsReason(String msg, String path) {
        return msg != null && (path == null || msg.contains(path))
                && msg.lastIndexOf('(') > 0 && msg.trim().endsWith(")");
    }

    static File newTempDir() throws Exception {
        File d = File.createTempFile("jck-fos", ".tmp");
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

    static byte[] readAll(File f) throws Exception {
        FileInputStream fis = new FileInputStream(f);
        byte[] all = new byte[(int) f.length()];
        int n = fis.read(all);
        fis.close();
        return Arrays.copyOf(all, n);
    }

    static void testBehavior() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "out.bin");

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(new byte[]{1, 2, 3});
            check(fos.getFD() != null, "getFD non-null");
            check(fos.getChannel() != null, "getChannel non-null");
            fos.close();
            check(f.length() == 3, "wrote 3 bytes");

            // append mode
            FileOutputStream app = new FileOutputStream(f, true);
            app.write(new byte[]{4, 5}, 0, 2);
            app.close();
            check(f.length() == 5, "append adds bytes");
            check(Arrays.equals(readAll(f), new byte[]{1, 2, 3, 4, 5}), "append roundtrip");

            // default mode truncates
            FileOutputStream trunc = new FileOutputStream(f);
            trunc.write(9);
            trunc.close();
            check(f.length() == 1, "default mode truncates");
            check(Arrays.equals(readAll(f), new byte[]{9}), "truncate content");

            // write(byte[],off,len) region
            FileOutputStream reg = new FileOutputStream(f);
            reg.write(new byte[]{10, 20, 30, 40}, 1, 2);
            reg.close();
            check(f.length() == 2, "write(byte[],off,len) length");
            check(Arrays.equals(readAll(f), new byte[]{20, 30}), "write(byte[],off,len) content");

            // String constructor + append
            FileOutputStream sc = new FileOutputStream(f.getPath(), true);
            sc.write(new byte[]{7});
            sc.close();
            check(Arrays.equals(readAll(f), new byte[]{20, 30, 7}), "String ctor append");

            // directory target -> FileNotFoundException
            try {
                new FileOutputStream(dir);
                check(false, "directory target must throw FileNotFoundException");
            } catch (FileNotFoundException expected) {
                passed++;
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- 深化追加（契约级），行为全部经 JDK-8 实测锁定 ----

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

    /** 构造负例矩阵：null NPE、目录 FNFE（精确消息）、缺失父目录 FNFE。 */
    static void testConstructorNegatives() throws Exception {
        File dir = newTempDir();
        try {
            checkThrows(NullPointerException.class, () -> new FileOutputStream((File) null),
                        "null File -> NPE");
            checkThrows(NullPointerException.class, () -> new FileOutputStream((String) null),
                        "null String -> NPE");
            checkThrows(NullPointerException.class, () -> new FileOutputStream((String) null, true),
                        "null String,append -> NPE");
            checkThrows(NullPointerException.class, () -> new FileOutputStream((File) null, false),
                        "null File,append -> NPE");
            try {
                new FileOutputStream(dir);
                check(false, "directory must throw");
            } catch (FileNotFoundException e) {
                check(hasOsReason(e.getMessage(), dir.getPath()),
                      "JDK-8 directory message shape: " + e.getMessage());
            }
            try {
                new FileOutputStream(dir, true);
                check(false, "directory append must throw");
            } catch (FileNotFoundException e) {
                check(hasOsReason(e.getMessage(), dir.getPath()), "append variant same reason");
            }
            File orphan = new File(dir, "no/such/x.bin");
            try {
                new FileOutputStream(orphan);
                check(false, "missing parent must throw");
            } catch (FileNotFoundException e) {
                String m = e.getMessage();
                check(hasOsReason(m, orphan.getPath()),
                      "missing parent message has path and reason: " + m);
            }
            checkThrows(FileNotFoundException.class, () -> new FileOutputStream(orphan.getPath()),
                        "missing parent String -> FNFE");
            checkThrows(FileNotFoundException.class, () -> new FileOutputStream(orphan.getPath(), false),
                        "missing parent String,bool -> FNFE");
        } finally {
            deleteRecursively(dir);
        }
    }

    /** close 语义：double close 幂等、write 后 "Stream Closed"、flush 是空实现不抛、FD 失效。 */
    static void testCloseSemantics() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "cs.bin");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(65);
            FileDescriptor fd = fos.getFD();
            check(fd.valid(), "fd valid while open");
            check(fos.getFD() == fd, "getFD same instance");
            fos.close();
            fos.close();
            passed++;                                    // idempotent double close
            check(!fd.valid(), "fd invalid after close");
            try {
                fos.write(66);
                check(false, "write after close must throw");
            } catch (IOException ioe) {
                check("Stream Closed".equals(ioe.getMessage()),
                      "JDK-8 write-after-close message: " + ioe.getMessage());
            }
            try {
                fos.write(new byte[]{1});
                check(false, "bulk write after close must throw");
            } catch (IOException ioe) {
                check("Stream Closed".equals(ioe.getMessage()), "bulk write message");
            }
            fos.flush();
            passed++;                                    // flush() 是空实现：close 后仍不抛
            checkThrows(IOException.class, () -> fos.write(new byte[2], 0, 1),
                        "region write after close -> IOException");
        } finally {
            deleteRecursively(dir);
        }
    }

    /** write(int) 只写低 8 位。 */
    static void testWriteLowByte() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "low.bin");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(0x1FF);                            // -> 0xFF
            fos.write(0x141);                            // -> 0x41
            fos.write(0x100);                            // -> 0x00
            fos.close();
            FileInputStream in = new FileInputStream(f);
            check(in.read() == 0xFF, "0x1FF truncated to 0xFF");
            check(in.read() == 0x41, "0x141 truncated to 0x41");
            check(in.read() == 0x00, "0x100 truncated to 0x00");
            in.close();
        } finally {
            deleteRecursively(dir);
        }
    }

    /** write(byte[],off,len) 参数校验矩阵。 */
    static void testWriteBounds() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "wb.bin");
            FileOutputStream fos = new FileOutputStream(f);
            checkThrows(IndexOutOfBoundsException.class, () -> fos.write(new byte[2], 2, 1),
                        "write(b,2,1) off+len overflow -> IOOBE");
            checkThrows(IndexOutOfBoundsException.class, () -> fos.write(new byte[2], -1, 1),
                        "write(b,-1,1) -> IOOBE");
            checkThrows(IndexOutOfBoundsException.class, () -> fos.write(new byte[2], 0, -1),
                        "write(b,0,-1) -> IOOBE");
            checkThrows(IndexOutOfBoundsException.class, () -> fos.write(new byte[2], 3, 0),
                        "write(b,3,0) off beyond -> IOOBE");
            checkThrows(NullPointerException.class, () -> fos.write((byte[]) null),
                        "write(null) -> NPE");
            checkThrows(NullPointerException.class, () -> fos.write(null, 0, 1),
                        "write(null,0,1) -> NPE");
            fos.write(new byte[2], 2, 0);
            passed++;                                    // off == b.length, len == 0 legal
            fos.write(new byte[0]);
            passed++;                                    // empty array no-op
            check(f.length() == 0, "failed checks wrote nothing");
            fos.close();
        } finally {
            deleteRecursively(dir);
        }
    }

    /** append 定位与 truncate 截断语义。 */
    static void testAppendAndTruncate() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "ap.bin");
            FileOutputStream a1 = new FileOutputStream(f);
            a1.write(new byte[]{1, 2});
            a1.close();
            FileOutputStream a2 = new FileOutputStream(f, true);
            a2.write(new byte[]{3});
            a2.close();
            FileOutputStream a3 = new FileOutputStream(f.getPath(), true);
            a3.write(new byte[]{4});
            a3.close();
            check(Arrays.equals(readAll(f), new byte[]{1, 2, 3, 4}),
                  "append keeps ordering across all ctor variants");

            FileOutputStream t1 = new FileOutputStream(f);
            t1.write(new byte[]{9, 9, 9, 9, 9});
            t1.close();
            FileOutputStream t2 = new FileOutputStream(f);          // default: truncate
            t2.write(new byte[]{7});
            t2.close();
            check(f.length() == 1 && Arrays.equals(readAll(f), new byte[]{7}),
                  "default ctor truncates to new content");
            FileOutputStream t3 = new FileOutputStream(f, false);
            t3.write(new byte[]{5, 5});
            t3.close();
            check(f.length() == 2, "File,false truncates");
            FileOutputStream t4 = new FileOutputStream(f.getPath(), false);
            t4.write(new byte[]{6});
            t4.close();
            check(f.length() == 1 && Arrays.equals(readAll(f), new byte[]{6}),
                  "String,false truncates");
        } finally {
            deleteRecursively(dir);
        }
    }

    /** 写后不 close，用新 FileInputStream 读回可见性（flush-to-fs 与回读通道独立）。 */
    static void testReadbackNeedsNewStream() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "rb.bin");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(new byte[]{1, 2, 3});
            fos.flush();
            check(f.length() == 3, "bytes are visible in file length without close");
            check(Arrays.equals(readAll(f), new byte[]{1, 2, 3}),
                  "a new FileInputStream reads the written content");
            fos.close();
            check(f.length() == 3, "length unchanged by close");
        } finally {
            deleteRecursively(dir);
        }
    }

    /** FD/Channel 暴露：channel 存在、position 随写前进、经基类引用写。 */
    static void testFdAndChannel() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "fdc.bin");
            FileOutputStream fos = new FileOutputStream(f);
            check(fos.getChannel() != null, "getChannel non-null while open");
            check(fos.getChannel().position() == 0, "channel starts at 0");
            fos.write(new byte[]{1, 2});
            check(fos.getChannel().position() == 2, "channel position tracks writes");
            java.io.OutputStream os = fos;               // 经基类引用写
            os.write(3);
            os.flush();
            os.close();
            check(Arrays.equals(readAll(f), new byte[]{1, 2, 3}),
                  "writes through base reference land in file");
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        testConstructorNegatives();
        testCloseSemantics();
        testWriteLowByte();
        testWriteBounds();
        testAppendAndTruncate();
        testReadbackNeedsNewStream();
        testFdAndChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
