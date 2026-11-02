/*
 * @test
 * @summary com.sun.org.glassfish.external.statistics.Stats conformance: reflection.
 * @run main StatsTest
 */

public class StatsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.statistics.Stats", false, StatsTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Stats"), "simple name Stats");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getStatistic"), "method getStatistic");
        check(has(c, "getStatisticNames"), "method getStatisticNames");
        check(has(c, "getStatistics"), "method getStatistics");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
