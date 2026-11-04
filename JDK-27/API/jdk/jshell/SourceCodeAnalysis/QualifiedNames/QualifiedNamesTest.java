/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.QualifiedNames conformance: qualified names via analysis.
 * @run main QualifiedNamesTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class QualifiedNamesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            QualifiedNames q = sca.listQualifiedNames("java.util.", 10);
            check(q != null, "non-null");
            check(q.getNames() != null, "names");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
