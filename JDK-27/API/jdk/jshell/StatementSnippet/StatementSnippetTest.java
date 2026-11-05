/*
 * @test
 * @summary jdk.jshell.StatementSnippet conformance: statement snippet.
 * @run main/othervm StatementSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class StatementSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract ----
    static void testStatementSemantics() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            // an expression statement of a void call is classified as STATEMENT
            StatementSnippet vs = (StatementSnippet) js.eval("System.out.println(2);").get(0).snippet();
            check(vs.kind() == Snippet.Kind.STATEMENT, "void call is STATEMENT");
            check(vs.source().equals("System.out.println(2);"), "statement source");
            // a statement never produces a value
            StatementSnippet loop = (StatementSnippet) js.eval("for (int stI = 0; stI < 3; stI++) ;").get(0).snippet();
            check(loop.kind() == Snippet.Kind.STATEMENT, "for statement kind");
            check(loop.subKind() == Snippet.SubKind.STATEMENT_SUBKIND, "for statement subkind");
            check(loop.subKind().isExecutable() && !loop.subKind().hasValue(), "statement executable without value");
            SnippetEvent ev = js.eval("do ; while (false);").get(0);
            check(ev.value() == null || ev.value().isEmpty(), "while statement no value");
            check(ev.status() == Snippet.Status.VALID, "while statement VALID");
            // statement side effects are visible to later snippets (real execution)
            js.eval("int stCounter = 10;");
            js.eval("stCounter += 5;");
            check(js.eval("stCounter").get(0).value().equals("15"), "statement side effect visible");
            // statements can declare nothing and still run
            check(js.eval("System.out.print(\"\")").get(0).snippet() instanceof StatementSnippet, "println is a statement");
            // broken statement is ERRONEOUS, not a statement
            Snippet bad = js.eval("for (").get(0).snippet();
            check(bad.kind() == Snippet.Kind.ERRONEOUS, "broken for ERRONEOUS");
            check(((ErroneousSnippet) bad).probableKind() == Snippet.Kind.STATEMENT, "probableKind STATEMENT hint");
            // statement snippets are not listed by methods()/types()/imports()
            js.eval("int stCheck1 = 1;");
            js.eval("stCheck1 = 2;");
            check(js.methods().count() == 0 && js.types().count() == 0 && js.imports().count() == 0,
                  "statements do not pollute declaration streams");
            // every entry of variables() is a VAR-kind declaration/temp var
            check(js.variables().allMatch(v -> v.kind() == Snippet.Kind.VAR), "variables() only VAR snippets");
        }
    }

    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            StatementSnippet s = (StatementSnippet) js.eval("System.out.println(1);").get(0).snippet();
            check(s.kind() == Snippet.Kind.STATEMENT, "kind");
            check(s.source() != null, "source");
        }
        testStatementSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
