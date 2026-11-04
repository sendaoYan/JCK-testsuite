/*
 * @test
 * @summary java.lang.reflect.InvocationHandler conformance: invoke receives method and args.
 * @run main InvocationHandlerTest
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class InvocationHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Foo {
        String bar(int x);
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

    public static void main(String[] args) {
        testInvocationHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
