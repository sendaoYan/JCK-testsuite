/*
 * @test
 * @summary java.lang.reflect.InvocationHandler conformance: white-box structure + black-box behavior.
 * @run main InvocationHandlerTest
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
