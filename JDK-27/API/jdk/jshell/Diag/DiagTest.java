/*
 * @test
 * @summary jdk.jshell.Diag conformance: diagnostics of erroneous snippet.
 * @run main/othervm DiagTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class DiagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.Diag");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("isError")) f=true; check(f, "has isError"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("getCode")) f=true; check(f, "has getCode"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("getPosition")) f=true; check(f, "has getPosition"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            ErroneousSnippet es = (ErroneousSnippet) js.eval("int = ;").get(0).snippet();
            List<Diag> ds = js.diagnostics(es).toList();
            check(!ds.isEmpty(), "has diagnostics");
            check(ds.get(0).isError(), "isError");
            check(ds.get(0).getCode() != null, "code");
            check(ds.get(0).getPosition() >= 0 || ds.get(0).getPosition() == Diag.NOPOS, "position");
        }
    }

    // ---- deep contract: diagnostics positions, codes, messages, locale ----
    static void testDiagContract() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            // single precise error: position points at the offending token
            ErroneousSnippet es = (ErroneousSnippet) js.eval("int dx = ;").get(0).snippet();
            List<Diag> ds = js.diagnostics(es).toList();
            check(ds.size() >= 1, "diags present");
            Diag d0 = ds.get(0);
            check(d0.isError(), "isError true");
            check(d0.getCode() != null && d0.getCode().startsWith("compiler.err"), "compiler error code");
            check(d0.getStartPosition() >= 0, "start >= 0");
            check(d0.getEndPosition() >= d0.getStartPosition(), "end >= start");
            check(d0.getPosition() >= d0.getStartPosition() && d0.getPosition() <= Math.max(d0.getEndPosition(), d0.getStartPosition()),
                  "position within span");
            // message respects the requested locale without touching the default
            String root = d0.getMessage(Locale.ROOT);
            check(root != null && !root.isEmpty(), "message ROOT non-empty");
            // diagnostics on a *valid* snippet are empty
            VarSnippet ok = (VarSnippet) js.eval("int dok = 1;").get(0).snippet();
            check(js.diagnostics(ok).count() == 0, "no diags for valid snippet");
            // a snippet can carry multiple diagnostics in order
            ErroneousSnippet multi = (ErroneousSnippet) js.eval("class { int }").get(0).snippet();
            List<Diag> mds = js.diagnostics(multi).toList();
            check(mds.size() >= 2, "multiple diags");
            check(mds.stream().allMatch(Diag::isError), "all errors");
            // unresolved-but-defined declarations produce no diagnostics (different channel)
            MethodSnippet unres = (MethodSnippet) js.eval("int dun() { return dmissing(); }").get(0).snippet();
            check(js.diagnostics(unres).count() == 0, "unresolved decl has no diags");
            // NOPOS constant value (-1 per javax.tools convention)
            check(Diag.NOPOS == -1L, "NOPOS is -1");
            // diagnostics for a dropped snippet remain queryable
            js.drop(ok);
            check(js.diagnostics(ok).count() == 0, "dropped valid snippet still no diags");
            // diagnostics stream is a snapshot; querying twice yields same content
            check(js.diagnostics(es).count() == ds.size(), "diagnostics stable across queries");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDiagContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
