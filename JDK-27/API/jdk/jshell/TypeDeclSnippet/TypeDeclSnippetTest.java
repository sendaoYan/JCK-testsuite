/*
 * @test
 * @summary jdk.jshell.TypeDeclSnippet conformance: type declaration snippet.
 * @run main/othervm TypeDeclSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class TypeDeclSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract ----
    static void testTypeDeclSemantics() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            TypeDeclSnippet t = (TypeDeclSnippet) js.eval("class TD1 {}").get(0).snippet();
            check(t.kind() == Snippet.Kind.TYPE_DECL, "kind");
            check(t.name().equals("TD1"), "name");
            check(t.subKind() == Snippet.SubKind.CLASS_SUBKIND, "class subkind");
            // all type forms share the Kind
            TypeDeclSnippet iface = (TypeDeclSnippet) js.eval("interface TD2 {}").get(0).snippet();
            check(iface.subKind() == Snippet.SubKind.INTERFACE_SUBKIND, "interface subkind");
            TypeDeclSnippet en = (TypeDeclSnippet) js.eval("enum TD3 { A, B }").get(0).snippet();
            check(en.subKind() == Snippet.SubKind.ENUM_SUBKIND, "enum subkind");
            TypeDeclSnippet rec = (TypeDeclSnippet) js.eval("record TD4(int q) {}").get(0).snippet();
            check(rec.subKind() == Snippet.SubKind.RECORD_SUBKIND, "record subkind");
            TypeDeclSnippet anno = (TypeDeclSnippet) js.eval("@interface TD5 {}").get(0).snippet();
            check(anno.subKind() == Snippet.SubKind.ANNOTATION_TYPE_SUBKIND, "annotation subkind");
            // types() stream lists exactly these
            check(js.types().count() == 5, "types() count");
            check(js.types().allMatch(s -> s instanceof TypeDeclSnippet), "types() yields TypeDeclSnippets");
            // type is usable from other snippets (closed loop)
            check(js.eval("new TD1()").get(0).status() == Snippet.Status.VALID, "instantiate declared class");
            check(js.eval("TD3.A.name()").get(0).value().equals("\"A\""), "enum constant usable");
            check(js.eval("TD3.valueOf(\"A\")").get(0).value().equals("A"), "valueOf usable (enum toString)");
            check(js.eval("new TD4(9).q()").get(0).value().equals("9"), "record accessor usable");
            // nested and generic types
            TypeDeclSnippet nested = (TypeDeclSnippet) js.eval("class TD6 { class Inner {} int z = 1; }").get(0).snippet();
            check(nested.name().equals("TD6"), "nested holder name");
            check(js.eval("new TD6().new Inner()").get(0).status() == Snippet.Status.VALID, "inner class usable");
            // nonexistent member reference on a snippet type is rejected
            check(js.eval("TD6.nosuchmember").get(0).status() == Snippet.Status.REJECTED, "bogus member rejected");
            // extending a snippet-declared type
            TypeDeclSnippet sub = (TypeDeclSnippet) js.eval("class TD7 extends TD1 {}").get(0).snippet();
            check(js.status(sub) == Snippet.Status.VALID, "subclass of snippet type VALID");
            // undefined supertype keeps the decl RECOVERABLE_NOT_DEFINED
            TypeDeclSnippet und = (TypeDeclSnippet) js.eval("class TD8 extends NoSuchTD {}").get(0).snippet();
            check(js.status(und) == Snippet.Status.RECOVERABLE_NOT_DEFINED, "undefined supertype RECOVERABLE_NOT_DEFINED");
            check(und.name().equals("TD8"), "name available while recoverable");
        }
    }

    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            TypeDeclSnippet t = (TypeDeclSnippet) js.eval("class C1 {}").get(0).snippet();
            check(t.kind() == Snippet.Kind.TYPE_DECL, "kind");
            check(t.name().equals("C1"), "name");
        }
        testTypeDeclSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
