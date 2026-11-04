/*
 * @test
 * @summary jdk.jshell.StatementSnippet conformance: statement snippet.
 * @run main StatementSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class StatementSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            StatementSnippet s = (StatementSnippet) js.eval("System.out.println(1);").get(0).snippet();
            check(s.kind() == Snippet.Kind.STATEMENT, "kind");
            check(s.source() != null, "source");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
