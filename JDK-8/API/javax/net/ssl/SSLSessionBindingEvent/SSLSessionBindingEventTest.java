/*
 * @test
 * @summary javax.net.ssl.SSLSessionBindingEvent conformance: class hierarchy and methods.
 * @run main SSLSessionBindingEventTest
 */

public class SSLSessionBindingEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLSessionBindingEvent");
        check(!c.isInterface(), "is class");
        check(c.getSuperclass() == java.util.EventObject.class, "extends EventObject");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getName"), "declares getName");
        check(ms.contains("getSession"), "declares getSession");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
