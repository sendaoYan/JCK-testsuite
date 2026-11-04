/*
 * @test
 * @summary com.sun.org.glassfish.external.statistics.impl.StatisticImpl conformance: reflection.
 * @run main StatisticImplTest
 */

public class StatisticImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.statistics.impl.StatisticImpl", false, StatisticImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("StatisticImpl"), "simple name StatisticImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.glassfish.external.statistics.Statistic"), "interface com.sun.org.glassfish.external.statistics.Statistic");
        check(has(c, "getStaticAsMap"), "method getStaticAsMap");
        check(has(c, "getName"), "method getName");
        check(has(c, "getDescription"), "method getDescription");
        check(has(c, "getUnit"), "method getUnit");
        check(has(c, "getLastSampleTime"), "method getLastSampleTime");
        check(has(c, "getStartTime"), "method getStartTime");
        check(has(c, "reset"), "method reset");
        check(has(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
