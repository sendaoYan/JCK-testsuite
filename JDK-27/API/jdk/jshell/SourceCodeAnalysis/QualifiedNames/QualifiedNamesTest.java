/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.QualifiedNames conformance: qualified names via analysis.
 * @run main/othervm QualifiedNamesTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class QualifiedNamesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: field semantics via the engine ----
    static void testSemantics() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            QualifiedNames q = sca.listQualifiedNames("System", 6);
            check(q.getSimpleNameLength() == 6, "full identifier length");
            check(q.isResolvable(), "System resolvable");
            check(q.getNames() != null, "names list non-null");
            QualifiedNames part = sca.listQualifiedNames("Sys", 3);
            check(part.getSimpleNameLength() == 3, "partial length");
            check(!part.isResolvable(), "partial not resolvable");
            QualifiedNames dot = sca.listQualifiedNames("java.util.", 10);
            check(dot.getSimpleNameLength() == -1, "no partial name after complete package prefix");
            check(!dot.isResolvable(), "package prefix itself not resolvable");
            // empty input is tolerated (no partial identifier)
            QualifiedNames empty = sca.listQualifiedNames("", 0);
            check(empty.getSimpleNameLength() == -1, "empty input has no partial name");
        }
    }

    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            QualifiedNames q = sca.listQualifiedNames("java.util.", 10);
            check(q != null, "non-null");
            check(q.getNames() != null, "names");
        }
        testSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
