/*
 * @test
 * @summary jdk.jshell.ErroneousSnippet conformance: erroneous snippet properties.
 * @run main/othervm ErroneousSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class ErroneousSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.ErroneousSnippet");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("probableKind")) f=true; check(f, "has probableKind"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("source")) f=true; check(f, "has source"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            ErroneousSnippet es = (ErroneousSnippet) js.eval("int = ;").get(0).snippet();
            check(es.kind() == Snippet.Kind.ERRONEOUS, "kind");
            check(es.probableKind() != null, "probableKind");
            check(es.source().contains("int"), "source");
        }
    }

    // ---- deep contract ----
    static void testProbableKindMatrix() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            // broken declarations carry a probable-kind hint matching the intended form
            ErroneousSnippet es1 = (ErroneousSnippet) js.eval("int esv = ;").get(0).snippet();
            check(es1.kind() == Snippet.Kind.ERRONEOUS, "broken var ERRONEOUS");
            check(es1.probableKind() == Snippet.Kind.VAR, "probableKind VAR");
            check(es1.subKind() == Snippet.SubKind.UNKNOWN_SUBKIND, "erroneous subkind UNKNOWN");
            ErroneousSnippet es2 = (ErroneousSnippet) js.eval("class {").get(0).snippet();
            check(es2.probableKind() == Snippet.Kind.TYPE_DECL, "probableKind TYPE_DECL");
            ErroneousSnippet es3 = (ErroneousSnippet) js.eval("int esm( {}").get(0).snippet();
            check(es3.probableKind() == Snippet.Kind.METHOD, "probableKind METHOD");
            ErroneousSnippet es4 = (ErroneousSnippet) js.eval("import").get(0).snippet();
            check(es4.probableKind() == Snippet.Kind.IMPORT, "probableKind IMPORT");
            ErroneousSnippet es5 = (ErroneousSnippet) js.eval("for (").get(0).snippet();
            check(es5.probableKind() == Snippet.Kind.STATEMENT, "probableKind STATEMENT");
            // garbage is hinted as a (broken) expression
            ErroneousSnippet es6 = (ErroneousSnippet) js.eval("###").get(0).snippet();
            check(es6.probableKind() == Snippet.Kind.EXPRESSION, "garbage probableKind EXPRESSION");
            // status is REJECTED and diagnostics are attached for each
            for (ErroneousSnippet es : new ErroneousSnippet[]{es1, es2, es3, es4, es5, es6}) {
                check(js.status(es) == Snippet.Status.REJECTED, "REJECTED: " + es.source());
                check(js.diagnostics(es).count() >= 1, "has diagnostics: " + es.source());
                check(es.source() != null && es.id() != null, "id/source present: " + es.source());
            }
            // erroneous snippets are queryable through snippets() but not declaration streams
            check(js.snippets().anyMatch(s -> s.kind() == Snippet.Kind.ERRONEOUS), "snippets() has ERRONEOUS");
            check(js.variables().noneMatch(v -> v.name().equals("esv")), "no var registered for broken decl");
            check(js.methods().noneMatch(m -> m.name().equals("esm")), "no method registered for broken decl");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testProbableKindMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
