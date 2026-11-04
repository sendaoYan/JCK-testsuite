/*
 * @test
 * @summary javax.tools.JavaCompiler conformance: white-box structure + black-box behavior.
 * @run main JavaCompilerTest
 */

import java.lang.reflect.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class JavaCompilerTest {
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
        Class<?> c = Class.forName("javax.tools.JavaCompiler");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 2, "2 superinterface(s)");
        check(hasInterface(c, "javax.tools.Tool"), "extends javax.tools.Tool");
        check(hasInterface(c, "javax.tools.OptionChecker"), "extends javax.tools.OptionChecker");
        check(hasMethod(c, "getTask"), "declares getTask");
        check(hasMethod(c, "getStandardFileManager"), "declares getStandardFileManager");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            return; // non-full JDK: degrade to structural assertions
        }
        check("javac".equals(jc.name()), "name() == javac");
        check(jc.getSourceVersions() != null && !jc.getSourceVersions().isEmpty(), "source versions non-empty");
        check(jc.getSourceVersions().contains(javax.lang.model.SourceVersion.valueOf("RELEASE_27")),
                "source versions contains RELEASE_27");
        check(jc.isSupportedOption("-source") == 1, "isSupportedOption -source == 1");
        check(jc.isSupportedOption("-g") == 0, "isSupportedOption -g == 0");
        check(jc.isSupportedOption("--bogus") == -1, "isSupportedOption --bogus == -1");

        javax.tools.StandardJavaFileManager fm =
                jc.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
        check(fm != null, "getStandardFileManager non-null");
        fm.close();

        // In-memory compile: valid source succeeds, invalid source reports a diagnostic.
        javax.tools.JavaFileObject good = new javax.tools.SimpleJavaFileObject(
                URI.create("string:///X.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
            public CharSequence getCharContent(boolean ignoreEncodingErrors) { return "public class X {}"; }
        };
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> ok =
                new javax.tools.DiagnosticCollector<javax.tools.JavaFileObject>();
        javax.tools.JavaCompiler.CompilationTask t1 =
                jc.getTask(null, null, ok, List.of("-proc:none"), null, List.of(good));
        check(t1.call(), "valid in-memory compile succeeds");
        check(ok.getDiagnostics().isEmpty(), "no diagnostics for valid source");

        javax.tools.JavaFileObject bad = new javax.tools.SimpleJavaFileObject(
                URI.create("string:///Y.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return "class Y { void m() { int x = ; } }";
            }
        };
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> badDc =
                new javax.tools.DiagnosticCollector<javax.tools.JavaFileObject>();
        javax.tools.JavaCompiler.CompilationTask t2 =
                jc.getTask(null, null, badDc, List.of("-proc:none"), null, List.of(bad));
        check(!t2.call(), "invalid in-memory compile fails");
        check(!badDc.getDiagnostics().isEmpty(), "invalid source produces diagnostics");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
