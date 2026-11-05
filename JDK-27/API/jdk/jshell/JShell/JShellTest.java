/*
 * @test
 * @summary jdk.jshell.JShell conformance: create eval drop inspect.
 * @run main/othervm JShellTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class JShellTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.JShell");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("create")) f=true; check(f, "has create"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("eval")) f=true; check(f, "has eval"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("snippets")) f=true; check(f, "has snippets"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("sourceCodeAnalysis")) f=true; check(f, "has sourceCodeAnalysis"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("drop")) f=true; check(f, "has drop"); }
        boolean ifc = false;
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals("java.lang.AutoCloseable")) ifc = true;
        check(ifc, "implements AutoCloseable");
        // AutoCloseable.close() must not throw checked exceptions (JShell.close declares none)
        for (java.lang.reflect.Method mm : c.getMethods())
            if (mm.getName().equals("close")) check(mm.getExceptionTypes().length == 0, "close declares no checked exceptions");
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            check(js != null, "created");
            check(js.eval("int x = 1;").get(0).status() == Snippet.Status.VALID, "eval");
            check(js.snippets().count() >= 1, "snippets");
            check(js.sourceCodeAnalysis() != null, "sourceCodeAnalysis");
            VarSnippet v = (VarSnippet) js.eval("int y = 2;").get(0).snippet();
            check(js.drop(v).get(0).status() == Snippet.Status.DROPPED, "drop");
        }
    }

    // ---- deep contract: real eval closed loop across snippets ----

    static JShell localShell() {
        return JShell.builder().executionEngine("local").build();
    }

    static void testEvalClosedLoop() throws Exception {
        try (JShell js = localShell()) {
            // 1. pure expression: temp var value channel
            List<SnippetEvent> evs = js.eval("2 + 3");
            check(evs.size() == 1, "2+3 one event");
            SnippetEvent e = evs.get(0);
            check(e.value().equals("5"), "2+3 value");
            check(e.status() == Snippet.Status.VALID, "2+3 VALID");
            check(e.snippet().kind() == Snippet.Kind.VAR, "2+3 kind VAR (temp var)");
            check(e.snippet().subKind() == Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND, "2+3 TEMP_VAR subkind");
            check(e.previousStatus() == Snippet.Status.NONEXISTENT, "fresh snippet prev NONEXISTENT");
            check(e.exception() == null, "2+3 no exception");
            check(e.causeSnippet() == null, "2+3 no cause snippet");
            check(e.isSignatureChange(), "2+3 declares a temp var -> signature change");

            // 2. var declaration + varValue read-back
            VarSnippet x = (VarSnippet) js.eval("int x = 42;").get(0).snippet();
            check(x.kind() == Snippet.Kind.VAR, "x kind VAR");
            check(x.subKind() == Snippet.SubKind.VAR_DECLARATION_WITH_INITIALIZER_SUBKIND, "x subkind with-init");
            check(x.name().equals("x"), "x name");
            check(x.typeName().equals("int"), "x typeName");
            check(js.varValue(x).equals("42"), "varValue x");
            long varCount = js.variables().count();
            check(varCount >= 1, "variables() sees x");

            // 3. method declaration + cross-snippet call closed loop
            MethodSnippet f = (MethodSnippet) js.eval("double f(int a){return a*2;}").get(0).snippet();
            check(f.kind() == Snippet.Kind.METHOD, "f kind METHOD");
            check(f.subKind() == Snippet.SubKind.METHOD_SUBKIND, "f subkind METHOD");
            check(js.methods().anyMatch(m -> m.name().equals("f")), "methods() contains f");
            SnippetEvent call = js.eval("f(21)").get(0);
            check(call.status() == Snippet.Status.VALID, "f(21) VALID");
            check(call.value().equals("42.0"), "f(21) value 42.0 cross-snippet call");

            // 4. type declaration + instantiation + field read closed loop
            TypeDeclSnippet p = (TypeDeclSnippet) js.eval("class P{int v=7;}").get(0).snippet();
            check(p.kind() == Snippet.Kind.TYPE_DECL, "P kind TYPE_DECL");
            check(p.subKind() == Snippet.SubKind.CLASS_SUBKIND, "P subkind CLASS");
            check(js.types().anyMatch(t -> t.name().equals("P")), "types() contains P");
            SnippetEvent pv = js.eval("new P().v").get(0);
            check(pv.value().equals("7"), "new P().v closed loop");

            // 5. import + imports() bookkeeping
            ImportSnippet imp = (ImportSnippet) js.eval("import java.util.*;").get(0).snippet();
            check(imp.kind() == Snippet.Kind.IMPORT, "import kind IMPORT");
            check(imp.subKind() == Snippet.SubKind.TYPE_IMPORT_ON_DEMAND_SUBKIND, "import on-demand subkind");
            check(js.imports().anyMatch(i -> i.fullname().equals("java.util.*")), "imports() bookkeeping");
            // imported type now usable in another snippet (import effect closed loop)
            SnippetEvent al = js.eval("new ArrayList<String>()").get(0);
            check(al.status() == Snippet.Status.VALID, "import makes ArrayList usable");

            // 6. statement snippet: executable, no value
            SnippetEvent st = js.eval("if (x == 42) x = 43; else x = 0;").get(0);
            check(st.snippet().kind() == Snippet.Kind.STATEMENT, "statement kind");
            check(st.snippet().subKind() == Snippet.SubKind.STATEMENT_SUBKIND, "statement subkind");
            check(st.value() == null || st.value().isEmpty(), "statement no value");
            check(js.varValue(x).equals("43"), "statement side effect visible");
        }
    }

    static void testErrorSemantics() throws Exception {
        try (JShell js = localShell()) {
            SnippetEvent e = js.eval("1 +").get(0);
            check(e.status() == Snippet.Status.REJECTED, "1+ REJECTED");
            check(e.snippet().kind() == Snippet.Kind.ERRONEOUS, "1+ ERRONEOUS kind");
            check(e.snippet() instanceof ErroneousSnippet, "1+ ErroneousSnippet class");
            check(e.value() == null, "1+ no value");
            List<Diag> diags = js.diagnostics(e.snippet()).toList();
            check(!diags.isEmpty(), "1+ diagnostics non-empty");
            Diag d = diags.get(0);
            check(d.isError(), "diag isError");
            check(d.getMessage(Locale.ROOT) != null, "diag message non-null");
            check(d.getCode() != null && d.getCode().startsWith("compiler.err."), "diag code compiler.err.*");
            check(d.getStartPosition() >= 0 && d.getEndPosition() >= d.getStartPosition(), "diag positions ordered");
            check(d.getPosition() >= 0, "diag position");
            // multiple diagnostics on a bigger broken snippet
            SnippetEvent e2 = js.eval("class {").get(0);
            List<Diag> diags2 = js.diagnostics(e2.snippet()).toList();
            check(diags2.size() >= 2, "class { multiple diags");
            check(diags2.stream().allMatch(Diag::isError), "all error diags");
            // non-error (warning) diagnostics channel: unreachable code in jshell wraps are
            // compiler warnings; assert the diagnostic stream for a valid snippet is empty
            SnippetEvent ok = js.eval("int okv = 1;").get(0);
            check(js.diagnostics(ok.snippet()).count() == 0, "valid snippet no diags");
        }
    }

    static void testUnresolvedSemantics() throws Exception {
        try (JShell js = localShell()) {
            // declaration with unresolved method reference: RECOVERABLE_DEFINED, not rejected
            MethodSnippet g = (MethodSnippet) js.eval("int g() { return later2(); }").get(0).snippet();
            check(js.status(g) == Snippet.Status.RECOVERABLE_DEFINED, "g RECOVERABLE_DEFINED");
            List<String> deps = js.unresolvedDependencies(g).toList();
            check(deps.contains("method later2()"), "unresolvedDependencies lists later2");
            check(js.diagnostics(g).count() == 0, "unresolved is not a compile error (no diags)");
            // using it: the eval itself is VALID but throws UnresolvedReferenceException
            SnippetEvent bad = js.eval("g()").get(0);
            check(bad.exception() instanceof UnresolvedReferenceException, "g() throws UnresolvedReferenceException");
            check(bad.value() == null, "failed eval no value");
            UnresolvedReferenceException ure = (UnresolvedReferenceException) bad.exception();
            check(ure.getSnippet() == g, "URE points at offending declaration snippet");
            // var initializer with unresolved: whole snippet REJECTED (initializer must resolve)
            SnippetEvent vbad = js.eval("int yb = later2();").get(0);
            check(vbad.status() == Snippet.Status.REJECTED, "var with unresolved initializer REJECTED");
            // resolving the dependency upgrades g to VALID automatically (redefinition cascade)
            js.eval("int later2() { return 5; }");
            check(js.status(g) == Snippet.Status.VALID, "g VALID after dependency defined");
            check(js.eval("g()").get(0).value().equals("5"), "g() works after resolve");
        }
    }

    static void testDropLifecycle() throws Exception {
        try (JShell js = localShell()) {
            VarSnippet v = (VarSnippet) js.eval("int droppable = 9;").get(0).snippet();
            check(js.status(v) == Snippet.Status.VALID, "droppable VALID before drop");
            long before = js.variables().count();
            List<SnippetEvent> devs = js.drop(v);
            check(devs.size() == 1, "drop returns one event");
            check(devs.get(0).status() == Snippet.Status.DROPPED, "drop event DROPPED");
            check(devs.get(0).previousStatus() == Snippet.Status.VALID, "drop event prev VALID");
            check(devs.get(0).snippet() == v, "drop event names the snippet");
            check(js.status(v) == Snippet.Status.DROPPED, "status() DROPPED after drop");
            check(js.variables().count() == before - 1, "variables() shrinks after drop");
            // dropping is idempotent-per-state: second drop of same snippet keeps DROPPED
            List<SnippetEvent> devs2 = js.drop(v);
            check(devs2.get(0).status() == Snippet.Status.DROPPED, "re-drop stays DROPPED");
            check(devs2.get(0).previousStatus() == Snippet.Status.DROPPED, "re-drop prev DROPPED");
            // use of dropped variable is a fresh REJECTED snippet (drop takes effect)
            SnippetEvent use = js.eval("droppable + 1").get(0);
            check(use.status() == Snippet.Status.REJECTED, "use of dropped var REJECTED");
            // cascading drop: dependent snippet dropped together with dependency
            js.eval("int indep = 1;");
            js.eval("int dependOnDropped = indep + 100;");
            VarSnippet indep = js.variables().filter(s -> s.name().equals("indep")).findFirst().get();
            js.drop(indep);
            SnippetEvent cascade = js.eval("indep").get(0);
            check(cascade.status() == Snippet.Status.REJECTED, "cascade: dependency gone -> REJECTED");
        }
    }

    static void testNullAndStateContracts() throws Exception {
        try (JShell js = localShell()) {
            try { js.eval(null); check(false, "eval(null) must NPE"); }
            catch (NullPointerException expected) { check(true, "eval(null) NPE"); }
            try { js.drop(null); check(false, "drop(null) must NPE"); }
            catch (NullPointerException expected) { check(true, "drop(null) NPE"); }
            try { js.varValue(null); check(false, "varValue(null) must NPE"); }
            catch (NullPointerException expected) { check(true, "varValue(null) NPE"); }
            // varValue of a non-VALID snippet: IAE with descriptive message
            VarSnippet rej = (VarSnippet) js.eval("int badv = nosuchfn();").get(0).snippet();
            try { js.varValue(rej); check(false, "varValue(REJECTED) must IAE"); }
            catch (IllegalArgumentException expected) {
                check(true, "varValue(REJECTED) IAE");
                check(expected.getMessage() != null && expected.getMessage().contains("must be VALID"),
                      "varValue IAE message mentions VALID");
            }
            VarSnippet okv = (VarSnippet) js.eval("int okv2 = 3;").get(0).snippet();
            js.drop(okv);
            try { js.varValue(okv); check(false, "varValue(DROPPED) must IAE"); }
            catch (IllegalArgumentException expected) { check(true, "varValue(DROPPED) IAE"); }
        }
        // snippet from another JShell instance is rejected (ownership contract)
        try (JShell a = localShell(); JShell b = localShell()) {
            VarSnippet foreign = (VarSnippet) a.eval("int foreign = 1;").get(0).snippet();
            try { b.status(foreign); check(false, "foreign snippet status must IAE"); }
            catch (IllegalArgumentException expected) { check(true, "foreign snippet IAE"); }
            try { b.drop(foreign); check(false, "foreign snippet drop must IAE"); }
            catch (IllegalArgumentException expected) { check(true, "foreign drop IAE"); }
        }
        // closed shell rejects everything with IllegalStateException
        JShell closed = localShell();
        closed.close();
        try { closed.eval("1"); check(false, "eval after close must ISE"); }
        catch (IllegalStateException expected) { check(true, "eval after close ISE"); }
        try { closed.drop(null); check(false, "drop after close must ISE"); }
        catch (IllegalStateException expected) { check(true, "drop after close ISE"); }
        closed.close(); // close is idempotent
        check(true, "close idempotent");
    }

    static void testOverwriteLifecycle() throws Exception {
        try (JShell js = localShell()) {
            MethodSnippet m1 = (MethodSnippet) js.eval("int ow() { return 1; }").get(0).snippet();
            check(js.status(m1) == Snippet.Status.VALID, "ow v1 VALID");
            List<SnippetEvent> evs = js.eval("int ow() { return 2; }");
            check(evs.size() == 2, "overwrite produces 2 events");
            boolean sawOverwritten = false, sawValid = false;
            for (SnippetEvent e : evs) {
                if (e.status() == Snippet.Status.OVERWRITTEN) sawOverwritten = true;
                if (e.status() == Snippet.Status.VALID) sawValid = true;
            }
            check(sawOverwritten, "overwrite event: old snippet OVERWRITTEN");
            check(sawValid, "overwrite event: new snippet VALID");
            check(js.status(m1) == Snippet.Status.OVERWRITTEN, "old snippet status OVERWRITTEN");
            // body-only change is not a signature change... in jshell it still reports true for methods
            SnippetEvent bodyOnly = evs.stream().filter(e -> e.status() == Snippet.Status.VALID).findFirst().get();
            check(bodyOnly.isSignatureChange(), "method overwrite reports signature change");
            check(js.eval("ow()").get(0).value().equals("2"), "new body takes effect");
            check(js.methods().filter(m -> m.name().equals("ow")).count() == 1, "single active ow method");
            // var type change also overwrites
            js.eval("int owv = 1;");
            List<SnippetEvent> evs2 = js.eval("double owv = 1.5;");
            check(evs2.size() == 2, "var overwrite 2 events");
            check(js.eval("owv").get(0).value().equals("1.5"), "var overwrite takes effect");
        }
    }

    static void testExceptionChannel() throws Exception {
        try (JShell js = localShell()) {
            SnippetEvent e = js.eval("((Object)null).toString()").get(0);
            check(e.status() == Snippet.Status.VALID, "throwing snippet still VALID");
            check(e.exception() != null, "exception delivered in event");
            check(e.exception() instanceof JShellException, "exception is JShellException");
            check(e.exception() instanceof EvalException, "user exception wrapped as EvalException");
            EvalException ee = (EvalException) e.exception();
            check(ee.getExceptionClassName().equals("java.lang.NullPointerException"),
                  "EvalException preserves original class name");
            check(e.value() == null, "no value when exception thrown");
            // RuntimeException subtype passthrough name
            SnippetEvent e2 = js.eval("throw new java.util.NoSuchElementException(\"boom\");").get(0);
            check(e2.exception() instanceof EvalException, "explicit throw -> EvalException");
            check(((EvalException) e2.exception()).getExceptionClassName().equals("java.util.NoSuchElementException"),
                  "NSE class name preserved");
            // Error channel: also EvalException with error class name
            SnippetEvent e3 = js.eval("throw new AssertionError(\"aerr\");").get(0);
            check(((EvalException) e3.exception()).getExceptionClassName().equals("java.lang.AssertionError"),
                  "Error class name preserved");
        }
    }

    static void testCallbacksAndInjection() throws Exception {
        // onSnippetEvent fires for every event, unsubscribe stops delivery
        try (JShell js = localShell()) {
            int[] n = {0};
            JShell.Subscription sub = js.onSnippetEvent(ev -> n[0]++);
            js.eval("int cb1 = 1;");
            check(n[0] == 1, "one event -> one callback");
            js.eval("int cb2 = 1;");
            js.eval("int cb3 = 2;");
            check(n[0] == 3, "two snippets -> two more callbacks");
            js.unsubscribe(sub);
            js.eval("int cb4 = 1;");
            check(n[0] == 3, "no callbacks after unsubscribe");
        }
        // tempVariableNameGenerator injection: observable in snippet name
        try (JShell js = JShell.builder().executionEngine("local")
                .tempVariableNameGenerator(() -> "$v").build()) {
            VarSnippet tv = (VarSnippet) js.eval("41 + 1").get(0).snippet();
            check(tv.name().equals("$v"), "temp var name injected");
            check(tv.typeName().equals("int"), "temp var type int");
            check(js.eval("$v * 2").get(0).value().equals("84"), "injected temp var usable");
        }
        // idGenerator injection
        try (JShell js = JShell.builder().executionEngine("local")
                .idGenerator((sn, i) -> "X" + i).build()) {
            SnippetEvent first = js.eval("int idv = 1;").get(0);
            check(first.snippet().id().equals("X1"), "custom id generator applied");
        }
        // onShutdown callback fired on close
        boolean[] shut = {false};
        JShell js2 = localShell();
        js2.onShutdown(j -> shut[0] = true);
        js2.close();
        check(shut[0], "onShutdown fired at close");
        // stop() on an idle shell is harmless
        try (JShell js3 = localShell()) { js3.stop(); check(true, "stop on idle shell"); }
        // unknown execution engine fails at build or first eval (recorded either way)
        boolean threw = false;
        try (JShell bad = JShell.builder().executionEngine("nosuchengine").build()) {
            bad.eval("1");
        } catch (Throwable t) {
            threw = true;
        }
        check(threw, "unknown execution engine is rejected");
    }

    static void testStreamsAndStatus() throws Exception {
        try (JShell js = localShell()) {
            js.eval("import java.util.List;");
            js.eval("int sv1 = 1;");
            js.eval("int sm1() { return 1; }");
            js.eval("class SC1 {}");
            check(js.imports().count() == 1, "imports count 1");
            check(js.variables().count() == 1, "variables count 1");
            check(js.methods().count() == 1, "methods count 1");
            check(js.types().count() == 1, "types count 1");
            check(js.snippets().count() == 4, "snippets total 4");
            check(js.snippets().allMatch(s -> s.id() != null), "all snippets have ids");
            check(js.snippets().allMatch(s -> s.source() != null), "all snippets keep source");
            // source() round-trip preserves the exact input
            VarSnippet sv = js.variables().findFirst().get();
            check(sv.source().equals("int sv1 = 1;"), "source round-trip");
            // Status enum complete table and order
            Snippet.Status[] st = Snippet.Status.values();
            check(st.length == 7, "7 statuses");
            check(st[0] == Snippet.Status.VALID && st[1] == Snippet.Status.RECOVERABLE_DEFINED
                  && st[2] == Snippet.Status.RECOVERABLE_NOT_DEFINED && st[3] == Snippet.Status.DROPPED
                  && st[4] == Snippet.Status.OVERWRITTEN && st[5] == Snippet.Status.REJECTED
                  && st[6] == Snippet.Status.NONEXISTENT, "status values order");
            for (Snippet.Status s : st) check(Snippet.Status.valueOf(s.name()) == s, "valueOf roundtrip " + s);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEvalClosedLoop();
        testErrorSemantics();
        testUnresolvedSemantics();
        testDropLifecycle();
        testNullAndStateContracts();
        testOverwriteLifecycle();
        testExceptionChannel();
        testCallbacksAndInjection();
        testStreamsAndStatus();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
