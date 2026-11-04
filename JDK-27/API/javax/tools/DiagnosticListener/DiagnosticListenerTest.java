/*
 * @test
 * @summary javax.tools.DiagnosticListener conformance: white-box structure + black-box behavior.
 * @run main DiagnosticListenerTest
 */

import java.lang.reflect.*;
import java.net.URI;
import java.util.List;

public class DiagnosticListenerTest {
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
        Class<?> c = Class.forName("javax.tools.DiagnosticListener");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "report"), "declares report");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // A listener receives diagnostics from a failing compile.
        final int[] count = new int[1];
        javax.tools.DiagnosticListener<javax.tools.JavaFileObject> listener =
                new javax.tools.DiagnosticListener<javax.tools.JavaFileObject>() {
            public void report(javax.tools.Diagnostic<? extends javax.tools.JavaFileObject> d) {
                check(d.getKind() == javax.tools.Diagnostic.Kind.ERROR, "listener sees ERROR kind");
                count[0]++;
            }
        };

        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc != null) {
            javax.tools.JavaFileObject bad = new javax.tools.SimpleJavaFileObject(
                    URI.create("string:///Z.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return "class Z { void m() { int x = ; } }";
                }
            };
            javax.tools.JavaCompiler.CompilationTask task =
                    jc.getTask(null, null, listener, List.of("-proc:none"), null, List.of(bad));
            check(!task.call(), "compile fails");
            check(count[0] > 0, "listener was invoked");

            // Listener is optional: a null listener must also be accepted.
            javax.tools.JavaFileObject good = new javax.tools.SimpleJavaFileObject(
                    URI.create("string:///G.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
                public CharSequence getCharContent(boolean ignoreEncodingErrors) { return "class G {}"; }
            };
            javax.tools.JavaCompiler.CompilationTask ok =
                    jc.getTask(null, null, null, List.of("-proc:none"), null, List.of(good));
            check(ok.call(), "compile with null listener succeeds");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
