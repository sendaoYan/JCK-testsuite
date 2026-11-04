/*
 * @test
 * @summary javax.tools.DiagnosticCollector conformance: white-box structure + black-box behavior.
 * @run main DiagnosticCollectorTest
 */

import java.lang.reflect.*;
import java.net.URI;
import java.util.Locale;

public class DiagnosticCollectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.tools.DiagnosticCollector");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(hasInterface(c, "javax.tools.DiagnosticListener"), "implements DiagnosticListener");
        check(hasMethod(c, "report"), "declares report");
        check(hasMethod(c, "getDiagnostics"), "declares getDiagnostics");
        check(c.getDeclaredConstructors().length == 1, "single public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Fresh collector starts empty and reports diagnostics in insertion order.
        javax.tools.DiagnosticCollector<Object> dc = new javax.tools.DiagnosticCollector<Object>();
        check(dc.getDiagnostics().isEmpty(), "empty after construction");
        check(dc.getDiagnostics().size() == 0, "size 0 after construction");

        for (int i = 0; i < 3; i++) {
            final int n = i;
            dc.report(new javax.tools.Diagnostic<Object>() {
                public javax.tools.Diagnostic.Kind getKind() { return javax.tools.Diagnostic.Kind.ERROR; }
                public Object getSource() { return "src" + n; }
                public long getPosition() { return n; }
                public long getStartPosition() { return n; }
                public long getEndPosition() { return n; }
                public long getLineNumber() { return n + 1; }
                public long getColumnNumber() { return n + 1; }
                public String getCode() { return "C" + n; }
                public String getMessage(Locale l) { return "m" + n; }
            });
        }
        check(dc.getDiagnostics().size() == 3, "size after 3 reports");
        check("src0".equals(dc.getDiagnostics().get(0).getSource()), "insertion order preserved");
        check("src2".equals(dc.getDiagnostics().get(2).getSource()), "last element order");
        check(dc.getDiagnostics().get(0).getKind() == javax.tools.Diagnostic.Kind.ERROR, "kind retained");

        // Collector works as a DiagnosticListener in a real in-memory compile.
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc != null) {
            javax.tools.JavaFileObject good = new javax.tools.SimpleJavaFileObject(
                    URI.create("string:///X.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
                public CharSequence getCharContent(boolean ignoreEncodingErrors) { return "public class X {}"; }
            };
            javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> ok =
                    new javax.tools.DiagnosticCollector<javax.tools.JavaFileObject>();
            javax.tools.JavaCompiler.CompilationTask t =
                    jc.getTask(null, null, ok, java.util.List.of("-proc:none"), null, java.util.List.of(good));
            check(t.call(), "valid in-memory compile succeeds");
            check(ok.getDiagnostics().isEmpty(), "no diagnostics for valid source");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
