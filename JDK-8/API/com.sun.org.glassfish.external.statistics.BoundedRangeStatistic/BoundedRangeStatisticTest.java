/*
 * @test
 * @summary com.sun.org.glassfish.external.statistics.BoundedRangeStatistic conformance: reflection.
 * @run main BoundedRangeStatisticTest
 */

public class BoundedRangeStatisticTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.statistics.BoundedRangeStatistic", false, BoundedRangeStatisticTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("BoundedRangeStatistic"), "simple name BoundedRangeStatistic");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.glassfish.external.statistics.BoundaryStatistic"), "interface com.sun.org.glassfish.external.statistics.BoundaryStatistic");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.glassfish.external.statistics.RangeStatistic"), "interface com.sun.org.glassfish.external.statistics.RangeStatistic");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
