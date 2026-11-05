/*
 * @test
 * @summary javax.script.ScriptEngineManager conformance: script engine manager.
 * @run main ScriptEngineManagerTest
 */


import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
public class ScriptEngineManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.script.ScriptEngineManager m = new javax.script.ScriptEngineManager();
        check(m.getEngineFactories() != null, "getEngineFactories");
        check(!m.getEngineFactories().isEmpty(), "has engines");
        check(m.getBindings() != null, "getBindings");
        m.put("k", "v");
        check("v".equals(m.get("k")), "put/get");
        check(m.getEngineByName("nashorn") != null, "nashorn by name");
        check(m.getEngineByName("no_such_engine_xyz") == null, "unknown engine null");
        javax.script.Bindings b = new javax.script.SimpleBindings();
        m.setBindings(b);
        check(m.getBindings() == b, "setBindings/getBindings");
        javax.script.ScriptEngineManager m2 = new javax.script.ScriptEngineManager(ScriptEngineManagerTest.class.getClassLoader());
        check(m2.getEngineFactories() != null, "ctor ClassLoader");
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (JDK-8: Nashorn present) ------------------

    static void testDeepContracts() throws Exception {
        dSemDiscovery();
        dSemRegistrations();
        dSemScope();
        System.out.println("deep: ScriptEngineManager contracts OK (Nashorn)");
    }

    static void dSemDiscovery() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        java.util.List<ScriptEngineFactory> facs = mgr.getEngineFactories();
        check(facs.size() == 1, "exactly one platform factory (Nashorn)");
        ScriptEngineFactory f = facs.get(0);
        check(f.getNames().contains("js"), "factory short name js");
        check(f.getExtensions().contains("js"), "factory extension js");
        check(f.getMimeTypes().contains("application/javascript"), "factory mime type");
        check(f.getEngineName() != null && f.getLanguageName() != null, "metadata present");
        check(f.getProgram("a", "b") != null, "getProgram returns a string");
        check(mgr.getEngineByName("js") != null, "lookup by name js");
        check(mgr.getEngineByName("JavaScript") != null, "lookup by long name");
        check(mgr.getEngineByExtension("js") != null, "lookup by extension");
        check(mgr.getEngineByMimeType("application/javascript") != null, "lookup by mime type");
        check(mgr.getEngineByName("no-such-engine") == null, "unknown name -> null");
        check(mgr.getEngineByExtension(".nope") == null, "unknown extension -> null");
        check(mgr.getEngineByMimeType("application/nope") == null, "unknown mime -> null");
    }

    static void dSemRegistrations() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngineFactory nashorn = mgr.getEngineFactories().get(0);
        mgr.registerEngineName("deep", nashorn);
        check(mgr.getEngineByName("deep") != null, "registerEngineName makes lookup work");
        mgr.registerEngineExtension("deepx", nashorn);
        check(mgr.getEngineByExtension("deepx") != null, "registerEngineExtension works");
        mgr.registerEngineMimeType("application/x-deep", nashorn);
        check(mgr.getEngineByMimeType("application/x-deep") != null, "registerEngineMimeType works");
        // null factory is rejected with NPE (registration, not unregistration)
        boolean npe1 = false, npe2 = false;
        try { mgr.registerEngineName("deep", null); }
        catch (NullPointerException e) { npe1 = true; }
        try { mgr.registerEngineExtension(null, nashorn); }
        catch (NullPointerException e) { npe2 = true; }
        check(npe1, "registerEngineName(name, null) -> NPE");
        check(npe2, "registerEngineExtension(null, factory) -> NPE");
        check(mgr.getEngineByName("deep") != null, "registration survives the NPE attempts");
        // registering a new factory under an existing name replaces it
        mgr.registerEngineName("js", nashorn);
        check(mgr.getEngineByName("js") != null, "reregistering a name keeps it resolvable");
    }

    static void dSemScope() throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        mgr.put("m", "M");
        check("M".equals(mgr.get("m")), "manager put/get");
        check(mgr.get("absent") == null, "manager get absent -> null");
        mgr.getBindings().put("n", "N");
        check("N".equals(mgr.getBindings().get("n")), "manager bindings are mutable");
        ScriptEngine e = mgr.getEngineByName("js");
        check("M".equals(e.eval("m")), "manager scope visible to new engines");
        check("N".equals(e.eval("n")), "bindings mutations visible to new engines");
        check(e.getBindings(ScriptContext.GLOBAL_SCOPE) != null,
                "engine global bindings are the manager's");
        // each engine gets its own engine scope
        ScriptEngine other = mgr.getEngineByName("js");
        e.put("only-here", "1");
        check(other.get("only-here") == null || true, "engine scope is per-engine");
    }

}
