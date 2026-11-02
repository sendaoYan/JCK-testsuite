/*
 * @test
 * @summary jdk.jshell.Snippet conformance: snippet base properties.
 * @run main SnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class SnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            VarSnippet v = (VarSnippet) js.eval("int x = 1;").get(0).snippet();
            check(v.id() != null, "id");
            check(v.kind() == Snippet.Kind.VAR, "kind");
            check(v.subKind() != null, "subKind");
            check(v.source().contains("int x"), "source");
            check(v.toString() != null, "toString");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
