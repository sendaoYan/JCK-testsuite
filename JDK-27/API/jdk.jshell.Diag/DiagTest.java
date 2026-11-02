/*
 * @test
 * @summary jdk.jshell.Diag conformance: diagnostics of erroneous snippet.
 * @run main DiagTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class DiagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            ErroneousSnippet es = (ErroneousSnippet) js.eval("int = ;").get(0).snippet();
            List<Diag> ds = js.diagnostics(es).toList();
            check(!ds.isEmpty(), "has diagnostics");
            check(ds.get(0).isError(), "isError");
            check(ds.get(0).getCode() != null, "code");
            check(ds.get(0).getPosition() >= 0 || ds.get(0).getPosition() == Diag.NOPOS, "position");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
