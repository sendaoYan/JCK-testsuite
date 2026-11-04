/*
 * @test
 * @summary com.sun.org.glassfish.external.statistics.impl.CountStatisticImpl conformance: reflection.
 * @run main CountStatisticImplTest
 */

public class CountStatisticImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.statistics.impl.CountStatisticImpl", false, CountStatisticImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("CountStatisticImpl"), "simple name CountStatisticImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.glassfish.external.statistics.impl.StatisticImpl"), "superclass com.sun.org.glassfish.external.statistics.impl.StatisticImpl");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.glassfish.external.statistics.CountStatistic"), "interface com.sun.org.glassfish.external.statistics.CountStatistic");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.reflect.InvocationHandler"), "interface java.lang.reflect.InvocationHandler");
        check(has(c, "getStatistic"), "method getStatistic");
        check(has(c, "getStaticAsMap"), "method getStaticAsMap");
        check(has(c, "toString"), "method toString");
        check(has(c, "getCount"), "method getCount");
        check(has(c, "setCount"), "method setCount");
        check(has(c, "increment"), "method increment");
        check(has(c, "decrement"), "method decrement");
        check(has(c, "reset"), "method reset");
        check(has(c, "invoke"), "method invoke");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
