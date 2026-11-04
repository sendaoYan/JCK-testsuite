/*
 * @test
 * @summary javax.script.Invocable conformance: white-box structure + black-box behavior.
 * @run main InvocableTest
 */

import java.lang.reflect.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
