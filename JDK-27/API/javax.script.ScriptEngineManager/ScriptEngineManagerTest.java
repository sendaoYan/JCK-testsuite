/*
 * @test
 * @summary javax.script.ScriptEngineManager conformance: script engine manager.
 * @run main ScriptEngineManagerTest
 */

public class ScriptEngineManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.script.ScriptEngineManager m = new javax.script.ScriptEngineManager();
        check(m.getEngineFactories() != null, "getEngineFactories");
        check(m.getBindings() != null, "getBindings");
        m.put("k", "v");
        check("v".equals(m.get("k")), "put/get");
        check(m.getEngineByName("no_such_engine_xyz") == null, "unknown engine null");
        javax.script.Bindings b = new javax.script.SimpleBindings();
        m.setBindings(b);
        check(m.getBindings() == b, "setBindings/getBindings");
        javax.script.ScriptEngineManager m2 = new javax.script.ScriptEngineManager(ScriptEngineManagerTest.class.getClassLoader());
        check(m2.getEngineFactories() != null, "ctor ClassLoader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
