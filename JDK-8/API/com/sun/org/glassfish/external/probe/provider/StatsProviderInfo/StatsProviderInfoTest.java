/*
 * @test
 * @summary com.sun.org.glassfish.external.probe.provider.StatsProviderInfo conformance: reflection.
 * @run main StatsProviderInfoTest
 */

public class StatsProviderInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.probe.provider.StatsProviderInfo", false, StatsProviderInfoTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("StatsProviderInfo"), "simple name StatsProviderInfo");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getConfigElement"), "method getConfigElement");
        check(has(c, "getPluginPoint"), "method getPluginPoint");
        check(has(c, "getSubTreeRoot"), "method getSubTreeRoot");
        check(has(c, "getStatsProvider"), "method getStatsProvider");
        check(has(c, "getConfigLevel"), "method getConfigLevel");
        check(has(c, "setConfigLevel"), "method setConfigLevel");
        check(has(c, "getInvokerId"), "method getInvokerId");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
