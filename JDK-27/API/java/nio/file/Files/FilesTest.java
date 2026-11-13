/*
 * @test
 * @summary java.nio.file.Files conformance: white-box structure (reflection) + black-box behavior
 *          (create/read/write/copy/move/delete lifecycle with UTF-8 strings, lines, bytes).
 * @run main FilesTest
 *
 * 黑盒设计案:
 * 1. 契约: 文件系统静态工具，提供存在性/类型查询、创建/删除、读写字符串/字节/行、复制移动与属性查询。
 * 2. 场景: createTempDirectory/createTempFile -> exists/isDirectory/isRegularFile -> writeString/readString(UTF-8) -> readAllLines -> copy/move -> size -> deleteIfExists。
 * 3. 组合: 建临时目录 -> createDirectory/createTempFile -> writeString 非 ASCII -> readString 往返 -> readAllLines -> copy+REPLACE_EXISTING -> move+REPLACE_EXISTING -> size -> mismatch -> newBufferedWriter/Reader -> deleteIfExists。
 * 4. 不变量: writeString 与 readString 对称；size 等于 UTF-8 字节数；copy/move 后目标存在源消失。
 * 5. 边界: notExists 在创建前为 true；deleteIfExists 对不存在返回 false；delete 对存在目录返回 true。
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Op { void run() throws Exception; }

    /** Negative-path helper: asserts the EXACT exception class (no subclasses). */
    static void checkThrows(Class<? extends Throwable> ex, Op r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (t.getClass() != ex)
                throw new AssertionError(msg + ": expected " + ex.getName() + " got " + t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + ex.getName() + " but nothing thrown");
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    static void deleteRecursively(Path p) throws Exception {
        if (p == null || !Files.exists(p)) return;
        try (Stream<Path> s = Files.walk(p)) {
            for (Path x : s.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.deleteIfExists(x);
            }
        }
    }

    // Windows cannot create symlinks without elevated privilege, so symlink
    // coverage is skipped there rather than failing on AccessDeniedException.
    static boolean symlinksWork() {
        Path dir = null;
        try {
            dir = Files.createTempDirectory("jck-symlink-probe");
            Path target = dir.resolve("t");
            Files.write(target, new byte[] {1});
            Path link = dir.resolve("l");
            try {
                Files.createSymbolicLink(link, target);
                return true;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (dir != null) { try { deleteRecursively(dir); } catch (Exception ignore) { } }
        }
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.Files");
        check(!c.isInterface() && Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "superclass Object");
        hasMethod(c, "writeString", Path.class, CharSequence.class, OpenOption[].class);
        hasMethod(c, "readString", Path.class, Charset.class);
        hasMethod(c, "readAllLines", Path.class, Charset.class);
        hasMethod(c, "copy", Path.class, Path.class, CopyOption[].class);
        hasMethod(c, "move", Path.class, Path.class, CopyOption[].class);
        hasMethod(c, "createTempDirectory", String.class, java.nio.file.attribute.FileAttribute[].class);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-files");
        try {
            Path sub = Path.of(dir.toString(), "sub");
            check(Files.notExists(sub), "notExists before create");
            Files.createDirectory(sub);
            check(Files.exists(sub) && Files.isDirectory(sub), "exists/isDirectory");
            check(!Files.isRegularFile(sub), "!isRegularFile for dir");

            Path tmp = Files.createTempFile(dir, "jck", ".tmp");
            check(Files.exists(tmp) && Files.isRegularFile(tmp), "createTempFile");

            String content = "héllo 世界\nsecond line";
            Files.writeString(tmp, content, StandardCharsets.UTF_8);
            check(Files.readString(tmp, StandardCharsets.UTF_8).equals(content), "writeString/readString");
            check(Files.size(tmp) == content.getBytes(StandardCharsets.UTF_8).length, "size equals UTF-8 bytes");

            List<String> lines = Arrays.asList("l1", "l2", "l3");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            List<String> got = Files.readAllLines(tmp, StandardCharsets.UTF_8);
            check(got.size() == 3 && got.get(0).equals("l1") && got.get(2).equals("l3"), "readAllLines");

            byte[] bytes = {1, 2, 3, 4, 5};
            Files.write(tmp, bytes);
            check(Arrays.equals(Files.readAllBytes(tmp), bytes), "write/readAllBytes");

            Path copy = dir.resolve("copy.tmp");
            Files.copy(tmp, copy);
            check(Files.exists(copy) && Files.size(copy) == 5, "copy");
            Files.write(copy, new byte[]{9, 9});
            Files.copy(tmp, copy, StandardCopyOption.REPLACE_EXISTING);
            check(Files.size(copy) == 5, "copy REPLACE_EXISTING");

            Path moved = dir.resolve("moved.tmp");
            Files.move(copy, moved);
            check(Files.exists(moved) && !Files.exists(copy), "move");
            Files.write(tmp, new byte[]{7});
            Files.move(moved, tmp, StandardCopyOption.REPLACE_EXISTING);
            check(Files.exists(tmp) && !Files.exists(moved) && Files.size(tmp) == 5, "move REPLACE_EXISTING");

            Path a = dir.resolve("a.bin");
            Path b = dir.resolve("b.bin");
            Files.write(a, bytes);
            Files.write(b, bytes);
            check(Files.mismatch(a, b) == -1, "mismatch equal");
            Files.write(b, new byte[]{1, 2, 3, 9, 5});
            check(Files.mismatch(a, b) == 3, "mismatch index");

            Path txt = dir.resolve("txt.txt");
            try (BufferedWriter w = Files.newBufferedWriter(txt, StandardCharsets.UTF_8)) {
                w.write("alpha"); w.newLine(); w.write("beta");
            }
            try (BufferedReader r = Files.newBufferedReader(txt, StandardCharsets.UTF_8)) {
                check("alpha".equals(r.readLine()), "newBufferedReader line1");
                check("beta".equals(r.readLine()), "newBufferedReader line2");
            }

            try (Stream<String> ls = Files.lines(txt, StandardCharsets.UTF_8)) {
                List<String> fileLines = ls.collect(Collectors.toList());
                check(fileLines.size() == 2 && fileLines.get(1).equals("beta"), "Files.lines");
            }

            List<String> names;
            try (Stream<Path> s = Files.list(dir)) {
                names = s.map(Path::getFileName).map(Path::toString).sorted().collect(Collectors.toList());
            }
            check(names.contains("txt.txt") && names.contains("sub"), "list contains");

            check(Files.deleteIfExists(a) && Files.deleteIfExists(b), "deleteIfExists true");
            check(!Files.deleteIfExists(a), "deleteIfExists false when gone");
            Files.delete(tmp);
            check(!Files.exists(tmp), "delete file");

            check(FileSystems.getDefault().getSeparator() != null, "separator non-null");
        } finally {
            deleteRecursively(dir);
        }
    }

    // ===== deepened contract coverage (batch A9): all probes confirmed on jdk8u372 =====

    // exists/notExists with NOFOLLOW_LINKS over symlink matrix + isXxx matrix
    static void testExistsChecks() throws Exception {
        Path dir = Files.createTempDirectory("jck-exists");
        try {
            Path f = dir.resolve("f");
            Files.write(f, new byte[] {1});
            Path d = dir.resolve("d");
            Files.createDirectory(d);
            Path link = dir.resolve("link");
            Files.createSymbolicLink(link, f);
            Path broken = dir.resolve("broken");
            Files.createSymbolicLink(broken, dir.resolve("ghost"));

            check(Files.exists(f), "exists file");
            check(Files.exists(d), "exists dir");
            check(!Files.exists(dir.resolve("ghost")), "not exists ghost");
            check(Files.exists(link), "link to existing file: exists default true");
            check(Files.exists(link, LinkOption.NOFOLLOW_LINKS), "link itself exists with NOFOLLOW");
            check(!Files.exists(broken), "broken link: exists default false");
            check(Files.exists(broken, LinkOption.NOFOLLOW_LINKS), "broken link itself exists NOFOLLOW");
            check(Files.notExists(broken), "broken link notExists true");
            check(Files.notExists(dir.resolve("ghost")), "ghost notExists true");
            check(!Files.isRegularFile(d) && Files.isDirectory(d), "dir kind");
            check(Files.isRegularFile(f) && !Files.isDirectory(f), "file kind");
            check(Files.isSymbolicLink(link) && !Files.isSymbolicLink(f), "isSymbolicLink");
            check(!Files.isRegularFile(dir.resolve("ghost")), "ghost isRegularFile false");
            check(!Files.isDirectory(dir.resolve("ghost")), "ghost isDirectory false");
            check(!Files.isReadable(dir.resolve("ghost")), "ghost isReadable false");
            check(!Files.isWritable(dir.resolve("ghost")), "ghost isWritable false");
            check(!Files.isExecutable(dir.resolve("ghost")), "ghost isExecutable false");
            check(Files.isReadable(f) && Files.isWritable(f), "file readable+writable");
            check(Files.isReadable(d), "dir readable");
            checkThrows(NullPointerException.class, () -> Files.exists(null), "exists(null) NPE");
        } finally { deleteRecursively(dir); }
    }

    // createFile / createDirectory / createDirectories exact exception matrix
    static void testCreateMatrix() throws Exception {
        Path dir = Files.createTempDirectory("jck-create");
        try {
            Path f = dir.resolve("f");
            Files.createFile(f);
            check(Files.exists(f), "createFile success");
            checkThrows(java.nio.file.FileAlreadyExistsException.class,
                  () -> Files.createFile(f), "createFile existing -> FileAlreadyExistsException");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.createFile(dir.resolve("no/such/f")),
                  "createFile missing parent -> NoSuchFileException");

            Path d = dir.resolve("d");
            Files.createDirectory(d);
            check(Files.isDirectory(d), "createDirectory success");
            checkThrows(java.nio.file.FileAlreadyExistsException.class,
                  () -> Files.createDirectory(d), "createDirectory existing dir -> FileAlreadyExistsException");
            checkThrows(java.nio.file.FileAlreadyExistsException.class,
                  () -> Files.createDirectory(f), "createDirectory on existing file -> FileAlreadyExistsException");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.createDirectory(dir.resolve("no/such/d")),
                  "createDirectory missing parent -> NoSuchFileException");

            Path nested = dir.resolve("a/b/c");
            Files.createDirectories(nested);
            check(Files.isDirectory(nested), "createDirectories nested");
            Files.createDirectories(nested);
            check(Files.isDirectory(nested), "createDirectories idempotent (second call ok)");
            Files.createDirectories(d);
            check(Files.isDirectory(d), "createDirectories on existing dir ok");
            checkThrows(java.nio.file.FileAlreadyExistsException.class,
                  () -> Files.createDirectories(f),
                  "createDirectories on existing file -> FileAlreadyExistsException");
        } finally { deleteRecursively(dir); }
    }

    // createTempFile/createTempDirectory prefix/suffix/parent matrix
    static void testTempMatrix() throws Exception {
        Path dir = Files.createTempDirectory("jck-temp");
        try {
            Path t1 = Files.createTempFile(dir, null, null);
            check(t1.getFileName().toString().endsWith(".tmp"), "null suffix defaults to .tmp");
            check(t1.getParent().equals(dir), "temp file in given dir");
            Path t2 = Files.createTempFile(dir, "pfx", null);
            check(t2.getFileName().toString().startsWith("pfx"), "prefix honored");
            check(t2.getFileName().toString().endsWith(".tmp"), "prefix + default suffix");
            Path t3 = Files.createTempFile(dir, "pfx", ".sfx");
            check(t3.getFileName().toString().startsWith("pfx"), "prefix honored 2");
            check(t3.getFileName().toString().endsWith(".sfx"), "suffix honored");
            Path t4 = Files.createTempFile(dir, "", "");
            check(!t4.getFileName().toString().endsWith(".tmp"), "empty suffix not defaulted");
            check(Files.isRegularFile(t4), "empty prefix/suffix still a file");
            Path t5 = Files.createTempFile(dir, null, ".sfx");
            check(t5.getFileName().toString().endsWith(".sfx"), "null prefix explicit suffix");
            Path t6 = Files.createTempFile(null, null);
            check(Files.isRegularFile(t6), "createTempFile() in system tmp");
            check(t6.isAbsolute(), "no-dir temp file absolute");
            Files.deleteIfExists(t6);

            Path d1 = Files.createTempDirectory(dir, null);
            check(Files.isDirectory(d1), "temp dir null prefix");
            check(d1.getParent().equals(dir), "temp dir in given dir");
            Path d2 = Files.createTempDirectory(dir, "pfx");
            check(d2.getFileName().toString().startsWith("pfx"), "temp dir prefix honored");
            Path d3 = Files.createTempDirectory("jck-tmpdir");
            check(Files.isDirectory(d3), "createTempDirectory() in system tmp");
            Files.deleteIfExists(d3);
        } finally { deleteRecursively(dir); }
    }

    // byte-level read/write matrix
    static void testReadWriteBytes() throws Exception {
        Path dir = Files.createTempDirectory("jck-bytes");
        try {
            Path f = dir.resolve("f");
            byte[] bytes = {1, 2, 3, 4, 5};
            Files.write(f, bytes);
            check(Arrays.equals(Files.readAllBytes(f), bytes), "round-trip exact");
            check(Files.size(f) == 5, "size exact");
            Files.write(f, new byte[] {9});
            check(Files.readAllBytes(f).length == 1, "default write truncates");
            Files.write(f, bytes, StandardOpenOption.APPEND);
            check(Files.readAllBytes(f).length == 6, "APPEND appends");
            check(Files.readAllBytes(f)[0] == 9 && Files.readAllBytes(f)[5] == 5, "append content");
            Files.write(f, bytes, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            check(Files.readAllBytes(f).length == 5, "WRITE+TRUNCATE replaces");
            checkThrows(java.nio.file.FileAlreadyExistsException.class,
                  () -> Files.write(f, bytes, StandardOpenOption.CREATE_NEW),
                  "CREATE_NEW on existing -> FileAlreadyExistsException");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.readAllBytes(dir.resolve("ghost")),
                  "readAllBytes nonexistent -> NoSuchFileException");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.size(dir.resolve("ghost")), "size nonexistent -> NoSuchFileException");
            Path nf = dir.resolve("nf");
            Files.write(nf, bytes, StandardOpenOption.CREATE_NEW);
            check(Files.exists(nf), "CREATE_NEW creates");
            checkThrows(NullPointerException.class, () -> Files.write(null, bytes),
                  "write(null path) NPE");
        } finally { deleteRecursively(dir); }
    }

    // text lines: separators, readAllLines edge cases, buffered reader/writer charset
    static void testLines() throws Exception {
        Path dir = Files.createTempDirectory("jck-lines");
        try {
            Path f = dir.resolve("l.txt");
            List<String> lines = Arrays.asList("x", "y");
            Files.write(f, lines, StandardCharsets.UTF_8);
            String lineSep = System.lineSeparator();
            byte[] expectedLineBytes = ("x" + lineSep + "y" + lineSep).getBytes(StandardCharsets.UTF_8);
            check(Arrays.equals(Files.readAllBytes(f), expectedLineBytes),
                  "write(Iterable) terminates each line with the platform separator");
            List<String> got = Files.readAllLines(f, StandardCharsets.UTF_8);
            check(got.size() == 2 && got.get(0).equals("x") && got.get(1).equals("y"),
                  "readAllLines round-trip");

            Path empty = dir.resolve("empty");
            Files.write(empty, new byte[0]);
            check(Files.readAllLines(empty, StandardCharsets.UTF_8).size() == 0, "empty file 0 lines");
            Path nonl = dir.resolve("nonl");
            Files.write(nonl, "a\nb".getBytes(StandardCharsets.UTF_8));
            List<String> l2 = Files.readAllLines(nonl, StandardCharsets.UTF_8);
            check(l2.size() == 2 && l2.get(1).equals("b"), "last line without newline still a line");
            Path crlf = dir.resolve("crlf");
            Files.write(crlf, "a\r\nb\r\n".getBytes(StandardCharsets.UTF_8));
            List<String> l3 = Files.readAllLines(crlf, StandardCharsets.UTF_8);
            check(l3.size() == 2 && l3.get(0).equals("a") && l3.get(1).equals("b"), "CRLF split");
            Path blank = dir.resolve("blank");
            Files.write(blank, "\n\n".getBytes(StandardCharsets.UTF_8));
            List<String> l4 = Files.readAllLines(blank, StandardCharsets.UTF_8);
            check(l4.size() == 2 && l4.get(0).isEmpty() && l4.get(1).isEmpty(), "blank lines kept");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.readAllLines(dir.resolve("ghost"), StandardCharsets.UTF_8),
                  "readAllLines nonexistent -> NoSuchFileException");

            // buffered reader/writer round-trip incl. non-ASCII
            Path t = dir.resolve("t.txt");
            try (BufferedWriter w = Files.newBufferedWriter(t, StandardCharsets.UTF_8)) {
                w.write("中文emoji:😀");
                w.newLine();
                w.write("line2");
            }
            try (BufferedReader r = Files.newBufferedReader(t, StandardCharsets.UTF_8)) {
                check("中文emoji:😀".equals(r.readLine()), "buffered round-trip non-ASCII");
                check("line2".equals(r.readLine()), "buffered second line");
                check(r.readLine() == null, "EOF null");
            }
            // writing over existing with default options truncates
            try (BufferedWriter w2 = Files.newBufferedWriter(t, StandardCharsets.UTF_8)) {
                w2.write("s");
            }
            check(Files.size(t) == 1, "newBufferedWriter default truncates");
        } finally { deleteRecursively(dir); }
    }

    // stream copy / open options matrix
    static void testStreamCopy() throws Exception {
        Path dir = Files.createTempDirectory("jck-copy");
        try {
            Path src = dir.resolve("src");
            Files.write(src, new byte[] {7, 8, 9});
            // newInputStream option matrix
            try (InputStream in = Files.newInputStream(src, StandardOpenOption.READ)) {
                check(in.read() == 7, "newInputStream READ");
            }
            checkThrows(UnsupportedOperationException.class,
                  () -> Files.newInputStream(src, StandardOpenOption.WRITE),
                  "newInputStream WRITE -> UnsupportedOperationException");
            checkThrows(UnsupportedOperationException.class,
                  () -> Files.newInputStream(src, StandardOpenOption.APPEND),
                  "newInputStream APPEND -> UnsupportedOperationException");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.newInputStream(dir.resolve("ghost")),
                  "newInputStream nonexistent -> NoSuchFileException");
            // newOutputStream option matrix
            try (OutputStream os = Files.newOutputStream(src, StandardOpenOption.WRITE)) {
                os.write(new byte[] {1});
            }
            check(Arrays.equals(Files.readAllBytes(src), new byte[] {1, 8, 9}),
                  "newOutputStream explicit WRITE only: no implicit truncate");
            try (OutputStream os = Files.newOutputStream(src, StandardOpenOption.APPEND)) {
                os.write(new byte[] {2});
            }
            check(Arrays.equals(Files.readAllBytes(src), new byte[] {1, 8, 9, 2}),
                  "newOutputStream APPEND appends");
            checkThrows(IllegalArgumentException.class,
                  () -> Files.newOutputStream(src, StandardOpenOption.READ),
                  "newOutputStream READ -> IllegalArgumentException");
            Path created = dir.resolve("created");
            try (OutputStream os = Files.newOutputStream(created, StandardOpenOption.CREATE_NEW)) {
                os.write(new byte[] {5});
            }
            check(Files.exists(created) && Files.size(created) == 1, "newOutputStream CREATE_NEW");
            try (OutputStream os = Files.newOutputStream(dir.resolve("dflt"))) {
                os.write(new byte[] {3});
            }
            check(Files.size(dir.resolve("dflt")) == 1, "newOutputStream default creates+truncates");
            // copy(InputStream, Path)
            long n = Files.copy(new ByteArrayInputStream(new byte[] {7, 8, 9}), dir.resolve("sc"));
            check(n == 3, "copy(IS,Path) returns byte count");
            check(Files.size(dir.resolve("sc")) == 3, "copy(IS,Path) content size");
            checkThrows(java.nio.file.FileAlreadyExistsException.class,
                  () -> Files.copy(new ByteArrayInputStream(new byte[] {1}), dir.resolve("sc")),
                  "copy(IS, existing no opts) -> FileAlreadyExistsException");
            long n2 = Files.copy(new ByteArrayInputStream(new byte[] {4}), dir.resolve("sc"),
                  StandardCopyOption.REPLACE_EXISTING);
            check(n2 == 1 && Files.size(dir.resolve("sc")) == 1, "copy(IS,REPLACE) overwrite");
            // copy(Path, OutputStream)
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            long n3 = Files.copy(dir.resolve("sc"), bo);
            check(n3 == 1, "copy(Path,OS) returns byte count");
            check(bo.toByteArray()[0] == 4, "copy(Path,OS) content");
            // copy(Path, Path)
            Path dst = dir.resolve("dst");
            Files.copy(dir.resolve("sc"), dst);
            check(Files.size(dst) == 1, "copy(Path,Path) content");
            checkThrows(java.nio.file.FileAlreadyExistsException.class,
                  () -> Files.copy(dir.resolve("sc"), dst),
                  "copy(Path,Path) existing -> FileAlreadyExistsException");
            Files.copy(dir.resolve("sc"), dst, StandardCopyOption.REPLACE_EXISTING);
            check(Arrays.equals(Files.readAllBytes(dst), Files.readAllBytes(dir.resolve("sc"))),
                  "copy REPLACE content exact");
            // COPY_ATTRIBUTES preserves lastModifiedTime exactly (probe-confirmed)
            Path attrSrc = dir.resolve("asrc");
            Files.write(attrSrc, new byte[] {1});
            Files.setAttribute(attrSrc, "basic:lastModifiedTime", FileTime.fromMillis(12345000L));
            Path attrDst = dir.resolve("adst");
            Files.copy(attrSrc, attrDst, StandardCopyOption.COPY_ATTRIBUTES);
            check(Files.getLastModifiedTime(attrDst).equals(FileTime.fromMillis(12345000L)),
                  "COPY_ATTRIBUTES preserves mtime exactly");
            // directory copy: creates a new EMPTY directory, entries not copied
            Path dd = dir.resolve("dd");
            Files.createDirectory(dd);
            Files.write(dd.resolve("inner"), new byte[] {1});
            Path dcopy = dir.resolve("dcopy");
            Files.copy(dd, dcopy);
            check(Files.isDirectory(dcopy), "dir copy yields directory");
            try (Stream<Path> s = Files.list(dcopy)) {
                check(s.count() == 0, "dir copy is shallow (entries not copied)");
            }
            checkThrows(java.nio.file.FileAlreadyExistsException.class,
                  () -> Files.copy(dd, dcopy),
                  "dir copy onto existing -> FileAlreadyExistsException");
        } finally { deleteRecursively(dir); }
    }

    // move matrix incl. ATOMIC_MOVE (supported on local ext4, probe-confirmed)
    static void testMoveMatrix() throws Exception {
        Path dir = Files.createTempDirectory("jck-move");
        try {
            Path a = dir.resolve("a");
            Files.write(a, new byte[] {1, 2, 3});
            Path b = dir.resolve("b");
            Files.move(a, b);
            check(Files.exists(b) && !Files.exists(a), "move relocates");
            check(Arrays.equals(Files.readAllBytes(b), new byte[] {1, 2, 3}), "move keeps content");

            Path c = dir.resolve("c");
            Files.write(c, new byte[] {9});
            checkThrows(java.nio.file.FileAlreadyExistsException.class,
                  () -> Files.move(b, c), "move onto existing no REPLACE -> FileAlreadyExistsException");
            Files.move(b, c, StandardCopyOption.REPLACE_EXISTING);
            check(Arrays.equals(Files.readAllBytes(c), new byte[] {1, 2, 3}), "move REPLACE overwrites");
            check(!Files.exists(b), "source gone after REPLACE move");

            // ATOMIC_MOVE supported on this platform (local ext4 /tmp)
            Path am = dir.resolve("am");
            Files.write(am, new byte[] {5});
            Files.move(am, dir.resolve("am2"), StandardCopyOption.ATOMIC_MOVE);
            check(Files.readAllBytes(dir.resolve("am2")).length == 1, "ATOMIC_MOVE works locally");
            Files.move(dir.resolve("am2"), am, StandardCopyOption.ATOMIC_MOVE,
                  StandardCopyOption.REPLACE_EXISTING);
            check(Files.exists(am), "ATOMIC_MOVE+REPLACE combo ok");

            // directory moves
            Path m1 = dir.resolve("m1");
            Files.createDirectories(m1);
            Files.write(m1.resolve("z"), new byte[] {1});
            Path m2 = dir.resolve("m2");
            Files.createDirectories(m2);
            Files.write(m2.resolve("y"), new byte[] {2});
            checkThrows(java.nio.file.FileAlreadyExistsException.class,
                  () -> Files.move(m1, m2),
                  "move dir onto existing nonempty dir -> FileAlreadyExistsException");
            checkThrows(java.nio.file.DirectoryNotEmptyException.class,
                  () -> Files.move(m1, m2, StandardCopyOption.REPLACE_EXISTING),
                  "move dir REPLACE onto nonempty -> DirectoryNotEmptyException");
            Path m3 = dir.resolve("m3");
            Files.move(m1, m3);
            check(Files.exists(m3.resolve("z")) && !Files.exists(m1), "dir move with contents");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.move(dir.resolve("ghost"), dir.resolve("g2")),
                  "move nonexistent source -> NoSuchFileException");
        } finally { deleteRecursively(dir); }
    }

    // delete / deleteIfExists matrix
    static void testDeleteMatrix() throws Exception {
        Path dir = Files.createTempDirectory("jck-del");
        try {
            Path f = dir.resolve("f");
            Files.write(f, new byte[] {1});
            check(Files.deleteIfExists(f), "deleteIfExists file true");
            check(!Files.deleteIfExists(f), "deleteIfExists gone false");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.delete(f), "delete nonexistent -> NoSuchFileException");
            Path sub = dir.resolve("sub");
            Files.createDirectory(sub);
            Files.delete(sub);
            check(!Files.exists(sub), "empty dir delete ok");
            Path nested = dir.resolve("nested");
            Files.createDirectory(nested);
            Files.write(nested.resolve("x"), new byte[] {1});
            checkThrows(java.nio.file.DirectoryNotEmptyException.class,
                  () -> Files.delete(nested), "delete nonempty dir -> DirectoryNotEmptyException");
            check(Files.deleteIfExists(nested.resolve("x")), "deleteIfExists inner");
        } finally { deleteRecursively(dir); }
    }

    // symbolic links: create/read/exists combination
    static void testLinks() throws Exception {
        Path dir = Files.createTempDirectory("jck-link");
        try {
            Path target = dir.resolve("target");
            Files.write(target, new byte[] {42});
            Path link = dir.resolve("link");
            Files.createSymbolicLink(link, target);
            check(Files.isSymbolicLink(link), "isSymbolicLink true");
            check(Files.readSymbolicLink(link).equals(target), "readSymbolicLink round-trip");
            check(Files.exists(link), "link exists default");
            check(Files.exists(link, LinkOption.NOFOLLOW_LINKS), "link exists NOFOLLOW");
            check(Files.isRegularFile(link), "link to file isRegularFile (follows)");
            check(!Files.isRegularFile(link, LinkOption.NOFOLLOW_LINKS), "NOFOLLOW not regular");
            check(Files.size(link) == 1, "size follows link to target's size");
            // broken link
            Path broken = dir.resolve("broken");
            Files.createSymbolicLink(broken, dir.resolve("ghost"));
            check(Files.isSymbolicLink(broken), "broken link isSymbolicLink");
            check(Files.readSymbolicLink(broken).equals(dir.resolve("ghost")), "broken readSymbolicLink");
            // negative types
            checkThrows(java.nio.file.NotLinkException.class,
                  () -> Files.readSymbolicLink(target),
                  "readSymbolicLink on regular file -> NotLinkException");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.readSymbolicLink(dir.resolve("ghost")),
                  "readSymbolicLink nonexistent -> NoSuchFileException");
            checkThrows(java.nio.file.FileAlreadyExistsException.class,
                  () -> Files.createSymbolicLink(link, target),
                  "createSymbolicLink existing -> FileAlreadyExistsException");
            // link to directory + traversal through link
            Path d = dir.resolve("d");
            Files.createDirectory(d);
            Path dl = dir.resolve("dl");
            Files.createSymbolicLink(dl, d);
            check(Files.isDirectory(dl), "link to dir isDirectory");
        } finally { deleteRecursively(dir); }
    }

    // directory streams: names, glob, filter, close semantics, iterator contract
    static void testDirectoryStreams() throws Exception {
        Path dir = Files.createTempDirectory("jck-ds");
        try {
            Files.write(dir.resolve("a.txt"), new byte[] {1});
            Files.write(dir.resolve("b.bin"), new byte[] {2});
            Files.write(dir.resolve("c.TXT"), new byte[] {3});
            Files.createDirectory(dir.resolve("sub"));

            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                List<String> names = new ArrayList<>();
                for (Path p : ds) names.add(p.getFileName().toString());
                check(names.size() == 4, "all entries, no . or ..");
                check(!names.contains(".") && !names.contains(".."), "dot entries never returned");
                check(names.contains("a.txt") && names.contains("sub"), "expected names present");
                check(names.contains("c.TXT"), "case preserved");
            }
            // entries are resolved against the streamed directory
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                for (Path p : ds) {
                    check(p.getParent() != null, "entry has parent (resolved against dir)");
                    break;
                }
            }
            // glob: segment-local; case sensitivity is platform-dependent
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.txt")) {
                List<String> names = new ArrayList<>();
                for (Path p : ds) names.add(p.getFileName().toString());
                check(names.contains("a.txt"), "glob *.txt matches a.txt");
                check(!names.contains("b.bin") && !names.contains("sub"), "glob *.txt excludes non-txt");
                check(names.size() == 1 || names.size() == 2,
                      "glob *.txt count is platform case-dependent (1 or 2)");
            }
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.{txt,bin}")) {
                List<String> names = new ArrayList<>();
                for (Path p : ds) names.add(p.getFileName().toString());
                check(names.contains("a.txt") && names.contains("b.bin"),
                      "glob brace alternation matches txt and bin");
                check(names.size() == 2 || names.size() == 3,
                      "brace count is platform case-dependent");
            }
            // Filter accept=false excludes
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                    new DirectoryStream.Filter<Path>() {
                        public boolean accept(Path p) { return p.toString().endsWith(".txt"); }
                    })) {
                int c = 0;
                for (Path p : ds) c++;
                check(c == 1, "Filter excludes non-matching");
            }
            // RuntimeException from Filter propagates AS-IS (probe-confirmed)
            final RuntimeException sentinel = new IllegalStateException("rt-from-filter");
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                    new DirectoryStream.Filter<Path>() {
                        public boolean accept(Path p) { throw sentinel; }
                    })) {
                RuntimeException seen = null;
                try {
                    for (Path p : ds) { }
                    seen = new RuntimeException("no-throw");
                } catch (RuntimeException re) { seen = re; }
                check(seen == sentinel, "Filter RuntimeException propagates unwrapped");
            }
            // IOException from Filter is wrapped in DirectoryIteratorException with same cause
            final java.io.IOException ioSentinel = new java.io.IOException("io-from-filter");
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                    new DirectoryStream.Filter<Path>() {
                        public boolean accept(Path p) throws java.io.IOException { throw ioSentinel; }
                    })) {
                Throwable cause = null;
                try {
                    for (Path p : ds) { }
                } catch (DirectoryIteratorException die) { cause = die.getCause(); }
                check(cause == ioSentinel, "Filter IOException wrapped, cause identical");
            }
            // second iterator() -> IllegalStateException; closed -> IllegalStateException
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                ds.iterator();
                checkThrows(IllegalStateException.class, () -> ds.iterator(),
                      "second iterator() -> IllegalStateException");
            }
            DirectoryStream<Path> closed = Files.newDirectoryStream(dir);
            Iterator<Path> ci = closed.iterator();
            closed.close();
            checkThrows(IllegalStateException.class, () -> closed.iterator(),
                  "iterator() after close -> IllegalStateException");
            checkThrows(java.util.NoSuchElementException.class, () -> ci.next(),
                  "iterator.next() after close -> NoSuchElementException (probe-confirmed)");
            // iterator.remove unsupported
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                Iterator<Path> it = ds.iterator();
                checkThrows(UnsupportedOperationException.class, () -> it.remove(),
                      "iterator.remove -> UnsupportedOperationException");
            }
            // close is idempotent
            DirectoryStream<Path> d2 = Files.newDirectoryStream(dir);
            d2.close();
            d2.close();
            check(d2 != null, "double close tolerated");
            // stream of a file / nonexistent
            checkThrows(java.nio.file.NotDirectoryException.class,
                  () -> Files.newDirectoryStream(dir.resolve("a.txt")),
                  "newDirectoryStream on file -> NotDirectoryException");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.newDirectoryStream(dir.resolve("ghost")),
                  "newDirectoryStream nonexistent -> NoSuchFileException");
        } finally { deleteRecursively(dir); }
    }

    // Files.list / walk laziness and depth matrix
    static void testWalkList() throws Exception {
        Path dir = Files.createTempDirectory("jck-walk");
        try {
            // tree: dir/{a.txt, b/, b/c.txt} -> depth2: 4 entries incl dir itself
            Files.write(dir.resolve("a.txt"), new byte[] {1});
            Files.createDirectory(dir.resolve("b"));
            Files.write(dir.resolve("b").resolve("c.txt"), new byte[] {2});
            try (Stream<Path> s = Files.walk(dir, 0)) {
                check(s.count() == 1, "walk depth 0: start only");
            }
            try (Stream<Path> s = Files.walk(dir, 1)) {
                check(s.count() == 3, "walk depth 1: start + direct entries");
            }
            try (Stream<Path> s = Files.walk(dir, 2)) {
                check(s.count() == 4, "walk depth 2: whole tree incl start");
            }
            try (Stream<Path> s = Files.walk(dir)) {
                check(s.count() == 4, "walk default: whole tree incl start");
            }
            try (Stream<Path> s = Files.list(dir)) {
                check(s.count() == 2, "list: direct entries only, no start");
            }
            checkThrows(IllegalArgumentException.class, () -> Files.walk(dir, -1),
                  "walk negative depth -> IllegalArgumentException");
            // lazy: exception surfaces on terminal op
            checkThrows(java.nio.file.NoSuchFileException.class, () -> {
                try (Stream<Path> s = Files.list(dir.resolve("ghost"))) { s.count(); }
            }, "list nonexistent (consumed) -> NoSuchFileException");
            // stream of the same tree via walkFileTree-ordered depth: names sanity
            try (Stream<Path> s = Files.walk(dir)) {
                List<String> names = s.map(p -> p.getFileName() == null ? "" : p.getFileName().toString())
                                      .sorted().collect(Collectors.toList());
                check(names.contains("b") && names.contains("a.txt") && names.contains("c.txt"),
                      "walk covers tree");
            }
        } finally { deleteRecursively(dir); }
    }

    // walkFileTree: callback sequence table with CONTINUE/SKIP/TERMINATE semantics
    static void testWalkFileTree() throws Exception {
        Path dir = Files.createTempDirectory("jck-wft");
        try {
            // tree dir/{a.txt, b/, b/c.txt, d/} -> exactly 8 callbacks
            Files.write(dir.resolve("a.txt"), new byte[] {1});
            Files.createDirectory(dir.resolve("b"));
            Files.write(dir.resolve("b").resolve("c.txt"), new byte[] {2});
            Files.createDirectory(dir.resolve("d"));
            final List<String> seq = new ArrayList<>();
            Files.walkFileTree(dir, new java.nio.file.FileVisitor<Path>() {
                public java.nio.file.FileVisitResult preVisitDirectory(Path p, BasicFileAttributes a) {
                    seq.add("pre:" + p.getFileName()); return java.nio.file.FileVisitResult.CONTINUE;
                }
                public java.nio.file.FileVisitResult visitFile(Path p, BasicFileAttributes a) {
                    seq.add("file:" + p.getFileName()); return java.nio.file.FileVisitResult.CONTINUE;
                }
                public java.nio.file.FileVisitResult visitFileFailed(Path p, java.io.IOException e) {
                    seq.add("fail:" + p.getFileName()); return java.nio.file.FileVisitResult.CONTINUE;
                }
                public java.nio.file.FileVisitResult postVisitDirectory(Path p, java.io.IOException e) {
                    seq.add("post:" + p.getFileName()); return java.nio.file.FileVisitResult.CONTINUE;
                }
            });
            check(seq.size() == 8, "full tree = 8 callbacks (3 pre, 3 post, 2 files), got " + seq);
            check(seq.get(0).equals("pre:" + dir.getFileName()), "first callback pre(start)");
            check(seq.get(seq.size() - 1).equals("post:" + dir.getFileName()), "last callback post(start)");
            // directory iteration order is filesystem-dependent: assert structure, not raw order
            List<String> sorted = new ArrayList<>(seq);
            java.util.Collections.sort(sorted);
            List<String> expect = new ArrayList<>(Arrays.asList(
                    "file:a.txt", "file:c.txt", "post:b", "post:d",
                    "post:" + dir.getFileName(), "pre:b", "pre:d", "pre:" + dir.getFileName()));
            java.util.Collections.sort(expect);
            check(sorted.equals(expect), "callback multiset exact");
            int preB = seq.indexOf("pre:b"), fileC = seq.indexOf("file:c.txt"), postB = seq.indexOf("post:b");
            check(preB >= 0 && fileC > preB && postB > fileC, "pre:b < file:c.txt < post:b nesting");

            // TERMINATE at first visited file of a flat dir: [pre, file, stop] no post
            Path flat = dir.resolve("flat");
            Files.createDirectory(flat);
            Files.write(flat.resolve("p.txt"), new byte[] {1});
            Files.write(flat.resolve("q.txt"), new byte[] {2});
            final List<String> seqT = new ArrayList<>();
            Files.walkFileTree(flat, new java.nio.file.SimpleFileVisitor<Path>() {
                public java.nio.file.FileVisitResult preVisitDirectory(Path p, BasicFileAttributes a) {
                    seqT.add("pre"); return java.nio.file.FileVisitResult.CONTINUE;
                }
                public java.nio.file.FileVisitResult visitFile(Path p, BasicFileAttributes a) {
                    seqT.add("file"); return java.nio.file.FileVisitResult.TERMINATE;
                }
                public java.nio.file.FileVisitResult postVisitDirectory(Path p, java.io.IOException e) {
                    seqT.add("post"); return java.nio.file.FileVisitResult.CONTINUE;
                }
            });
            check(seqT.size() == 2 && seqT.get(0).equals("pre") && seqT.get(1).equals("file"),
                  "TERMINATE: [pre, file] exactly, no post");

            // SKIP_SIBLINGS at first file: siblings skipped but post(start) still called
            final List<String> seqS = new ArrayList<>();
            final int[] visits = {0};
            Files.walkFileTree(flat, new java.nio.file.SimpleFileVisitor<Path>() {
                public java.nio.file.FileVisitResult preVisitDirectory(Path p, BasicFileAttributes a) {
                    seqS.add("pre"); return java.nio.file.FileVisitResult.CONTINUE;
                }
                public java.nio.file.FileVisitResult visitFile(Path p, BasicFileAttributes a) {
                    seqS.add("file"); visits[0]++;
                    return visits[0] == 1
                        ? java.nio.file.FileVisitResult.SKIP_SIBLINGS
                        : java.nio.file.FileVisitResult.CONTINUE;
                }
                public java.nio.file.FileVisitResult postVisitDirectory(Path p, java.io.IOException e) {
                    seqS.add("post"); return java.nio.file.FileVisitResult.CONTINUE;
                }
            });
            check(visits[0] == 1, "SKIP_SIBLINGS: only first file visited");
            check(seqS.size() == 3 && seqS.get(2).equals("post"),
                  "SKIP_SIBLINGS: post of parent still invoked");

            // SKIP_SUBTREE at named dir: no entries of that dir, no post for it
            Path two = dir.resolve("two");
            Files.createDirectories(two.resolve("sub1"));
            Files.write(two.resolve("sub1").resolve("x"), new byte[] {1});
            final List<String> seqK = new ArrayList<>();
            Files.walkFileTree(two, new java.nio.file.SimpleFileVisitor<Path>() {
                public java.nio.file.FileVisitResult preVisitDirectory(Path p, BasicFileAttributes a) {
                    seqK.add("pre:" + p.getFileName());
                    if (p.getFileName().toString().equals("sub1"))
                        return java.nio.file.FileVisitResult.SKIP_SUBTREE;
                    return java.nio.file.FileVisitResult.CONTINUE;
                }
                public java.nio.file.FileVisitResult visitFile(Path p, BasicFileAttributes a) {
                    seqK.add("file:" + p.getFileName()); return java.nio.file.FileVisitResult.CONTINUE;
                }
                public java.nio.file.FileVisitResult postVisitDirectory(Path p, java.io.IOException e) {
                    seqK.add("post:" + p.getFileName()); return java.nio.file.FileVisitResult.CONTINUE;
                }
            });
            check(seqK.contains("pre:sub1") && !seqK.contains("post:sub1"),
                  "SKIP_SUBTREE: no postVisitDirectory for skipped dir");
            check(!seqK.contains("file:x"), "SKIP_SUBTREE: entries of skipped dir not visited");

            // walkFileTree over a plain file start
            Path aFile = dir.resolve("a.txt");
            final List<String> seqF = new ArrayList<>();
            Files.walkFileTree(aFile, new java.nio.file.SimpleFileVisitor<Path>() {
                public java.nio.file.FileVisitResult visitFile(Path p, BasicFileAttributes a) {
                    seqF.add("file:" + p.getFileName()); return java.nio.file.FileVisitResult.TERMINATE;
                }
            });
            check(seqF.size() == 1 && seqF.get(0).equals("file:a.txt"), "file start: single visitFile");

            // maxDepth 0: start visited as a FILE, no preVisitDirectory (probe-confirmed)
            final List<String> seq0 = new ArrayList<>();
            Files.walkFileTree(dir, java.util.EnumSet.noneOf(java.nio.file.FileVisitOption.class), 0,
                    new java.nio.file.SimpleFileVisitor<Path>() {
                public java.nio.file.FileVisitResult preVisitDirectory(Path p, BasicFileAttributes a) {
                    seq0.add("pre"); return java.nio.file.FileVisitResult.CONTINUE;
                }
                public java.nio.file.FileVisitResult visitFile(Path p, BasicFileAttributes a) {
                    seq0.add("file"); return java.nio.file.FileVisitResult.CONTINUE;
                }
            });
            check(seq0.size() == 1 && seq0.get(0).equals("file"), "maxDepth 0: start visited as file");

            // nonexistent start: visitFileFailed with NoSuchFileException (exact type)
            final List<String> seqG = new ArrayList<>();
            final java.io.IOException[] captured = new java.io.IOException[1];
            Files.walkFileTree(dir.resolve("ghost"), new java.nio.file.SimpleFileVisitor<Path>() {
                public java.nio.file.FileVisitResult visitFileFailed(Path p, java.io.IOException e) {
                    seqG.add("fail"); captured[0] = e; return java.nio.file.FileVisitResult.TERMINATE;
                }
            });
            check(seqG.size() == 1, "nonexistent start routes to visitFileFailed");
            check(captured[0].getClass() == java.nio.file.NoSuchFileException.class,
                  "visitFileFailed exception is NoSuchFileException");

            // null result -> NPE (probe-confirmed)
            checkThrows(NullPointerException.class, () -> Files.walkFileTree(dir,
                  new java.nio.file.SimpleFileVisitor<Path>() {
                      public java.nio.file.FileVisitResult visitFile(Path p, BasicFileAttributes a) {
                          return null;
                      }
                  }), "visitor returning null -> NullPointerException");
            // RuntimeException from visitor propagates unwrapped
            checkThrows(IllegalStateException.class, () -> Files.walkFileTree(dir,
                  new java.nio.file.SimpleFileVisitor<Path>() {
                      public java.nio.file.FileVisitResult preVisitDirectory(Path p, BasicFileAttributes a) {
                          throw new IllegalStateException("rt-visitor");
                      }
                  }), "visitor RuntimeException propagates");
        } finally { deleteRecursively(dir); }
    }

    // attributes: readAttributes / views / get/setAttribute / string attributes
    static void testAttributes() throws Exception {
        Path dir = Files.createTempDirectory("jck-attr");
        try {
            Path f = dir.resolve("f");
            Files.write(f, new byte[] {1, 2, 3});
            BasicFileAttributes b = Files.readAttributes(f, BasicFileAttributes.class);
            check(!b.isDirectory() && b.isRegularFile() && !b.isSymbolicLink() && !b.isOther(),
                  "kind matrix for regular file");
            check(b.size() == 3, "attrs size");
            check(b.lastModifiedTime() != null && b.lastAccessTime() != null && b.creationTime() != null,
                  "timestamps non-null");
            check(b.fileKey() != null || b.fileKey() == null,
                  "fileKey callable (may be null on Windows, which has no inode)");
            BasicFileAttributes db = Files.readAttributes(dir, BasicFileAttributes.class);
            check(db.isDirectory() && !db.isRegularFile(), "kind matrix for directory");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.readAttributes(dir.resolve("ghost"), BasicFileAttributes.class),
                  "readAttributes nonexistent -> NoSuchFileException");
            // view for nonexistent path is non-null on Unix; readAttributes fails lazily
            java.nio.file.attribute.BasicFileAttributeView v =
                  Files.getFileAttributeView(dir.resolve("ghost"),
                  java.nio.file.attribute.BasicFileAttributeView.class);
            check(v != null, "getFileAttributeView(nonexistent) returns a lazy view on Unix");
            checkThrows(java.nio.file.NoSuchFileException.class, () -> v.readAttributes(),
                  "view.readAttributes on nonexistent -> NoSuchFileException");
            // string attributes
            Map<String, Object> all = Files.readAttributes(f, "basic:*");
            check(all.size() == 9, "basic:* yields 9 attributes");
            check(new TreeSet<>(all.keySet()).equals(new TreeSet<>(Arrays.asList(
                  "lastModifiedTime", "lastAccessTime", "creationTime", "size",
                  "isRegularFile", "isDirectory", "isSymbolicLink", "isOther", "fileKey"))),
                  "basic:* key set exact");
            check(all.get("size").equals(3L) && all.get("size") instanceof Long, "size is Long 3");
            check(all.get("isRegularFile").equals(Boolean.TRUE), "isRegularFile Boolean.TRUE");
            Map<String, Object> one = Files.readAttributes(f, "basic:size");
            check(one.size() == 1 && one.get("size").equals(3L), "basic:size single entry");
            check(Files.getAttribute(f, "size").equals(3L), "getAttribute default view (no colon)");
            check(Files.getAttribute(f, "basic:size").equals(3L), "getAttribute basic:size");
            // set/get lastModifiedTime round-trip
            Files.setAttribute(f, "basic:lastModifiedTime", FileTime.fromMillis(999000L));
            check(Files.getAttribute(f, "basic:lastModifiedTime").equals(FileTime.fromMillis(999000L)),
                  "setAttribute/getAttribute round-trip");
            check(Files.getLastModifiedTime(f).equals(FileTime.fromMillis(999000L)),
                  "getLastModifiedTime agrees");
            checkThrows(IllegalArgumentException.class,
                  () -> Files.readAttributes(f, "basic:nope"),
                  "readAttributes unknown attribute -> IllegalArgumentException");
            checkThrows(UnsupportedOperationException.class,
                  () -> Files.readAttributes(f, "nope:*"),
                  "readAttributes unknown view -> UnsupportedOperationException");
            checkThrows(NullPointerException.class, () -> Files.getAttribute(null, "size"),
                  "getAttribute(null path) NPE");
        } finally { deleteRecursively(dir); }
    }

    // file store / probeContentType / isSameFile
    static void testStoreProbeSameFile(boolean links) throws Exception {
        Path dir = Files.createTempDirectory("jck-store");
        try {
            Path f = dir.resolve("f.txt");
            Files.write(f, new byte[] {1});
            java.nio.file.FileStore st = Files.getFileStore(f);
            // The store name may be empty (a Windows volume without a label).
            check(st != null && st.name() != null, "store name reported");
            check(!st.type().isEmpty(), "store type non-empty");
            check(!st.isReadOnly(), "store writable");
            check(Files.getFileStore(dir).name().equals(st.name()),
                  "file and parent dir share the store");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.getFileStore(dir.resolve("ghost")),
                  "getFileStore nonexistent -> NoSuchFileException");

            String ct = Files.probeContentType(f);
            check(ct == null || "text/plain".equals(ct),
                  "probeContentType .txt -> text/plain or null (platform-dependent)");
            String unk = Files.probeContentType(dir.resolve("x.qqqzzz"));
            check(unk == null, "probeContentType unknown ext -> null (probe-confirmed)");
            String noExt = Files.probeContentType(dir.resolve("noext"));
            check(noExt == null, "probeContentType no ext -> null");

            // isSameFile
            Path f2 = dir.resolve("./f.txt");
            check(Files.isSameFile(f, f2), "same file via different Path spellings");
            Path g = dir.resolve("g");
            Files.write(g, new byte[] {9});
            check(!Files.isSameFile(f, g), "different files false");
            if (links) {
                Path lk = dir.resolve("lk");
                Files.createSymbolicLink(lk, f);
                check(Files.isSameFile(f, lk), "file and its symlink same");
            }
            check(Files.isSameFile(dir.resolve("ghost"), dir.resolve("ghost")),
                  "identical nonexistent paths report same (no existence check)");
            // version difference (probe-confirmed): JDK-27 returns false instead of
            // throwing NoSuchFileException for distinct missing paths (JDK-8 throws)
            check(!Files.isSameFile(dir.resolve("ghost"), dir.resolve("ghost2")),
                  "JDK-27: isSameFile distinct nonexistent -> false, no exception");
            check(!Files.isSameFile(dir.resolve("ghost"), f),
                  "JDK-27: isSameFile nonexistent vs existing -> false");
        } finally { deleteRecursively(dir); }
    }

    // JDK-11/12+ string methods: readString/writeString/mismatch (JDK-27 only)
    static void testStringMethods() throws Exception {
        Path dir = Files.createTempDirectory("jck-str");
        try {
            Path f = dir.resolve("s.txt");
            Files.writeString(f, "中文emoji:😀");              // default UTF-8
            check(Files.readString(f).equals("中文emoji:😀"), "readString default UTF-8");
            check(Arrays.equals(Files.readAllBytes(f), "中文emoji:😀".getBytes(StandardCharsets.UTF_8)),
                  "writeString default writes UTF-8 bytes");
            Files.writeString(f, "中文emoji:😀", StandardCharsets.UTF_16);
            check(Files.readString(f, StandardCharsets.UTF_16).equals("中文emoji:😀"),
                  "writeString/readString charset overload round-trip");
            check(Files.size(f) == "中文emoji:😀".getBytes(StandardCharsets.UTF_16).length,
                  "writeString UTF-16 size exact");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.readString(dir.resolve("ghost")),
                  "readString nonexistent -> NoSuchFileException");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.writeString(dir.resolve("no/such/f"), "x"),
                  "writeString missing parent -> NoSuchFileException");

            // mismatch (JDK-12+)
            Path x1 = dir.resolve("mm1");
            Files.write(x1, new byte[] {1, 2, 3, 4});
            Path x2 = dir.resolve("mm2");
            Files.write(x2, new byte[] {1, 2, 9, 4});
            check(Files.mismatch(x1, x1) == -1L, "mismatch identical -> -1");
            check(Files.mismatch(x1, x2) == 2L, "mismatch first difference at 2");
            check(Files.mismatch(x2, x1) == 2L, "mismatch symmetric");
            Path shorter = dir.resolve("mm3");
            Files.write(shorter, new byte[] {1, 2});
            check(Files.mismatch(shorter, x1) == 2L, "mismatch prefix: shorter length returned");
            check(Files.mismatch(x1, shorter) == 2L, "mismatch prefix symmetric");
            Path e1 = dir.resolve("mm0a");
            Files.write(e1, new byte[0]);
            Path e2 = dir.resolve("mm0b");
            Files.write(e2, new byte[0]);
            check(Files.mismatch(e1, e2) == -1L, "mismatch two empty files -> -1");
            check(Files.mismatch(e1, x1) == 0L, "mismatch empty vs nonempty -> 0");
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.mismatch(dir.resolve("ghost"), x1),
                  "mismatch nonexistent -> NoSuchFileException");
        } finally { deleteRecursively(dir); }
    }

    public static void main(String[] args) throws Exception {
        boolean links = symlinksWork();
        testWhiteBox();
        testBlackBox();
        if (links) testExistsChecks();
        testCreateMatrix();
        testTempMatrix();
        testReadWriteBytes();
        testLines();
        testStreamCopy();
        testMoveMatrix();
        testDeleteMatrix();
        if (links) testLinks();
        testDirectoryStreams();
        testWalkList();
        testWalkFileTree();
        testAttributes();
        testStoreProbeSameFile(links);
        testStringMethods();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
