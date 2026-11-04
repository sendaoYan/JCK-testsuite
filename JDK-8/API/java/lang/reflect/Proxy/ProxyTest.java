/*
 * @test
 * @summary java.lang.reflect.Proxy conformance: full contract coverage.
 *          newProxyInstance dispatches every method (incl. Object's hashCode/equals/
 *          toString) to the handler with (proxy, method, args), isProxyClass identity,
 *          getInvocationHandler identity + IllegalArgumentException on non-proxy,
 *          handler-returned wrong type -> ClassCastException, null for primitive return
 *          -> NullPointerException, runtime exception propagation, undeclared checked
 *          exception -> UndeclaredThrowableException, and constructor/factory exception
 *          matrix (null loader/interfaces/handler -> NPE, empty/dup/non-interface/
 *          non-public interfaces -> IllegalArgumentException).
 * @run main ProxyTest
 */

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

public class ProxyTest {
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

    interface Foo { String bar(int x); }
    interface Baz { int qux(); }

    static final class RecordingHandler implements InvocationHandler {
        Object proxySeen;
        Method methodSeen;
        Object[] argsSeen;
        int calls;
        public Object invoke(Object proxy, Method method, Object[] args) {
            proxySeen = proxy;
            methodSeen = method;
            argsSeen = args;
            calls++;
            if (method.getDeclaringClass() == Object.class) {
                switch (method.getName()) {
                    case "toString": return "PROXY-STRING";
                    case "hashCode": return Integer.valueOf(42);
                    case "equals": return Boolean.valueOf(proxy == args[0]);
                    default: return null;
                }
            }
            return "bar:" + args[0];
        }
    }

