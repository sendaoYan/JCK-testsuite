/*
 * @test
 * @summary jdk.jshell.TypeDeclSnippet conformance: type declaration snippet.
 * @run main TypeDeclSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class TypeDeclSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            TypeDeclSnippet t = (TypeDeclSnippet) js.eval("class C1 {}").get(0).snippet();
            check(t.kind() == Snippet.Kind.TYPE_DECL, "kind");
            check(t.name().equals("C1"), "name");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
