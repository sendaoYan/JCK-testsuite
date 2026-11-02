/*
 * @test
 * @summary jdk.jshell.ErroneousSnippet conformance: erroneous snippet properties.
 * @run main ErroneousSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class ErroneousSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            ErroneousSnippet es = (ErroneousSnippet) js.eval("int = ;").get(0).snippet();
            check(es.kind() == Snippet.Kind.ERRONEOUS, "kind");
            check(es.probableKind() != null, "probableKind");
            check(es.source().contains("int"), "source");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