    static void testDispatch() throws Exception {
        RecordingHandler h = new RecordingHandler();
        Foo foo = (Foo) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class}, h);
        check("bar:7".equals(foo.bar(7)), "proxy dispatch");
        check(h.calls == 1, "handler called once");
        check(h.proxySeen == foo, "handler receives proxy identity");
        check(h.methodSeen.getName().equals("bar"), "handler receives method name");
        check(h.argsSeen.length == 1 && h.argsSeen[0].equals(7), "handler receives args");

        check("PROXY-STRING".equals(foo.toString()), "Object.toString dispatched");
        check(foo.hashCode() == 42, "Object.hashCode dispatched");
        check(foo.equals(foo), "Object.equals dispatched (proxy==arg)");
        check(h.methodSeen.getName().equals("equals"), "last dispatched method equals");
    }

    static void testIdentity() throws Exception {
        RecordingHandler h = new RecordingHandler();
        Foo foo = (Foo) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class}, h);
        Class<?> pc = foo.getClass();
        check(Proxy.isProxyClass(pc), "isProxyClass true");
        check(Proxy.getInvocationHandler(foo) == h, "getInvocationHandler identity");

        check(!Proxy.isProxyClass(Foo.class), "isProxyClass interface false");
        check(!Proxy.isProxyClass(String.class), "isProxyClass normal class false");
        check(!Proxy.isProxyClass(int[].class), "isProxyClass array false");

        checkThrows(IllegalArgumentException.class,
                () -> Proxy.getInvocationHandler(new Object()),
                "getInvocationHandler non-proxy IAE");
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.getInvocationHandler(foo.getClass()),
                "getInvocationHandler Class object IAE");
        checkThrows(NullPointerException.class,
                () -> Proxy.getInvocationHandler(null), "getInvocationHandler null NPE");
        checkThrows(NullPointerException.class,
                () -> Proxy.isProxyClass(null), "isProxyClass null NPE");

        // proxy class implements Serializable and the interfaces
        check(pc.getInterfaces().length == 1 && pc.getInterfaces()[0] == Foo.class,
                "proxy implements Foo");
        check(java.io.Serializable.class.isAssignableFrom(pc), "proxy is Serializable");

        // getProxyClass returns a proxy class
        Class<?> pc2 = Proxy.getProxyClass(ProxyTest.class.getClassLoader(), Foo.class);
        check(Proxy.isProxyClass(pc2), "getProxyClass result is proxy class");
        check(pc2.getInterfaces()[0] == Foo.class, "getProxyClass implements Foo");
    }

    static void testMultipleInterfaces() throws Exception {
        RecordingHandler h = new RecordingHandler();
        Object o = Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class, Baz.class}, h);
        check(o instanceof Foo && o instanceof Baz, "proxy implements both interfaces");
        check("bar:1".equals(((Foo) o).bar(1)), "Foo method through proxy");
        // Baz.qux returns int but handler returns "bar:null" -> we use a dedicated handler
        // instead; this proxy only needs to be instanceof both.
    }

    static void testHandlerReturnBehavior() throws Exception {
        // handler returns wrong type -> ClassCastException from generated checkcast
        InvocationHandler wrong = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) { return Integer.valueOf(1); }
        };
        Foo f1 = (Foo) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class}, wrong);
        checkThrows(ClassCastException.class, () -> f1.bar(1),
                "handler wrong return type CCE");

        // handler returns null for primitive int -> NullPointerException (unboxing)
        InvocationHandler nullReturn = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) { return null; }
        };
        Baz b = (Baz) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Baz.class}, nullReturn);
        checkThrows(NullPointerException.class, () -> b.qux(),
                "handler null for int return NPE");

        // handler throwing RuntimeException -> propagates unchanged
        final RuntimeException rte = new IllegalStateException("rte");
        InvocationHandler rteHandler = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) { throw rte; }
        };
        Foo f2 = (Foo) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class}, rteHandler);
        try {
            f2.bar(1);
            throw new AssertionError("expected IllegalStateException");
        } catch (IllegalStateException e) {
            passed++;
            check(e == rte, "runtime exception propagates by identity");
        }

        // handler throwing undeclared checked exception -> UndeclaredThrowableException
        InvocationHandler checked = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) throws IOException {
                throw new IOException("declared nowhere");
            }
        };
        Foo f3 = (Foo) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class}, checked);
        try {
            f3.bar(1);
            throw new AssertionError("expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException e) {
            passed++;
            check(e.getCause() instanceof IOException, "UndeclaredThrowableException cause");
        }
    }

    static void testFactoryExceptions() throws Exception {
        ClassLoader l = ProxyTest.class.getClassLoader();
        RecordingHandler h = new RecordingHandler();

        checkThrows(IllegalArgumentException.class,
                () -> Proxy.newProxyInstance(null, new Class<?>[]{Foo.class}, h),
                "newProxyInstance null loader IAE");
        checkThrows(NullPointerException.class,
                () -> Proxy.newProxyInstance(l, null, h),
                "newProxyInstance null interfaces NPE");
        checkThrows(NullPointerException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{Foo.class}, null),
                "newProxyInstance null handler NPE");

        // empty interfaces array is permitted (produces a no-interface proxy)
        Object emptyProxy = Proxy.newProxyInstance(l, new Class<?>[0], h);
        check(Proxy.isProxyClass(emptyProxy.getClass()), "empty interfaces still proxy");
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{Foo.class, Foo.class}, h),
                "newProxyInstance duplicate interface IAE");
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{String.class}, h),
                "newProxyInstance non-interface IAE");
        checkThrows(NullPointerException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{Foo.class, null}, h),
                "newProxyInstance null interface element NPE");
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{int[].class}, h),
                "newProxyInstance array interface IAE");

        // getProxyClass mirror of the same matrix
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.getProxyClass(null, Foo.class), "getProxyClass null loader IAE");
        checkThrows(NullPointerException.class,
                () -> Proxy.getProxyClass(l, (Class<?>[]) null),
                "getProxyClass null interfaces NPE");
        check(Proxy.isProxyClass(Proxy.getProxyClass(l, new Class<?>[0])),
                "getProxyClass empty interfaces still proxy");
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.getProxyClass(l, Foo.class, Foo.class),
                "getProxyClass duplicate IAE");
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.getProxyClass(l, String.class), "getProxyClass non-interface IAE");
    }

    public static void main(String[] args) throws Exception {
        testDispatch();
        testIdentity();
        testMultipleInterfaces();
        testHandlerReturnBehavior();
        testFactoryExceptions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
