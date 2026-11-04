/*
 * @test
 * @summary java.nio.file.Files conformance: white-box structure (reflection) + black-box behavior
 *          (create/read/write/copy/move/delete lifecycle with bytes, lines, streams; JDK-8 has no
 *          writeString/readString/mismatch).
 * @run main FilesTest
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilesTest {
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

    static void deleteRecursively(Path p) throws Exception {
        if (p == null || !Files.exists(p)) return;
        try (Stream<Path> s = Files.walk(p)) {
            for (Path x : s.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.deleteIfExists(x);
            }
        }
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.Files");
        check(!c.isInterface() && Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "superclass Object");
        hasMethod(c, "readAllBytes", Path.class);
        hasMethod(c, "readAllLines", Path.class, Charset.class);
        hasMethod(c, "write", Path.class, byte[].class, OpenOption[].class);
        hasMethod(c, "write", Path.class, Iterable.class, Charset.class, OpenOption[].class);
        hasMethod(c, "copy", Path.class, Path.class, CopyOption[].class);
        hasMethod(c, "move", Path.class, Path.class, CopyOption[].class);
        hasMethod(c, "createDirectory", Path.class, FileAttribute[].class);
        hasMethod(c, "createDirectories", Path.class, FileAttribute[].class);
        hasMethod(c, "createTempDirectory", String.class, FileAttribute[].class);
        hasMethod(c, "createTempFile", String.class, String.class, FileAttribute[].class);
        hasMethod(c, "newBufferedReader", Path.class, Charset.class);
        hasMethod(c, "newBufferedWriter", Path.class, Charset.class, OpenOption[].class);
        hasMethod(c, "list", Path.class);
        hasMethod(c, "walk", Path.class, int.class, java.nio.file.FileVisitOption[].class);
        hasMethod(c, "lines", Path.class, Charset.class);
        hasMethod(c, "isSameFile", Path.class, Path.class);
        hasMethod(c, "isSymbolicLink", Path.class);
        hasMethod(c, "getFileStore", Path.class);
        hasMethod(c, "probeContentType", Path.class);
        hasMethod(c, "exists", Path.class, java.nio.file.LinkOption[].class);
        hasMethod(c, "notExists", Path.class, java.nio.file.LinkOption[].class);
        hasMethod(c, "size", Path.class);
        hasMethod(c, "deleteIfExists", Path.class);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-files");
        try {
            Path sub = dir.resolve("sub");
            check(Files.notExists(sub), "notExists before create");
            Files.createDirectory(sub);
            check(Files.exists(sub) && Files.isDirectory(sub), "exists/isDirectory");
            check(!Files.isRegularFile(sub), "!isRegularFile for dir");

            Path nested = dir.resolve("sub2").resolve("leaf");
            Files.createDirectories(nested);
            check(Files.isDirectory(nested), "createDirectories");

            Path tmp = Files.createTempFile(dir, "jck", ".tmp");
            check(Files.exists(tmp) && Files.isRegularFile(tmp), "createTempFile");

            byte[] bytes = {1, 2, 3, 4, 5};
            Files.write(tmp, bytes);
            check(Arrays.equals(Files.readAllBytes(tmp), bytes), "write/readAllBytes");
            check(Files.size(tmp) == 5, "size");

            String content = "héllo 世界\nsecond line\n";
            List<String> lines = Arrays.asList("héllo 世界", "second line");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            List<String> got = Files.readAllLines(tmp, StandardCharsets.UTF_8);
            check(got.size() == 2 && got.get(0).equals("héllo 世界")
                    && got.get(1).equals("second line"), "write lines/readAllLines");
            check(Files.size(tmp) == content.getBytes(StandardCharsets.UTF_8).length, "size equals UTF-8 bytes");

            Path copy = dir.resolve("copy.tmp");
            Files.copy(tmp, copy);
            check(Files.exists(copy) && Files.size(copy) == Files.size(tmp), "copy");
            Files.write(copy, new byte[]{9, 9});
            Files.copy(tmp, copy, StandardCopyOption.REPLACE_EXISTING);
            check(Files.size(copy) == Files.size(tmp), "copy REPLACE_EXISTING");

            Path moved = dir.resolve("moved.tmp");
            Files.move(copy, moved);
            check(Files.exists(moved) && !Files.exists(copy), "move");
            Files.write(tmp, new byte[]{7});
            Files.move(moved, tmp, StandardCopyOption.REPLACE_EXISTING);
            check(Files.exists(tmp) && !Files.exists(moved), "move REPLACE_EXISTING");

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

            check(Files.isSameFile(tmp, tmp), "isSameFile self");
            check(!Files.isSameFile(tmp, txt), "isSameFile distinct");
            check(!Files.isSymbolicLink(tmp), "!isSymbolicLink regular file");
            check(Files.getFileStore(tmp) != null, "getFileStore non-null");
            check(Files.getLastModifiedTime(tmp) != null, "getLastModifiedTime non-null");
            check(Files.isReadable(tmp), "isReadable");
            check(Files.isWritable(tmp), "isWritable");
            String ct = Files.probeContentType(txt);
            check(ct == null || ct.startsWith("text/"), "probeContentType null or text/*");

            check(FileSystems.getDefault().getSeparator() != null, "separator non-null");

            check(Files.deleteIfExists(nested), "deleteIfExists nested true");
            check(!Files.deleteIfExists(nested), "deleteIfExists nested false when gone");

            check(Files.deleteIfExists(tmp) && Files.deleteIfExists(txt), "deleteIfExists true");
            check(!Files.deleteIfExists(tmp), "deleteIfExists false when gone");

            Files.delete(sub);
            check(!Files.exists(sub), "delete dir");
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
