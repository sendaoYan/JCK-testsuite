/*
 * @test
 * @summary com.sun.org.glassfish.external.statistics.AverageRangeStatistic conformance: reflection.
 * @run main AverageRangeStatisticTest
 */

public class AverageRangeStatisticTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.statistics.AverageRangeStatistic", false, AverageRangeStatisticTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AverageRangeStatistic"), "simple name AverageRangeStatistic");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.glassfish.external.statistics.RangeStatistic"), "interface com.sun.org.glassfish.external.statistics.RangeStatistic");
        check(has(c, "getAverage"), "method getAverage");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
