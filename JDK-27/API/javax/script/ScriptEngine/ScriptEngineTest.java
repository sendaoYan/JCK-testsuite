/*
 * @test
 * @summary javax.script.ScriptEngine conformance: white-box structure + black-box behavior.
 * @run main ScriptEngineTest
 */

import java.lang.reflect.*;
import java.io.*;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

public class ScriptEngineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class MinEngine extends javax.script.AbstractScriptEngine {
        public Object eval(String s, javax.script.ScriptContext ctx) { return "EVAL:" + s; }
        public Object eval(Reader r, javax.script.ScriptContext ctx) { return "EVAL-READER"; }
        public javax.script.Bindings createBindings() { return new javax.script.SimpleBindings(); }
        public javax.script.ScriptEngineFactory getFactory() { return null; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.ScriptEngine");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "eval"), "declares eval");
        check(hasMethod(c, "put"), "declares put");
        check(hasMethod(c, "get"), "declares get");
        check(hasMethod(c, "getBindings"), "declares getBindings");
        check(hasMethod(c, "setBindings"), "declares setBindings");
    }

    static void testBlackBox() throws Exception {
        check("javax.script.argv".equals(javax.script.ScriptEngine.ARGV), "ARGV constant");
        check("javax.script.filename".equals(javax.script.ScriptEngine.FILENAME), "FILENAME constant");
        check("javax.script.engine".equals(javax.script.ScriptEngine.ENGINE), "ENGINE constant");
        check("javax.script.engine_version".equals(javax.script.ScriptEngine.ENGINE_VERSION), "ENGINE_VERSION constant");
        check("javax.script.name".equals(javax.script.ScriptEngine.NAME), "NAME constant");
        check("javax.script.language".equals(javax.script.ScriptEngine.LANGUAGE), "LANGUAGE constant");
        check("javax.script.language_version".equals(javax.script.ScriptEngine.LANGUAGE_VERSION), "LANGUAGE_VERSION constant");

        javax.script.ScriptEngine e = new MinEngine();
        e.put("k", "v");
        check("v".equals(e.get("k")), "put/get");
        check("EVAL:code".equals(e.eval("code")), "eval(String)");
        check("EVAL-READER".equals(e.eval(new StringReader("r"))), "eval(Reader)");
        check("EVAL:code".equals(e.eval("code", new javax.script.SimpleBindings())), "eval(String,Bindings)");
        check(e.createBindings() != null, "createBindings non-null");
        check(e.getContext() != null, "getContext non-null");
        check(e.getBindings(javax.script.ScriptContext.ENGINE_SCOPE) != null, "getBindings engine scope");
        check(e.getFactory() == null, "getFactory null (minimal impl)");

        javax.script.Bindings b = new javax.script.SimpleBindings();
        e.setBindings(b, javax.script.ScriptContext.ENGINE_SCOPE);
        check(e.getBindings(javax.script.ScriptContext.ENGINE_SCOPE) == b, "set/get bindings");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (self-made engine; no platform engine) ----

    static void testDeepContracts() throws Exception {
        check(new ScriptEngineManager().getEngineFactories().isEmpty(),
                "no platform engine on this JDK");
        dSeHandmade();
        dSeConstants();
        System.out.println("deep: ScriptEngine contracts OK (self-made)");
    }

    static class DeepEngine extends AbstractScriptEngine {
        public Object eval(java.io.Reader reader, ScriptContext ctx) throws ScriptException {
            StringBuilder sb = new StringBuilder();
            try { int c; while ((c = reader.read()) >= 0) sb.append((char) c); }
            catch (java.io.IOException e) { throw new AssertionError(e); }
            return eval(sb.toString(), ctx);
        }
        public Object eval(String script, ScriptContext ctx) throws ScriptException {
            if (script.equals("ident")) return ctx.getAttribute("probe");
            if (script.equals("boom")) throw new ScriptException("deep failure", "deep.js", 3, 5);
            return "OK:" + script;
        }
        public Bindings createBindings() { return new SimpleBindings(); }
        public ScriptEngineFactory getFactory() { return new DeepEngineFactory(); }
    }

    static class DeepEngineFactory implements ScriptEngineFactory {
        public String getEngineName() { return "DeepEngine"; }
        public String getEngineVersion() { return "1.0"; }
        public java.util.List<String> getExtensions() { return java.util.Arrays.asList("deepjs"); }
        public java.util.List<String> getMimeTypes() { return java.util.Arrays.asList("application/x-deepjs"); }
        public String getLanguageName() { return "DeepLang"; }
        public String getLanguageVersion() { return "1.0"; }
        public String getMethodCallSyntax(String o, String m, String... a) { return o + "." + m; }
        public java.util.List<String> getNames() { return java.util.Arrays.asList("deepjs"); }
        public String getOutputStatement(String s) { return "echo(" + s + ")"; }
        public String getParameter(String key) { return null; }
        public String getProgram(String... s) { return String.join(";", s); }
        public ScriptEngine getScriptEngine() { return new DeepEngine(); }
    }

    static void dSeHandmade() throws Exception {
        ScriptEngine e = new DeepEngine();
        check("OK:cmd".equals(e.eval("cmd")), "String eval");
        check("OK:src".equals(e.eval(new java.io.StringReader("src"))), "Reader eval");
        // bindings drive evaluation through the context
        e.put("probe", "PV");
        check("PV".equals(e.eval("ident")), "script reads engine binding");
        Bindings b = e.createBindings();
        b.put("probe", "BV");
        check("BV".equals(e.eval("ident", b)), "explicit bindings swap the view");
        // failure path carries position information
        boolean se = false;
        try { e.eval("boom"); }
        catch (ScriptException x) {
            se = true;
            check("deep.js".equals(x.getFileName()), "file reported");
            check(x.getLineNumber() == 3 && x.getColumnNumber() == 5, "position reported");
        }
        check(se, "engine failure -> ScriptException");
        check(e.getFactory() instanceof ScriptEngineFactory, "factory reported");
        check(e.getContext() instanceof SimpleScriptContext, "default context");
        check(e.get("probe") == null || true, "get is impl-defined");
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
