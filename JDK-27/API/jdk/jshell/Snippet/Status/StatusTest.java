/*
 * @test
 * @summary jdk.jshell.Snippet.Status conformance: snippet status enum.
 * @run main/othervm StatusTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class StatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testEnumTable() {
        check(Snippet.Status.VALID != null && Snippet.Status.REJECTED != null && Snippet.Status.DROPPED != null, "constants");
        check(Snippet.Status.RECOVERABLE_DEFINED != null && Snippet.Status.RECOVERABLE_NOT_DEFINED != null && Snippet.Status.NONEXISTENT != null && Snippet.Status.OVERWRITTEN != null, "more constants");
        check(Snippet.Status.values().length == 7, "7 constants");
        check(Snippet.Status.valueOf("VALID") == Snippet.Status.VALID, "valueOf");
        // exact declaration order (javadoc order)
        Snippet.Status[] st = Snippet.Status.values();
        check(st[0] == Snippet.Status.VALID, "order[0] VALID");
        check(st[1] == Snippet.Status.RECOVERABLE_DEFINED, "order[1] RECOVERABLE_DEFINED");
        check(st[2] == Snippet.Status.RECOVERABLE_NOT_DEFINED, "order[2] RECOVERABLE_NOT_DEFINED");
        check(st[3] == Snippet.Status.DROPPED, "order[3] DROPPED");
        check(st[4] == Snippet.Status.OVERWRITTEN, "order[4] OVERWRITTEN");
        check(st[5] == Snippet.Status.REJECTED, "order[5] REJECTED");
        check(st[6] == Snippet.Status.NONEXISTENT, "order[6] NONEXISTENT");
        // Enum mechanics: name/toString/valueOf round-trip for all
        Set<Snippet.Status> seen = EnumSet.noneOf(Snippet.Status.class);
        for (Snippet.Status s : st) {
            check(Snippet.Status.valueOf(s.toString()) == s, "toString->valueOf roundtrip " + s);
            seen.add(s);
        }
        check(seen.size() == 7, "distinct constants");
        try {
            Snippet.Status.valueOf("NOT_A_STATUS");
            check(false, "valueOf bogus must throw");
        } catch (IllegalArgumentException expected) { check(true, "valueOf bogus IAE"); }
    }

    // ---- deep contract: every status value is reachable through the real engine ----
    static void testStatusReachability() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            // VALID
            Snippet ok = js.eval("int stv = 1;").get(0).snippet();
            check(js.status(ok) == Snippet.Status.VALID, "VALID reachable");
            // RECOVERABLE_DEFINED: method body references undefined symbol
            MethodSnippet rec = (MethodSnippet) js.eval("int stm() { return stmLater(); }").get(0).snippet();
            check(js.status(rec) == Snippet.Status.RECOVERABLE_DEFINED, "RECOVERABLE_DEFINED reachable");
            // RECOVERABLE_NOT_DEFINED: declaration cannot be defined yet (undefined supertype)
            TypeDeclSnippet und = (TypeDeclSnippet) js.eval("class stCE extends UndefinedBase {}").get(0).snippet();
            check(js.status(und) == Snippet.Status.RECOVERABLE_NOT_DEFINED, "RECOVERABLE_NOT_DEFINED reachable");
            // dropping a dependency can also demote a VALID subclass to RECOVERABLE_NOT_DEFINED
            js.eval("class stCB {}");
            TypeDeclSnippet sub = (TypeDeclSnippet) js.eval("class stCD2 extends stCB {}").get(0).snippet();
            check(js.status(sub) == Snippet.Status.VALID, "subclass VALID with defined base");
            TypeDeclSnippet base = js.types().filter(t -> t.name().equals("stCB")).findFirst().get();
            js.drop(base);
            check(js.status(base) == Snippet.Status.DROPPED, "DROPPED reachable");
            check(js.status(sub) == Snippet.Status.RECOVERABLE_NOT_DEFINED, "subclass demoted to RECOVERABLE_NOT_DEFINED");
            // dependent *variable* stays VALID when its initializer dependency is dropped
            // (the already-compiled definition keeps running) — recorded engine contract
            js.eval("int stDep = 1;");
            VarSnippet dep = (VarSnippet) js.eval("int stUsesDep = stDep;").get(0).snippet();
            check(js.status(dep) == Snippet.Status.VALID, "dependent var initially VALID");
            VarSnippet stDepSn = js.variables().filter(v -> v.name().equals("stDep")).findFirst().get();
            js.drop(stDepSn);
            check(js.status(dep) == Snippet.Status.VALID, "dependent var stays VALID after drop (no cascade)");
            // OVERWRITTEN: redefine the method
            MethodSnippet ow = (MethodSnippet) js.eval("int stow() { return 1; }").get(0).snippet();
            js.eval("int stow() { return 2; }");
            check(js.status(ow) == Snippet.Status.OVERWRITTEN, "OVERWRITTEN reachable");
            // REJECTED
            Snippet bad = js.eval("int stbad = ;").get(0).snippet();
            check(js.status(bad) == Snippet.Status.REJECTED, "REJECTED reachable");
            // NONEXISTENT: never-seen snippet would be rejected by ownership; but the
            // documented NONEXISTENT state is observable as previousStatus of fresh snippets
            SnippetEvent fresh = js.eval("int stfresh = 1;").get(0);
            check(fresh.previousStatus() == Snippet.Status.NONEXISTENT, "NONEXISTENT as initial previousStatus");
            // status transitions are engine-tracked: resolving the dependency upgrades RECOVERABLE_DEFINED
            js.eval("int stmLater() { return 1; }");
            check(js.status(rec) == Snippet.Status.VALID, "RECOVERABLE_DEFINED -> VALID cascade");
            // defining the missing supertype upgrades RECOVERABLE_NOT_DEFINED
            js.eval("class UndefinedBase {}");
            check(js.status(und) == Snippet.Status.VALID, "RECOVERABLE_NOT_DEFINED -> VALID cascade");
        }
    }

    public static void main(String[] args) throws Exception {
        testEnumTable();
        testStatusReachability();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
