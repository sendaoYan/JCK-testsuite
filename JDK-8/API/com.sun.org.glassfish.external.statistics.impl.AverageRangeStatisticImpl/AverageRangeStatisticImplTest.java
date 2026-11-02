/*
 * @test
 * @summary com.sun.org.glassfish.external.statistics.impl.AverageRangeStatisticImpl conformance: reflection.
 * @run main AverageRangeStatisticImplTest
 */

public class AverageRangeStatisticImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.statistics.impl.AverageRangeStatisticImpl", false, AverageRangeStatisticImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AverageRangeStatisticImpl"), "simple name AverageRangeStatisticImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.glassfish.external.statistics.impl.StatisticImpl"), "superclass com.sun.org.glassfish.external.statistics.impl.StatisticImpl");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.glassfish.external.statistics.AverageRangeStatistic"), "interface com.sun.org.glassfish.external.statistics.AverageRangeStatistic");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.reflect.InvocationHandler"), "interface java.lang.reflect.InvocationHandler");
        check(has(c, "getStatistic"), "method getStatistic");
        check(has(c, "toString"), "method toString");
        check(has(c, "getStaticAsMap"), "method getStaticAsMap");
        check(has(c, "reset"), "method reset");
        check(has(c, "getAverage"), "method getAverage");
        check(has(c, "getCurrent"), "method getCurrent");
        check(has(c, "setCurrent"), "method setCurrent");
        check(has(c, "getHighWaterMark"), "method getHighWaterMark");
        check(has(c, "getLowWaterMark"), "method getLowWaterMark");
        check(has(c, "invoke"), "method invoke");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
