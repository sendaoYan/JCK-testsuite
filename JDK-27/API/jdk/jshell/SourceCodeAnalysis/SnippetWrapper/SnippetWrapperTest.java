/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.SnippetWrapper conformance: snippet wrapper interface.
 * @run main SnippetWrapperTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.lang.reflect.*;

public class SnippetWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: real wrappers with position mapping ----
    static void testReal() throws Exception {
        try (jdk.jshell.JShell js = jdk.jshell.JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            VarSnippet w = (VarSnippet) js.eval("int sw1 = 1;").get(0).snippet();
            SourceCodeAnalysis.SnippetWrapper wr = sca.wrapper(w);
            check(wr.source().equals("int sw1 = 1;"), "source exact");
            check(wr.kind() == Snippet.Kind.VAR, "kind");
            check(wr.fullClassName().startsWith("REPL.$JShell$"), "fullClassName REPL prefix");
            check(wr.wrapped().startsWith("package REPL;"), "wrapped package decl");
            check(wr.wrapped().contains("class $JShell$"), "wrapped host class");
            // bijective position mapping around the wrapped body
            for (int i = 0; i < wr.source().length(); i += 3) {
                int to = wr.sourceToWrappedPosition(i);
                check(to >= 0, "mapped position non-negative at " + i);
                check(wr.wrappedToSourcePosition(to) <= i, "reverse mapping lands before/at source index");
            }
            check(wr.wrappedToSourcePosition(wr.sourceToWrappedPosition(0)) == 0, "0 maps back to 0");
            // wrappers(String): same shape without an eval'd snippet
            java.util.List<SourceCodeAnalysis.SnippetWrapper> ws = sca.wrappers("int sw2 = 2;");
            check(ws.size() == 1, "one wrapper");
            check(ws.get(0).source().equals("int sw2 = 2;"), "string source");
            check(ws.get(0).kind() == Snippet.Kind.VAR, "string kind");
            check(ws.get(0).wrapped().contains("sw2"), "string wrapped body");
            // method wrapper shape
            java.util.List<SourceCodeAnalysis.SnippetWrapper> ms = sca.wrappers("int swm() { return 1; }");
            check(ms.size() == 1 && ms.get(0).kind() == Snippet.Kind.METHOD, "method wrapper kind");
        }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$SnippetWrapper");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        testReal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
