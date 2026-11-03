/*
 * @test
 * @summary java.util.zip.ZipFile conformance: white-box structure + black-box open/entries/content.
 * @run main ZipFileTest
 */

import java.io.Closeable;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipFileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> clazz(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void deleteTree(Path root) throws java.io.IOException {
        if (root == null || !Files.exists(root)) return;
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws java.io.IOException {
                Files.delete(f);
                return FileVisitResult.CONTINUE;
            }
            public FileVisitResult postVisitDirectory(Path d, java.io.IOException e) throws java.io.IOException {
                if (e != null) throw e;
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = clazz("java.util.zip.ZipFile");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(ZipFile.OPEN_READ == 1, "OPEN_READ == 1");
        check(ZipFile.OPEN_DELETE == 4, "OPEN_DELETE == 4");

        c.getConstructor(String.class);
        c.getConstructor(java.io.File.class);
        c.getConstructor(java.io.File.class, int.class);
        c.getConstructor(java.io.File.class, java.nio.charset.Charset.class);
        c.getMethod("getComment");
        c.getMethod("getEntry", String.class);
        c.getMethod("getInputStream", ZipEntry.class);
        c.getMethod("getName");
        c.getMethod("entries");
        c.getMethod("stream");
        c.getMethod("size");
        c.getMethod("close");
        check(true, "constructors and access methods present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path zip = dir.resolve("test.zip");
            ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(zip));
            zout.setComment("zip-comment");
            zout.putNextEntry(new ZipEntry("b.txt"));
            zout.write("bbb".getBytes("UTF-8"));
            zout.closeEntry();
            zout.putNextEntry(new ZipEntry("a.txt"));
            zout.write("aaa".getBytes("UTF-8"));
            zout.closeEntry();
            zout.close();

            ZipFile zf = new ZipFile(zip.toFile());
            check(zf.size() == 2, "size");
            check("zip-comment".equals(zf.getComment()), "getComment");
            check(zf.getName().endsWith("test.zip"), "getName endsWith test.zip");
            check(zf.getEntry("a.txt") != null, "getEntry a.txt");
            check(zf.getEntry("missing") == null, "getEntry missing returns null");

            ZipEntry ea = zf.getEntry("a.txt");
            InputStream in = zf.getInputStream(ea);
            byte[] ca = in.readAllBytes();
            check("aaa".equals(new String(ca, "UTF-8")), "entry a content");
            in.close();

            // entries() enumeration: collect and sort names before asserting
            List<String> names = new ArrayList<>();
            for (Enumeration<? extends ZipEntry> en = zf.entries(); en.hasMoreElements(); ) {
                names.add(en.nextElement().getName());
            }
            Collections.sort(names);
            check(names.equals(Arrays.asList("a.txt", "b.txt")), "entries enumeration names (sorted)");

            // stream(): collect and sort names
            List<String> streamNames = zf.stream().map(ZipEntry::getName).sorted().collect(Collectors.toList());
            check(streamNames.equals(Arrays.asList("a.txt", "b.txt")), "stream names (sorted)");

            zf.close();
        } finally {
            deleteTree(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
