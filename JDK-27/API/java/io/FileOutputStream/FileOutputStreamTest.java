/*
 * @test
 * @summary java.io.FileOutputStream conformance: white-box structure (reflection) + black-box
 *          behavior (write/append modes, FD/channel, truncate-vs-append).
 * @run main FileOutputStreamTest
 *
 * 黑盒设计案:
 * 1. 契约: 向文件写字节的输出流；默认截断，append=true 时追加；暴露 getFD/getChannel。
 * 2. 场景: FileOutputStream(File) 写 -> 读回；FileOutputStream(File,true) 追加。
 * 3. 组合: 写 "ab" -> 追加 "cd" -> FileInputStream 读回 "abcd" -> 长度断言。
 * 4. 不变量: 默认构造覆盖已有内容；append 模式保留并追加。
 * 5. 边界: write(byte[],off,len) 只写指定范围。
 */

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileOutputStreamTest {
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
        Class<?> c = Class.forName("java.io.FileOutputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.OutputStream.class, "extends OutputStream");
        hasMethod(c, "write", int.class);
        hasMethod(c, "getFD");
        hasMethod(c, "getChannel");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-fos");
        File f = dir.resolve("out.bin").toFile();
        try {
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(new byte[]{1, 2, 3});
                check(fos.getFD() != null, "getFD");
                check(fos.getChannel() != null, "getChannel");
            }
            check(f.length() == 3, "wrote 3 bytes");

            try (FileOutputStream fos = new FileOutputStream(f, true)) {
                fos.write(new byte[]{4, 5}, 0, 2);
            }
            check(f.length() == 5, "append adds bytes");
            try (FileInputStream fis = new FileInputStream(f)) {
                check(Arrays.equals(fis.readAllBytes(), new byte[]{1, 2, 3, 4, 5}), "append roundtrip");
            }

            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(9);
            }
            check(f.length() == 1, "default mode truncates");

            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(new byte[]{10, 20, 30, 40}, 1, 2);
            }
            check(f.length() == 2, "write(byte[],off,len) length");
            try (FileInputStream fis = new FileInputStream(f)) {
                check(Arrays.equals(fis.readAllBytes(), new byte[]{20, 30}), "write(byte[],off,len) content");
            }
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

    /** 构造负例矩阵：null NPE（JDK-27 带 helpful 消息）、目录 FNFE、缺失父目录 FNFE。 */
    static void testConstructorNegatives() throws Exception {
        Path dir = Files.createTempDirectory("jck-fos27");
        try {
            try {
                new FileOutputStream((File) null);
                check(false, "null File must throw");
            } catch (NullPointerException npe) {
                check(npe.getMessage() != null && npe.getMessage().contains("\"file\" is null"),
                      "JDK-27 null File NPE message: " + npe.getMessage());
            }
            checkThrows(NullPointerException.class, () -> new FileOutputStream((String) null),
                        "null String -> NPE");
            checkThrows(NullPointerException.class, () -> new FileOutputStream((String) null, true),
                        "null String,append -> NPE");
            checkThrows(NullPointerException.class, () -> new FileOutputStream((File) null, false),
                        "null File,append -> NPE");
            try {
                new FileOutputStream(dir.toFile());
                check(false, "directory must throw");
            } catch (FileNotFoundException e) {
                // "<path> (<OS reason>)" — the reason is a localized strerror
                // ("Is a directory" on Linux, 拒绝访问 on a zh_CN Windows).
                check(e.getMessage().startsWith(dir.toString() + " ("),
                      "directory message starts with path and paren: " + e.getMessage());
                check(e.getMessage().matches(".*\\(.+\\)$"),
                      "directory message ends with an OS reason: " + e.getMessage());
            }
            File orphan = dir.resolve("no/such/x.bin").toFile();
            try {
                new FileOutputStream(orphan);
                check(false, "missing parent must throw");
            } catch (FileNotFoundException e) {
                String m = e.getMessage();
                check(m.contains("No such file or directory") && m.contains(orphan.getPath()),
                      "missing parent message has path and reason: " + m);
            }
            checkThrows(FileNotFoundException.class, () -> new FileOutputStream(orphan.getPath(), false),
                        "missing parent String,bool -> FNFE");
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

    /** close 语义：double close 幂等、write 后 "Stream Closed"、flush 不抛、FD 失效。 */
    static void testCloseSemantics() throws Exception {
        Path dir = Files.createTempDirectory("jck-fos27c");
        File f = dir.resolve("cs.bin").toFile();
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(65);
            FileDescriptor fd = fos.getFD();
            check(fd.valid() && fos.getFD() == fd, "fd valid, same instance while open");
            fos.close();
            fos.close();
            passed();                                    // idempotent double close
            check(!fd.valid(), "fd invalid after close");
            try {
                fos.write(66);
                check(false, "write after close must throw");
            } catch (IOException ioe) {
                check("Stream Closed".equals(ioe.getMessage()),
                      "JDK-27 write-after-close message: " + ioe.getMessage());
            }
            try {
                fos.write(new byte[]{1});
                check(false, "bulk write after close must throw");
            } catch (IOException ioe) {
                check("Stream Closed".equals(ioe.getMessage()), "bulk write message");
            }
            fos.flush();
            passed();                                    // flush() 空实现：close 后不抛
            checkThrows(IOException.class, () -> fos.write(new byte[2], 0, 1),
                        "region write after close -> IOException");
        } finally {
            delTree(dir);
        }
    }

    static void passed() { passed++; }

    /** write(int) 只写低 8 位。 */
    static void testWriteLowByte() throws Exception {
        Path dir = Files.createTempDirectory("jck-fos27l");
        File f = dir.resolve("low.bin").toFile();
        try {
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(0x1FF);                        // -> 0xFF
                fos.write(0x141);                        // -> 0x41
                fos.write(0x100);                        // -> 0x00
            }
            try (FileInputStream in = new FileInputStream(f)) {
                check(in.read() == 0xFF, "0x1FF truncated to 0xFF");
                check(in.read() == 0x41, "0x141 truncated to 0x41");
                check(in.read() == 0x00, "0x100 truncated to 0x00");
            }
        } finally {
            delTree(dir);
        }
    }

    /** write(byte[],off,len) 参数校验矩阵。 */
    static void testWriteBounds() throws Exception {
        Path dir = Files.createTempDirectory("jck-fos27b");
        File f = dir.resolve("wb.bin").toFile();
        try {
            FileOutputStream fos = new FileOutputStream(f);
            checkThrows(IndexOutOfBoundsException.class, () -> fos.write(new byte[2], 2, 1),
                        "write(b,2,1) -> IOOBE");
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
            passed();                                    // off == b.length, len == 0 legal
            fos.write(new byte[0]);
            passed();                                    // empty array no-op
            check(f.length() == 0, "failed checks wrote nothing");
            fos.close();
        } finally {
            delTree(dir);
        }
    }

    /** append 定位与 truncate 截断语义（readAllBytes 回读）。 */
    static void testAppendAndTruncate() throws Exception {
        Path dir = Files.createTempDirectory("jck-fos27a");
        File f = dir.resolve("ap.bin").toFile();
        try {
            try (FileOutputStream a1 = new FileOutputStream(f)) { a1.write(new byte[]{1, 2}); }
            try (FileOutputStream a2 = new FileOutputStream(f, true)) { a2.write(new byte[]{3}); }
            try (FileOutputStream a3 = new FileOutputStream(f.getPath(), true)) { a3.write(new byte[]{4}); }
            check(Arrays.equals(readAllBytes(f), new byte[]{1, 2, 3, 4}),
                  "append keeps ordering across all ctor variants");

            try (FileOutputStream t1 = new FileOutputStream(f)) { t1.write(new byte[]{9, 9, 9, 9, 9}); }
            try (FileOutputStream t2 = new FileOutputStream(f)) { t2.write(new byte[]{7}); }
            check(f.length() == 1 && Arrays.equals(readAllBytes(f), new byte[]{7}),
                  "default ctor truncates");
            try (FileOutputStream t3 = new FileOutputStream(f, false)) { t3.write(new byte[]{5, 5}); }
            check(f.length() == 2, "File,false truncates");
            try (FileOutputStream t4 = new FileOutputStream(f.getPath(), false)) { t4.write(new byte[]{6}); }
            check(f.length() == 1 && Arrays.equals(readAllBytes(f), new byte[]{6}),
                  "String,false truncates");
        } finally {
            delTree(dir);
        }
    }

    static byte[] readAllBytes(File f) throws Exception {
        try (FileInputStream in = new FileInputStream(f)) { return in.readAllBytes(); }
    }

    /** 写后可见性与 FD/channel。 */
    static void testVisibilityAndChannel() throws Exception {
        Path dir = Files.createTempDirectory("jck-fos27v");
        File f = dir.resolve("v.bin").toFile();
        try {
            FileOutputStream fos = new FileOutputStream(f);
            check(fos.getChannel() != null && fos.getChannel().position() == 0,
                  "channel non-null, starts at 0");
            fos.write(new byte[]{1, 2});
            check(f.length() == 2, "length visible without close");
            check(fos.getChannel().position() == 2, "channel position tracks writes");
            java.io.OutputStream os = fos;
            os.write(3);
            os.flush();
            os.close();
            check(Arrays.equals(readAllBytes(f), new byte[]{1, 2, 3}),
                  "writes through base reference land in file");
        } finally {
            delTree(dir);
        }
    }

    /** close 后访问器仍可用（channel 关闭）；FOS 不具备读侧方法（白盒）。 */
    static void testPostCloseAccessAndShape() throws Exception {
        Path dir = Files.createTempDirectory("jck-fos27p");
        File f = dir.resolve("p.bin").toFile();
        try {
            FileOutputStream fos = new FileOutputStream(f);
            FileDescriptor fd = fos.getFD();
            java.nio.channels.FileChannel ch = fos.getChannel();
            fos.write(1);
            fos.close();
            check(!ch.isOpen(), "channel closed with the stream");
            check(fos.getFD() == fd, "getFD still callable after close");
            check(fos.getChannel() != null, "getChannel still returns after close");
            Class<?> c = FileOutputStream.class;
            for (String no : new String[]{"read", "skip", "available", "readAllBytes"}) {
                boolean found = false;
                for (java.lang.reflect.Method m : c.getMethods())
                    if (m.getName().equals(no)) found = true;
                check(!found, "output stream has no " + no + " method");
            }
        } finally {
            delTree(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorNegatives();
        testCloseSemantics();
        testWriteLowByte();
        testWriteBounds();
        testAppendAndTruncate();
        testVisibilityAndChannel();
        testPostCloseAccessAndShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
