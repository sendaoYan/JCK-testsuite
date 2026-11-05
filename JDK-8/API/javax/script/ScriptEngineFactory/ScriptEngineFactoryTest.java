/*
 * @test
 * @summary javax.script.ScriptEngineFactory conformance: reflection shape checks.
 * @run main ScriptEngineFactoryTest
 */

import java.lang.reflect.*;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class ScriptEngineFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.script.ScriptEngineFactory");
        check(c != null, "javax.script.ScriptEngineFactory loads");
        check(c.getName().equals("javax.script.ScriptEngineFactory"), "binary name is javax.script.ScriptEngineFactory");
        check(Modifier.isPublic(c.getModifiers()), "javax.script.ScriptEngineFactory is public");
        check(c.isInterface(), "javax.script.ScriptEngineFactory is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getEngineName"), "declares getEngineName");
        check(declares(c, "getEngineVersion"), "declares getEngineVersion");
        check(declares(c, "getExtensions"), "declares getExtensions");
        check(declares(c, "getMimeTypes"), "declares getMimeTypes");
        check(declares(c, "getNames"), "declares getNames");
        check(declares(c, "getLanguageName"), "declares getLanguageName");
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
