/*
 * @test
 * @summary jdk.jshell.UnresolvedReferenceException conformance: unresolved reference exception.
 * @run main UnresolvedReferenceExceptionTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class UnresolvedReferenceExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            MethodSnippet m = (MethodSnippet) js.eval("void mm() { int x = z; }").get(0).snippet();
            SnippetEvent e = js.eval("mm()").get(0);
            UnresolvedReferenceException ex = (UnresolvedReferenceException) e.exception();
            check(ex != null, "exception present");
            check(ex.getSnippet() == m, "snippet");
            check(ex.getMessage() != null, "message");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
