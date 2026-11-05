/*
 * @test
 * @summary javax.script.ScriptEngineManager conformance: white-box structure + black-box behavior.
 * @run main ScriptEngineManagerTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.List;

public class ScriptEngineManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class MinEngine extends javax.script.AbstractScriptEngine {
        public Object eval(String s, javax.script.ScriptContext ctx) { return "E"; }
        public Object eval(Reader r, javax.script.ScriptContext ctx) { return "E"; }
        public javax.script.Bindings createBindings() { return new javax.script.SimpleBindings(); }
        public javax.script.ScriptEngineFactory getFactory() { return null; }
    }

    static javax.script.ScriptEngineFactory makeFactory() {
        return new javax.script.ScriptEngineFactory() {
            public String getEngineName() { return "MyEngine"; }
            public String getEngineVersion() { return "1.0"; }
            public List<String> getExtensions() { return List.of("me"); }
            public List<String> getMimeTypes() { return List.of("application/x-me"); }
            public List<String> getNames() { return List.of("MyEngine", "myengine"); }
            public String getLanguageName() { return "MyLang"; }
            public String getLanguageVersion() { return "1.0"; }
            public Object getParameter(String key) { return null; }
            public String getMethodCallSyntax(String o, String m, String... a) { return o + "." + m + "()"; }
            public String getOutputStatement(String s) { return s; }
            public String getProgram(String... s) { return String.join("", s); }
            public javax.script.ScriptEngine getScriptEngine() { return new MinEngine(); }
        };
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.ScriptEngineManager");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "getEngineFactories"), "declares getEngineFactories");
        check(hasMethod(c, "getEngineByName"), "declares getEngineByName");
        check(hasMethod(c, "getEngineByExtension"), "declares getEngineByExtension");
        check(hasMethod(c, "getEngineByMimeType"), "declares getEngineByMimeType");
        check(hasMethod(c, "registerEngineName"), "declares registerEngineName");
    }

    static void testBlackBox() throws Exception {
        javax.script.ScriptEngineManager m = new javax.script.ScriptEngineManager();
        check(m.getEngineFactories() != null, "getEngineFactories non-null");
        check(m.getBindings() != null, "getBindings non-null");

        m.put("k", "v");
        check("v".equals(m.get("k")), "put/get");

        javax.script.Bindings b = new javax.script.SimpleBindings();
        m.setBindings(b);
        check(m.getBindings() == b, "setBindings/getBindings");

        check(m.getEngineByName("no_such_engine_xyz") == null, "unknown engine null");
        check(m.getEngineByExtension("no_such_ext_xyz") == null, "unknown extension null");
        check(m.getEngineByMimeType("no/such/mime/xyz") == null, "unknown mime type null");

        javax.script.ScriptEngineFactory f = makeFactory();
        m.registerEngineName("MyEngine", f);
        check(m.getEngineByName("MyEngine") != null, "registered name resolved");
        check(m.getEngineByName("MyEngine") instanceof javax.script.ScriptEngine, "registered engine type");

        m.registerEngineExtension("me", f);
        check(m.getEngineByExtension("me") != null, "registered extension resolved");

        m.registerEngineMimeType("application/x-me", f);
        check(m.getEngineByMimeType("application/x-me") != null, "registered mime type resolved");

        boolean npe1 = false;
        try { m.registerEngineName(null, f); } catch (NullPointerException e) { npe1 = true; }
        check(npe1, "registerEngineName null name NPE");
        boolean npe2 = false;
        try { m.registerEngineName("x", null); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "registerEngineName null factory NPE");

        javax.script.ScriptEngineManager m2 = new javax.script.ScriptEngineManager(
                ScriptEngineManagerTest.class.getClassLoader());
        check(m2.getEngineFactories() != null, "ctor ClassLoader factories non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (no platform engine on this JDK) ----------

    static void testDeepContracts() throws Exception {
        dSemEmptyWorld();
        dSemRegistration();
        dSemServiceDiscovery();
        System.out.println("deep: ScriptEngineManager contracts OK (self-made)");
    }

    public static class DeepFactory implements ScriptEngineFactory {
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

    static class DeepEngine extends AbstractScriptEngine {
        public Object eval(java.io.Reader r, ScriptContext ctx) throws ScriptException {
            StringBuilder sb = new StringBuilder();
            try { int c; while ((c = r.read()) >= 0) sb.append((char) c); }
            catch (java.io.IOException e) { throw new AssertionError(e); }
            return eval(sb.toString(), ctx);
        }
        public Object eval(String s, ScriptContext ctx) throws ScriptException { return "OK:" + s; }
        public Bindings createBindings() { return new SimpleBindings(); }
        public ScriptEngineFactory getFactory() { return new DeepFactory(); }
    }

    static void dSemEmptyWorld() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        check(mgr.getEngineFactories().isEmpty(), "no platform engine (removed from this JDK)");
        check(mgr.getEngineByName("js") == null, "js not resolvable");
        check(mgr.getEngineByName("nashorn") == null, "nashorn not resolvable");
        check(mgr.getEngineByExtension("js") == null, "extension js not resolvable");
        check(mgr.getEngineByMimeType("application/javascript") == null, "mime not resolvable");
        check(mgr.getBindings() != null, "manager bindings still exist");
    }

    static void dSemRegistration() throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        DeepFactory f = new DeepFactory();
        mgr.registerEngineName("deepjs", f);
        ScriptEngine e = mgr.getEngineByName("deepjs");
        check(e != null, "name registration resolves");
        check("OK:hi".equals(e.eval("hi")), "registered engine evaluates");
        // name registration alone does not cover extensions or mime types
        check(mgr.getEngineByExtension("deepjs") == null, "extension lookup unaffected");
        check(mgr.getEngineByMimeType("application/x-deepjs") == null, "mime lookup unaffected");
        mgr.registerEngineExtension("deepjs", f);
        check(mgr.getEngineByExtension("deepjs") != null, "extension registration resolves");
        mgr.registerEngineMimeType("application/x-deepjs", f);
        check(mgr.getEngineByMimeType("application/x-deepjs") != null, "mime registration resolves");
        // null factory rejected
        boolean npe = false;
        try { mgr.registerEngineName("x", null); }
        catch (NullPointerException ex) { npe = true; }
        check(npe, "registerEngineName(x, null) -> NPE");
        // manager scope still flows to engines
        mgr.put("m", "M");
        check("M".equals(mgr.get("m")), "manager put/get");
    }

    static void dSemServiceDiscovery() throws Exception {
        // runtime-generated META-INF/services + URLClassLoader discovery (cleaned up after)
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("deep-sem-svc");
        java.nio.file.Path svc = dir.resolve("META-INF/services/javax.script.ScriptEngineFactory");
        java.nio.file.Files.createDirectories(svc.getParent());
        java.nio.file.Files.write(svc, ("ScriptEngineManagerTest$DeepFactory\n").getBytes("UTF-8"));
        try {
            java.net.URL[] urls = { dir.toUri().toURL() };
            java.net.URLClassLoader loader =
                    new java.net.URLClassLoader(urls, ScriptEngineManagerTest.class.getClassLoader());
            try {
                ScriptEngineManager mgr = new ScriptEngineManager(loader);
                java.util.List<ScriptEngineFactory> facs = mgr.getEngineFactories();
                boolean found = false;
                for (ScriptEngineFactory f : facs)
                    if ("DeepEngine".equals(f.getEngineName())) found = true;
                check(found, "service file discovered through the given class loader");
                ScriptEngine e = mgr.getEngineByExtension("deepjs");
                check(e != null, "discovered factory resolvable by extension");
                check("OK:probe".equals(e.eval("probe")), "discovered engine evaluates");
                // discovery and explicit registration coexist
                mgr.registerEngineName("extra", new DeepFactory());
                check(mgr.getEngineByName("extra") != null
                        && mgr.getEngineByExtension("deepjs") != null, "both sources visible");
            } finally { loader.close(); }
        } finally {
            java.util.List<java.nio.file.Path> paths = new java.util.ArrayList<>();
            java.nio.file.Files.walk(dir).forEach(paths::add);
            java.util.Collections.reverse(paths);
            for (java.nio.file.Path p : paths) java.nio.file.Files.delete(p);
        }
        check(!java.nio.file.Files.exists(dir), "temp service dir removed");
        check(new ScriptEngineManager().getEngineFactories().isEmpty(),
                "fresh manager unaffected (no global pollution)");
    }

}
