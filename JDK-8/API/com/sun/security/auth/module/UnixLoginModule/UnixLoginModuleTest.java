/*
 * @test
 * @summary com.sun.security.auth.module.UnixLoginModule conformance: hierarchy and declared members via reflection.
 * @run main UnixLoginModuleTest
 */

public class UnixLoginModuleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.security.auth.module.UnixLoginModule", false, null);
        check(c.getName().equals("com.sun.security.auth.module.UnixLoginModule"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.security.auth.spi.LoginModule"), "implements javax.security.auth.spi.LoginModule");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("abort"), "declares abort");
        check(ms.contains("commit"), "declares commit");
        check(ms.contains("initialize"), "declares initialize");
        check(ms.contains("login"), "declares login");
        check(ms.contains("logout"), "declares logout");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
