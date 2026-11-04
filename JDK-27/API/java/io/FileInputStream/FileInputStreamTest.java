/*
 * @test
 * @summary java.io.FileInputStream conformance: white-box structure (reflection) + black-box
 *          behavior (reads, skip, available, readAllBytes/readNBytes/transferTo, channel/FD).
 * @run main FileInputStreamTest
 *
 * 黑盒设计案:
 * 1. 契约: 从文件读取字节的输入流，支持单字节/批量读、skip、available，并暴露 getFD/getChannel。
 * 2. 场景: FileOutputStream 写文件 -> FileInputStream(File)/try-with-resources 读回。
 * 3. 组合: 写已知字节序列 -> read()/read(byte[],off,len)/skip/available 逐步读 -> readAllBytes 全量比对 -> transferTo 拷贝。
 * 4. 不变量: available 初值为剩余字节数；读到末尾 read()==-1；readAllBytes 返回完整内容。
 * 5. 边界: 打开不存在的文件抛 FileNotFoundException。
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.FileInputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.InputStream.class, "extends InputStream");
        hasMethod(c, "read");
        hasMethod(c, "readAllBytes");
        hasMethod(c, "readNBytes", int.class);
        hasMethod(c, "transferTo", java.io.OutputStream.class);
        hasMethod(c, "getChannel");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-fis");
        File f = dir.resolve("data.bin").toFile();
        byte[] data = {10, 20, 30, 40, 50, 60, 70};
        try {
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(data);
            }

            try (FileInputStream fis = new FileInputStream(f)) {
                check(fis.getFD() != null, "getFD");
                check(fis.getChannel() != null, "getChannel");
                check(fis.available() == data.length, "available");
                check(fis.read() == 10, "read 10");
                byte[] buf = new byte[3];
                check(fis.read(buf, 1, 2) == 2 && buf[1] == 20 && buf[2] == 30, "read(byte[],off,len)");
                check(fis.skip(1) == 1, "skip");
                check(fis.read() == 50, "read 50 after skip");
                byte[] rest = fis.readNBytes(2);
                check(Arrays.equals(rest, new byte[]{60, 70}), "readNBytes");
                check(fis.read() == -1, "EOF");
            }

            try (FileInputStream fis = new FileInputStream(f)) {
                check(Arrays.equals(fis.readAllBytes(), data), "readAllBytes");
            }

            try (FileInputStream fis = new FileInputStream(f);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                check(fis.transferTo(bos) == data.length, "transferTo count");
                check(Arrays.equals(bos.toByteArray(), data), "transferTo content");
            }

            boolean fnf = false;
            try { new FileInputStream(dir.resolve("missing.bin").toFile()); }
            catch (FileNotFoundException e) { fnf = true; }
            check(fnf, "FileNotFoundException for missing file");
        } finally {
            Files.deleteIfExists(f.toPath());
            Files.deleteIfExists(dir);
        }
    }

    // ---- 深化追加（契约级），行为全部经 JDK-27 实测锁定 ----

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

    /** 构造负例矩阵：消息含路径与原因子串（JDK-27 与 8 同文案，NPE 带 helpful 消息）。 */
    static void testConstructorNegatives() throws Exception {
        Path dir = Files.createTempDirectory("jck-fis27");
        try {
            File missing = dir.resolve("missing.bin").toFile();
            checkThrows(FileNotFoundException.class, () -> new FileInputStream(missing),
                        "missing File -> FNFE");
            try {
                new FileInputStream(missing);
            } catch (FileNotFoundException e) {
                String m = e.getMessage();
                check(m.contains(missing.getPath()) && m.contains("No such file or directory"),
                      "missing-file message has path and reason: " + m);
            }
            checkThrows(FileNotFoundException.class,
                        () -> new FileInputStream(dir.resolve("no/such").toString()),
                        "missing String -> FNFE");
            checkThrows(FileNotFoundException.class, () -> new FileInputStream(dir.toFile()),
                        "directory File -> FNFE");
            try {
                new FileInputStream(dir.toFile());
            } catch (FileNotFoundException e) {
                check(e.getMessage().equals(dir.toString() + " (Is a directory)"),
                      "JDK-27 directory message exact: " + e.getMessage());
            }
            try {
                new FileInputStream((File) null);
                check(false, "null File must throw");
            } catch (NullPointerException npe) {
                check(npe.getMessage() != null && npe.getMessage().contains("\"file\" is null"),
                      "JDK-27 null File NPE message: " + npe.getMessage());
            }
            checkThrows(NullPointerException.class, () -> new FileInputStream((String) null),
                        "null String -> NPE");
        } finally {
            delTree(dir);
        }
    }

    static void delTree(Path p) throws Exception {
        if (Files.isDirectory(p)) {
            try (java.util.stream.Stream<Path> s = Files.list(p)) {
                for (Path c : (Iterable<Path>) s::iterator) delTree(c);
            }
        }
        Files.deleteIfExists(p);
    }

    /** close 语义：幂等、read/available 报 "Stream Closed"、skip 报 "Bad file descriptor"（与 8 差异）。 */
    static void testCloseSemantics() throws Exception {
        Path dir = Files.createTempDirectory("jck-fis27c");
        File f = dir.resolve("cs.bin").toFile();
        try {
            try (FileOutputStream fos = new FileOutputStream(f)) { fos.write(new byte[]{1, 2, 3}); }
            FileInputStream fis = new FileInputStream(f);
            java.io.FileDescriptor fd = fis.getFD();
            java.nio.channels.FileChannel ch = fis.getChannel();
            check(fd.valid() && fis.getFD() == fd, "fd valid, same instance while open");
            check(ch.isOpen(), "channel open while stream open");
            fis.close();
            fis.close();
            passed++;                                    // idempotent double close
            check(!fd.valid(), "fd invalid after close");
            check(!ch.isOpen(), "channel closed with the stream");
            try {
                fis.read();
                check(false, "read after close must throw");
            } catch (IOException ioe) {
                check("Stream Closed".equals(ioe.getMessage()),
                      "read-after-close message: " + ioe.getMessage());
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
                // JDK-27 实测：skip 路径报 Bad file descriptor；JDK-8 报 Stream Closed（见 DESIGN 差异表）
                check("Bad file descriptor".equals(ioe.getMessage()),
                      "JDK-27 skip-after-close message: " + ioe.getMessage());
            }
            check(fis.getFD() == fd, "getFD still callable after close");
            check(fis.getChannel() != null, "getChannel still returns after close");
        } finally {
            delTree(dir);
        }
    }

    /** read 参数校验矩阵（JDK-27 readAllBytes/readNBytes 附加路径）。 */
    static void testReadBounds() throws Exception {
        Path dir = Files.createTempDirectory("jck-fis27b");
        File f = dir.resolve("rb.bin").toFile();
        try {
            try (FileOutputStream fos = new FileOutputStream(f)) { fos.write(new byte[]{1, 2, 3, 4}); }
            FileInputStream fis = new FileInputStream(f);
            checkThrows(IndexOutOfBoundsException.class, () -> fis.read(new byte[2], 1, 2),
                        "read(b,1,2) overflow -> IOOBE");
            checkThrows(IndexOutOfBoundsException.class, () -> fis.read(new byte[2], 3, 0),
                        "read(b,3,0) off beyond -> IOOBE");
            checkThrows(NullPointerException.class, () -> fis.read((byte[]) null),
                        "read(null) -> NPE");
            checkThrows(NullPointerException.class, () -> fis.read(null, 0, 1),
                        "read(null,0,1) -> NPE");
            check(fis.read(new byte[2], 2, 0) == 0, "read(b,b.length,0) == 0");
            check(fis.read(new byte[0]) == 0, "read(empty array) == 0");
            checkThrows(IndexOutOfBoundsException.class, () -> fis.readNBytes(new byte[2], 1, 2),
                        "readNBytes(b,1,2) overflow -> IOOBE");
            checkThrows(IllegalArgumentException.class, () -> fis.readNBytes(-1),
                        "readNBytes(-1) -> IAE");
            check(fis.readNBytes(0).length == 0, "readNBytes(0) empty");
            check(fis.read() == 1, "position preserved across failed arg checks");
            fis.close();
        } finally {
            delTree(dir);
        }
    }

    /** skip 契约：负数 native EINVAL（同 8）、可越过 EOF、空文件。 */
    static void testSkipSemantics() throws Exception {
        Path dir = Files.createTempDirectory("jck-fis27s");
        File f = dir.resolve("sk.bin").toFile();
        try {
            try (FileOutputStream fos = new FileOutputStream(f)) { fos.write(new byte[]{1, 2, 3, 4}); }
            try (FileInputStream fis = new FileInputStream(f)) {
                check(fis.skip(0) == 0, "skip(0) == 0");
                try {
                    fis.skip(-5);
                    check(false, "FIS skip(negative) must throw");
                } catch (IOException ioe) {
                    check("Invalid argument".equals(ioe.getMessage()),
                          "negative skip message: " + ioe.getMessage());
                }
                check(fis.read() == 1, "failed negative skip did not move position");
                check(fis.skip(2) == 2 && fis.read() == 4, "skip(2) precise");
                check(fis.skip(100) == 100, "skip may run past EOF (lseek semantics)");
                check(fis.read() == -1, "read after over-skip is EOF");
            }
            File empty = dir.resolve("empty.bin").toFile();
            new FileOutputStream(empty).close();
            try (FileInputStream fis = new FileInputStream(empty)) {
                check(fis.read() == -1 && fis.available() == 0, "empty file EOF/available");
                check(fis.skip(3) == 3, "empty file skip reports lseek result (3)");
            }
        } finally {
            delTree(dir);
        }
    }

    /** 大文件分块读：readAllBytes/readNBytes/transferTo/skipNBytes 组合（>2 个内部缓冲区）。 */
    static void testBulkReadMatrix() throws Exception {
        Path dir = Files.createTempDirectory("jck-fis27m");
        File f = dir.resolve("big.bin").toFile();
        try {
            final int n = 30_000;
            try (FileOutputStream fos = new FileOutputStream(f)) {
                for (int i = 0; i < n; i++) fos.write(i & 0xFF);
            }
            try (FileInputStream fis = new FileInputStream(f)) {
                byte[] all = fis.readAllBytes();
                check(all.length == n, "readAllBytes drains 30000 bytes");
                check(all[0] == 0 && all[29999] == (29999 & 0xFF), "content endpoints exact");
            }
            try (FileInputStream fis = new FileInputStream(f)) {
                byte[] head = fis.readNBytes(5);
                check(head.length == 5 && head[4] == 4, "readNBytes(5)");
                byte[] buf = new byte[3];
                check(fis.readNBytes(buf, 0, 3) == 3 && buf[0] == 5, "readNBytes(b,0,3)");
                fis.skipNBytes(10);
                check(fis.read() == 18, "position after skipNBytes(10)");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                check(fis.transferTo(bos) == n - 19, "transferTo reports remainder count");
                check(bos.size() == n - 19 && bos.toByteArray()[0] == 19, "transferTo content");
            }
            try (FileInputStream fis = new FileInputStream(f)) {
                fis.skipNBytes(n);
                check(fis.read() == -1, "skipNBytes(exact length) lands at EOF");
                fis.skipNBytes(1);
                passed++;      // FIS 覆写 skip：越过 EOF 的 skip 返回成功，故 skipNBytes 不抛 EOF
            }
            byte[] partial = new byte[10];
            try (FileInputStream fis = new FileInputStream(f)) {
                check(fis.readNBytes(partial, 0, 10) == 10, "full fill 10");
                int more = fis.readNBytes(partial, 0, 10);
                check(more == 10 && partial[0] == 10, "second fill reuses buffer slots");
            }
        } finally {
            delTree(dir);
        }
    }

    /** FileChannel 位置随读前进；File 与 String 构造一致。 */
    static void testChannelAndCtorConsistency() throws Exception {
        Path dir = Files.createTempDirectory("jck-fis27ch");
        File f = dir.resolve("ch.bin").toFile();
        try {
            try (FileOutputStream fos = new FileOutputStream(f)) { fos.write(new byte[]{1, 2, 3, 4, 5}); }
            try (FileInputStream fis = new FileInputStream(f)) {
                check(fis.getChannel().position() == 0, "channel starts at 0");
                fis.read();
                fis.read(new byte[2]);
                check(fis.getChannel().position() == 3, "channel position tracks reads");
            }
            byte[] viaFile, viaString;
            try (FileInputStream a = new FileInputStream(f)) { viaFile = a.readAllBytes(); }
            try (FileInputStream b = new FileInputStream(f.getPath())) { viaString = b.readAllBytes(); }
            check(Arrays.equals(viaFile, viaString) && viaFile.length == 5,
                  "File ctor and String ctor read identical content");
        } finally {
            delTree(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorNegatives();
        testCloseSemantics();
        testReadBounds();
        testSkipSemantics();
        testBulkReadMatrix();
        testChannelAndCtorConsistency();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
