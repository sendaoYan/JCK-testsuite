/*
 * @test
 * @summary javax.script.CompiledScript conformance: reflection shape checks.
 * @run main CompiledScriptTest
 */

import java.lang.reflect.*;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class CompiledScriptTest {
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
        Class<?> c = Class.forName("javax.script.CompiledScript");
        check(c != null, "javax.script.CompiledScript loads");
        check(c.getName().equals("javax.script.CompiledScript"), "binary name is javax.script.CompiledScript");
        check(Modifier.isPublic(c.getModifiers()), "javax.script.CompiledScript is public");
        check(!c.isInterface(), "javax.script.CompiledScript is not an interface");
        check(Modifier.isAbstract(c.getModifiers()), "javax.script.CompiledScript is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (JDK-8: Nashorn compiled scripts) ---------

    static void testDeepContracts() throws Exception {
        ScriptEngine e = new ScriptEngineManager().getEngineByName("js");
        Compilable c = (Compilable) e;
        dCsEvaluation(c, e);
        dCsContextSharing(c, e);
        System.out.println("deep: CompiledScript contracts OK (Nashorn)");
    }

    static void dCsEvaluation(Compilable c, ScriptEngine e) throws Exception {
        CompiledScript cs = c.compile("6*7");
        check(Integer.valueOf(42).equals(cs.eval()), "eval() executes");
        check(cs.getEngine() == e, "getEngine is the creating engine");
        // eval() and eval(engine context) are equivalent
        check(Integer.valueOf(42).equals(cs.eval(e.getContext())),
                "eval(engineContext) same as eval()");
        // eval(null bindings) documented as equal to eval(engine context)
        check(Integer.valueOf(42).equals(cs.eval((Bindings) null)),
                "eval(null Bindings) uses engine context");
        Bindings b = e.createBindings();
        check(Integer.valueOf(42).equals(cs.eval(b)), "eval(Bindings) executes");
        // the engine keeps working through its own eval path afterwards
        check(Integer.valueOf(3).equals(e.eval("1+2")), "engine unaffected by compiled use");
    }

    static void dCsContextSharing(Compilable c, ScriptEngine e) throws Exception {
        // state written by a compiled script is visible to later engine evals
        CompiledScript setter = c.compile("var csState = 41");
        setter.eval();
        check(Double.valueOf(42.0).equals(e.eval("csState + 1")),
                "compiled writes are visible to the engine");
        // state written through the engine is visible to a compiled script
        e.eval("var engState = 'e'");
        CompiledScript getter = c.compile("engState + '!'");
        check("e!".equals(getter.eval()), "engine writes visible to compiled scripts");
        // a value returned through bindings survives across compiled evals
        Bindings b = e.createBindings();
        b.put("k", "K");
        CompiledScript reader = c.compile("k");
        check("K".equals(reader.eval(b)), "compiled script reads supplied bindings");
        check(b.get("k").equals("K"), "supplied bindings unchanged");
    }

}
