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
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
