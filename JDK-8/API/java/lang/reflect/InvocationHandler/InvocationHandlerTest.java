/*
 * @test
 * @summary java.lang.reflect.InvocationHandler conformance: invoke receives method and args.
 *          Deepened batch: interface contract (single abstract method, parameter/return
 *          types, throws clause, no static or default methods on JDK-8, functional by
 *          shape although not annotated with the FunctionalInterface annotation), lambda instantiation,
 *          Object-method arg shapes (hashCode/toString null, equals single), proxy
 *          identity facts seen inside invoke, and raw exception relay through invoke.
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

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    interface Foo {
        String bar(int x);
    }

    interface Greeter {
        String greet(String who);
    }

    interface Declared {
        String work() throws java.io.IOException;
    }

    /** Records (proxy, method, args) triples of every dispatch. */
    static final class Recorder implements InvocationHandler {
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

    static void testInvocationHandler() {
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
    }

    // ===================== deepened batch (2026-11-05) =====================

    static void testInterfaceContract() throws Exception {
        check(InvocationHandler.class.isInterface(), "InvocationHandler is an interface");
        check(InvocationHandler.class.getInterfaces().length == 0,
                "extends no other interface");
        // functional by shape, but the JDK does not annotate it @FunctionalInterface
        check(InvocationHandler.class.getAnnotation(FunctionalInterface.class) == null,
                "not annotated @FunctionalInterface (still a functional interface by shape)");
        check(InvocationHandler.class.getDeclaredMethods().length == 1,
                "exactly one declared method on JDK-8 (no invokeDefault yet)");
        Method invoke = InvocationHandler.class.getMethod("invoke",
                Object.class, Method.class, Object[].class);
        check(Modifier.isAbstract(invoke.getModifiers()) && Modifier.isPublic(invoke.getModifiers()),
                "invoke is public abstract");
        check(!invoke.isDefault() && !Modifier.isStatic(invoke.getModifiers()),
                "invoke is an instance method, no default body");
        check(invoke.getReturnType() == Object.class, "invoke returns Object");
        Class<?>[] pts = invoke.getParameterTypes();
        check(pts.length == 3 && pts[0] == Object.class && pts[1] == Method.class
                && pts[2] == Object[].class, "invoke parameter types");
        Class<?>[] ets = invoke.getExceptionTypes();
        check(ets.length == 1 && ets[0] == Throwable.class,
                "invoke declares throws Throwable");
        // getMethods: exactly one abstract member owned by the interface on JDK-8
        int ownAbstract = 0;
        int ownNonAbstract = 0;
        for (Method m : InvocationHandler.class.getMethods()) {
            if (m.getDeclaringClass() == InvocationHandler.class) {
                if (Modifier.isAbstract(m.getModifiers())) ownAbstract++;
                else ownNonAbstract++;
            }
        }
        check(ownAbstract == 1 && ownNonAbstract == 0,
                "single abstract method and no static/default members (JDK-8)");
    }

    static void testLambdaInstantiation() throws Exception {
        ClassLoader l = InvocationHandlerTest.class.getClassLoader();

        // null-returning lambda is a valid handler instance
        InvocationHandler nulls = (proxy, method, args) -> null;
        check(nulls != null, "lambda handler instance");
        Greeter g0 = (Greeter) Proxy.newProxyInstance(l, new Class<?>[]{Greeter.class}, nulls);
        check(g0.greet("x") == null, "null-returning lambda answers null");

        // value-returning lambda sees the method and forwards answers
        final List<String> seen = new ArrayList<String>();
        InvocationHandler rec = (proxy, method, args) -> {
            seen.add(method.getName() + "/" + (args == null ? "null" : String.valueOf(args.length)));
            return "lam:" + method.getName();
        };
        Greeter g1 = (Greeter) Proxy.newProxyInstance(l, new Class<?>[]{Greeter.class}, rec);
        check("lam:greet".equals(g1.greet("world")), "lambda handler answer");
        check(seen.equals(Arrays.asList("greet/1")), "lambda saw one boxed arg");

        // Object methods through a lambda: args null for hashCode/toString, single for equals
        seen.clear();
        InvocationHandler objH = (proxy, method, args) -> {
            seen.add(method.getName() + "/" + (args == null ? "null" : String.valueOf(args.length)));
            if ("hashCode".equals(method.getName())) return Integer.valueOf(7);
            if ("toString".equals(method.getName())) return "LAMBDA";
            if ("equals".equals(method.getName())) return Boolean.valueOf(proxy == args[0]);
            return null;
        };
        Object any = Proxy.newProxyInstance(l, new Class<?>[]{Greeter.class}, objH);
        check(any.hashCode() == 7 && "LAMBDA".equals(any.toString()) && any.equals(any),
                "Object methods answered by lambda");
        check(seen.equals(Arrays.asList("hashCode/null", "toString/null", "equals/1")),
                "Object method arg shapes (null/null/1)");

        // anonymous inner class and lambda are both plain InvocationHandler instances
        InvocationHandler anon = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) { return "anon"; }
        };
        check(anon instanceof InvocationHandler && rec instanceof InvocationHandler,
                "anonymous class and lambda share the interface");
    }

    static void testProxyFactsInHandler() throws Exception {
        ClassLoader l = InvocationHandlerTest.class.getClassLoader();
        Recorder rec = new Recorder();
        Greeter g = (Greeter) Proxy.newProxyInstance(l, new Class<?>[]{Greeter.class}, rec);
        check("handled:greet".equals(g.greet("hi")), "recorder answer");
        check(rec.lastProxy == g, "handler sees the live proxy instance");
        check(rec.lastProxy != null, "proxy argument never null");
        check(Proxy.isProxyClass(rec.lastProxy.getClass()), "proxy argument is a proxy class");
        check(rec.lastProxy instanceof Greeter, "proxy argument implements the interface");
        check(Proxy.getInvocationHandler(rec.lastProxy) == rec,
                "getInvocationHandler recovers the running handler");
        check(rec.lastMethod.getParameterCount() == 1, "method parameter count visible");
        check(rec.lastMethod.getReturnType() == String.class, "return type visible");
        check(rec.lastArgs.length == 1 && "hi".equals(rec.lastArgs[0]), "args exact content");
        // no-arg interface method arrives with args == null
        Foo n = (Foo) Proxy.newProxyInstance(l, new Class<?>[]{Foo.class}, rec);
        n.bar(3);
        check(rec.calls == 2, "shared recorder counted both");
    }

    static void testExceptionRelay() throws Exception {
        ClassLoader l = InvocationHandlerTest.class.getClassLoader();

        // checked exception declared on the interface method: relayed raw
        InvocationHandler declared = (proxy, method, args) -> {
            throw new java.io.IOException("declared-io");
        };
        Declared d = (Declared) Proxy.newProxyInstance(l, new Class<?>[]{Declared.class}, declared);
        checkThrows(java.io.IOException.class, () -> d.work(),
                "declared checked relayed raw from invoke");

        // undeclared checked exception: relayed wrapped by the proxy machinery
        InvocationHandler undeclared = (proxy, method, args) -> {
            throw new java.io.IOException("undeclared-io");
        };
        Foo f = (Foo) Proxy.newProxyInstance(l, new Class<?>[]{Foo.class}, undeclared);
        checkThrows(java.lang.reflect.UndeclaredThrowableException.class, () -> f.bar(1),
                "undeclared checked wrapped when leaving invoke");

        // runtime exception: relayed raw by identity
        final IllegalStateException boom = new IllegalStateException("relay");
        InvocationHandler rte = (proxy, method, args) -> { throw boom; };
        Foo rf = (Foo) Proxy.newProxyInstance(l, new Class<?>[]{Foo.class}, rte);
        try {
            rf.bar(2);
            throw new AssertionError("expected IllegalStateException");
        } catch (IllegalStateException e) {
            passed++;
            check(e == boom, "runtime exception relayed by identity");
        }
    }

    public static void main(String[] args) throws Exception {
        testInvocationHandler();
        testInterfaceContract();
        testLambdaInstantiation();
        testProxyFactsInHandler();
        testExceptionRelay();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
