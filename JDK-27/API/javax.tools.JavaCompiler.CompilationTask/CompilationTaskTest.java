/*
 * @test
 * @summary javax.tools.JavaCompiler.CompilationTask conformance: white-box structure + black-box behavior.
 * @run main CompilationTaskTest
 */

import java.lang.reflect.*;
import java.net.URI;
import java.util.List;
import java.util.Locale;

public class CompilationTaskTest {
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
        Class<?> c = Class.forName("javax.tools.JavaCompiler$CompilationTask");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "java.util.concurrent.Callable"), "extends java.util.concurrent.Callable");
        check(hasMethod(c, "addModules"), "declares addModules");
        check(hasMethod(c, "setProcessors"), "declares setProcessors");
        check(hasMethod(c, "setLocale"), "declares setLocale");
        check(hasMethod(c, "call"), "declares call");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            return;
        }
        javax.tools.JavaFileObject src = new javax.tools.SimpleJavaFileObject(
                URI.create("string:///X.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
            public CharSequence getCharContent(boolean ignoreEncodingErrors) { return "public class X {}"; }
        };
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> dc =
                new javax.tools.DiagnosticCollector<javax.tools.JavaFileObject>();
        javax.tools.JavaCompiler.CompilationTask task =
                jc.getTask(null, null, dc, List.of("-proc:none"), null, List.of(src));
        check(task != null, "getTask returns non-null task");

        task.setLocale(Locale.ROOT);
        task.addModules(List.of());
        task.setProcessors(List.of());
        Boolean result = task.call();
        check(Boolean.TRUE.equals(result), "task.call() succeeds for valid source");
        check(dc.getDiagnostics().isEmpty(), "no diagnostics for valid source");

        // A task may only be called once; a second call is not required to succeed,
        // so we only assert the first call result deterministically.
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
