/*
 * @test
 * @summary com.sun.org.glassfish.external.statistics.Statistic conformance: reflection.
 * @run main StatisticTest
 */

public class StatisticTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.statistics.Statistic", false, StatisticTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Statistic"), "simple name Statistic");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getName"), "method getName");
        check(has(c, "getUnit"), "method getUnit");
        check(has(c, "getDescription"), "method getDescription");
        check(has(c, "getStartTime"), "method getStartTime");
        check(has(c, "getLastSampleTime"), "method getLastSampleTime");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
