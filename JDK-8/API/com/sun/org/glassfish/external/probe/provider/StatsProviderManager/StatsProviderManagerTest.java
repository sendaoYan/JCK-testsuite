/*
 * @test
 * @summary com.sun.org.glassfish.external.probe.provider.StatsProviderManager conformance: reflection.
 * @run main StatsProviderManagerTest
 */

public class StatsProviderManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.probe.provider.StatsProviderManager", false, StatsProviderManagerTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("StatsProviderManager"), "simple name StatsProviderManager");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "register"), "method register");
        check(has(c, "unregister"), "method unregister");
        check(has(c, "hasListeners"), "method hasListeners");
        check(has(c, "setStatsProviderManagerDelegate"), "method setStatsProviderManagerDelegate");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
