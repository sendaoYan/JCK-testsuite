/*
 * @test
 * @summary javax.script.ScriptEngineFactory conformance: white-box structure + black-box behavior.
 * @run main ScriptEngineFactoryTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.List;
import javax.script.ScriptEngine;

public class ScriptEngineFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.ScriptEngineFactory");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getEngineName"), "declares getEngineName");
        check(hasMethod(c, "getEngineVersion"), "declares getEngineVersion");
        check(hasMethod(c, "getExtensions"), "declares getExtensions");
        check(hasMethod(c, "getMimeTypes"), "declares getMimeTypes");
        check(hasMethod(c, "getNames"), "declares getNames");
    }

    static void testBlackBox() throws Exception {
        javax.script.ScriptEngineFactory f = new javax.script.ScriptEngineFactory() {
            public String getEngineName() { return "MyEngine"; }
            public String getEngineVersion() { return "1.0"; }
            public List<String> getExtensions() { return List.of("me", "mye"); }
            public List<String> getMimeTypes() { return List.of("application/x-myengine"); }
            public List<String> getNames() { return List.of("MyEngine", "myengine"); }
            public String getLanguageName() { return "MyLang"; }
            public String getLanguageVersion() { return "2.0"; }
            public Object getParameter(String key) {
                if (javax.script.ScriptEngine.ENGINE.equals(key)) return getEngineName();
                if (javax.script.ScriptEngine.ENGINE_VERSION.equals(key)) return getEngineVersion();
                if (javax.script.ScriptEngine.LANGUAGE.equals(key)) return getLanguageName();
                if (javax.script.ScriptEngine.LANGUAGE_VERSION.equals(key)) return getLanguageVersion();
                if (javax.script.ScriptEngine.NAME.equals(key)) return getNames().get(0);
                return null;
            }
            public String getMethodCallSyntax(String obj, String m, String... args) {
                return obj + "." + m + "(" + String.join(",", args) + ")";
            }
            public String getOutputStatement(String toDisplay) { return "print(" + toDisplay + ")"; }
            public String getProgram(String... statements) { return String.join(";\n", statements); }
            public javax.script.ScriptEngine getScriptEngine() { return new javax.script.AbstractScriptEngine() {
                public Object eval(String s, javax.script.ScriptContext ctx) { return "E"; }
                public Object eval(Reader r, javax.script.ScriptContext ctx) { return "E"; }
                public javax.script.Bindings createBindings() { return new javax.script.SimpleBindings(); }
                public javax.script.ScriptEngineFactory getFactory() { return null; }
            }; }
        };

        check("MyEngine".equals(f.getEngineName()), "getEngineName");
        check("1.0".equals(f.getEngineVersion()), "getEngineVersion");
        check(f.getExtensions().size() == 2 && "me".equals(f.getExtensions().get(0)), "getExtensions");
        check(f.getMimeTypes().size() == 1, "getMimeTypes");
        check(f.getNames().size() == 2 && "MyEngine".equals(f.getNames().get(0)), "getNames");
        check("MyLang".equals(f.getLanguageName()), "getLanguageName");
        check("2.0".equals(f.getLanguageVersion()), "getLanguageVersion");

        check("MyEngine".equals(f.getParameter(javax.script.ScriptEngine.ENGINE)), "getParameter ENGINE");
        check("1.0".equals(f.getParameter(javax.script.ScriptEngine.ENGINE_VERSION)), "getParameter ENGINE_VERSION");
        check("MyLang".equals(f.getParameter(javax.script.ScriptEngine.LANGUAGE)), "getParameter LANGUAGE");
        check("2.0".equals(f.getParameter(javax.script.ScriptEngine.LANGUAGE_VERSION)), "getParameter LANGUAGE_VERSION");
        check("MyEngine".equals(f.getParameter(javax.script.ScriptEngine.NAME)), "getParameter NAME");
        check(f.getParameter("unknown.key") == null, "getParameter unknown null");

        check("obj.method(a,b)".equals(f.getMethodCallSyntax("obj", "method", "a", "b")), "getMethodCallSyntax");
        check("print(x)".equals(f.getOutputStatement("x")), "getOutputStatement");
        check("a;\nb".equals(f.getProgram("a", "b")), "getProgram");
        check(f.getScriptEngine() != null, "getScriptEngine non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dSfSurface();
        dSfHandmade();
        System.out.println("deep: ScriptEngineFactory contracts OK");
    }

    static class DeepFactory implements ScriptEngineFactory {
        public String getEngineName() { return "DeepEngine"; }
        public String getEngineVersion() { return "1.2.3"; }
        public java.util.List<String> getExtensions() { return java.util.Arrays.asList("deepjs", "dj"); }
        public java.util.List<String> getMimeTypes() { return java.util.Arrays.asList("application/x-deepjs"); }
        public String getLanguageName() { return "DeepLang"; }
        public String getLanguageVersion() { return "0.9"; }
        public String getMethodCallSyntax(String obj, String m, String... args) {
            return obj + "->" + m + "(" + String.join(",", args) + ")";
        }
        public java.util.List<String> getNames() { return java.util.Arrays.asList("deepjs", "deep"); }
        public String getOutputStatement(String toDisplay) { return "echo(" + toDisplay + ")"; }
        public String getParameter(String key) {
            if (ScriptEngine.ENGINE.equals(key)) return getEngineName();
            if (ScriptEngine.ENGINE_VERSION.equals(key)) return getEngineVersion();
            if (ScriptEngine.NAME.equals(key)) return getNames().get(0);
            if (ScriptEngine.LANGUAGE.equals(key)) return getLanguageName();
            if (ScriptEngine.LANGUAGE_VERSION.equals(key)) return getLanguageVersion();
            return null;
        }
        public String getProgram(String... statements) { return String.join(";\n", statements); }
        public ScriptEngine getScriptEngine() { return new DeepEngine(); }
    }

    static class DeepEngine extends AbstractScriptEngine {
        public Object eval(java.io.Reader reader, ScriptContext ctx) throws ScriptException {
            return "READ";
        }
        public Object eval(String script, ScriptContext ctx) throws ScriptException {
            return "OK:" + script;
        }
        public Bindings createBindings() { return new SimpleBindings(); }
        public ScriptEngineFactory getFactory() { return new DeepFactory(); }
    }

    static void dSfSurface() throws Exception {
        check(ScriptEngineFactory.class.isInterface(), "is interface");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : ScriptEngineFactory.class.getMethods()) names.add(m.getName());
        for (String want : new String[] {"getEngineName", "getEngineVersion", "getExtensions",
                "getMimeTypes", "getNames", "getLanguageName", "getLanguageVersion",
                "getMethodCallSyntax", "getOutputStatement", "getParameter", "getProgram",
                "getScriptEngine"})
            check(names.contains(want), "declares " + want);
    }

    static void dSfHandmade() throws Exception {
        DeepFactory f = new DeepFactory();
        check("DeepEngine".equals(f.getEngineName()), "engine name");
        check(f.getNames().contains("deepjs"), "names list");
        check(f.getExtensions().contains("deepjs") && f.getExtensions().contains("dj"),
                "extensions list");
        check(f.getMimeTypes().contains("application/x-deepjs"), "mime types");
        check("DeepLang".equals(f.getLanguageName()), "language name");
        check("o->m(a,b)".equals(f.getMethodCallSyntax("o", "m", "a", "b")),
                "method call syntax");
        check("echo(x)".equals(f.getOutputStatement("x")), "output statement");
        check("a;\nb".equals(f.getProgram("a", "b")), "program joins statements");
        // getParameter resolves the standard keys
        check("DeepEngine".equals(f.getParameter(ScriptEngine.ENGINE)), "ENGINE key");
        check("1.2.3".equals(f.getParameter(ScriptEngine.ENGINE_VERSION)), "ENGINE_VERSION key");
        check("deepjs".equals(f.getParameter(ScriptEngine.NAME)), "NAME key");
        check("DeepLang".equals(f.getParameter(ScriptEngine.LANGUAGE)), "LANGUAGE key");
        check("0.9".equals(f.getParameter(ScriptEngine.LANGUAGE_VERSION)), "LANGUAGE_VERSION key");
        check(f.getParameter("THREADING") == null, "unknown key -> null by our policy");
        // produced engine round-trips to the factory
        ScriptEngine e = f.getScriptEngine();
        check(e != null, "engine produced");
        check("OK:x".equals(e.eval("x")), "engine evaluates");
        check(e.getFactory() != null, "engine has a factory");
        // argument order for getMethodCallSyntax: object, method, args
        check("x->f(1,2)".equals(f.getMethodCallSyntax("x", "f", "1", "2")), "syntax arg order");
        check(f.getMethodCallSyntax("x", "f").endsWith("()"), "no args form");
    }

}
