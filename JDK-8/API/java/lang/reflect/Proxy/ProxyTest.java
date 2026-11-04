/*
 * @test
 * @summary java.lang.reflect.Proxy conformance: newProxyInstance/isProxyClass/getInvocationHandler.
 * @run main ProxyTest
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Foo {
        String bar(int x);
    }

    static void testProxy() throws Exception {
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
    }

    public static void main(String[] args) throws Exception {
        testProxy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
