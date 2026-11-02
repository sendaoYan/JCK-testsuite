/*
 * @test
 * @summary com.sun.org.glassfish.external.probe.provider.StatsProviderManagerDelegate conformance: reflection.
 * @run main StatsProviderManagerDelegateTest
 */

public class StatsProviderManagerDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.probe.provider.StatsProviderManagerDelegate", false, StatsProviderManagerDelegateTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("StatsProviderManagerDelegate"), "simple name StatsProviderManagerDelegate");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "register"), "method register");
        check(has(c, "unregister"), "method unregister");
        check(has(c, "hasListeners"), "method hasListeners");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
