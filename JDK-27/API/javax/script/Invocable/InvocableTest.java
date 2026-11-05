/*
 * @test
 * @summary javax.script.Invocable conformance: white-box structure + black-box behavior.
 * @run main InvocableTest
 */

import java.lang.reflect.*;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class InvocableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.Invocable");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "invokeMethod"), "declares invokeMethod");
        check(hasMethod(c, "invokeFunction"), "declares invokeFunction");
        check(hasMethod(c, "getInterface"), "declares getInterface");
    }

    static void testBlackBox() throws Exception {
        final Runnable runnable = new Runnable() { public void run() { } };
        javax.script.Invocable inv = new javax.script.Invocable() {
            public Object invokeMethod(Object thiz, String name, Object... args) {
                return "method:" + name + ":" + args.length;
            }
            public Object invokeFunction(String name, Object... args) {
                return "function:" + name;
            }
            public <T> T getInterface(Class<T> clazz) {
                return (T) runnable;
            }
            public <T> T getInterface(Object thiz, Class<T> clazz) {
                return (T) runnable;
            }
        };

        check("method:foo:2".equals(inv.invokeMethod(null, "foo", "a", "b")), "invokeMethod");
        check("function:bar".equals(inv.invokeFunction("bar")), "invokeFunction");
        check(inv.getInterface(Runnable.class) == runnable, "getInterface(Class)");
        check(inv.getInterface(null, Runnable.class) == runnable, "getInterface(Object,Class)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (self-made invocable engine) --------------

    static void testDeepContracts() throws Exception {
        check(new ScriptEngineManager().getEngineFactories().isEmpty(),
                "no platform engine: Invocable tested with self-made engine");
        DeepInvEngine e = new DeepInvEngine();
        check(e instanceof Invocable, "engine implements Invocable");
        dInv27Functions(e);
        dInv27Interfaces(e);
        System.out.println("deep: Invocable contracts OK (self-made)");
    }

    /** A script "function" stored in engine bindings: multiplies the sum of args by a factor. */
    public interface DeepFn { Object apply(Object[] args); }

    static class DeepInvEngine extends AbstractScriptEngine implements Invocable {
        public Object invokeFunction(String name, Object... args) throws ScriptException,
                NoSuchMethodException {
            Object fn = getContext().getAttribute(name, ScriptContext.ENGINE_SCOPE);
            if (fn == null) throw new NoSuchMethodException("No such function " + name);
            if (!(fn instanceof DeepFn)) throw new NoSuchMethodException("Not a function: " + name);
            return ((DeepFn) fn).apply(args);
        }

        public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException,
                NoSuchMethodException {
            if (thiz == null) throw new IllegalArgumentException("script object can not be null");
            if (thiz instanceof DeepObj) return ((DeepObj) thiz).invoke(name, args);
            throw new IllegalArgumentException("not a script object");
        }

        public <T> T getInterface(Class<T> clazz) {
            return getInterfaceProxy(null, clazz);
        }

        public <T> T getInterface(Object thiz, Class<T> clazz) {
            return getInterfaceProxy(thiz, clazz);
        }

        @SuppressWarnings("unchecked")
        <T> T getInterfaceProxy(final Object thiz, final Class<T> clazz) {
            if (thiz != null && !(thiz instanceof DeepObj)) return null;
            final DeepObj target = (DeepObj) thiz;
            Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                    clazz.getClassLoader(), new Class<?>[] { clazz },
                    new java.lang.reflect.InvocationHandler() {
                        public Object invoke(Object p, java.lang.reflect.Method m, Object[] a)
                                throws Throwable {
                            if (m.getDeclaringClass() == Object.class)
                                return m.invoke(target != null ? target : p, a);
                            Object[] wrapped = a == null ? new Object[0] : a;
                            if (target != null) return target.invoke(m.getName(), wrapped);
                            Object fn = getContext().getAttribute(m.getName(),
                                    ScriptContext.ENGINE_SCOPE);
                            return ((DeepFn) fn).apply(wrapped);
                        }
                    });
            return (T) proxy;
        }

        public Object eval(String s, ScriptContext ctx) throws ScriptException { return "OK:" + s; }
        public Object eval(java.io.Reader r, ScriptContext ctx) throws ScriptException {
            return eval("read", ctx);
        }
        public Bindings createBindings() { return new SimpleBindings(); }
        public ScriptEngineFactory getFactory() { return null; }
    }

    static class DeepObj {
        Object invoke(String name, Object[] args) {
            if (name.equals("mul")) {
                int v = ((Number) args[0]).intValue();
                return Integer.valueOf(v * 10);
            }
            throw new IllegalStateException("no member " + name);
        }
    }

    static void dInv27Functions(DeepInvEngine e) throws Exception {
        e.put("add", (DeepFn) args -> Integer.valueOf(((Number) args[0]).intValue()
                + ((Number) args[1]).intValue()));
        Invocable inv = e;
        check(Integer.valueOf(7).equals(inv.invokeFunction("add", 3, 4)),
                "invokeFunction dispatches to engine scope");
        boolean nsme = false;
        try { inv.invokeFunction("nope"); }
        catch (NoSuchMethodException x) {
            nsme = true;
            check(x.getMessage() != null && x.getMessage().contains("No such function nope"),
                    "absent function message");
        }
        check(nsme, "absent function -> NoSuchMethodException");
        // a non-function binding is not invokable
        e.put("notfn", "text");
        boolean nsme2 = false;
        try { inv.invokeFunction("notfn"); }
        catch (NoSuchMethodException x) { nsme2 = true; }
        catch (ScriptException x) { nsme2 = true; }
        check(nsme2, "non-function binding rejected");
        // invokeMethod routes through the object, null thiz is rejected
        DeepObj obj = new DeepObj();
        check(Integer.valueOf(120).equals(inv.invokeMethod(obj, "mul", 12)),
                "invokeMethod dispatches on the object");
        boolean iae = false;
        try { inv.invokeMethod(null, "mul", 1); }
        catch (IllegalArgumentException x) { iae = true; }
        check(iae, "invokeMethod(null thiz) -> IllegalArgumentException");
    }

    public interface DeepCalc27 { Number mul(int x); }

    static void dInv27Interfaces(DeepInvEngine e) {
        Invocable inv = e;
        // top-level function adapter: function name equals the interface method
        e.put("mul", (DeepFn) args -> Integer.valueOf(((Number) args[0]).intValue() * 3));
        DeepCalc27 top = inv.getInterface(DeepCalc27.class);
        check(top != null && top.mul(4).intValue() == 12, "function adapts to interface");
        // object-backed adapter: thiz routes to the object
        DeepCalc27 viaObj = inv.getInterface(new DeepObj(), DeepCalc27.class);
        check(viaObj != null && viaObj.mul(4).intValue() == 40, "object adapts to interface");
        // a non-script object yields null rather than throwing
        check(inv.getInterface("plain", DeepCalc27.class) == null,
                "non-script object yields null");
    }

}
