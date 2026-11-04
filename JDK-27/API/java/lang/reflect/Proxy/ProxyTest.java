/*
 * @test
 * @summary java.lang.reflect.Proxy conformance: white-box structure + black-box behavior.
 * @run main ProxyTest
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

public class ProxyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Foo {
        String bar(int x);
    }

    interface Service {
        String name();
        int add(int a, int b);
        default String describe() { return "default-describe"; }
        void checkedOp() throws java.io.IOException;
    }

    /** Records every dispatch: method name + args, answers from a canned table. */
    static class RecordingHandler implements InvocationHandler {
        final List<String> calls = new ArrayList<>();
        Object answer = null;
        Throwable toThrow = null;

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            calls.add(method.getName() + "(" + (args == null ? "" : args.length) + ")");
            if (method.getName().equals("hashCode")) return 42;
            if (method.getName().equals("toString")) return "ProxyStr";
            if (method.getName().equals("equals")) return args[0] == proxy;
            if (toThrow != null) { Throwable t = toThrow; toThrow = null; throw t; }
            return answer;
        }
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        InvocationHandler h = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                return "bar:" + args[0];
            }
        };
        Foo foo = (Foo) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class}, h);
        check("bar:7".equals(foo.bar(7)), "proxy dispatch");
        check(Proxy.isProxyClass(foo.getClass()), "isProxyClass");
        check(Proxy.getInvocationHandler(foo) == h, "getInvocationHandler");

        Class<?> pc = Proxy.getProxyClass(ProxyTest.class.getClassLoader(), Foo.class);
        check(Proxy.isProxyClass(pc), "getProxyClass result is proxy class");
        check(java.io.Serializable.class.isAssignableFrom(Proxy.class), "Proxy is Serializable");
        check(!Proxy.class.isInterface(), "Proxy is a class");
        check(!java.lang.reflect.Modifier.isAbstract(Proxy.class.getModifiers()),
              "Proxy concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ClassLoader ld = ProxyTest.class.getClassLoader();

        // 1. dispatch records method and args; answers flow back
        RecordingHandler rec = new RecordingHandler();
        Service svc = (Service) Proxy.newProxyInstance(ld, new Class<?>[]{Service.class}, rec);
        check(svc instanceof Service, "proxy implements interface");
        check(svc.getClass().getInterfaces().length >= 1, "proxy class lists interface");
        rec.answer = "svc-name";
        check("svc-name".equals(svc.name()), "answer returned");
        rec.answer = 30;
        check(svc.add(10, 20) == 30, "boxed int unboxed");
        rec.answer = null;
        check(rec.calls.get(0).equals("name()") && rec.calls.get(1).equals("add(2)"),
              "calls recorded with arity");
        try { svc.add(1, 2); check(false, "null return must unbox-fail"); }
        catch (NullPointerException e) { check(true, "NPE unboxing null int"); }

        // 2. Object methods are dispatched to the handler
        rec.calls.clear();
        check(svc.hashCode() == 42, "hashCode via handler");
        check("ProxyStr".equals(svc.toString()), "toString via handler");
        check(svc.equals(svc), "equals(proxy) via handler true");
        check(!svc.equals("other"), "equals(other) via handler false");
        check(rec.calls.size() == 4, "Object methods recorded");

        // 3. wrong-typed handler answer -> ClassCastException at the call site
        RecordingHandler bad = new RecordingHandler();
        Service badSvc = (Service) Proxy.newProxyInstance(ld, new Class<?>[]{Service.class}, bad);
        bad.answer = "not-an-int";
        try { badSvc.add(1, 2); check(false, "wrong type must fail"); }
        catch (ClassCastException e) { check(true, "ClassCastException wrong return type"); }

        // 4. undeclared checked throwable -> UndeclaredThrowableException
        RecordingHandler thrower = new RecordingHandler();
        Service throwSvc = (Service) Proxy.newProxyInstance(ld, new Class<?>[]{Service.class}, thrower);
        thrower.toThrow = new java.io.IOException("io-boom");
        try {
            throwSvc.name(); // declares no checked exceptions
            check(false, "undeclared checked must wrap");
        } catch (UndeclaredThrowableException e) {
            check(e.getUndeclaredThrowable() instanceof java.io.IOException, "UTE cause exact type");
        }
        // declared checked exception propagates raw through InvocationTargetException-free path
        thrower.toThrow = new java.io.IOException("declared-ok");
        try {
            throwSvc.checkedOp(); // declares IOException
            check(false, "declared checked must propagate");
        } catch (java.io.IOException e) {
            check("declared-ok".equals(e.getMessage()), "declared checked propagates raw");
        } catch (UndeclaredThrowableException e) {
            check(false, "declared checked must not wrap");
        }
        // runtime exception propagates raw
        thrower.toThrow = new IllegalStateException("rt-boom");
        try {
            throwSvc.name();
            check(false, "runtime must propagate");
        } catch (IllegalStateException e) {
            check("rt-boom".equals(e.getMessage()), "runtime propagates raw");
        }

        // 5. InvocationHandler.invokeDefault runs the real default method
        InvocationHandler def = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("describe"))
                    return InvocationHandler.invokeDefault(proxy, method, args);
                return null;
            }
        };
        Service defSvc = (Service) Proxy.newProxyInstance(ld, new Class<?>[]{Service.class}, def);
        check("default-describe".equals(defSvc.describe()), "invokeDefault executes default body");
        check(Service.class.getMethod("describe").isDefault(), "describe is default");

        // 6. class caching: same loader + same interfaces -> same proxy class
        RecordingHandler h1 = new RecordingHandler(), h2 = new RecordingHandler();
        Service s1 = (Service) Proxy.newProxyInstance(ld, new Class<?>[]{Service.class}, h1);
        Service s2 = (Service) Proxy.newProxyInstance(ld, new Class<?>[]{Service.class}, h2);
        check(s1.getClass() == s2.getClass(), "proxy class cached per loader+interfaces");
        check(Proxy.getProxyClass(ld, Service.class) == s1.getClass(),
              "getProxyClass matches newProxyInstance class");
        check(Proxy.isProxyClass(s1.getClass()), "isProxyClass on live proxy");
        check(!Proxy.isProxyClass(Service.class), "plain interface not proxy class");
        check(!Proxy.isProxyClass(String.class), "String not proxy class");
        check(s1 != s2 && Proxy.getInvocationHandler(s1) == h1
              && Proxy.getInvocationHandler(s2) == h2, "distinct instances hold their handlers");

        // 7. multiple interfaces on one proxy
        RecordingHandler multi = new RecordingHandler();
        Object both = Proxy.newProxyInstance(ld,
                new Class<?>[]{Foo.class, Service.class}, multi);
        check(both instanceof Foo && both instanceof Service, "proxy implements both");
        multi.answer = "multi";
        check("multi".equals(((Foo) both).bar(1)), "Foo dispatch");
        check("multi".equals(((Service) both).name()), "Service dispatch");
        try {
            Proxy.newProxyInstance(ld, new Class<?>[]{Foo.class, Foo.class}, multi);
            check(false, "duplicate interfaces must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IllegalArgumentException duplicate interfaces");
        }

        // 8. argument validation
        try {
            Proxy.newProxyInstance(ld, new Class<?>[]{String.class}, multi);
            check(false, "non-interface must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IllegalArgumentException non-interface");
        }
        try {
            Proxy.newProxyInstance(ld, new Class<?>[]{Service.class}, null);
            check(false, "null handler must fail");
        } catch (NullPointerException e) {
            check(true, "NullPointerException null handler");
        }
        try {
            Proxy.getInvocationHandler("not-a-proxy");
            check(false, "non-proxy must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IllegalArgumentException getInvocationHandler non-proxy");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
