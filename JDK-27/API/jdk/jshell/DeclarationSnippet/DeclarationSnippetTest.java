/*
 * @test
 * @summary jdk.jshell.DeclarationSnippet conformance: declaration snippet via var.
 * @run main/othervm DeclarationSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class DeclarationSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: DeclarationSnippet = var/method/typedecl (not import); unresolvedDependencies ----
    static void testDeclarationSemantics() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            VarSnippet v = (VarSnippet) js.eval("int dv1 = 1;").get(0).snippet();
            check(v instanceof DeclarationSnippet, "var is a declaration");
            MethodSnippet m = (MethodSnippet) js.eval("int dm1() { return 1; }").get(0).snippet();
            check(m instanceof DeclarationSnippet, "method is a declaration");
            TypeDeclSnippet t = (TypeDeclSnippet) js.eval("class DT1 {}").get(0).snippet();
            check(t instanceof DeclarationSnippet, "typedecl is a declaration");
            ImportSnippet i = (ImportSnippet) js.eval("import java.util.List;").get(0).snippet();
            Snippet si = i; // widen to avoid inconvertible-type compile error
            check(!(si instanceof DeclarationSnippet), "import is persistent but NOT a declaration");
            // unresolvedDependencies accepts DeclarationSnippet: unresolved method body is listed
            MethodSnippet u = (MethodSnippet) js.eval("int du() { return dmissing(); }").get(0).snippet();
            List<String> deps = js.unresolvedDependencies(u).toList();
            check(deps.size() == 1 && deps.get(0).equals("method dmissing()"), "unresolvedDependencies format: method name()");
            check(js.unresolvedDependencies(v).count() == 0, "resolved declaration has no unresolved deps");
            check(js.unresolvedDependencies(t).count() == 0, "resolved type has no unresolved deps");
            // the same declaration can list several unresolved symbols
            MethodSnippet u2 = (MethodSnippet) js.eval("int du2() { return dA() + dB(); }").get(0).snippet();
            check(js.unresolvedDependencies(u2).count() == 2, "two unresolved deps listed");
            // resolving one shrinks the list
            js.eval("int dA() { return 1; }");
            check(js.unresolvedDependencies(u2).count() == 1, "one dep resolved -> list shrinks");
            check(js.unresolvedDependencies(u2).toList().contains("method dB()"), "remaining dep is dB");
            // unresolved type reference
            TypeDeclSnippet ut = (TypeDeclSnippet) js.eval("class DUT implements DMissing {}").get(0).snippet();
            check(js.status(ut) == Snippet.Status.RECOVERABLE_NOT_DEFINED, "unresolved supertype status");
            // null argument is rejected
            try { js.unresolvedDependencies(null); check(false, "unresolvedDependencies(null) must NPE"); }
            catch (NullPointerException expected) { check(true, "unresolvedDependencies(null) NPE"); }
        }
    }

    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            VarSnippet v = (VarSnippet) js.eval("int x = 1;").get(0).snippet();
            check(v instanceof DeclarationSnippet, "is DeclarationSnippet");
            check(v.name().equals("x"), "name");
            check(v.typeName().equals("int"), "typeName");
        }
        testDeclarationSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
