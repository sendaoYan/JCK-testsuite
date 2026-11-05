/*
 * @test
 * @summary jdk.jshell.Snippet.Kind conformance: snippet kind enum.
 * @run main/othervm KindTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class KindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testEnumTable() {
        check(Snippet.Kind.VAR != null && Snippet.Kind.METHOD != null && Snippet.Kind.IMPORT != null, "constants");
        check(Snippet.Kind.TYPE_DECL != null && Snippet.Kind.EXPRESSION != null && Snippet.Kind.STATEMENT != null && Snippet.Kind.ERRONEOUS != null, "more constants");
        check(Snippet.Kind.values().length == 7, "7 constants");
        check(Snippet.Kind.valueOf("VAR") == Snippet.Kind.VAR, "valueOf");
        // exact declaration order
        Snippet.Kind[] ks = Snippet.Kind.values();
        check(ks[0] == Snippet.Kind.IMPORT, "order[0] IMPORT");
        check(ks[1] == Snippet.Kind.TYPE_DECL, "order[1] TYPE_DECL");
        check(ks[2] == Snippet.Kind.METHOD, "order[2] METHOD");
        check(ks[3] == Snippet.Kind.VAR, "order[3] VAR");
        check(ks[4] == Snippet.Kind.EXPRESSION, "order[4] EXPRESSION");
        check(ks[5] == Snippet.Kind.STATEMENT, "order[5] STATEMENT");
        check(ks[6] == Snippet.Kind.ERRONEOUS, "order[6] ERRONEOUS");
        // isPersistent: declarations persist in the engine, expressions/statements do not
        check(Snippet.Kind.IMPORT.isPersistent(), "IMPORT persistent");
        check(Snippet.Kind.TYPE_DECL.isPersistent(), "TYPE_DECL persistent");
        check(Snippet.Kind.METHOD.isPersistent(), "METHOD persistent");
        check(Snippet.Kind.VAR.isPersistent(), "VAR persistent");
        check(!Snippet.Kind.EXPRESSION.isPersistent(), "EXPRESSION not persistent");
        check(!Snippet.Kind.STATEMENT.isPersistent(), "STATEMENT not persistent");
        check(!Snippet.Kind.ERRONEOUS.isPersistent(), "ERRONEOUS not persistent");
        try {
            Snippet.Kind.valueOf("BOGUS");
            check(false, "valueOf bogus must throw");
        } catch (IllegalArgumentException expected) { check(true, "valueOf bogus IAE"); }
        // EnumSet machinery works
        check(EnumSet.of(Snippet.Kind.IMPORT, Snippet.Kind.VAR).size() == 2, "EnumSet usable");
    }

    // ---- deep contract: each Kind is produced by exactly the documented source forms ----
    static void testKindBySource() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            check(js.eval("import java.util.List;").get(0).snippet().kind() == Snippet.Kind.IMPORT, "single type import");
            check(js.eval("import java.util.*;").get(0).snippet().kind() == Snippet.Kind.IMPORT, "on-demand import");
            check(js.eval("import static java.lang.Math.max;").get(0).snippet().kind() == Snippet.Kind.IMPORT, "single static import");
            check(js.eval("import static java.lang.Math.*;").get(0).snippet().kind() == Snippet.Kind.IMPORT, "static on-demand import");
            check(js.eval("class K1 {}").get(0).snippet().kind() == Snippet.Kind.TYPE_DECL, "class");
            check(js.eval("interface K2 {}").get(0).snippet().kind() == Snippet.Kind.TYPE_DECL, "interface");
            check(js.eval("enum K3 { A }").get(0).snippet().kind() == Snippet.Kind.TYPE_DECL, "enum");
            check(js.eval("record K4(int q) {}").get(0).snippet().kind() == Snippet.Kind.TYPE_DECL, "record");
            check(js.eval("int km() { return 1; }").get(0).snippet().kind() == Snippet.Kind.METHOD, "method");
            check(js.eval("int kv;").get(0).snippet().kind() == Snippet.Kind.VAR, "var decl no init");
            check(js.eval("int kv2 = 1;").get(0).snippet().kind() == Snippet.Kind.VAR, "var decl with init");
            check(js.eval("kv2").get(0).snippet().kind() == Snippet.Kind.EXPRESSION, "bare name expression");
            check(js.eval("kv2 = 3").get(0).snippet().kind() == Snippet.Kind.EXPRESSION, "assignment expression");
            check(js.eval("System.out.println();").get(0).snippet().kind() == Snippet.Kind.STATEMENT, "statement");
            check(js.eval("for (int i2 = 0; i2 < 1; i2++) ;").get(0).snippet().kind() == Snippet.Kind.STATEMENT, "for statement");
            check(js.eval("if (true) ; else ;").get(0).snippet().kind() == Snippet.Kind.STATEMENT, "if statement");
            check(js.eval("1 +").get(0).snippet().kind() == Snippet.Kind.ERRONEOUS, "erroneous");
            check(js.eval("###").get(0).snippet().kind() == Snippet.Kind.ERRONEOUS, "garbage erroneous");
            // statement snippets are never persistent even though they ran
            Snippet stmt = js.eval("for (int q = 0; q < 1; q++) ;").get(0).snippet();
            check(stmt.kind() == Snippet.Kind.STATEMENT && !stmt.kind().isPersistent(), "statement not persistent after run");
            // ErroneousSnippet also exposes a probableKind hint
            ErroneousSnippet err = (ErroneousSnippet) js.eval("class {").get(0).snippet();
            check(err.probableKind() == Snippet.Kind.TYPE_DECL, "probableKind hint for broken class");
        }
    }

    public static void main(String[] args) throws Exception {
        testEnumTable();
        testKindBySource();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
