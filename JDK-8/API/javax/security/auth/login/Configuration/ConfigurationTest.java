/*
 * @test
 * @summary javax.security.auth.login.Configuration conformance: abstract class hierarchy and key methods.
 * @run main ConfigurationTest
 */

public class ConfigurationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.security.auth.login.Configuration");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getAppConfigurationEntry"), "declares getAppConfigurationEntry");
        check(ms.contains("getConfiguration"), "declares getConfiguration");
        check(ms.contains("getInstance"), "declares getInstance");
        check(ms.contains("getParameters"), "declares getParameters");
        check(ms.contains("getProvider"), "declares getProvider");
        check(ms.contains("getType"), "declares getType");
        check(ms.contains("refresh"), "declares refresh");
        check(ms.contains("setConfiguration"), "declares setConfiguration");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
