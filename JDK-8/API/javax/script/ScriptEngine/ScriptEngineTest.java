/*
 * @test
 * @summary javax.script.ScriptEngine conformance: nashorn eval/put/get/bindings.
 * @run main ScriptEngineTest
 */


import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.ScriptContext;
public class ScriptEngineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.script.ScriptEngineManager mgr = new javax.script.ScriptEngineManager();
        javax.script.ScriptEngine e = mgr.getEngineByName("nashorn");
        check(e != null, "nashorn engine");
        check(((Number) e.eval("1+2")).intValue() == 3, "eval 1+2");
        e.put("x", 42);
        check(Integer.valueOf(42).equals(e.get("x")), "put/get");
        check(e.createBindings() != null, "createBindings");
        check(e.getContext() != null, "getContext");
        check(e.getFactory() != null, "getFactory");
        check(e.getBindings(javax.script.ScriptContext.ENGINE_SCOPE) != null, "getBindings");
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (JDK-8: real engine) ----------------------

    static void testDeepContracts() throws Exception {
        dSeRealEval();
        dSeBindings();
        dSeReaderForm();
        dSeConstants();
        System.out.println("deep: ScriptEngine contracts OK (Nashorn)");
    }

    static ScriptEngine nashorn() {
        ScriptEngine e = new ScriptEngineManager().getEngineByName("js");
        if (e == null) throw new AssertionError("Nashorn engine expected on this JDK");
        return e;
    }

    static void dSeRealEval() throws Exception {
        ScriptEngine e = nashorn();
        check(Integer.valueOf(3).equals(e.eval("1+2")), "int arithmetic -> Integer");
        check(Double.valueOf(20.5).equals(e.eval("41/2")), "division -> Double");
        check("ab".equals(e.eval("'a'+'b'")), "string concatenation");
        check(Boolean.TRUE.equals(e.eval("1<2")), "comparison -> Boolean");
        check(e.eval(";") == null, "empty statement -> null");
        check(e.eval("") == null, "empty script -> null");
        // state persists across eval calls (engine scope)
        e.eval("var carry = 40;");
        check(Double.valueOf(42.0).equals(e.eval("carry + 2")),
                "state persists across evals (var arithmetic boxes to Double)");
    }

    static void dSeBindings() throws Exception {
        ScriptEngine e = nashorn();
        e.put("k", "v");
        check("v".equals(e.get("k")), "put/get pair");
        check("v".equals(e.getBindings(ScriptContext.ENGINE_SCOPE).get("k")),
                "put is visible in engine bindings");
        // engine reads Java objects from bindings
        check("v!".equals(e.eval("k + '!'")), "script reads engine binding");
        // fresh bindings isolate evaluation
        Bindings b = e.createBindings();
        b.put("q", "Q");
        check("Q".equals(e.eval("q", b)), "eval with explicit bindings");
        boolean se = false;
        try { e.eval("nosuchvar.x", b); }
        catch (ScriptException x) { se = true; }
        check(se, "undefined reference -> ScriptException");
        // global (manager) scope is shared
        ScriptEngineManager mgr = new ScriptEngineManager();
        mgr.put("shared", "S");
        ScriptEngine e2 = mgr.getEngineByName("js");
        check("S".equals(e2.eval("shared")), "manager scope visible to engines");
        check(e2.getBindings(ScriptContext.GLOBAL_SCOPE) != null, "global bindings present");
    }

    static void dSeReaderForm() throws Exception {
        ScriptEngine e = nashorn();
        Object out = e.eval(new java.io.StringReader("7*6"));
        check(Integer.valueOf(42).equals(out), "Reader overload evaluates");
        check(e.getContext() != null, "default context present");
        check(e.getContext().getBindings(ScriptContext.ENGINE_SCOPE) != null,
                "context has engine bindings");
        // the engine reports its factory
        check(e.getFactory() != null, "factory non-null");
        check(e.getFactory().getNames().contains("js"), "factory knows its short name");
    }

    static void dSeConstants() {
        check("javax.script.engine".equals(ScriptEngine.ENGINE), "ENGINE key");
        check("javax.script.engine_version".equals(ScriptEngine.ENGINE_VERSION), "ENGINE_VERSION key");
        check("javax.script.name".equals(ScriptEngine.NAME), "NAME key");
        check("javax.script.language".equals(ScriptEngine.LANGUAGE), "LANGUAGE key");
        check("javax.script.language_version".equals(ScriptEngine.LANGUAGE_VERSION), "LANGUAGE_VERSION key");
        check("javax.script.argv".equals(ScriptEngine.ARGV), "ARGV key");
        check("javax.script.filename".equals(ScriptEngine.FILENAME), "FILENAME key");
    }

}
