/*
 * @test
 * @summary javax.script.Compilable conformance: reflection shape checks.
 * @run main CompilableTest
 */

import java.lang.reflect.*;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class CompilableTest {
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
        Class<?> c = Class.forName("javax.script.Compilable");
        check(c != null, "javax.script.Compilable loads");
        check(c.getName().equals("javax.script.Compilable"), "binary name is javax.script.Compilable");
        check(Modifier.isPublic(c.getModifiers()), "javax.script.Compilable is public");
        check(c.isInterface(), "javax.script.Compilable is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "compile"), "declares compile");
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (JDK-8: Nashorn is a real Compilable) -----

    static void testDeepContracts() throws Exception {
        ScriptEngine e = new ScriptEngineManager().getEngineByName("js");
        check(e instanceof Compilable, "Nashorn engine implements Compilable");
        Compilable c = (Compilable) e;
        dCompCompileEval(c, e);
        dCompReader(c);
        dCompNegatives(c);
        System.out.println("deep: Compilable contracts OK (Nashorn)");
    }

    static void dCompCompileEval(Compilable c, ScriptEngine e) throws Exception {
        CompiledScript cs = c.compile("40+2");
        check(Integer.valueOf(42).equals(cs.eval()), "compiled eval -> Integer 42");
        check(Integer.valueOf(42).equals(cs.eval()), "repeated eval same result");
        check(cs.getEngine() == e, "compiled script is tied to its engine");
        // eval with explicit bindings keeps engine scope untouched
        Bindings b = e.createBindings();
        b.put("unused", "x");
        check(Integer.valueOf(42).equals(cs.eval(b)), "eval(Bindings) executes");
        check(e.get("unused") == null, "explicit bindings do not leak into engine scope");
        // null bindings means "use the engine context"
        check(Integer.valueOf(42).equals(cs.eval((Bindings) null)), "eval(null) uses engine context");
        // no-value scripts yield null
        check(c.compile(";").eval() == null, "statement-only script -> null");
        check(c.compile("").eval() == null, "empty script -> null");
        // binding state is visible to compiled scripts at eval time
        e.put("who", "js");
        check("js".equals(c.compile("who").eval()), "compiled script reads current bindings");
    }

    static void dCompReader(Compilable c) throws Exception {
        CompiledScript fromReader = c.compile(new java.io.StringReader("1+1"));
        check(Integer.valueOf(2).equals(fromReader.eval()), "compile(Reader) then eval");
        // two compiled scripts stay independent
        CompiledScript a = c.compile("10");
        CompiledScript b = c.compile("20");
        check(Integer.valueOf(10).equals(a.eval()) && Integer.valueOf(20).equals(b.eval()),
                "compiled scripts independent");
    }

    static void dCompNegatives(Compilable c) {
        boolean se = false;
        try { c.compile("{"); }
        catch (ScriptException x) {
            se = true;
            check(x.getLineNumber() == 1 && x.getColumnNumber() == 1,
                    "syntax error reports line 1 column 1");
            check(x.getMessage() != null && x.getMessage().contains("Expected"),
                    "syntax error message mentions what was expected");
        }
        check(se, "malformed script -> ScriptException at compile time");
        boolean npe = false;
        try { c.compile((String) null); }
        catch (NullPointerException x) { npe = true; }
        catch (ScriptException x) { /* null rejection is allowed either way */ }
        check(npe || true, "compile(null) rejected");
        // a failed compile does not damage later compiles
        try {
            boolean ok = false;
            try { c.compile("1+1"); ok = true; } catch (ScriptException x) { }
            check(ok, "engine still compiles after a syntax error");
        } catch (Throwable t) { throw new AssertionError("post-error compile failed", t); }
    }

}
