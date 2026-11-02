/*
 * @test
 * @summary jdk.jshell.SnippetEvent conformance: snippet event fields.
 * @run main SnippetEventTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class SnippetEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            SnippetEvent e = js.eval("int x = 1;").get(0);
            check(e.snippet() != null, "snippet");
            check(e.status() == Snippet.Status.VALID, "status");
            check(e.previousStatus() != null, "previousStatus");
            check(e.snippet().id() != null, "snippet id");
            check(e.causeSnippet() == null, "no cause snippet");
            check(e.exception() == null, "no exception");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
