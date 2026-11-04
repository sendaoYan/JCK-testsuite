/*
 * @test
 * @summary javax.tools.JavaFileManager conformance: white-box structure + black-box behavior.
 * @run main JavaFileManagerTest
 */

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class JavaFileManagerTest {
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
        Class<?> c = Class.forName("javax.tools.JavaFileManager");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 3, "3 superinterface(s)");
        check(hasInterface(c, "java.io.Closeable"), "extends java.io.Closeable");
        check(hasInterface(c, "java.io.Flushable"), "extends java.io.Flushable");
        check(hasInterface(c, "javax.tools.OptionChecker"), "extends javax.tools.OptionChecker");
        check(hasMethod(c, "getClassLoader"), "declares getClassLoader");
        check(hasMethod(c, "list"), "declares list");
        check(hasMethod(c, "inferBinaryName"), "declares inferBinaryName");
        check(hasMethod(c, "isSameFile"), "declares isSameFile");
        check(hasMethod(c, "handleOption"), "declares handleOption");
        check(hasMethod(c, "hasLocation"), "declares hasLocation");
        check(hasMethod(c, "getJavaFileForInput"), "declares getJavaFileForInput");
        check(hasMethod(c, "getJavaFileForOutput"), "declares getJavaFileForOutput");
        check(hasMethod(c, "getFileForOutput"), "declares getFileForOutput");
        check(hasMethod(c, "flush"), "declares flush");
        check(hasMethod(c, "close"), "declares close");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            return;
        }
        javax.tools.StandardJavaFileManager fm =
                jc.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
        Path dir = Files.createTempDirectory("jckjfm");
        try {
            fm.setLocation(javax.tools.StandardLocation.SOURCE_PATH, List.of(dir.toFile()));
            Path src = dir.resolve("X.java");
            Files.write(src, "class X {}".getBytes(StandardCharsets.UTF_8));

            javax.tools.JavaFileObject jfo = fm.getJavaFileObjects(src.toFile()).iterator().next();
            check(fm.hasLocation(javax.tools.StandardLocation.SOURCE_PATH), "hasLocation SOURCE_PATH");
            check("X".equals(fm.inferBinaryName(javax.tools.StandardLocation.SOURCE_PATH, jfo)),
                    "inferBinaryName returns X");
            check(fm.isSameFile(jfo, jfo), "isSameFile self true");
            check(fm.handleOption("-classpath", List.of("x").iterator()), "handleOption -classpath");
            check(fm.isSupportedOption("-classpath") == 1, "isSupportedOption -classpath == 1");
            check(fm.getClassLoader(javax.tools.StandardLocation.CLASS_PATH) != null, "getClassLoader non-null");

            Iterable<javax.tools.JavaFileObject> listed = fm.list(
                    javax.tools.StandardLocation.SOURCE_PATH, "", java.util.Set.of(javax.tools.JavaFileObject.Kind.SOURCE), true);
            check(listed != null, "list returns non-null iterable");

            javax.tools.JavaFileObject out = fm.getJavaFileForOutput(
                    javax.tools.StandardLocation.CLASS_OUTPUT, "X", javax.tools.JavaFileObject.Kind.CLASS, null);
            check(out != null, "getJavaFileForOutput non-null");

            fm.flush();
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
