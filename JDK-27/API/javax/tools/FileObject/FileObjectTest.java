/*
 * @test
 * @summary javax.tools.FileObject conformance: white-box structure + black-box behavior.
 * @run main FileObjectTest
 */

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class FileObjectTest {
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
        Class<?> c = Class.forName("javax.tools.FileObject");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "toUri"), "declares toUri");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "openInputStream"), "declares openInputStream");
        check(hasMethod(c, "openOutputStream"), "declares openOutputStream");
        check(hasMethod(c, "openReader"), "declares openReader");
        check(hasMethod(c, "getCharContent"), "declares getCharContent");
        check(hasMethod(c, "openWriter"), "declares openWriter");
        check(hasMethod(c, "getLastModified"), "declares getLastModified");
        check(hasMethod(c, "delete"), "declares delete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            return;
        }
        javax.tools.StandardJavaFileManager fm =
                jc.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
        Path dir = Files.createTempDirectory("jckfo");
        try {
            fm.setLocation(javax.tools.StandardLocation.CLASS_OUTPUT, List.of(dir.toFile()));

            // Acquire a FileObject and use the full write -> read -> delete cycle.
            javax.tools.FileObject fo = fm.getFileForOutput(javax.tools.StandardLocation.CLASS_OUTPUT, "", "data.txt", null);
            check(fo != null, "getFileForOutput non-null");
            check(fo.toUri() != null, "toUri non-null");
            check(fo.getName() != null && fo.getName().endsWith("data.txt"), "name ends with data.txt");
            check(fo.getLastModified() >= 0, "lastModified non-negative");

            java.io.Writer w = fo.openWriter();
            w.write("hello file object");
            w.close();

            CharSequence content = fo.getCharContent(false);
            check("hello file object".contentEquals(content), "write then read char content");

            java.io.Reader r = fo.openReader(false);
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[64];
            int n;
            while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
            r.close();
            check("hello file object".contentEquals(sb), "openReader round-trips content");

            check(fm.isSameFile(fo, fo), "isSameFile self true");
            check(fo.delete(), "delete returns true");
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
