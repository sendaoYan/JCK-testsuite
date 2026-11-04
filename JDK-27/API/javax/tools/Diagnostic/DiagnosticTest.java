/*
 * @test
 * @summary javax.tools.Diagnostic conformance: white-box structure + black-box behavior.
 * @run main DiagnosticTest
 */

import java.lang.reflect.*;
import java.net.URI;
import java.util.Locale;

public class DiagnosticTest {
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
        Class<?> c = Class.forName("javax.tools.Diagnostic");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getKind"), "declares getKind");
        check(hasMethod(c, "getSource"), "declares getSource");
        check(hasMethod(c, "getPosition"), "declares getPosition");
        check(hasMethod(c, "getStartPosition"), "declares getStartPosition");
        check(hasMethod(c, "getEndPosition"), "declares getEndPosition");
        check(hasMethod(c, "getLineNumber"), "declares getLineNumber");
        check(hasMethod(c, "getColumnNumber"), "declares getColumnNumber");
        check(hasMethod(c, "getCode"), "declares getCode");
        check(hasMethod(c, "getMessage"), "declares getMessage");
        boolean hasNOPOS = false;
        for (Field f : c.getDeclaredFields()) if (f.getName().equals("NOPOS")) hasNOPOS = true;
        check(hasNOPOS, "has NOPOS field");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // NOPOS is the sentinel value -1.
        check(javax.tools.Diagnostic.NOPOS == -1L, "NOPOS == -1");

        // Build a Diagnostic implementation and round-trip it through a collector.
        final Object src = "source-unit";
        javax.tools.Diagnostic<Object> d = new javax.tools.Diagnostic<Object>() {
            public javax.tools.Diagnostic.Kind getKind() { return javax.tools.Diagnostic.Kind.WARNING; }
            public Object getSource() { return src; }
            public long getPosition() { return 42; }
            public long getStartPosition() { return 40; }
            public long getEndPosition() { return 45; }
            public long getLineNumber() { return 3; }
            public long getColumnNumber() { return 7; }
            public String getCode() { return "compiler.warn.foo"; }
            public String getMessage(Locale l) { return "some warning"; }
        };
        javax.tools.DiagnosticCollector<Object> dc = new javax.tools.DiagnosticCollector<Object>();
        dc.report(d);
        javax.tools.Diagnostic<?> r = dc.getDiagnostics().get(0);
        check(r.getKind() == javax.tools.Diagnostic.Kind.WARNING, "round-trip kind");
        check(r.getSource() == src, "round-trip source");
        check(r.getPosition() == 42, "round-trip position");
        check(r.getStartPosition() == 40, "round-trip startPosition");
        check(r.getEndPosition() == 45, "round-trip endPosition");
        check(r.getLineNumber() == 3, "round-trip lineNumber");
        check(r.getColumnNumber() == 7, "round-trip columnNumber");
        check("compiler.warn.foo".equals(r.getCode()), "round-trip code");
        check("some warning".equals(r.getMessage(Locale.ROOT)), "round-trip message ROOT");
        check("some warning".equals(r.getMessage(null)), "round-trip message null");

        // Real compiler diagnostic from a failing in-memory compile.
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc != null) {
            javax.tools.JavaFileObject bad = new javax.tools.SimpleJavaFileObject(
                    URI.create("string:///Y.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return "class Y { void m() { int x = ; } }";
                }
            };
            javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> collect =
                    new javax.tools.DiagnosticCollector<javax.tools.JavaFileObject>();
            javax.tools.JavaCompiler.CompilationTask task =
                    jc.getTask(null, null, collect, java.util.List.of("-proc:none"), null, java.util.List.of(bad));
            check(!task.call(), "invalid source fails to compile");
            check(collect.getDiagnostics().size() > 0, "diagnostics collected");
            javax.tools.Diagnostic<? extends javax.tools.JavaFileObject> rd = collect.getDiagnostics().get(0);
            check(rd.getKind() == javax.tools.Diagnostic.Kind.ERROR, "real diagnostic kind ERROR");
            check(rd.getLineNumber() == 1, "real diagnostic line 1");
            check(rd.getColumnNumber() > 0, "real diagnostic column > 0");
            check(rd.getPosition() >= 0, "real diagnostic position >= 0");
            check(rd.getCode() != null && !rd.getCode().isEmpty(), "real diagnostic code present");
            check(rd.getMessage(Locale.ROOT) != null && !rd.getMessage(Locale.ROOT).isEmpty(),
                    "real diagnostic message present");
            check(rd.getSource() == bad, "real diagnostic source is the file object");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
