/*
 * @test
 * @summary javax.tools.DocumentationTool.DocumentationTask conformance: white-box structure + black-box behavior.
 * @run main DocumentationTaskTest
 */

import java.lang.reflect.*;
import java.net.URI;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DocumentationTaskTest {
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
        Class<?> c = Class.forName("javax.tools.DocumentationTool$DocumentationTask");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "java.util.concurrent.Callable"), "extends java.util.concurrent.Callable");
        check(hasMethod(c, "addModules"), "declares addModules");
        check(hasMethod(c, "setLocale"), "declares setLocale");
        check(hasMethod(c, "call"), "declares call");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.tools.DocumentationTool dt = javax.tools.ToolProvider.getSystemDocumentationTool();
        if (dt == null) {
            return; // degrade: only structural checks apply
        }

        Path outDir = Files.createTempDirectory("jckdoc");
        javax.tools.StandardJavaFileManager fm =
                dt.getStandardFileManager(null, Locale.ROOT, java.nio.charset.StandardCharsets.UTF_8);
        try {
            javax.tools.JavaFileObject src = new javax.tools.SimpleJavaFileObject(
                    URI.create("string:///X.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return "/** A documented class. */ public class X {}";
                }
            };
            java.io.StringWriter out = new java.io.StringWriter();
            javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> dc =
                    new javax.tools.DiagnosticCollector<javax.tools.JavaFileObject>();
            javax.tools.DocumentationTool.DocumentationTask task =
                    dt.getTask(out, fm, dc, null, List.of("-d", outDir.toString()), List.of(src));
            check(task != null, "getTask returns non-null task");
            task.setLocale(Locale.ROOT);
            task.addModules(List.of());
            Boolean result = task.call();
            check(Boolean.TRUE.equals(result), "javadoc task call() succeeds");
            check(Files.isRegularFile(outDir.resolve("X.html")), "javadoc produced X.html");
        } finally {
            fm.close();
            if (Files.isDirectory(outDir)) {
                Files.walk(outDir).sorted(Comparator.reverseOrder())
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception e) { } });
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
