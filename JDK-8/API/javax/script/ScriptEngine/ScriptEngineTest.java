/*
 * @test
 * @summary javax.script.ScriptEngine conformance: nashorn eval/put/get/bindings.
 * @run main ScriptEngineTest
 */

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
