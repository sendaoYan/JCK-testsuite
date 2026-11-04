/*
 * @test
 * @summary com.sun.org.glassfish.external.statistics.BoundaryStatistic conformance: reflection.
 * @run main BoundaryStatisticTest
 */

public class BoundaryStatisticTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.statistics.BoundaryStatistic", false, BoundaryStatisticTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("BoundaryStatistic"), "simple name BoundaryStatistic");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.glassfish.external.statistics.Statistic"), "interface com.sun.org.glassfish.external.statistics.Statistic");
        check(has(c, "getUpperBound"), "method getUpperBound");
        check(has(c, "getLowerBound"), "method getLowerBound");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
