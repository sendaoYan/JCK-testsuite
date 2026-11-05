/*
 * @test
 * @summary jdk.jshell.Snippet conformance: snippet base properties.
 * @run main/othervm SnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class SnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.Snippet");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("id")) f=true; check(f, "has id"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("kind")) f=true; check(f, "has kind"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("source")) f=true; check(f, "has source"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("subKind")) f=true; check(f, "has subKind"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("toString")) f=true; check(f, "has toString"); }
        // class hierarchy: DeclarationSnippet extends PersistentSnippet extends Snippet
        check(jdk.jshell.PersistentSnippet.class.getSuperclass() == jdk.jshell.Snippet.class, "PersistentSnippet extends Snippet");
        check(jdk.jshell.DeclarationSnippet.class.getSuperclass() == jdk.jshell.PersistentSnippet.class, "DeclarationSnippet extends PersistentSnippet");
        check(jdk.jshell.VarSnippet.class.getSuperclass() == jdk.jshell.DeclarationSnippet.class, "VarSnippet extends DeclarationSnippet");
        check(jdk.jshell.MethodSnippet.class.getSuperclass() == jdk.jshell.DeclarationSnippet.class, "MethodSnippet extends DeclarationSnippet");
        check(jdk.jshell.TypeDeclSnippet.class.getSuperclass() == jdk.jshell.DeclarationSnippet.class, "TypeDeclSnippet extends DeclarationSnippet");
        check(jdk.jshell.ImportSnippet.class.getSuperclass() == jdk.jshell.PersistentSnippet.class, "ImportSnippet extends PersistentSnippet");
        check(jdk.jshell.StatementSnippet.class.getSuperclass() == jdk.jshell.Snippet.class, "StatementSnippet extends Snippet");
        check(jdk.jshell.ExpressionSnippet.class.getSuperclass() == jdk.jshell.Snippet.class, "ExpressionSnippet extends Snippet");
        check(jdk.jshell.ErroneousSnippet.class.getSuperclass() == jdk.jshell.Snippet.class, "ErroneousSnippet extends Snippet");
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            VarSnippet v = (VarSnippet) js.eval("int x = 1;").get(0).snippet();
            check(v.id() != null, "id");
            check(v.kind() == Snippet.Kind.VAR, "kind");
            check(v.subKind() != null, "subKind");
            check(v.source().contains("int x"), "source");
            check(v.toString() != null, "toString");
        }
    }

    // ---- deep contract ----

    static void testIdStabilityAndSource() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            VarSnippet a = (VarSnippet) js.eval("int sxA = 1;").get(0).snippet();
            // id is stable across calls and unique per snippet
            check(a.id().equals(a.id()), "id stable across calls");
            VarSnippet b = (VarSnippet) js.eval("int sxB = 2;").get(0).snippet();
            check(!a.id().equals(b.id()), "distinct snippets have distinct ids");
            // source() round-trips the exact input, including interior spacing
            check(a.source().equals("int sxA = 1;"), "source exact round-trip");
            VarSnippet spaced = (VarSnippet) js.eval("int   spaced   =   3 ;").get(0).snippet();
            check(spaced.source().equals("int   spaced   =   3 ;"), "source preserves spacing");
            // toString contains identifying info
            check(a.toString().contains("sxA"), "toString mentions name");
            // source preserved after drop (snippet object remains queryable)
            js.drop(b);
            check(b.source().equals("int sxB = 2;"), "source preserved after drop");
            check(b.kind() == Snippet.Kind.VAR, "kind preserved after drop");
            // subKind consistency: subKind().kind() matches snippet kind for all families
            String[] probes = {
                "import java.util.List;", "import java.util.*;", "import static java.lang.Math.max;",
                "class SC {}", "interface SI {}", "enum SE { A }", "record SR(int q) {}",
                "int sm() { return 1; }", "int sv;", "int sv2 = 1;", "2 + 2", "sv", "sv = 5;",
                "System.out.println();"
            };
            for (String src : probes) {
                Snippet s = js.eval(src).get(0).snippet();
                check(s.subKind().kind() == s.kind(), "subKind consistent for: " + src);
                check(s.id() != null && s.source() != null && s.toString() != null, "base props for: " + src);
            }
            // every kind reachable through eval
            Set<Snippet.Kind> seen = new HashSet<>();
            for (String src : probes) seen.add(js.eval(src).get(0).snippet().kind());
            check(seen.contains(Snippet.Kind.IMPORT) && seen.contains(Snippet.Kind.TYPE_DECL)
                  && seen.contains(Snippet.Kind.METHOD) && seen.contains(Snippet.Kind.VAR)
                  && seen.contains(Snippet.Kind.EXPRESSION) && seen.contains(Snippet.Kind.STATEMENT),
                  "six kinds reachable by eval");
            js.eval("1 +"); // ERRONEOUS
            check(js.snippets().anyMatch(s -> s.kind() == Snippet.Kind.ERRONEOUS), "ERRONEOUS kind reachable");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testIdStabilityAndSource();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
