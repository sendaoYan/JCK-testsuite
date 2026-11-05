/*
 * @test
 * @summary javax.script.Invocable conformance: reflection shape checks.
 * @run main InvocableTest
 */

import java.lang.reflect.*;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class InvocableTest {
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
        Class<?> c = Class.forName("javax.script.Invocable");
        check(c != null, "javax.script.Invocable loads");
        check(c.getName().equals("javax.script.Invocable"), "binary name is javax.script.Invocable");
        check(Modifier.isPublic(c.getModifiers()), "javax.script.Invocable is public");
        check(c.isInterface(), "javax.script.Invocable is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "invokeMethod"), "declares invokeMethod");
        check(declares(c, "invokeFunction"), "declares invokeFunction");
        check(declares(c, "getInterface"), "declares getInterface");
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (JDK-8: Nashorn is a real Invocable) ------

    static void testDeepContracts() throws Exception {
        ScriptEngine e = new ScriptEngineManager().getEngineByName("js");
        check(e instanceof Invocable, "Nashorn engine implements Invocable");
        Invocable inv = (Invocable) e;
        dInvFunctions(e, inv);
        dInvMethods(e, inv);
        dInvInterfaces(e, inv);
        System.out.println("deep: Invocable contracts OK (Nashorn)");
    }

    static void dInvFunctions(ScriptEngine e, Invocable inv) throws Exception {
        e.eval("function add(a,b){ return a+b }");
        Object r = inv.invokeFunction("add", 3, 4);
        check(r instanceof Double && ((Double) r).doubleValue() == 7.0,
                "invokeFunction numeric result is Double 7.0");
        check("xy".equals(inv.invokeFunction("add", "x", "y")),
                "invokeFunction adapts to argument types");
        boolean nsme = false;
        try { inv.invokeFunction("nope"); }
        catch (NoSuchMethodException x) {
            nsme = true;
            check(x.getMessage() != null && x.getMessage().contains("No such function"),
                    "absent function message");
        }
        check(nsme, "absent function -> NoSuchMethodException");
        boolean npe = false;
        try { inv.invokeFunction(null, 1); }
        catch (NullPointerException x) { npe = true; }
        catch (NoSuchMethodException x) { npe = true; }
        check(npe, "null function name rejected");
        // no-argument functions
        e.eval("function zero(){ return 5 }");
        check(Integer.valueOf(5).equals(inv.invokeFunction("zero")),
                "no-arg function returns Integer 5 (int literal)");
    }

    static void dInvMethods(ScriptEngine e, Invocable inv) throws Exception {
        e.eval("var o = { f: function(x){ return x*2 } }");
        Object r = inv.invokeMethod(e.get("o"), "f", 21);
        check(r instanceof Double && ((Double) r).doubleValue() == 42.0,
                "invokeMethod numeric result is Double 42.0");
        boolean iae = false;
        try { inv.invokeMethod(null, "f", 1); }
        catch (IllegalArgumentException x) {
            iae = true;
            check(x.getMessage() != null && x.getMessage().contains("can not be null"),
                    "null thiz message");
        }
        check(iae, "invokeMethod(null, ...) -> IllegalArgumentException");
        boolean nsme = false;
        try { inv.invokeMethod(e.get("o"), "absent", 1); }
        catch (NoSuchMethodException x) { nsme = true; }
        check(nsme, "absent method -> NoSuchMethodException");
    }

    public interface DeepCalc { Number mul(int x); }

    static void dInvInterfaces(ScriptEngine e, Invocable inv) throws Exception {
        e.eval("var n = 0; function run(){ n = n + 1 }");
        Runnable rn = inv.getInterface(Runnable.class);
        check(rn != null, "top-level function adapts to interface");
        rn.run();
        rn.run();
        check(Double.valueOf(2.0).equals(e.eval("n")), "interface calls execute the function");
        // object-backed interface adapters
        e.eval("var calcer = { mul: function(x){ return x*10 } }");
        DeepCalc cc = inv.getInterface(e.get("calcer"), DeepCalc.class);
        check(cc != null && cc.mul(4).doubleValue() == 40.0, "object method adapts to interface");
        // interface calls go through invokeMethod semantics
        Object viaMethod = inv.invokeMethod(e.get("calcer"), "mul", 5);
        check(viaMethod instanceof Double && ((Double) viaMethod).doubleValue() == 50.0,
                "same member through invokeMethod");
        boolean iae1 = false;
        try { inv.getInterface(null, Runnable.class); }
        catch (IllegalArgumentException x) { iae1 = true; }
        check(iae1, "getInterface(null thiz) -> IllegalArgumentException");
        e.eval("var bare = ({})");
        check(inv.getInterface(e.get("bare"), Runnable.class) == null,
                "script object without the members yields null");
        boolean iae2 = false;
        try { inv.getInterface("plain string", DeepCalc.class); }
        catch (IllegalArgumentException x) { iae2 = true; }
        check(iae2, "getInterface(non-script object) -> IllegalArgumentException");
    }

}
