/*
 * @test
 * @summary java.lang.reflect.InvocationHandler conformance: white-box structure + black-box behavior.
 *          Deepened batch: full interface contract reflection (single abstract method,
 *          static invokeDefault companion, parameter/return types, throws clause,
 *          not annotated with the FunctionalInterface annotation on this JDK either), invokeDefault
 *          varargs pass-through, declared-checked relay and undeclared-checked wrap,
 *          handler exception identity, proxy facts visible inside invoke.
 * @run main InvocationHandlerTest
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InvocationHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Foo {
        String bar(int x);
    }

    interface Greeter {
        String greet(String who);
    }

    interface DefaultSvc {
        default String fallback() { return "fallback-body"; }

        String strict();
    }

    /** Captures the full (proxy, method, args) triple of each dispatch. */
    static class Recorder implements InvocationHandler {
        Object lastProxy;
        Method lastMethod;
        Object[] lastArgs;
        int calls;

        public Object invoke(Object proxy, Method method, Object[] args) {
            calls++;
            lastProxy = proxy;
            lastMethod = method;
            lastArgs = args;
            if (method.getReturnType() == String.class) return "handled:" + method.getName();
            return null;
        }
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        final String[] invokedMethod = new String[1];
        InvocationHandler h = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                invokedMethod[0] = method.getName();
                return "bar:" + args[0];
            }
        };
        Foo foo = (Foo) Proxy.newProxyInstance(InvocationHandlerTest.class.getClassLoader(),
                new Class<?>[]{Foo.class}, h);
        check("bar:7".equals(foo.bar(7)), "invoke result");
        check("bar".equals(invokedMethod[0]), "invoke received method name");
        check(InvocationHandler.class.isInterface(), "is an interface");
        int abstractCount = 0;
        for (Method m : InvocationHandler.class.getMethods())
            if (!m.isDefault() && java.lang.reflect.Modifier.isAbstract(m.getModifiers())
                  && m.getDeclaringClass() == InvocationHandler.class)
                abstractCount++;
        check(abstractCount == 1, "single abstract method (functional interface)");
        check(java.lang.reflect.Modifier.isStatic(InvocationHandler.class.getMethod("invokeDefault", Object.class, Method.class, Object[].class).getModifiers()),
              "invokeDefault is static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ClassLoader ld = InvocationHandlerTest.class.getClassLoader();

        // 1. handler receives the exact (proxy, method, args) triple
        Recorder rec = new Recorder();
        Foo foo = (Foo) Proxy.newProxyInstance(ld, new Class<?>[]{Foo.class}, rec);
        check("handled:bar".equals(foo.bar(9)), "handler answer by return type");
        check(rec.calls == 1, "exactly one dispatch");
        check(rec.lastProxy == foo, "proxy argument is the live proxy");
        check("bar".equals(rec.lastMethod.getName()), "method argument name");
        check(rec.lastMethod.getDeclaringClass() == Foo.class, "method declaring class");
        check(rec.lastArgs.length == 1 && Integer.valueOf(9).equals(rec.lastArgs[0]),
              "args boxed and ordered");
        check(rec.lastMethod.getReturnType() == String.class, "method return type visible");

        // 2. lambda implementation (functional interface) and no-arg dispatch shape
        List<String> seen = new ArrayList<>();
        InvocationHandler lambda = (proxy, method, args) -> {
            seen.add(method.getName() + "/" + (args == null ? "null" : String.valueOf(args.length)));
            return "lam";
        };
        Greeter g = (Greeter) Proxy.newProxyInstance(ld, new Class<?>[]{Greeter.class}, lambda);
        check("lam".equals(g.greet("world")), "lambda handler answer");
        check(seen.get(0).equals("greet/1"), "lambda saw one arg");
        seen.clear();
        InvocationHandler objH = (proxy, method, args) -> {
            seen.add(method.getName() + "/" + (args == null ? "null" : String.valueOf(args.length)));
            return switch (method.getName()) {
                case "hashCode" -> 7;
                case "toString" -> "T";
                case "equals" -> args[0] == proxy;
                default -> null;
            };
        };
        Object anyProxy = Proxy.newProxyInstance(ld, new Class<?>[]{Greeter.class}, objH);
        anyProxy.hashCode();
        anyProxy.toString();
        anyProxy.equals(anyProxy);
        check(seen.get(0).equals("hashCode/null") && seen.get(1).equals("toString/null")
              && seen.get(2).equals("equals/1"), "Object method arg shapes");

        // 3. invokeDefault executes the interface default body from within a handler
        InvocationHandler defaultAware = (proxy, method, args) -> {
            if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method, args);
            return "strict-handled";
        };
        DefaultSvc svc = (DefaultSvc) Proxy.newProxyInstance(ld, new Class<?>[]{DefaultSvc.class}, defaultAware);
        check("fallback-body".equals(svc.fallback()), "invokeDefault runs default body");
        check("strict-handled".equals(svc.strict()), "non-default still handled");
        try {
            InvocationHandler.invokeDefault(svc, DefaultSvc.class.getMethod("strict"));
            check(false, "invokeDefault on abstract method must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IllegalArgumentException invokeDefault on abstract method");
        } catch (Throwable t) {
            check(false, "unexpected " + t);
        }
        try {
            InvocationHandler.invokeDefault(new Object(), DefaultSvc.class.getMethod("fallback"));
            check(false, "invokeDefault on non-proxy must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IllegalArgumentException invokeDefault on non-proxy");
        } catch (Throwable t) {
            check(false, "unexpected " + t);
        }

        // 4. handler-thrown exceptions surface per proxy rules
        InvocationHandler boom = (proxy, method, args) -> {
            throw new IllegalStateException("handler-boom");
        };
        Foo bf = (Foo) Proxy.newProxyInstance(ld, new Class<?>[]{Foo.class}, boom);
        try {
            bf.bar(1);
            check(false, "handler exception must surface");
        } catch (IllegalStateException e) {
            check("handler-boom".equals(e.getMessage()), "runtime exception raw");
        }

        // 5. same handler instance reused across distinct proxies/interfaces
        Recorder shared = new Recorder();
        Foo f2 = (Foo) Proxy.newProxyInstance(ld, new Class<?>[]{Foo.class}, shared);
        Greeter g2 = (Greeter) Proxy.newProxyInstance(ld, new Class<?>[]{Greeter.class}, shared);
        f2.bar(1);
        g2.greet("x");
        check(shared.calls == 2, "shared handler saw both");
        check(shared.lastProxy == g2 && "greet".equals(shared.lastMethod.getName()),
              "last dispatch wins");
        check("handled:greet".equals(g2.greet("y")), "still answers after reuse");
    }

    // ===================== deepened batch (2026-11-05) =====================

    static void testInterfaceContractDeep() throws Exception {
        check(InvocationHandler.class.isInterface(), "is an interface");
        check(InvocationHandler.class.getInterfaces().length == 0, "extends nothing");
        // functional by shape, but the JDK does not attach the annotation itself
        check(InvocationHandler.class.getAnnotation(FunctionalInterface.class) == null,
              "not annotated @FunctionalInterface (shape-functional only)");
        Method invoke = InvocationHandler.class.getMethod("invoke",
              Object.class, Method.class, Object[].class);
        check(Modifier.isPublic(invoke.getModifiers()) && Modifier.isAbstract(invoke.getModifiers()),
              "invoke public abstract");
        check(invoke.getReturnType() == Object.class, "invoke returns Object");
        Class<?>[] pts = invoke.getParameterTypes();
        check(pts.length == 3 && pts[0] == Object.class && pts[1] == Method.class
              && pts[2] == Object[].class, "invoke parameter types exact");
        Class<?>[] ets = invoke.getExceptionTypes();
        check(ets.length == 1 && ets[0] == Throwable.class, "invoke declares throws Throwable");
        // JDK-27 surface: exactly invoke (abstract) + invokeDefault (static varargs)
        Method[] declared = InvocationHandler.class.getDeclaredMethods();
        check(declared.length == 2, "exactly two declared members on this JDK");
        Method invokeDefault = InvocationHandler.class.getMethod("invokeDefault",
              Object.class, Method.class, Object[].class);
        check(Modifier.isStatic(invokeDefault.getModifiers()), "invokeDefault static");
        check(invokeDefault.isVarArgs(), "invokeDefault is varargs");
        check(invokeDefault.getReturnType() == Object.class, "invokeDefault returns Object");
        check(invokeDefault.getExceptionTypes().length == 1
              && invokeDefault.getExceptionTypes()[0] == Throwable.class,
              "invokeDefault declares throws Throwable");
        int ownAbstract = 0;
        int ownStatic = 0;
        int ownDefault = 0;
        for (Method m : InvocationHandler.class.getMethods()) {
            if (m.getDeclaringClass() == InvocationHandler.class) {
                if (Modifier.isStatic(m.getModifiers())) ownStatic++;
                else if (m.isDefault()) ownDefault++;
                else ownAbstract++;
            }
        }
        check(ownAbstract == 1 && ownStatic == 1 && ownDefault == 0,
              "member census: one abstract, one static, no defaults");
    }

    static void testInvokeDefaultVariants() throws Exception {
        ClassLoader ld = InvocationHandlerTest.class.getClassLoader();
        // varargs pass-through: single array form equals spread form
        InvocationHandler aware = (proxy, method, args) -> {
            if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method, args);
            return "plain";
        };
        DefaultSvc svc = (DefaultSvc) Proxy.newProxyInstance(ld,
              new Class<?>[]{DefaultSvc.class}, aware);
        check("fallback-body".equals(svc.fallback()), "array form invokeDefault");
        Method fb = DefaultSvc.class.getMethod("fallback");
        // direct static call with spread (no) args and with explicit array are equal
        check("fallback-body".equals(callDefault(svc, fb)), "spread (empty) form");
        check("fallback-body".equals(callDefault(svc, fb, (Object[]) null)),
              "explicit null args form");
        check("fallback-body".equals(callDefault(svc, fb, new Object[0])),
              "explicit empty array form");
        // invokeDefault result obeys the declared return type; wrong-typed default body would CCE,
        // here only the exact contract of a well-typed default is asserted
        check(fb.getReturnType() == String.class, "default method return type");
    }

    /** invokeDefault declares throws Throwable; funnel it back into a checked Exception. */
    static String callDefault(Object proxy, Method m, Object... args) throws Exception {
        try {
            return (String) InvocationHandler.invokeDefault(proxy, m, args);
        } catch (Throwable t) {
            if (t instanceof Error) throw (Error) t;
            if (t instanceof Exception) throw (Exception) t;
            throw new AssertionError("unexpected checked non-Exception throwable", t);
        }
    }

    static void testExceptionRelayDeep() throws Exception {
        ClassLoader ld = InvocationHandlerTest.class.getClassLoader();

        // declared checked exception on the interface method: relayed raw, never wrapped
        InvocationHandler declared = (p, m, a) -> { throw new java.io.IOException("decl-io"); };
        Declared d = (Declared) Proxy.newProxyInstance(ld, new Class<?>[]{Declared.class}, declared);
        try {
            d.work();
            check(false, "declared checked must relay raw");
        } catch (java.io.IOException e) {
            check("decl-io".equals(e.getMessage()), "declared checked raw message");
        }

        // undeclared checked exception: wrapped by the proxy machinery
        InvocationHandler undeclared = (p, m, a) -> { throw new java.io.IOException("und-io"); };
        Foo f = (Foo) Proxy.newProxyInstance(ld, new Class<?>[]{Foo.class}, undeclared);
        try {
            f.bar(1);
            check(false, "undeclared checked must wrap");
        } catch (java.lang.reflect.UndeclaredThrowableException e) {
            check(e.getCause() instanceof java.io.IOException, "wrap cause type");
            check("und-io".equals(e.getCause().getMessage()), "wrap cause message");
        }

        // runtime exception relays with identity preserved
        final IllegalStateException boom = new IllegalStateException("id-relay");
        InvocationHandler rte = (p, m, a) -> { throw boom; };
        Foo rf = (Foo) Proxy.newProxyInstance(ld, new Class<?>[]{Foo.class}, rte);
        try {
            rf.bar(2);
            check(false, "runtime must relay");
        } catch (IllegalStateException e) {
            check(e == boom, "runtime relayed by identity");
        }
    }

    static void testProxyFactsDeep() throws Exception {
        ClassLoader ld = InvocationHandlerTest.class.getClassLoader();
        Recorder rec = new Recorder();
        Foo f = (Foo) Proxy.newProxyInstance(ld, new Class<?>[]{Foo.class}, rec);
        f.bar(11);
        check(rec.lastMethod.getParameterCount() == 1, "method parameter count visible");
        check(rec.lastMethod.getParameterTypes()[0] == int.class, "parameter type visible");
        check(rec.lastMethod.getReturnType() == String.class, "return type visible");
        check(Proxy.getInvocationHandler(rec.lastProxy) == rec, "handler recoverable from inside");
        check(Proxy.isProxyClass(rec.lastProxy.getClass()), "proxy class flag from inside");
        check(java.util.Arrays.toString(rec.lastArgs).equals("[11]"), "args snapshot exact");
    }

    interface Declared {
        String work() throws java.io.IOException;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInterfaceContractDeep();
        testInvokeDefaultVariants();
        testExceptionRelayDeep();
        testProxyFactsDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
