/*
 * @test
 * @summary java.util.zip.ZipOutputStream conformance: white-box structure + black-box write STORED/DEFLATED.
 * @run main ZipOutputStreamTest
 */

import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipOutputStreamTest {
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
        Class<?> c = clazz("java.util.zip.ZipOutputStream");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == DeflaterOutputStream.class, "extends DeflaterOutputStream");
        check(ZipOutputStream.STORED == 0, "STORED == 0");
        check(ZipOutputStream.DEFLATED == 8, "DEFLATED == 8");

        c.getConstructor(Class.forName("java.io.OutputStream"));
        c.getConstructor(Class.forName("java.io.OutputStream"), java.nio.charset.Charset.class);
        c.getMethod("setComment", String.class);
        c.getMethod("setMethod", int.class);
        c.getMethod("setLevel", int.class);
        c.getMethod("putNextEntry", ZipEntry.class);
        c.getMethod("closeEntry");
        c.getMethod("write", byte[].class, int.class, int.class);
        c.getMethod("finish");
        c.getMethod("close");
        check(true, "constructors and entry methods present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path zip = dir.resolve("out.zip");
            ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(zip));
            zout.setMethod(ZipEntry.DEFLATED);
            zout.setLevel(Deflater.DEFAULT_COMPRESSION);
            zout.setComment("zout-comment");

            byte[] sData = "stored!".getBytes("UTF-8");
            CRC32 crc = new CRC32();
            crc.update(sData);
            ZipEntry stored = new ZipEntry("stored.txt");
            stored.setMethod(ZipEntry.STORED);
            stored.setSize(sData.length);
            stored.setCrc(crc.getValue());
            stored.setTime(1000L);
            zout.putNextEntry(stored);
            zout.write(sData);
            zout.closeEntry();

            ZipEntry deflated = new ZipEntry("deflated.txt");
            deflated.setMethod(ZipEntry.DEFLATED);
            byte[] dData = "deflated content deflated content".getBytes("UTF-8");
            zout.putNextEntry(deflated);
            zout.write(dData);
            zout.closeEntry();
            zout.finish();
            zout.close();

            check(Files.size(zip) > 0, "zip output produced");

            // read back and assert entry order + contents
            ZipInputStream zin = new ZipInputStream(Files.newInputStream(zip));
            List<String> names = new ArrayList<>();
            Map<String, byte[]> contents = new HashMap<>();
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                names.add(e.getName());
                contents.put(e.getName(), zin.readAllBytes());
                zin.closeEntry();
            }
            zin.close();
            check(names.equals(Arrays.asList("stored.txt", "deflated.txt")), "entry order preserved");
            check(Arrays.equals(sData, contents.get("stored.txt")), "STORED entry content round-trip");
            check(Arrays.equals(dData, contents.get("deflated.txt")), "DEFLATED entry content round-trip");
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
