/*
 * @test
 * @summary jdk.jshell.JShell conformance: create eval drop inspect.
 * @run main JShellTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class JShellTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            check(js != null, "created");
            check(js.eval("int x = 1;").get(0).status() == Snippet.Status.VALID, "eval");
            check(js.snippets().count() >= 1, "snippets");
            check(js.sourceCodeAnalysis() != null, "sourceCodeAnalysis");
            VarSnippet v = (VarSnippet) js.eval("int y = 2;").get(0).snippet();
            check(js.drop(v).get(0).status() == Snippet.Status.DROPPED, "drop");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
