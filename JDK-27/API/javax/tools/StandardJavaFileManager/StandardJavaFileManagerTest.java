/*
 * @test
 * @summary javax.tools.StandardJavaFileManager conformance: white-box structure + black-box behavior.
 * @run main StandardJavaFileManagerTest
 */

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class StandardJavaFileManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.tools.StandardJavaFileManager");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "javax.tools.JavaFileManager"), "extends javax.tools.JavaFileManager");
        check(hasMethod(c, "isSameFile"), "declares isSameFile");
        check(hasMethod(c, "getJavaFileObjectsFromFiles"), "declares getJavaFileObjectsFromFiles");
        check(hasMethod(c, "getJavaFileObjectsFromPaths"), "declares getJavaFileObjectsFromPaths");
        check(hasMethod(c, "getJavaFileObjects"), "declares getJavaFileObjects");
        check(hasMethod(c, "getJavaFileObjectsFromStrings"), "declares getJavaFileObjectsFromStrings");
        check(hasMethod(c, "setLocation"), "declares setLocation");
        check(hasMethod(c, "getLocation"), "declares getLocation");
        check(hasMethod(c, "asPath"), "declares asPath");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            return;
        }
        javax.tools.StandardJavaFileManager fm =
                jc.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
        Path dir = Files.createTempDirectory("jcksfm");
        try {
            // setLocation / getLocation / getLocationAsPaths round-trip.
            fm.setLocation(javax.tools.StandardLocation.CLASS_OUTPUT, List.of(dir.toFile()));
            boolean found = false;
            for (java.io.File f : fm.getLocation(javax.tools.StandardLocation.CLASS_OUTPUT)) {
                if (f.toPath().toAbsolutePath().equals(dir.toAbsolutePath())) found = true;
            }
            check(found, "getLocation reflects setLocation");
            check(fm.getLocationAsPaths(javax.tools.StandardLocation.CLASS_OUTPUT).iterator().hasNext(),
                    "getLocationAsPaths non-empty");

            // Write a source file and open it as JavaFileObjects through several factories.
            Path src = dir.resolve("X.java");
            Files.write(src, "class X {}".getBytes(StandardCharsets.UTF_8));

            javax.tools.JavaFileObject byFile = fm.getJavaFileObjects(src.toFile()).iterator().next();
            javax.tools.JavaFileObject byPath = fm.getJavaFileObjects(src).iterator().next();
            javax.tools.JavaFileObject byString =
                    fm.getJavaFileObjectsFromStrings(List.of(src.toString())).iterator().next();
            check(byFile.getKind() == javax.tools.JavaFileObject.Kind.SOURCE, "getJavaFileObjects kind SOURCE");
            check(byPath.getKind() == javax.tools.JavaFileObject.Kind.SOURCE, "getJavaFileObjects(Path) kind SOURCE");
            check(byString.getKind() == javax.tools.JavaFileObject.Kind.SOURCE, "getJavaFileObjectsFromStrings kind SOURCE");
            check(byFile.toUri().equals(byPath.toUri()), "file/path objects same uri");
            check(byFile.toUri().equals(byString.toUri()), "file/string objects same uri");

            check(fm.isSameFile(byFile, byFile), "isSameFile self true");
            check(fm.isSameFile(byFile, byPath), "isSameFile equivalent objects true");

            // getFileForOutput produces a FileObject whose path can be derived.
            javax.tools.FileObject out = fm.getFileForOutput(javax.tools.StandardLocation.CLASS_OUTPUT, "", "Y.txt", null);
            check(out != null, "getFileForOutput non-null");
            check(out.getName().endsWith("Y.txt"), "output name ends with Y.txt");

            // getJavaFileObjectsFromFiles with multiple files.
            Path src2 = dir.resolve("Z.java");
            Files.write(src2, "class Z {}".getBytes(StandardCharsets.UTF_8));
            int count = 0;
            for (javax.tools.JavaFileObject j : fm.getJavaFileObjectsFromFiles(List.of(src.toFile(), src2.toFile()))) {
                count++;
            }
            check(count == 2, "getJavaFileObjectsFromFiles returns both files");
        } finally {
            fm.close();
            Files.walk(dir).sorted(Comparator.reverseOrder())
                    .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception e) { } });
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
