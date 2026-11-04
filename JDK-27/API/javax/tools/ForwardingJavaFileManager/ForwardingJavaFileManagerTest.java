/*
 * @test
 * @summary javax.tools.ForwardingJavaFileManager conformance: white-box structure + black-box behavior.
 * @run main ForwardingJavaFileManagerTest
 */

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ForwardingJavaFileManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.tools.ForwardingJavaFileManager");
        check(!c.isInterface(), "not an interface");
        check(hasInterface(c, "javax.tools.JavaFileManager"), "implements JavaFileManager");
        Field f = c.getDeclaredField("fileManager");
        check(java.lang.reflect.Modifier.isProtected(f.getModifiers())
                && java.lang.reflect.Modifier.isFinal(f.getModifiers()), "protected final fileManager field");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            return;
        }
        javax.tools.StandardJavaFileManager fm =
                jc.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
        javax.tools.ForwardingJavaFileManager<javax.tools.JavaFileManager> m =
                new javax.tools.ForwardingJavaFileManager<javax.tools.JavaFileManager>(fm) {};
        Path dir = Files.createTempDirectory("jckfjm");
        try {
            check(m.getClassLoader(javax.tools.StandardLocation.CLASS_PATH) != null, "getClassLoader forwarded");
            check(m.hasLocation(javax.tools.StandardLocation.CLASS_PATH), "hasLocation forwarded");
            check(m.isSupportedOption("-classpath") == 1, "isSupportedOption forwarded");
            check(m.isSupportedOption("--bogus") == -1, "isSupportedOption unknown forwarded");
            check(m.handleOption("-classpath", List.of("x").iterator()), "handleOption forwarded");

            // Delegated output file creation (setLocation lives on StandardJavaFileManager).
            fm.setLocation(javax.tools.StandardLocation.CLASS_OUTPUT, List.of(dir.toFile()));
            javax.tools.FileObject fo = m.getFileForOutput(javax.tools.StandardLocation.CLASS_OUTPUT, "", "Out.txt", null);
            check(fo != null, "getFileForOutput forwarded");
            check(m.isSameFile(fo, fo), "isSameFile forwarded");

            // list delegates and returns an iterable.
            Iterable<javax.tools.JavaFileObject> listed =
                    m.list(javax.tools.StandardLocation.CLASS_PATH, "", java.util.Set.of(javax.tools.JavaFileObject.Kind.CLASS), false);
            check(listed != null, "list forwarded non-null");

            m.flush();
        } finally {
            m.close();
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
