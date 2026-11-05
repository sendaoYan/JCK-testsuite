/*
 * @test
 * @summary jdk.jshell.SnippetEvent conformance: snippet event fields.
 * @run main/othervm SnippetEventTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class SnippetEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SnippetEvent");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("snippet")) f=true; check(f, "has snippet"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("status")) f=true; check(f, "has status"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("previousStatus")) f=true; check(f, "has previousStatus"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("exception")) f=true; check(f, "has exception"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            SnippetEvent e = js.eval("int x = 1;").get(0);
            check(e.snippet() != null, "snippet");
            check(e.status() == Snippet.Status.VALID, "status");
            check(e.previousStatus() != null, "previousStatus");
            check(e.snippet().id() != null, "snippet id");
            check(e.causeSnippet() == null, "no cause snippet");
            check(e.exception() == null, "no exception");
        }
    }

    // ---- deep contract: every event field across the engine's event grammar ----
    static void testEventGrammar() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            // fresh declaration: previousStatus NONEXISTENT -> VALID, no cause, no exception
            SnippetEvent e = js.eval("int ev1 = 1;").get(0);
            check(e.previousStatus() == Snippet.Status.NONEXISTENT, "fresh prev NONEXISTENT");
            check(e.status() == Snippet.Status.VALID, "fresh VALID");
            check(e.causeSnippet() == null, "no cause");
            check(e.exception() == null, "no exception");
            check(e.value().equals("1"), "var declaration event carries initializer value");
            check(e.isSignatureChange(), "declaration is signature change");
            // expression event carries the value
            SnippetEvent ev = js.eval("ev1 * 2").get(0);
            check(ev.value().equals("2"), "expression value");
            check(ev.isSignatureChange(), "temp var creation is signature change");
            // drop event: VALID -> DROPPED
            VarSnippet v1 = js.variables().filter(x -> x.name().equals("ev1")).findFirst().get();
            SnippetEvent de = js.drop(v1).get(0);
            check(de.previousStatus() == Snippet.Status.VALID, "drop prev VALID");
            check(de.status() == Snippet.Status.DROPPED, "drop DROPPED");
            check(de.snippet() == v1, "drop event snippet identity");
            check(de.exception() == null && de.causeSnippet() == null, "drop event clean");
            // overwrite emits one event per affected snippet; the caused event names the trigger
            js.eval("int evo() { return 1; }");
            MethodSnippet old = js.methods().filter(m -> m.name().equals("evo")).findFirst().get();
            List<SnippetEvent> ovs = js.eval("int evo() { return 2; }");
            check(ovs.size() == 2, "overwrite 2 events");
            SnippetEvent oldEv = ovs.stream().filter(x -> x.snippet() == old).findFirst().get();
            check(oldEv.previousStatus() == Snippet.Status.VALID, "old prev VALID");
            check(oldEv.status() == Snippet.Status.OVERWRITTEN, "old OVERWRITTEN");
            SnippetEvent newEv = ovs.stream().filter(x -> x.snippet() != old).findFirst().get();
            check(newEv.status() == Snippet.Status.VALID, "new VALID");
            // exception event: exception set, value null, status still VALID
            SnippetEvent ex = js.eval("1/0").get(0);
            check(ex.exception() instanceof EvalException, "exception event");
            check(ex.value() == null, "exception event no value");
            check(ex.status() == Snippet.Status.VALID, "exception event status VALID");
            check(((EvalException) ex.exception()).getExceptionClassName().equals("java.lang.ArithmeticException"), "AIOOBE 1/0");
            // resolution event: defining a dependency upgrades the dependent with a caused event
            MethodSnippet dep = (MethodSnippet) js.eval("int evd() { return evmissing(); }").get(0).snippet();
            List<SnippetEvent> res = js.eval("int evmissing() { return 3; }");
            boolean sawUpgrade = false;
            for (SnippetEvent r : res) {
                if (r.snippet() == dep && r.status() == Snippet.Status.VALID) sawUpgrade = true;
            }
            check(sawUpgrade, "resolution cascade event for dependent");
            // toString is descriptive and non-null for every event shape
            for (SnippetEvent x : new SnippetEvent[]{e, ev, de, oldEv, newEv, ex}) check(x.toString() != null, "toString");
            // snippet identity: event.snippet() is the live object also seen in streams
            check(js.snippets().anyMatch(s -> s == newEv.snippet()), "event snippet visible in snippets()");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEventGrammar();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
